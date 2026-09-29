// app/src/main/java/com/solvyx/ui/screens/journey/JourneyLogic.kt
package com.solvyx.ui.screens.journey

import com.solvyx.ui.screens.journey.achievements.DiaryBadge

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

/** Empty when there are no achievements to show (what used to leave the grid blank); Content otherwise. */
fun achievementsStateFrom(
    list: List<UiAchievement>,
    currentStreak: Int = 0,
    badges: List<DiaryBadge> = emptyList(),
    goalMedals: List<UiAchievement> = emptyList(),
    completedGoals: Int = 0
): AchievementsUiState =
    if (list.isEmpty()) AchievementsUiState.Empty
    else AchievementsUiState.Content(list, list.count { it.unlocked }, currentStreak, badges, goalMedals, completedGoals)

/** How close [currentStreak] is to [threshold], 0f-1f; always 1f once [unlocked]. */
fun progressToward(currentStreak: Int, threshold: Int, unlocked: Boolean): Float =
    if (unlocked) 1f else (currentStreak.toFloat() / threshold).coerceIn(0f, 1f)
