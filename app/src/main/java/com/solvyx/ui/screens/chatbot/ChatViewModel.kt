package com.solvyx.ui.screens.chatbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType
import com.solvyx.backend.decisiontree.repository.DecisionTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val bertoState: BertoState = BertoState.TRANQUILO
)

enum class BertoState { TRANQUILO, PREOCUPADO, CELEBRANDO, CRISIS }

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
    private val treeRepository: DecisionTreeRepository
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

    // Control del flujo actual
    private var currentTree: DecisionTree? = null
    private var currentNode: DecisionNode? = null
    private var stateTransitionJob: Job? = null

    // ID virtual para identificar cuándo estamos parados en el menú principal
    private val MAIN_MENU_ID = "menu_principal_virtual"

    init {
        loadMainMenu()
    }

    //Carga de Arboles
    private fun loadMainMenu() {
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

        val menuTree = DecisionTree(
            id = MAIN_MENU_ID,
            nombre = "Menú Principal",
            nodoInicialId = "raiz_menu",
            nodos = mapOf("raiz_menu" to nodoRaizMenu)
        )

        currentTree = menuTree
        currentNode = nodoRaizMenu

        addBertoMessage(
            content = nodoRaizMenu.texto,
            quickReplies = nodoRaizMenu.opciones.map { it.texto },
            state = BertoState.TRANQUILO
        )
    }

    fun onInputChange(text: String) { inputText = text }

    fun sendMessage(text: String = inputText) {
        if (text.isBlank()) return

        //Mostrar mensaje del usuario en la pantalla
        messages = messages + ChatMessage(
            content = text,
            isFromBerto = false,
            timestamp = now(),
            bertoState = currentBertoState
        )
        inputText = ""

        // Interceptor de pánico por texto libre
        if (detectEmergencyState(text) == BertoState.CRISIS) {
            updateState(BertoState.CRISIS)
            simulateEmergencyResponse()
            return
        }

        // Detectar estado emocional del usuario en texto libre
        detectStateFromFreeText(text)?.let { updateState(it) }

        // Procesar navegación del árbol de decisiones
        processTreeNavigation(text)
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

    private fun detectEmergencyState(text: String): BertoState {
        val lower = text.lowercase()
        return if (CRISIS_KEYWORDS.any { lower.contains(it) }) BertoState.CRISIS else currentBertoState
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

    private fun mapNodeTypeToBertoState(type: NodeType): BertoState {
        return when (type.name.uppercase()) {
            "CRISIS" -> BertoState.CRISIS
            "CRAVING" -> BertoState.PREOCUPADO
            "SUCCESS", "CELEBRACION" -> BertoState.CELEBRANDO
            else -> BertoState.TRANQUILO
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
    }

    fun clearMessages() {
        messages = emptyList()
        currentBertoState = BertoState.TRANQUILO
        loadMainMenu()
    }

    fun toggleSosDialog() { showSosDialog = !showSosDialog }

    private fun now(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${c.get(Calendar.MINUTE).toString().padStart(2, '0')}"
    }
}