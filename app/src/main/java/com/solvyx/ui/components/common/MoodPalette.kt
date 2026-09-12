package com.solvyx.ui.components.common

import androidx.compose.ui.graphics.Color
import com.solvyx.R
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.MoodEuforico
import com.solvyx.ui.theme.MoodNeutral
import com.solvyx.ui.theme.MoodTriste

/**
 * Single source of truth for the 5 moods used across Home and "Mi camino": id (matches
 * `JournalEntry.mood`), display label, face icon, brand color, and the numeric value used to
 * plot mood on the wellbeing chart's Y-axis. Was duplicated, drifting slightly in shape, across
 * HomeMoodCard/CheckInCard/CheckInSuccessDialog/DayDetailSheet/CheckInWizard/FeelingsChart —
 * consolidated here so a future 6th mood, or a color/icon change, only needs one edit.
 */
data class MoodOption(
    val id: String,
    val label: String,
    val icon: Int,
    val color: Color,
    val value: Float
)

val MoodOptions: List<MoodOption> = listOf(
    MoodOption("triste", "Triste", R.drawable.ic_face_sad, MoodTriste, 1f),
    MoodOption("ansioso", "Ansioso", R.drawable.ic_face_anxious, MoodAnsioso, 3f),
    MoodOption("neutral", "Neutral", R.drawable.ic_face_neutral, MoodNeutral, 5f),
    MoodOption("bien", "Bien", R.drawable.ic_face_happy, MoodBien, 7f),
    MoodOption("euforico", "Eufórico", R.drawable.ic_face_euphoric, MoodEuforico, 10f)
)

/** Looks up a mood by id, falling back to "neutral" for null/unknown ids — matches every
 *  existing call site's old `?: R.drawable.ic_face_neutral` / `?: "Neutral"` fallback. */
fun moodOption(id: String?): MoodOption =
    MoodOptions.firstOrNull { it.id == id } ?: MoodOptions.first { it.id == "neutral" }
