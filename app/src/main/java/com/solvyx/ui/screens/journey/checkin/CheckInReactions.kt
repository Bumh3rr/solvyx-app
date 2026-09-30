package com.solvyx.ui.screens.journey.checkin

import com.solvyx.ui.screens.journey.WizardStep

/**
 * The moment Berto is reacting to. `BertoStage` maps each one to its reaction in
 * `reacts_berto.riv` (the moods and reading already exist; PROUD, CELEBRATE and the calm moments
 * reuse the closest one until they get their own, see "Berto — Animaciones pendientes" in the
 * vault). That mapping is the only place to touch when a new animation arrives.
 */
enum class BertoGesture {
    GREET,
    REACT_SAD,
    REACT_ANXIOUS,
    REACT_NEUTRAL,
    REACT_GOOD,
    REACT_EUPHORIC,
    READING,
    ATTENTIVE,
    PROUD,
    SUPPORTIVE,
    CELEBRATE,
    COMFORT
}

/** What Berto does and says at a given moment of the check-in. */
data class BertoReaction(val gesture: BertoGesture, val message: String)

private val MoodReactions = mapOf(
    "triste" to BertoReaction(BertoGesture.REACT_SAD, "Gracias por contármelo. Aquí estoy contigo."),
    "ansioso" to BertoReaction(BertoGesture.REACT_ANXIOUS, "Respira conmigo. Reconocerlo ya es un gran paso."),
    "neutral" to BertoReaction(BertoGesture.REACT_NEUTRAL, "Un día tranquilo también cuenta."),
    "bien" to BertoReaction(BertoGesture.REACT_GOOD, "¡Qué bueno! Me alegra mucho."),
    "euforico" to BertoReaction(BertoGesture.REACT_EUPHORIC, "¡Qué energía! Vamos a aprovecharla bien.")
)

/** Short harm-reduction tip per substance, shown once the user picks one. */
val SubstanceTips = mapOf(
    "alcohol" to "Alterna cada bebida con un vaso de agua y come antes de tomar.",
    "cristal" to "Hidrátate, intenta dormir y no mezcles con otras sustancias.",
    "vape" to "Date pausas largas y evita vapear justo antes de dormir.",
    "cigarro" to "Cada cigarro que no fumas cuenta. Prueba retrasar el siguiente."
)

/**
 * Berto's reaction for the current step and answers. Pure: the same inputs always give the same
 * reaction, so every line of the check-in's copy is testable.
 */
fun reactionFor(
    step: WizardStep,
    mood: String?,
    used: Boolean?,
    substance: String?,
    isTyping: Boolean
): BertoReaction = when (step) {
    WizardStep.MOOD -> mood?.let { MoodReactions[it] }
        ?: BertoReaction(BertoGesture.GREET, "¡Hola! ¿Cómo te fue hoy?")
    WizardStep.NOTE -> if (isTyping) {
        BertoReaction(BertoGesture.READING, "Te leo. Tómate tu tiempo.")
    } else {
        BertoReaction(BertoGesture.ATTENTIVE, "Si quieres, cuéntame un poco más. Es opcional.")
    }
    WizardStep.USE -> when (used) {
        null -> BertoReaction(BertoGesture.ATTENTIVE, "Aquí no hay juicios. Solo cuéntame cómo fue.")
        false -> BertoReaction(BertoGesture.PROUD, "¡Un día más para ti! Estoy orgulloso.")
        true -> BertoReaction(
            BertoGesture.SUPPORTIVE,
            "Gracias por tu honestidad. Tu racha vuelve a empezar, pero lo que aprendes se queda."
        )
    }
    WizardStep.SUBSTANCE -> BertoReaction(
        BertoGesture.ATTENTIVE,
        substance?.let { SubstanceTips[it] } ?: "¿Qué consumiste? Te doy un tip para cuidarte."
    )
}

/** Berto on the closing screen, after saving. */
fun resultReaction(used: Boolean): BertoReaction = if (used) {
    BertoReaction(
        BertoGesture.COMFORT,
        "Registrarlo es cuidarte. Mañana es una nueva oportunidad y aquí voy a estar."
    )
} else {
    BertoReaction(BertoGesture.CELEBRATE, "¡Lo lograste! Un día sin consumo más en tu camino.")
}
