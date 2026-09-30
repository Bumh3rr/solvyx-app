package com.solvyx.ui.screens.chatbot

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.preferences.ChatPreferencesRepository
import com.solvyx.backend.data.remote.chat.ChatRemoteRepository
import com.solvyx.backend.data.remote.chat.EnviarMensajeResult
import com.solvyx.backend.data.remote.connectivity.ConnectivityRepository
import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.repository.DecisionTreeRepository
import com.solvyx.di.ApplicationScope
import com.solvyx.ui.components.common.substanceLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** Optional chat route arguments: a [TopicIntent] name and a substance id open that guide directly. */
const val CHAT_TOPIC_ARG = "topic"
const val CHAT_SUBSTANCE_ARG = "substance"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val treeRepository: DecisionTreeRepository,
    @ApplicationContext appContext: Context,
    private val chatRemoteRepository: ChatRemoteRepository,
    private val connectivity: ConnectivityRepository,
    private val chatPreferences: ChatPreferencesRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var inputText by mutableStateOf("")
        private set
    var isBertoTyping by mutableStateOf(false)
        private set
    var currentBertoState by mutableStateOf(BertoState.TRANQUILO)
        private set
    // Bumps every time Berto starts celebrating; the stage fires one confetti burst per value.
    var celebrationCount by mutableIntStateOf(0)
        private set
    var showSosDialog by mutableStateOf(false)
        private set
    var isBreathingOpen by mutableStateOf(false)
        private set
    // true mientras se espera la respuesta de la API; la UI bloquea la entrada (salvo SOS).
    var isWaitingForApi by mutableStateOf(false)
        private set
    // true si la API tarda más de UMBRAL_PENSANDO_MS: la UI cambia a "Berto está pensando…".
    var isApiSlow by mutableStateOf(false)
        private set
    // Guided until the stored consent is read: never claim "Conectado" before knowing it.
    var chatMode by mutableStateOf(ChatMode.MODO_GUIADO)
        private set
    // Consent card: nothing is sent to the server until the user accepts it.
    var showAiConsentPrompt by mutableStateOf(false)
        private set

    private val voice = BertoVoice(appContext)
    val isTtsMuted: Boolean get() = voice.isMuted
    val isSpeaking: Boolean get() = voice.isSpeaking

    /**
     * The Berto message whose chips or picker still accept taps: the latest one with choices, and
     * only until the user answers (anything the user sends after it turns it into history).
     */
    val activeInteractiveMessageId: String?
        get() {
            for (message in messages.asReversed()) {
                if (!message.isFromBerto) return null
                if (message.isInteractive) return message.id
            }
            return null
        }

    /** Chips and pickers wait while Berto is still answering, so two answers never interleave. */
    val canUseChoices: Boolean get() = !isWaitingForApi && !isBertoTyping

    // ── Guided flow (trees, grounding) ───────────────────────────────────────
    private var currentTree: DecisionTree? = null
    private var currentNode: DecisionNode? = null
    private var groundingStep: Int? = null
    // Scripted answers run one after another: each one waits for the previous to finish.
    private var guidedJob: Job? = null
    private val conversationMood = ConversationMood()

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
    private var aiConsentGranted = false
    // "Ahora no" only hides the card for this chat; it is asked again the next time the chat opens.
    private var aiConsentPostponed = false

    // Opened from "Pregúntale a Berto" (Plan): start straight in that guide instead of the topic picker.
    private val presetTopic: TopicIntent? = savedStateHandle.get<String>(CHAT_TOPIC_ARG)
        ?.let { topic -> TopicIntent.entries.firstOrNull { it.name.equals(topic, ignoreCase = true) } }
    private val presetSubstance: String? = savedStateHandle.get<String>(CHAT_SUBSTANCE_ARG)?.takeIf { it.isNotBlank() }

    init {
        viewModelScope.launch {
            // Consent must be known before the greeting and before anything is sent to the server.
            aiConsentGranted = chatPreferences.aiConsentGranted.first()
            val topic = presetTopic
            val substance = presetSubstance
            if (topic != null && substance != null) startTree(topic, substance, announce = true) else showWelcome()
            if (aiConsentGranted) startOnlineChat()
            actualizarModo()
            observarConectividad()
        }
    }

    // ── Input ────────────────────────────────────────────────────────────────

    // Tope de la API; aplica también al texto dictado con el micrófono.
    fun onInputChange(text: String) { inputText = text.take(ChatRemoteRepository.MAX_CARACTERES) }

    /** Texto escrito o dictado: va a la API cuando se puede; si no, lo responde el flujo guiado. */
    fun sendMessage(text: String = inputText) {
        if (text.isBlank() || isWaitingForApi) return
        inputText = ""
        addUserMessage(text)
        processFreeText(text)
    }

    fun onQuickReplySelected(option: String) {
        if (!canUseChoices) return
        addUserMessage(option)
        when {
            option == OPCION_REINTENTAR -> reintentarEnvio()
            option == OPCION_TEMAS_GUIADOS || option == BertoScripts.OPTION_OTHER_TOPICS ->
                showTopicPicker(BertoScripts.TOPICS_AGAIN)
            option == BertoScripts.OPTION_BREATHE -> openBreathing()
            option == BertoScripts.OPTION_THATS_ALL -> sayGoodbye()
            option == BertoScripts.OPTION_FEEL_BETTER -> onFeelBetter()
            option == BertoScripts.OPTION_STILL_BAD -> onStillBad()
            option == BertoScripts.OPTION_GROUNDING_NEXT && groundingStep != null -> advanceGrounding()
            else -> answerGuided(option)
        }
    }

    fun onTopicSelected(intent: TopicIntent, presetSubstanceId: String?) {
        if (!canUseChoices) return
        addUserMessage(intent.label)
        when {
            intent == TopicIntent.FEELING_BAD -> openFeelingBadSupport()
            presetSubstanceId != null -> startTree(intent, presetSubstanceId, announce = false)
            else -> bertoReplies {
                say(BertoScripts.askSubstance(intent), attachment = ChatAttachment.SubstancePicker(intent))
            }
        }
    }

    fun onSubstanceSelected(intent: TopicIntent, substanceId: String) {
        if (!canUseChoices) return
        addUserMessage(substanceLabel(substanceId))
        startTree(intent, substanceId, announce = false)
    }

    private fun processFreeText(text: String) {
        val reaction = conversationMood.react(currentBertoState, text)
        when (reaction.shift) {
            // Interceptor de pánico: corre antes que la API para que una crisis nunca dependa
            // de la red ni de DeepSeek.
            CrisisShift.DETECTED -> {
                enterCrisisSupport()
                return
            }
            // A calmer crisis must look calmer right away, whatever answers next (AI or guided).
            CrisisShift.EASED, CrisisShift.STEPPED_DOWN -> updateState(reaction.state, celebrate = false)
            CrisisShift.NONE -> updateState(reaction.state)
        }
        when {
            canUseApi() -> enviarAApi(text)
            reaction.shift == CrisisShift.EASED -> answerFeelingBetter()
            else -> answerGuided(text)
        }
    }

    private fun addUserMessage(text: String) {
        messages = messages + ChatMessage(
            content = text,
            isFromBerto = false,
            timestamp = now(),
            bertoState = currentBertoState
        )
    }

    // ── Guided flow ──────────────────────────────────────────────────────────

    private fun showWelcome() {
        resetGuidedFlow()
        say(
            BertoScripts.greeting(canChatFreely = canUseApi()),
            attachment = ChatAttachment.TopicPicker()
        )
    }

    private fun showTopicPicker(text: String, extras: List<String> = emptyList()) {
        resetGuidedFlow()
        bertoReplies { say(text, quickReplies = extras, attachment = ChatAttachment.TopicPicker()) }
    }

    /** Offline (or button) answer: follows the current tree, or guesses a topic from the words. */
    private fun answerGuided(text: String) {
        val node = currentNode
        val option = node?.opciones?.firstOrNull { it.texto.equals(text.trim(), ignoreCase = true) }
        when {
            option != null -> followTreeOption(option)
            // Mid-topic, words that name another topic or a feeling change course instead of being ignored.
            node != null && !OfflineTopicMatcher.match(text).isEmpty -> answerFromTopicMatch(text)
            node != null && MoodDetector.detect(text) != null -> answerFromMood(text)
            node != null -> bertoReplies {
                say(BertoScripts.IN_TREE_NOT_UNDERSTOOD, quickReplies = node.opciones.map { it.texto })
            }
            else -> answerFromTopicMatch(text)
        }
    }

    private fun answerFromTopicMatch(text: String) {
        val match = OfflineTopicMatcher.match(text)
        val intent = match.intent
        val substanceId = match.substanceId
        when {
            intent != null && substanceId != null -> startTree(intent, substanceId, announce = true)
            intent != null -> bertoReplies {
                say(BertoScripts.askSubstance(intent), attachment = ChatAttachment.SubstancePicker(intent))
            }
            substanceId != null -> bertoReplies {
                say(
                    BertoScripts.askIntentFor(substanceLabel(substanceId)),
                    attachment = ChatAttachment.TopicPicker(substanceId)
                )
            }
            // In crisis, offline, Berto cannot improvise: he stays close and offers to breathe.
            currentBertoState == BertoState.CRISIS -> bertoReplies {
                say(
                    BertoScripts.CRISIS_OFFLINE_REPLY,
                    quickReplies = listOf(BertoScripts.OPTION_BREATHE, BertoScripts.OPTION_FEEL_BETTER)
                )
            }
            else -> answerFromMood(text)
        }
    }

    /** No topic in the words: Berto still answers how the user sounds instead of a cold "elige un tema". */
    private fun answerFromMood(text: String) {
        when (MoodDetector.detect(text)) {
            BertoState.PREOCUPADO -> openFeelingBadSupport()
            BertoState.CELEBRANDO -> bertoReplies {
                resetGuidedFlow()
                say(
                    BertoScripts.POSITIVE_REPLY,
                    quickReplies = listOf(BertoScripts.OPTION_OTHER_TOPICS, BertoScripts.OPTION_THATS_ALL)
                )
            }
            else -> bertoReplies {
                resetGuidedFlow()
                val reply = if (GreetingDetector.isGreeting(text)) BertoScripts.GREETING_REPLY else BertoScripts.OFFLINE_NOT_UNDERSTOOD
                say(reply, attachment = ChatAttachment.TopicPicker())
            }
        }
    }

    private fun startTree(intent: TopicIntent, substanceId: String, announce: Boolean) {
        val tree = intent.treeIdFor(substanceId)
            ?.let { runCatching { treeRepository.obtenerArbol(it) }.getOrNull() }
        val firstNode = tree?.nodos?.get(tree.nodoInicialId)
        if (tree == null || firstNode == null) {
            showTopicPicker(BertoScripts.TOPICS_AGAIN)
            return
        }
        resetGuidedFlow()
        currentTree = tree
        if (intent == TopicIntent.CRAVING) updateStateUnlessCrisis(BertoState.PREOCUPADO)
        bertoReplies {
            if (announce) {
                say(BertoScripts.startingTopic(substanceLabel(substanceId)))
                typeFor(SHORT_TYPING_MS)
            }
            showNode(firstNode)
        }
    }

    private fun followTreeOption(option: DecisionOption) {
        val next = currentTree?.nodos?.get(option.siguienteNodoId)
        bertoReplies {
            option.reaccion?.let {
                say(it)
                typeFor(SHORT_TYPING_MS)
            }
            if (next == null) {
                resetGuidedFlow()
                say(BertoScripts.TOPICS_AGAIN, attachment = ChatAttachment.TopicPicker())
            } else {
                showNode(next)
            }
        }
    }

    /** A node reads as a short conversation: the detail first, then the question with its chips. */
    private suspend fun showNode(node: DecisionNode) {
        currentNode = node
        bertoStateFromTree(node.bertoState)?.let { updateStateUnlessCrisis(it) }
        node.mensaje?.takeIf { it.isNotBlank() }?.let {
            say(it)
            typeFor(node.delayMs.coerceIn(MIN_TYPING_MS, MAX_TYPING_MS))
        }
        say(node.texto, quickReplies = node.opciones.map { it.texto })
        if (node.esFinal || node.opciones.isEmpty()) {
            typeFor(DEFAULT_TYPING_MS)
            resetGuidedFlow()
            say(
                BertoScripts.TREE_FINISHED,
                quickReplies = listOf(
                    BertoScripts.OPTION_OTHER_TOPICS,
                    BertoScripts.OPTION_BREATHE,
                    BertoScripts.OPTION_THATS_ALL
                )
            )
        }
    }

    private fun sayGoodbye() {
        resetGuidedFlow()
        updateStateUnlessCrisis(BertoState.TRANQUILO)
        bertoReplies { say(BertoScripts.GOODBYE) }
    }

    private fun resetGuidedFlow() {
        currentTree = null
        currentNode = null
        groundingStep = null
    }

    // ── Support and crisis ───────────────────────────────────────────────────

    /** From the top-bar menu: the user asks for help without having to find the words. */
    fun requestCrisisSupport() {
        addUserMessage(MSG_NECESITO_AYUDA)
        enterCrisisSupport()
    }

    private fun enterCrisisSupport() {
        resetGuidedFlow()
        conversationMood.reset()
        updateState(BertoState.CRISIS)
        bertoReplies(SHORT_TYPING_MS) {
            say(BertoScripts.CRISIS_OPENING, attachment = ChatAttachment.SupportActions(urgent = true))
        }
    }

    private fun openFeelingBadSupport() {
        resetGuidedFlow()
        updateStateUnlessCrisis(BertoState.PREOCUPADO)
        bertoReplies {
            say(BertoScripts.FEELING_BAD_OPENING, attachment = ChatAttachment.SupportActions(urgent = false))
        }
    }

    /** Calls are dialed by the screen (it owns the Context); here Berto only keeps the user company. */
    fun onSupportAction(action: SupportAction) {
        when (action) {
            SupportAction.BREATHE -> openBreathing()
            SupportAction.GROUND -> startGrounding()
            SupportAction.CALL_LIFELINE,
            SupportAction.CALL_SAPTEL,
            SupportAction.CALL_EMERGENCY -> {
                // The dialer is opening: Berto must not talk over the call.
                voice.stop()
                bertoReplies(SHORT_TYPING_MS) { say(BertoScripts.CALL_STARTED, speak = false) }
            }
            SupportAction.ALERT_NETWORK -> showSosDialog = true
            SupportAction.KEEP_TALKING -> {
                addUserMessage(action.label)
                resetGuidedFlow()
                bertoReplies { say(BertoScripts.KEEP_TALKING) }
            }
        }
    }

    /** "Ya estoy mejor" from the crisis bar. */
    fun leaveCrisisSupport() {
        addUserMessage(BertoScripts.LEAVE_CRISIS)
        onFeelBetter()
    }

    private fun onFeelBetter() {
        conversationMood.reset()
        updateState(BertoState.TRANQUILO)
        answerFeelingBetter()
    }

    private fun answerFeelingBetter() {
        resetGuidedFlow()
        bertoReplies {
            say(
                BertoScripts.FEEL_BETTER_REPLY,
                quickReplies = listOf(BertoScripts.OPTION_OTHER_TOPICS, BertoScripts.OPTION_THATS_ALL)
            )
        }
    }

    private fun onStillBad() {
        resetGuidedFlow()
        updateStateUnlessCrisis(BertoState.PREOCUPADO)
        bertoReplies {
            say(BertoScripts.STILL_BAD_REPLY, attachment = ChatAttachment.SupportActions(urgent = true))
        }
    }

    private fun askHowYouFeel() {
        say(
            BertoScripts.ASK_HOW_YOU_FEEL,
            quickReplies = listOf(BertoScripts.OPTION_FEEL_BETTER, BertoScripts.OPTION_STILL_BAD)
        )
    }

    // ── Breathing ────────────────────────────────────────────────────────────

    fun openBreathing() {
        voice.stop()
        isBreathingOpen = true
    }

    /** Berto says each phase out loud, so the user can breathe with their eyes closed. */
    fun onBreathPhase(phaseName: String) = voice.speak(phaseName)

    fun closeBreathing(completedCycles: Int) {
        isBreathingOpen = false
        voice.stop()
        bertoReplies(SHORT_TYPING_MS) {
            if (completedCycles > 0) {
                say(BertoScripts.BREATHING_DONE)
                typeFor(SHORT_TYPING_MS)
            }
            askHowYouFeel()
        }
    }

    // ── 5-4-3-2-1 grounding ──────────────────────────────────────────────────

    private fun startGrounding() {
        addUserMessage(SupportAction.GROUND.label)
        resetGuidedFlow()
        groundingStep = 0
        bertoReplies {
            say(BertoScripts.GROUNDING_INTRO)
            typeFor(DEFAULT_TYPING_MS)
            say(BertoScripts.groundingSteps.first(), quickReplies = listOf(BertoScripts.OPTION_GROUNDING_NEXT))
        }
    }

    private fun advanceGrounding() {
        val next = (groundingStep ?: return) + 1
        if (next < BertoScripts.groundingSteps.size) {
            groundingStep = next
            bertoReplies {
                say(BertoScripts.groundingSteps[next], quickReplies = listOf(BertoScripts.OPTION_GROUNDING_NEXT))
            }
        } else {
            groundingStep = null
            bertoReplies {
                say(BertoScripts.GROUNDING_DONE)
                typeFor(SHORT_TYPING_MS)
                askHowYouFeel()
            }
        }
    }

    // ── Chat online ──────────────────────────────────────────────────────────

    /** Without consent the API is never used, whatever the network state. */
    private fun canUseApi(isRetry: Boolean = false): Boolean =
        aiConsentGranted && modePolicy.debeUsarApi(connectivity.isCurrentlyConnected(), esReintento = isRetry)

    private fun startOnlineChat() {
        // Limpia una sesión que haya quedado abierta (p. ej. si Android cerró la app con el chat abierto),
        // para que el contexto del servidor coincida con el chat vacío que ve el usuario.
        cerrarSesionOnline(forzar = true)
        comprobarServidorAlEntrar()
    }

    private fun enviarAApi(texto: String) {
        resetGuidedFlow()
        sesionOnlineAbierta = true
        ultimoMensajeFallido = null
        isWaitingForApi = true
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
            }
            responderResultadoApi(texto, result)
        }
    }

    private fun responderResultadoApi(texto: String, result: EnviarMensajeResult) {
        when (result) {
            is EnviarMensajeResult.Exito -> {
                registrarExitoApi()
                say(result.respuesta, quickReplies = listOf(OPCION_TEMAS_GUIADOS))
            }
            // En estos dos casos el servidor sí respondió: si estaba en modo guiado, se sale.
            EnviarMensajeResult.ContenidoNoPermitido -> {
                registrarExitoApi()
                showTopicPicker(MSG_CONTENIDO_NO_PERMITIDO)
            }
            EnviarMensajeResult.RespuestaVacia -> {
                registrarExitoApi()
                ultimoMensajeFallido = texto
                showTopicPicker(MSG_RESPUESTA_VACIA, extras = listOf(OPCION_REINTENTAR))
            }
            EnviarMensajeResult.ServidorNoDisponible,
            EnviarMensajeResult.ErrorInesperado -> {
                ultimoMensajeFallido = texto
                registrarFalloApi()
                showTopicPicker(MSG_SERVIDOR_NO_DISPONIBLE, extras = listOf(OPCION_REINTENTAR))
            }
            // Sin usuario de Firebase no hay userId: se comporta como offline.
            EnviarMensajeResult.SinSesion -> {
                sesionOnlineAbierta = false
                answerGuided(texto)
            }
            EnviarMensajeResult.MensajeInvalido -> say(MSG_MENSAJE_LARGO)
        }
    }

    /** "Reintentar" es una acción explícita: ignora la espera tras un fallo, pero no la falta de red. */
    private fun reintentarEnvio() {
        val texto = ultimoMensajeFallido
        when {
            texto == null -> showTopicPicker(BertoScripts.TOPICS_AGAIN)
            canUseApi(isRetry = true) -> enviarAApi(texto)
            else -> showTopicPicker(MSG_SIGUE_SIN_CONEXION, extras = listOf(OPCION_REINTENTAR))
        }
    }

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
        addAvisoSistema(AvisoSistema.SERVIDOR_NO_DISPONIBLE, AVISO_SERVIDOR_NO_DISPONIBLE)
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
                    addAvisoSistema(AvisoSistema.SERVIDOR_RECUPERADO, AVISO_SERVIDOR_RECUPERADO)
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
            !aiConsentGranted || modePolicy.enEspera -> ChatMode.MODO_GUIADO
            else -> ChatMode.CONECTADO
        }
        showAiConsentPrompt = !aiConsentGranted && !aiConsentPostponed
    }

    private fun observarConectividad() {
        viewModelScope.launch {
            // drop(1): el primer valor es el estado al abrir el chat, no un cambio.
            connectivity.observeConnected().drop(1).collect { hayRed ->
                conectado = hayRed
                reconexionJob?.cancel()
                // Without consent the chat is tree-only: losing or recovering internet changes nothing.
                if (!aiConsentGranted) {
                    actualizarModo()
                    return@collect
                }
                if (hayRed) {
                    // Hay internet, pero antes de decir "recuperada" se revisa que el servidor responda.
                    sondeoJob?.cancel()
                    reconexionJob = viewModelScope.launch {
                        if (chatRemoteRepository.servidorDisponible()) {
                            limpiarEspera()
                            addAvisoSistema(AvisoSistema.CONEXION_RECUPERADA, AVISO_CONEXION_RECUPERADA)
                        } else {
                            marcarServidorCaido()
                        }
                    }
                } else {
                    sondeoJob?.cancel()
                    // Si se estaba hablando con la IA, se ofrecen los temas guiados; si se estaba en
                    // medio de un árbol o de una técnica, sus botones siguen activos.
                    val talkingToAi = messages.lastOrNull { it.isInteractive }
                        ?.let { it.attachment == null && it.quickReplies == listOf(OPCION_TEMAS_GUIADOS) }
                        ?: true
                    if (talkingToAi) resetGuidedFlow()
                    addAvisoSistema(
                        AvisoSistema.SIN_CONEXION,
                        AVISO_SIN_CONEXION,
                        attachment = if (talkingToAi) ChatAttachment.TopicPicker() else null
                    )
                }
                actualizarModo()
            }
        }
    }

    fun acceptAiConsent() {
        aiConsentGranted = true
        appScope.launch { chatPreferences.grantAiConsent() }
        actualizarModo()
        startOnlineChat()
        // Offline, the "connection recovered" notice will tell the user once internet is back.
        if (conectado) say(BertoScripts.AI_CONSENT_ACCEPTED)
    }

    fun postponeAiConsent() {
        aiConsentPostponed = true
        actualizarModo()
    }

    private fun addAvisoSistema(tipo: AvisoSistema, content: String, attachment: ChatAttachment? = null) {
        messages = messages + ChatMessage(
            content = content,
            isFromBerto = true,
            timestamp = now(),
            bertoState = currentBertoState,
            avisoSistema = tipo,
            attachment = attachment
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

    // ── Berto's state and messages ───────────────────────────────────────────

    /** [celebrate] = false skips the confetti, e.g. right after a crisis eases. */
    private fun updateState(newState: BertoState, celebrate: Boolean = true) {
        if (newState == currentBertoState) return
        currentBertoState = newState
        if (newState == BertoState.CELEBRANDO && celebrate) celebrationCount++
    }

    private fun updateStateUnlessCrisis(newState: BertoState) {
        if (currentBertoState != BertoState.CRISIS) updateState(newState)
    }

    /** Shows "typing" for [typingMs] and then runs [answer]; answers queue up in order. */
    private fun bertoReplies(typingMs: Long = DEFAULT_TYPING_MS, answer: suspend () -> Unit) {
        val previous = guidedJob
        guidedJob = viewModelScope.launch {
            previous?.join()
            typeFor(typingMs)
            answer()
        }
    }

    private suspend fun typeFor(typingMs: Long) {
        isBertoTyping = true
        try {
            delay(typingMs)
        } finally {
            isBertoTyping = false
        }
    }

    private fun say(
        content: String,
        quickReplies: List<String> = emptyList(),
        attachment: ChatAttachment? = null,
        speak: Boolean = true
    ) {
        messages = messages + ChatMessage(
            content = content,
            isFromBerto = true,
            timestamp = now(),
            quickReplies = quickReplies,
            bertoState = currentBertoState,
            attachment = attachment
        )
        if (speak) voice.speak(content)
    }

    fun toggleMute() = voice.toggleMute()

    fun clearMessages() {
        envioJob?.cancel()
        guidedJob?.cancel()
        guidedJob = null
        isBertoTyping = false
        cerrarSesionOnline()
        ultimoMensajeFallido = null
        voice.stop()
        messages = emptyList()
        currentBertoState = BertoState.TRANQUILO
        showWelcome()
    }

    fun toggleSosDialog() { showSosDialog = !showSosDialog }

    override fun onCleared() {
        voice.shutdown()
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

        const val MSG_NECESITO_AYUDA = "Necesito ayuda ahora"
        const val MSG_CONTENIDO_NO_PERMITIDO = "No puedo responder a eso. Puedo acompañarte con temas de " +
            "alcohol, vape, cristal o tabaco. ¿Me lo cuentas de otra forma o prefieres elegir un tema?"
        const val MSG_RESPUESTA_VACIA = "No pude responderte esta vez. ¿Lo intentamos de nuevo?"
        const val MSG_SERVIDOR_NO_DISPONIBLE = "Tengo problemas para conectarme ahora. Te acompaño en modo guiado:"
        const val MSG_SIGUE_SIN_CONEXION = "Sigo sin conexión. Mientras tanto te acompaño en modo guiado:"
        const val MSG_MENSAJE_LARGO = "Tu mensaje es muy largo. ¿Me lo cuentas en menos palabras?"

        const val UMBRAL_PENSANDO_MS = 8_000L
        const val SHORT_TYPING_MS = 700L
        const val DEFAULT_TYPING_MS = 1_100L
        const val MIN_TYPING_MS = 700L
        const val MAX_TYPING_MS = 1_800L

        const val AVISO_SIN_CONEXION = "Perdí la conexión. Sigo contigo en modo guiado."
        const val AVISO_CONEXION_RECUPERADA = "Conexión recuperada. Ya puedes escribirme libremente."
        const val AVISO_SERVIDOR_NO_DISPONIBLE = "No puedo conectarme con el servidor. Te acompaño en modo guiado."
        const val AVISO_SERVIDOR_RECUPERADO = "Berto volvió a estar en línea. Ya puedes escribirme libremente."
    }
}
