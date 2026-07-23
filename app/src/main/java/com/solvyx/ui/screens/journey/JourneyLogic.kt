// app/src/main/java/com/solvyx/ui/screens/journey/JourneyLogic.kt
package com.solvyx.ui.screens.journey

import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate

const val TAB_PROGRESS = 0
const val TAB_ACHIEVEMENTS = 1

/** Check-in wizard steps, in order. The SUBSTANCE step only applies if there was use. */
enum class WizardStep { MOOD, NOTE, USE, SUBSTANCE }

/** 3 steps if the user answered NO use; 4 otherwise (Yes or not yet answered). */
fun totalWizardSteps(used: Boolean?): Int = if (used == false) 3 else 4

fun isLastWizardStep(stepIndex: Int, used: Boolean?): Boolean =
    stepIndex == totalWizardSteps(used) - 1

/** Whether the current step allows advancing/saving. Mood required; use required; if used, substance required. */
fun canAdvanceWizard(
    stepIndex: Int,
    mood: String?,
    used: Boolean?,
    substance: String?
): Boolean = when (stepIndex) {
    WizardStep.MOOD.ordinal      -> mood != null
    WizardStep.NOTE.ordinal      -> true
    WizardStep.USE.ordinal       -> used != null
    WizardStep.SUBSTANCE.ordinal -> used != true || substance != null
    else -> false
}

/** "Logged today" for the check-in card: today's doc with mood AND consumed both non-null. */
fun isDayComplete(entries: List<JournalEntry>, today: LocalDate): Boolean =
    entries.any { it.date == today && it.mood != null && it.consumed != null }

fun dateForChartIndex(index: Int, days: List<LocalDate>): LocalDate? = days.getOrNull(index)

/** Empty when there are no achievements to show (what used to leave the grid blank); Content otherwise. */
fun achievementsStateFrom(list: List<UiAchievement>): AchievementsUiState =
    if (list.isEmpty()) AchievementsUiState.Empty
    else AchievementsUiState.Content(list, list.count { it.unlocked })

/** How close [currentStreak] is to [threshold], 0f-1f; always 1f once [unlocked]. */
fun progressToward(currentStreak: Int, threshold: Int, unlocked: Boolean): Float =
    if (unlocked) 1f else (currentStreak.toFloat() / threshold).coerceIn(0f, 1f)
