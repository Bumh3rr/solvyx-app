package com.solvyx.ui.screens.journey.achievements

import androidx.annotation.DrawableRes
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.MoodOptions

/**
 * Where Berto stands on the achievements trail, in segments from the start node: 0 = start,
 * 1 = first milestone, … Between two milestones it moves proportionally to the streak.
 * [milestones] are the thresholds in days, ascending.
 */
fun trailPosition(streak: Int, milestones: List<Int>): Float {
    if (milestones.isEmpty() || streak <= 0) return 0f
    val stops = listOf(0) + milestones
    val segment = stops.zipWithNext().indexOfFirst { (_, end) -> streak < end }
    if (segment == -1) return milestones.size.toFloat()
    val (start, end) = stops[segment] to stops[segment + 1]
    return segment + (streak - start).toFloat() / (end - start)
}

/** The first milestone not reached yet, or `null` when every one is done. */
fun nextMilestone(streak: Int, milestones: List<Int>): Int? = milestones.firstOrNull { it > streak }

/** What Berto says above the trail. */
fun trailMessage(streak: Int, milestones: List<Int>): String {
    val next = nextMilestone(streak, milestones)
    return when {
        next == null -> "¡Llegaste a la cima! Completaste todos los logros de racha."
        streak == 0 -> "Cada día sin consumo es un paso en este camino. Hoy puede ser el primero."
        next - streak == 1 -> "¡Mañana desbloqueas un logro! Solo te falta un día."
        else -> "Vas en el día $streak. Te faltan ${next - streak} días para tu siguiente logro."
    }
}

/**
 * Diary badges: small recognitions computed from the journal on the device. They are not the
 * streak achievements (those live in Firestore); they reward the habit of writing and being honest.
 */
data class DiaryBadge(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val icon: Int,
    val current: Int,
    val target: Int
) {
    val unlocked: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)
}

private const val FIRST_ENTRY = 1
private const val WEEK_OF_ENTRIES = 7
private const val NOTES_TARGET = 5
private const val GOALS_TARGET = 3
private const val HONEST_DAYS_TARGET = 1

/** Only full check-ins count (a doc with only "meta lograda" is not a written day). */
fun diaryBadges(journal: List<JournalEntry>): List<DiaryBadge> {
    val written = journal.filter { it.isRegistered }
    val moodsUsed = written.mapNotNull { it.mood }.toSet()
    return listOf(
        DiaryBadge(
            "primer_registro", "Primer paso", "Escribiste tu primer día",
            R.drawable.ic_footsteps, written.size.coerceAtMost(FIRST_ENTRY), FIRST_ENTRY
        ),
        DiaryBadge(
            "siete_registros", "Constancia", "7 días escritos, aunque no sean seguidos",
            R.drawable.ic_calendar, written.size.coerceAtMost(WEEK_OF_ENTRIES), WEEK_OF_ENTRIES
        ),
        DiaryBadge(
            "cinco_notas", "Palabras que cuentan", "Escribiste 5 notas sobre tu día",
            R.drawable.ic_pencil, written.count { !it.note.isNullOrBlank() }.coerceAtMost(NOTES_TARGET), NOTES_TARGET
        ),
        DiaryBadge(
            "honestidad", "Honestidad", "Registraste un día con consumo. Decirlo también es avanzar",
            R.drawable.ic_heart, written.count { it.consumed == true }.coerceAtMost(HONEST_DAYS_TARGET), HONEST_DAYS_TARGET
        ),
        DiaryBadge(
            "todos_los_animos", "Todas mis emociones", "Registraste los 5 ánimos",
            R.drawable.ic_face_happy, moodsUsed.size.coerceAtMost(MoodOptions.size), MoodOptions.size
        ),
        DiaryBadge(
            "tres_metas", "Meta tras meta", "Cumpliste tu meta del día 3 veces",
            R.drawable.ic_target, journal.count { it.metaLograda }.coerceAtMost(GOALS_TARGET), GOALS_TARGET
        )
    )
}
