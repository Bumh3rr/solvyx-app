package com.solvyx.ui.screens.firstaid.crisischeck

import androidx.annotation.DrawableRes
import com.solvyx.R

/** One sign the user can recognize in themselves. [isRedFlag] alone is enough to escalate. */
data class CrisisSignal(val label: String, val isRedFlag: Boolean = false)

/** One question of the self-check: Berto asks [question], the user marks any of [signals]. */
data class CrisisSignalGroup(
    @DrawableRes val icon: Int,
    val title: String,
    val question: String,
    val signals: List<CrisisSignal>
)

/** What the self-check recommends, from calmest to most urgent. */
enum class CrisisCheckResult { NOTHING_SELECTED, SELF_CARE, POSSIBLE_CRISIS, URGENT }

/** From this many signs on, the check suggests the user may be in a crisis. */
const val POSSIBLE_CRISIS_THRESHOLD = 3

val CrisisSignalGroups = listOf(
    CrisisSignalGroup(
        icon = R.drawable.ic_heart_pulse,
        title = "En tu cuerpo",
        question = "¿Notas algo de esto en tu cuerpo?",
        signals = listOf(
            CrisisSignal("Corazón acelerado"),
            CrisisSignal("Sudoración sin calor"),
            CrisisSignal("Temblores"),
            CrisisSignal("Falta de aire"),
            CrisisSignal("Mareo o náuseas"),
            CrisisSignal("Entumecimiento en manos o cara")
        )
    ),
    CrisisSignalGroup(
        icon = R.drawable.ic_mood_sad,
        title = "En lo que sientes",
        question = "¿Y en lo que sientes?",
        signals = listOf(
            CrisisSignal("Miedo intenso sin razón clara"),
            CrisisSignal("Sensación de que algo malo pasará"),
            CrisisSignal("Pensamientos de hacerte daño", isRedFlag = true),
            CrisisSignal("Sentirte sin salida"),
            CrisisSignal("Llanto que no puedes parar")
        )
    ),
    CrisisSignalGroup(
        icon = R.drawable.ic_activity,
        title = "En lo que haces",
        question = "¿Y en lo que estás haciendo?",
        signals = listOf(
            CrisisSignal("Querer consumir aunque no quieras"),
            CrisisSignal("Aislarte de la gente cercana"),
            CrisisSignal("Dejar de responder mensajes"),
            CrisisSignal("No poder quedarte quieto o quieta")
        )
    )
)

/**
 * Suggests a next step from the signs the user tapped. It's guidance, not a diagnosis: any red
 * flag escalates straight to [CrisisCheckResult.URGENT], regardless of how many signs were chosen.
 */
fun assessCrisisCheck(selected: Collection<CrisisSignal>): CrisisCheckResult = when {
    selected.isEmpty() -> CrisisCheckResult.NOTHING_SELECTED
    selected.any { it.isRedFlag } -> CrisisCheckResult.URGENT
    selected.size >= POSSIBLE_CRISIS_THRESHOLD -> CrisisCheckResult.POSSIBLE_CRISIS
    else -> CrisisCheckResult.SELF_CARE
}
