package com.solvyx.ui.screens.chatbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ChatViewModel @Inject constructor() : ViewModel() {

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var inputText by mutableStateOf("")
        private set
    var isBertoTyping by mutableStateOf(false)
        private set
    var currentBertoState by mutableStateOf(BertoState.TRANQUILO)
        private set
    var showBertoPeek by mutableStateOf(false)
        private set
    var showSosDialog by mutableStateOf(false)
        private set

    init {
        addBertoMessage(
            content = "Hola. ¿Cómo amaneciste hoy?",
            quickReplies = listOf("Bien gracias", "Más o menos", "Mal la verdad"),
            state = BertoState.TRANQUILO
        )
    }

    fun onInputChange(text: String) { inputText = text }

    fun sendMessage(text: String = inputText) {
        if (text.isBlank()) return
        messages = messages + ChatMessage(
            content = text,
            isFromBerto = false,
            timestamp = now(),
            bertoState = currentBertoState
        )
        inputText = ""
        val newState = detectState(text)
        currentBertoState = newState
        simulateResponse(text, newState)
    }

    private fun simulateResponse(userText: String, state: BertoState) {
        viewModelScope.launch {
            showBertoPeek = true
            isBertoTyping = true
            delay(1500L + (userText.length * 20L).coerceAtMost(1500L))
            val (response, replies) = buildResponse(state)
            isBertoTyping = false
            delay(300L)
            showBertoPeek = false
            addBertoMessage(response, replies, state)
        }
    }

    private fun addBertoMessage(
        content: String,
        quickReplies: List<String> = emptyList(),
        state: BertoState
    ) {
        messages = messages + ChatMessage(
            content = content,
            isFromBerto = true,
            timestamp = now(),
            quickReplies = quickReplies,
            bertoState = state
        )
    }

    private fun detectState(text: String): BertoState {
        val lower = text.lowercase()
        return when {
            CRISIS_KEYWORDS.any { lower.contains(it) }   -> BertoState.CRISIS
            ANXIETY_KEYWORDS.any { lower.contains(it) }  -> BertoState.PREOCUPADO
            POSITIVE_KEYWORDS.any { lower.contains(it) } -> BertoState.CELEBRANDO
            else                                          -> BertoState.TRANQUILO
        }
    }

    private fun buildResponse(state: BertoState): Pair<String, List<String>> = when (state) {
        BertoState.CRISIS -> Pair(
            "Gracias por contarme. Lo que sientes es real y tiene salida. " +
            "¿Quieres que avise a alguien de tu red de apoyo ahora mismo?",
            listOf("Sí, avisa a mi red", "No, prefiero hablar contigo", "Dame técnicas de respiración")
        )
        BertoState.PREOCUPADO -> Pair(
            "Gracias por decírmelo. Vamos paso a paso, " +
            "¿quieres que probemos una respiración juntos?",
            listOf("Sí, quiero intentarlo", "Ahora no, gracias", "Necesito hablar de algo")
        )
        BertoState.CELEBRANDO -> Pair(
            "¡Eso es increíble! Cada paso cuenta. ¿Quieres contarme más?",
            listOf("Sí, fue difícil pero lo logré", "Quiero ver mi progreso", "Gracias Berto")
        )
        BertoState.TRANQUILO -> Pair(
            "Entiendo. ¿Hubo algo en particular hoy que te afectó?",
            listOf("Sí, quiero contarte", "No, solo quería hablar", "Enséñame algo nuevo")
        )
    }

    fun clearMessages() {
        messages = emptyList()
        currentBertoState = BertoState.TRANQUILO
        addBertoMessage(
            content = "Hola. ¿Cómo amaneciste hoy?",
            quickReplies = listOf("Bien gracias", "Más o menos", "Mal la verdad"),
            state = BertoState.TRANQUILO
        )
    }

    fun toggleSosDialog() { showSosDialog = !showSosDialog }

    private fun now(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${c.get(Calendar.MINUTE).toString().padStart(2, '0')}"
    }
}
