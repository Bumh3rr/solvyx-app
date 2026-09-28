package com.solvyx.ui.screens.chatbot

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromBerto: Boolean,
    val timestamp: String,
    val quickReplies: List<String> = emptyList(),
    val bertoState: BertoState = BertoState.TRANQUILO,
    // Avisos de la app (no de Berto): tienen su propio estilo y no se leen en voz alta.
    val avisoSistema: AvisoSistema? = null,
    // Rich content under the bubble: topic cards, substance picker or support actions.
    val attachment: ChatAttachment? = null
) {
    /** Offers choices that only make sense while it is the latest one (chips, pickers). */
    val isInteractive: Boolean
        get() = quickReplies.isNotEmpty() || attachment is ChatAttachment.TopicPicker ||
            attachment is ChatAttachment.SubstancePicker
}

sealed interface ChatAttachment {
    /** Guided-menu cards. With [substanceId] the user already named a substance, so FEELING_BAD is hidden. */
    data class TopicPicker(val substanceId: String? = null) : ChatAttachment

    data class SubstancePicker(val intent: TopicIntent) : ChatAttachment

    /**
     * Calm-down and help actions. Always enabled, even in old messages: a call must never be one
     * scroll and one disabled button away. [urgent] leads with calls and adds 911.
     */
    data class SupportActions(val urgent: Boolean) : ChatAttachment
}

enum class SupportAction(val label: String) {
    BREATHE("Respirar conmigo"),
    GROUND("Anclarme al presente"),
    CALL_LIFELINE("Llamar a Línea de la Vida"),
    CALL_SAPTEL("Llamar a SAPTEL"),
    CALL_EMERGENCY("Llamar al 911"),
    ALERT_NETWORK("Avisar a mi red de apoyo"),
    KEEP_TALKING("Seguir hablando contigo")
}

enum class AvisoSistema { SIN_CONEXION, CONEXION_RECUPERADA, SERVIDOR_NO_DISPONIBLE, SERVIDOR_RECUPERADO }

enum class BertoState { TRANQUILO, PREOCUPADO, CELEBRANDO, CRISIS }

/** Quién responde el texto libre, para mostrarlo en la barra superior. */
enum class ChatMode {
    /** Hay red y la API está disponible: responde la IA. */
    CONECTADO,
    /** Sin internet: responde el árbol. */
    SIN_CONEXION,
    /** Hay red pero la IA no está disponible (falló hace poco o el usuario no ha aceptado el aviso): responde el árbol. */
    MODO_GUIADO
}

/** The `bertoState` strings used by the decision trees; unknown values keep Berto as he is. */
fun bertoStateFromTree(value: String): BertoState? = when (value) {
    "FELIZ" -> BertoState.CELEBRANDO
    "PREOCUPADO" -> BertoState.PREOCUPADO
    "TRANQUILO" -> BertoState.TRANQUILO
    else -> null
}
