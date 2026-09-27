package com.solvyx.ui.screens.chatbot

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.preferences.ChatPreferencesRepository
import com.solvyx.backend.data.remote.chat.ChatRemoteRepository
import com.solvyx.backend.data.remote.chat.EnviarMensajeResult
import com.solvyx.backend.data.remote.connectivity.ConnectivityRepository
import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType
import com.solvyx.backend.decisiontree.repository.DecisionTreeRepository
import com.solvyx.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromBerto: Boolean,
    val timestamp: String,
    val quickReplies: List<String> = emptyList(),
    val bertoState: BertoState = BertoState.TRANQUILO,
    // Avisos de la app (no de Berto): tienen su propio estilo y no se leen en voz alta.
    val avisoSistema: AvisoSistema? = null
)

enum class AvisoSistema { SIN_CONEXION, CONEXION_RECUPERADA, SERVIDOR_NO_DISPONIBLE, SERVIDOR_RECUPERADO }

enum class BertoState { TRANQUILO, PREOCUPADO, CELEBRANDO, CRISIS }

/** Quién responde el texto libre, para mostrarlo en la barra superior. */
enum class ChatMode {
    /** Hay red y la API está disponible: responde la IA. */
    CONECTADO,
    /** Sin internet: responde el árbol. */
    SIN_CONEXION,
    /** Hay red pero la API falló hace poco: responde el árbol durante la espera. */
    MODO_GUIADO
}

private val CRISIS_KEYWORDS = listOf(
    "suicidio", "hacerme daño", "quiero morir", "no puedo más",
    "crisis", "emergencia", "socorro"
)
private val ANXIETY_KEYWORDS = listOf(
    "ansiedad", "ansioso", "angustia", "miedo", "pánico",
    "nervioso", "estresado", "craving", "ganas de consumir"
)
private val POSITIVE_KEYWORDS = listOf(
    "logré", "gracias", "mejor", "bien", "lo conseguí", "racha"
)


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val treeRepository: DecisionTreeRepository,
    @ApplicationContext private val appContext: Context,
    private val chatRemoteRepository: ChatRemoteRepository,
    private val connectivity: ConnectivityRepository,
    private val chatPreferences: ChatPreferencesRepository,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var inputText by mutableStateOf("")
        private set
    var isBertoTyping by mutableStateOf(false)
        private set
    var currentBertoState by mutableStateOf(BertoState.TRANQUILO)
        private set
    // Non-null for 2.8 s after a state transition; drives the banner animation in the UI.
    var stateTransition by mutableStateOf<BertoState?>(null)
        private set
    var showBertoPeek by mutableStateOf(false)
        private set
    var showSosDialog by mutableStateOf(false)
        private set
    // true mientras se espera la respuesta de la API; la UI bloquea la entrada (salvo SOS).
    var isWaitingForApi by mutableStateOf(false)
        private set
    // true si la API tarda más de UMBRAL_PENSANDO_MS: la UI cambia a "Berto está pensando…".
    var isApiSlow by mutableStateOf(false)
        private set
    var chatMode by mutableStateOf(ChatMode.CONECTADO)
        private set
    // Aviso de que, con internet, los mensajes van al servidor. Se muestra una sola vez.
    var mostrarAvisoPrivacidad by mutableStateOf(false)
        private set

    // ── TTS ──────────────────────────────────────────────────────────────────
    var isTtsMuted  by mutableStateOf(false); private set
    var isSpeaking  by mutableStateOf(false); private set
    var isTtsReady  by mutableStateOf(false); private set

    private var tts: TextToSpeech?   = null
    private val mainHandler          = Handler(Looper.getMainLooper())
    private var pendingTtsText: String? = null

    // Control del flujo actual
    private var currentTree: DecisionTree? = null
    private var currentNode: DecisionNode? = null
    private var stateTransitionJob: Job? = null

    // ID virtual para identificar cuándo estamos parados en el menú principal
    private val MAIN_MENU_ID = "menu_principal_virtual"

    private val menuTree: DecisionTree = run {
        val opcionesMenu = listOf(
            DecisionOption("Ansiedad por Alcohol", "alcohol_craving"),
            DecisionOption("Información de Alcohol", "alcohol_info"),
            DecisionOption("Ansiedad por Cristal", "cristal_craving"),
            DecisionOption("Información de Cristal", "cristal_info"),
            DecisionOption("Ansiedad por Vape", "vape_craving"),
            DecisionOption("Información de Vape", "vape_info"),
            DecisionOption("Ansiedad por Cigarro", "cigarro_craving"),
            DecisionOption("Información de Cigarro", "cigarro_info")
        )

        val nodoRaizMenu = DecisionNode(
            id = "raiz_menu",
            texto = "Hola, soy Berto. ¿En qué te puedo apoyar el día de hoy?",
            tipo = NodeType.MESSAGE, // Ajusta según tus tipos en el enum NodeType (ej. NodeType.INFO o NORMAL)
            opciones = opcionesMenu,
            esFinal = false
        )

        DecisionTree(
            id = MAIN_MENU_ID,
            nombre = "Menú Principal",
            nodoInicialId = "raiz_menu",
            nodos = mapOf("raiz_menu" to nodoRaizMenu)
        )
    }
    private val menuRoot: DecisionNode = menuTree.nodos.getValue(menuTree.nodoInicialId)

    // ── Chat online (API) ────────────────────────────────────────────────────
    // Texto libre → API si hay red; botones → siempre árbol. Si la API falla, el árbol responde.
    private val modePolicy = ChatModePolicy()
    private var envioJob: Job? = null
    // Cierre de sesión en curso: el siguiente envío lo espera para que el servidor no cierre
    // una sesión recién creada por ese mismo mensaje.
    private var cierreSesionJob: Job? = null
    // true desde el primer envío a la API hasta que se cierra la sesión.
    private var sesionOnlineAbierta = false
    private var ultimoMensajeFallido: String? = null
    // Estado de red para la UI (el envío consulta el snapshot al momento de mandar).
    private var conectado = connectivity.isCurrentlyConnected()
    // Mientras el servidor no responde, lo vuelve a revisar cada minuto (solo con el chat abierto).
    private var sondeoJob: Job? = null
    // Revisión del servidor al recuperar internet; se cancela si la red vuelve a cambiar.
    private var reconexionJob: Job? = null
    private var avisoPrivacidadVisto = true

    init {
        loadMainMenu()
        initTts()
        // Limpia una sesión que haya quedado abierta (p. ej. si Android cerró la app con el chat abierto),
        // para que el contexto del servidor coincida con el chat vacío que ve el usuario.
        cerrarSesionOnline(forzar = true)
        actualizarModo()
        observarConectividad()
        observarAvisoPrivacidad()
        comprobarServidorAlEntrar()
    }

    // ── TTS helpers ──────────────────────────────────────────────────────────

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech

            // Misma voz que EjercicioGuiadoViewModel: femenina española (female / esd)
            val voice = tts?.voices?.firstOrNull { v ->
                v.locale.language == "es" &&
                (v.name.contains("female", ignoreCase = true) ||
                 v.name.contains("esd", ignoreCase = true))
            } ?: tts?.voices?.firstOrNull { v -> v.locale.language == "es" }
            voice?.let { tts?.voice = it }

            tts?.setPitch(1.15f)
            tts?.setSpeechRate(0.85f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) { mainHandler.post { isSpeaking = true  } }
                override fun onDone(id: String?)  { mainHandler.post { isSpeaking = false } }
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) { mainHandler.post { isSpeaking = false } }
            })

            mainHandler.post {
                isTtsReady = true
                pendingTtsText?.let { text ->
                    pendingTtsText = null
                    if (!isTtsMuted) doSpeak(text)
                }
            }
        }
    }

    private fun doSpeak(text: String) {
        val clean = textoParaVoz(text).trim()
            .replace(Regex("\n+"), ". ")
            .replace(Regex(" +"), " ")
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "berto_tts")
    }

    private fun speakBertoMessage(text: String) {
        if (isTtsMuted) return
        if (isTtsReady) doSpeak(text) else pendingTtsText = text
    }

    fun toggleMute() {
        isTtsMuted = !isTtsMuted
        if (isTtsMuted) {
            tts?.stop()
            isSpeaking = false
        }
    }

    //Carga de Arboles
    private fun loadMainMenu() {
        irAlMenu()
        // Saludo online solo si la IA está disponible (hay red y el servidor no está en espera tras un fallo).
        val saludo = if (connectivity.isCurrentlyConnected() && !modePolicy.enEspera) SALUDO_ONLINE else menuRoot.texto
        addBertoMessage(
            content = saludo,
            quickReplies = menuLabels(),
            state = BertoState.TRANQUILO
        )
    }

    private fun irAlMenu() {
        currentTree = menuTree
        currentNode = menuRoot
    }

    private fun menuLabels(): List<String> = menuRoot.opciones.map { it.texto }

    // Tope de la API; aplica también al texto dictado con el micrófono.
    fun onInputChange(text: String) { inputText = text.take(ChatRemoteRepository.MAX_CARACTERES) }

    /** Texto escrito o dictado: va a la API cuando se puede; si no, lo responde el árbol. */
    fun sendMessage(text: String = inputText) {
        if (text.isBlank() || isWaitingForApi) return
        inputText = ""
        procesarEntrada(text, permitirApi = true)
    }

    /** Botones de respuesta rápida: siempre árbol, salvo las acciones propias del chat online. */
    fun onQuickReplySelected(option: String) {
        if (isWaitingForApi) return
        when (option) {
            OPCION_REINTENTAR -> {
                agregarMensajeUsuario(option)
                reintentarEnvio()
            }
            OPCION_TEMAS_GUIADOS -> {
                agregarMensajeUsuario(option)
                mostrarMenu(MSG_TEMAS_GUIADOS)
            }
            else -> procesarEntrada(option, permitirApi = false)
        }
    }

    private fun procesarEntrada(text: String, permitirApi: Boolean) {
        agregarMensajeUsuario(text)

        // Interceptor de pánico: corre antes que la API para que una crisis nunca dependa
        // de la red ni de DeepSeek. Provisional hasta el nuevo detector de crisis (pendiente P1).
        if (containsCrisisKeywords(text)) {
            updateState(BertoState.CRISIS)
            simulateEmergencyResponse()
            return
        }

        // Detectar estado emocional del usuario en texto libre
        detectStateFromFreeText(text)?.let { updateState(it) }

        if (permitirApi && modePolicy.debeUsarApi(connectivity.isCurrentlyConnected())) {
            enviarAApi(text)
        } else {
            processTreeNavigation(text)
        }
    }

    private fun agregarMensajeUsuario(text: String) {
        messages = messages + ChatMessage(
            content = text,
            isFromBerto = false,
            timestamp = now(),
            bertoState = currentBertoState
        )
    }

    // ── Chat online ──────────────────────────────────────────────────────────

    private fun enviarAApi(texto: String) {
        sesionOnlineAbierta = true
        ultimoMensajeFallido = null
        isWaitingForApi = true
        showBertoPeek = true
        isBertoTyping = true
        val cierrePendiente = cierreSesionJob

        envioJob = viewModelScope.launch {
            val avisoLento = launch {
                delay(UMBRAL_PENSANDO_MS)
                isApiSlow = true
            }
            val result = try {
                cierrePendiente?.join()
                chatRemoteRepository.enviarMensaje(texto)
            } finally {
                avisoLento.cancel()
                isApiSlow = false
                isWaitingForApi = false
                isBertoTyping = false
                showBertoPeek = false
            }
            responderResultadoApi(texto, result)
        }
    }

    private fun responderResultadoApi(texto: String, result: EnviarMensajeResult) {
        when (result) {
            is EnviarMensajeResult.Exito -> {
                registrarExitoApi()
                addBertoMessage(
                    content = result.respuesta,
                    quickReplies = listOf(OPCION_TEMAS_GUIADOS),
                    state = currentBertoState
                )
            }
            // En estos dos casos el servidor sí respondió: si estaba en modo guiado, se sale.
            EnviarMensajeResult.ContenidoNoPermitido -> {
                registrarExitoApi()
                mostrarMenu(MSG_CONTENIDO_NO_PERMITIDO)
            }
            EnviarMensajeResult.RespuestaVacia -> {
                registrarExitoApi()
                ultimoMensajeFallido = texto
                mostrarMenu(MSG_RESPUESTA_VACIA, extras = listOf(OPCION_REINTENTAR))
            }
            EnviarMensajeResult.ServidorNoDisponible,
            EnviarMensajeResult.ErrorInesperado -> {
                ultimoMensajeFallido = texto
                registrarFalloApi()
                mostrarMenu(MSG_SERVIDOR_NO_DISPONIBLE, extras = listOf(OPCION_REINTENTAR))
            }
            // Sin usuario de Firebase no hay userId: se comporta como offline.
            EnviarMensajeResult.SinSesion -> {
                sesionOnlineAbierta = false
                processTreeNavigation(texto)
            }
            EnviarMensajeResult.MensajeInvalido ->
                addBertoMessage(MSG_MENSAJE_LARGO, ultimasOpciones(), currentBertoState)
        }
    }

    /** "Reintentar" es una acción explícita: ignora la espera tras un fallo, pero no la falta de red. */
    private fun reintentarEnvio() {
        val texto = ultimoMensajeFallido
        when {
            texto == null -> mostrarMenu(MSG_TEMAS_GUIADOS)
            modePolicy.debeUsarApi(connectivity.isCurrentlyConnected(), esReintento = true) -> enviarAApi(texto)
            else -> mostrarMenu(MSG_SIGUE_SIN_CONEXION, extras = listOf(OPCION_REINTENTAR))
        }
    }

    /** Muestra los 8 temas sin borrar la conversación y deja el árbol parado en el menú. */
    private fun mostrarMenu(texto: String, extras: List<String> = emptyList()) {
        irAlMenu()
        addBertoMessage(content = texto, quickReplies = extras + menuLabels(), state = currentBertoState)
    }

    private fun ultimasOpciones(): List<String> =
        messages.lastOrNull { it.isFromBerto }?.quickReplies.orEmpty()

    private fun registrarExitoApi() {
        sondeoJob?.cancel()
        limpiarEspera()
    }

    private fun limpiarEspera() {
        modePolicy.registrarExito()
        actualizarModo()
    }

    private fun registrarFalloApi() {
        modePolicy.registrarFallo()
        actualizarModo()
        programarSondeo()
    }

    /**
     * La API no tiene endpoint de salud: sin esto la app solo sabría que el servidor está caído
     * al fallar un mensaje. Al abrir el chat se revisa una vez; si no responde, pasa a modo guiado.
     */
    private fun comprobarServidorAlEntrar() {
        if (!conectado) return
        viewModelScope.launch {
            if (!chatRemoteRepository.servidorDisponible()) marcarServidorCaido()
        }
    }

    private fun marcarServidorCaido() {
        addAvisoSistema(AvisoSistema.SERVIDOR_NO_DISPONIBLE, AVISO_SERVIDOR_NO_DISPONIBLE, ultimasOpciones())
        registrarFalloApi()
    }

    /**
     * Cada [ChatModePolicy.ESPERA_TRAS_FALLO_MS] revisa el servidor: si responde se sale del modo
     * guiado; si no, se extiende la espera (en vez de volver a "Conectado" a ciegas).
     */
    private fun programarSondeo() {
        sondeoJob?.cancel()
        sondeoJob = viewModelScope.launch {
            while (true) {
                delay(ChatModePolicy.ESPERA_TRAS_FALLO_MS)
                if (!conectado) continue // al volver internet lo revisa observarConectividad
                if (chatRemoteRepository.servidorDisponible()) {
                    limpiarEspera()
                    addAvisoSistema(AvisoSistema.SERVIDOR_RECUPERADO, AVISO_SERVIDOR_RECUPERADO, ultimasOpciones())
                    break
                }
                modePolicy.registrarFallo()
                actualizarModo()
            }
        }
    }

    private fun actualizarModo() {
        chatMode = when {
            !conectado -> ChatMode.SIN_CONEXION
            modePolicy.enEspera -> ChatMode.MODO_GUIADO
            else -> ChatMode.CONECTADO
        }
        mostrarAvisoPrivacidad = conectado && !avisoPrivacidadVisto
    }

    private fun observarConectividad() {
        viewModelScope.launch {
            // drop(1): el primer valor es el estado al abrir el chat, no un cambio.
            connectivity.observeConnected().drop(1).collect { hayRed ->
                conectado = hayRed
                reconexionJob?.cancel()
                if (hayRed) {
                    // Hay internet, pero antes de decir "recuperada" se revisa que el servidor responda.
                    sondeoJob?.cancel()
                    reconexionJob = viewModelScope.launch {
                        if (chatRemoteRepository.servidorDisponible()) {
                            limpiarEspera()
                            addAvisoSistema(AvisoSistema.CONEXION_RECUPERADA, AVISO_CONEXION_RECUPERADA, ultimasOpciones())
                        } else {
                            marcarServidorCaido()
                        }
                    }
                } else {
                    sondeoJob?.cancel()
                    // Si se estaba hablando con la IA (sin botones o solo "Temas guiados"),
                    // se ofrecen los temas del árbol; si se estaba en medio de un árbol, se conservan sus botones.
                    val opciones = ultimasOpciones()
                    if (opciones.isEmpty() || opciones == listOf(OPCION_TEMAS_GUIADOS)) {
                        irAlMenu()
                        addAvisoSistema(AvisoSistema.SIN_CONEXION, AVISO_SIN_CONEXION, menuLabels())
                    } else {
                        addAvisoSistema(AvisoSistema.SIN_CONEXION, AVISO_SIN_CONEXION, opciones)
                    }
                }
                actualizarModo()
            }
        }
    }

    private fun observarAvisoPrivacidad() {
        viewModelScope.launch {
            chatPreferences.avisoPrivacidadVisto.collect { visto ->
                avisoPrivacidadVisto = visto
                actualizarModo()
            }
        }
    }

    fun cerrarAvisoPrivacidad() {
        mostrarAvisoPrivacidad = false
        avisoPrivacidadVisto = true
        appScope.launch { chatPreferences.marcarAvisoPrivacidadVisto() }
    }

    private fun addAvisoSistema(tipo: AvisoSistema, content: String, quickReplies: List<String>) {
        messages = messages + ChatMessage(
            content = content,
            isFromBerto = true,
            timestamp = now(),
            quickReplies = quickReplies,
            bertoState = currentBertoState,
            avisoSistema = tipo
        )
    }

    /**
     * Cierra la sesión del servidor en segundo plano (en [appScope], para que termine aunque
     * el ViewModel ya no exista). Solo si hubo mensajes a la API, salvo [forzar].
     */
    private fun cerrarSesionOnline(forzar: Boolean = false) {
        if (!forzar && !sesionOnlineAbierta) return
        sesionOnlineAbierta = false
        val anterior = cierreSesionJob
        cierreSesionJob = appScope.launch {
            anterior?.join()
            chatRemoteRepository.cerrarSesion()
        }
    }

    private fun processTreeNavigation(userChoice: String) {
        viewModelScope.launch {
            showBertoPeek = true
            isBertoTyping = true

            // Retraso de escritura
            delay(1200L + (userChoice.length * 15L).coerceAtMost(1200L))

            // Buscar si lo que el usuario presionó coincide con una opción del nodo actual
            val opcionSeleccionada = currentNode?.opciones?.find {
                it.texto.equals(userChoice, ignoreCase = true)
            }

            var nextNode: DecisionNode? = null

            if (opcionSeleccionada != null) {
                val destinoId = opcionSeleccionada.siguienteNodoId

                // CASO A: Estamos en el menú principal y el usuario elige uno de los 8 árboles
                if (currentTree?.id == MAIN_MENU_ID) {
                    try {
                        val selectedTree = treeRepository.obtenerArbol(destinoId)
                        currentTree = selectedTree
                        nextNode = selectedTree.nodos[selectedTree.nodoInicialId]
                        updateState(stateFromTreeId(destinoId))
                    } catch (e: Exception) {
                        // Resguardo por si el ID del repositorio fallara
                        nextNode = null
                    }
                }
                // CASO B: Ya estamos navegando dentro de uno de los 8 árboles de sustancias
                else {
                    nextNode = currentTree?.nodos?.get(destinoId)
                }
            }

            isBertoTyping = false
            delay(200L)
            showBertoPeek = false

            // Responder e interactuar según el nodo obtenido
            if (nextNode != null) {
                currentNode = nextNode

                val respuestaTexto = if (nextNode.mensaje != null) {
                    "${nextNode.mensaje}\n\n${nextNode.texto}"
                } else {
                    nextNode.texto
                }

                addBertoMessage(
                    content = respuestaTexto,
                    quickReplies = nextNode.opciones.map { it.texto },
                    state = currentBertoState
                )

                if (nextNode.esFinal) {
                    delay(1000L)
                    handleEndOfTree()
                }

            } else {
                if (userChoice.equals("Regresar al menú principal", ignoreCase = true)) {
                    clearMessages()
                } else {
                    addBertoMessage(
                        content = "Para poder ayudarte mejor, por favor selecciona una de las siguientes opciones de la lista:",
                        quickReplies = currentNode?.opciones?.map { it.texto } ?: emptyList(),
                        state = currentBertoState
                    )
                }
            }
        }
    }

    private fun handleEndOfTree() {
        updateState(BertoState.TRANQUILO)
        addBertoMessage(
            content = "¿Deseas revisar alguna otra sección o regresar al menú principal?",
            quickReplies = listOf("Regresar al menú principal"),
            state = BertoState.TRANQUILO
        )
        val opcionesReinicio = listOf(DecisionOption("Regresar al menú principal", "regresar"))
        currentNode = DecisionNode("fin", "", NodeType.MESSAGE, opcionesReinicio)
        currentTree = DecisionTree(MAIN_MENU_ID, "", "fin", emptyMap())
    }

    // Solo el contenido del mensaje decide si es crisis. Antes devolvía el estado actual de Berto
    // cuando no había palabras clave, así que tras una crisis todo mensaje se trataba como crisis.
    private fun containsCrisisKeywords(text: String): Boolean {
        val lower = text.lowercase()
        return CRISIS_KEYWORDS.any { lower.contains(it) }
    }

    private fun simulateEmergencyResponse() {
        viewModelScope.launch {
            showBertoPeek = true
            isBertoTyping = true
            delay(1000L)
            isBertoTyping = false
            showBertoPeek = false

            try {
                val crisisTree = treeRepository.obtenerArbol("alcohol_craving")
                currentTree = crisisTree
                currentNode = crisisTree.nodos[crisisTree.nodoInicialId]
            } catch (e: Exception) {
                currentTree = null
                currentNode = null
            }

            addBertoMessage(
                content = currentNode?.texto ?: "Gracias por decírmelo. Lo que estás sintiendo en este momento es muy real, pero no estás solo. ¿Te gustaría activar tu red de apoyo o probar una técnica de respiración?",
                quickReplies = currentNode?.opciones?.map { it.texto } ?: listOf("Sí, avisa a mi red", "Dame técnicas de respiración", "Regresar al menú principal"),
                state = BertoState.CRISIS
            )
        }
    }

    private fun updateState(newState: BertoState) {
        if (newState == currentBertoState) return
        currentBertoState = newState
        stateTransitionJob?.cancel()
        stateTransition = newState
        stateTransitionJob = viewModelScope.launch {
            delay(2800)
            stateTransition = null
        }
    }

    private fun stateFromTreeId(treeId: String): BertoState = when {
        treeId.endsWith("_craving") -> BertoState.PREOCUPADO
        else -> BertoState.TRANQUILO
    }

    private fun detectStateFromFreeText(text: String): BertoState? {
        val lower = text.lowercase()
        return when {
            ANXIETY_KEYWORDS.any { lower.contains(it) } -> BertoState.PREOCUPADO
            POSITIVE_KEYWORDS.any { lower.contains(it) } -> BertoState.CELEBRANDO
            else -> null
        }
    }

    private fun addBertoMessage(content: String, quickReplies: List<String> = emptyList(), state: BertoState) {
        messages = messages + ChatMessage(
            content = content,
            isFromBerto = true,
            timestamp = now(),
            quickReplies = quickReplies,
            bertoState = state
        )
        speakBertoMessage(content)
    }

    fun clearMessages() {
        envioJob?.cancel()
        cerrarSesionOnline()
        ultimoMensajeFallido = null
        tts?.stop()
        isSpeaking = false
        messages = emptyList()
        currentBertoState = BertoState.TRANQUILO
        loadMainMenu()
    }

    fun toggleSosDialog() { showSosDialog = !showSosDialog }

    override fun onCleared() {
        // Best-effort: if the user backs out fast (before the async TextToSpeech engine
        // finishes connecting, or mid-utterance), shutdown() can race the engine's own
        // connection setup. On some OEM TTS engines that race throws from a background
        // binder thread, which crashes the whole process instead of just this cleanup —
        // never worth that for releasing a resource that's about to be garbage collected.
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
        }
        tts = null
        cerrarSesionOnline()
        super.onCleared()
    }

    private fun now(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${c.get(Calendar.MINUTE).toString().padStart(2, '0')}"
    }

    private companion object {
        const val OPCION_REINTENTAR = "Reintentar"
        const val OPCION_TEMAS_GUIADOS = "Temas guiados"

        const val SALUDO_ONLINE = "Hola, soy Berto. Puedes escribirme lo que sientes o elegir un tema:"
        const val MSG_TEMAS_GUIADOS = "Estos son los temas en los que puedo acompañarte:"
        const val MSG_CONTENIDO_NO_PERMITIDO = "No puedo responder a eso. Puedo acompañarte con temas de " +
            "alcohol, vape, cristal o tabaco. ¿Me lo cuentas de otra forma o prefieres elegir un tema?"
        const val MSG_RESPUESTA_VACIA = "No pude responderte esta vez. ¿Lo intentamos de nuevo?"
        const val MSG_SERVIDOR_NO_DISPONIBLE = "Tengo problemas para conectarme ahora. Te acompaño en modo guiado:"
        const val MSG_SIGUE_SIN_CONEXION = "Sigo sin conexión. Mientras tanto te acompaño en modo guiado:"
        const val MSG_MENSAJE_LARGO = "Tu mensaje es muy largo. ¿Me lo cuentas en menos palabras?"

        const val UMBRAL_PENSANDO_MS = 8_000L

        const val AVISO_SIN_CONEXION = "Perdí la conexión. Sigo contigo en modo guiado."
        const val AVISO_CONEXION_RECUPERADA = "Conexión recuperada. Ya puedes escribirme libremente."
        const val AVISO_SERVIDOR_NO_DISPONIBLE = "No puedo conectarme con el servidor. Te acompaño en modo guiado."
        const val AVISO_SERVIDOR_RECUPERADO = "Berto volvió a estar en línea. Ya puedes escribirme libremente."
    }
}