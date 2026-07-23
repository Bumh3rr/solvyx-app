package com.solvyx.ui.screens.journey

/** Achievement card model. [progress] is 1f once unlocked; otherwise currentStreak/threshold, clamped 0f-1f. */
data class UiAchievement(
    val id: String,
    val icon: Int,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val progress: Float
)

/** Progress tab data. Loading until the first Firestore snapshot arrives. */
sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Content(
        val streak: Int,
        val bestStreak: Int,
        val nextMilestone: Int,
        val milestoneProgress: Float,
        val feelingsWeek: List<Float>,
        val feelingsMonth: List<Float>,
        val useWeek: List<Float>,
        val useMonth: List<Float>,
        val insight: String,
        val hasHistory: Boolean
    ) : ProgressUiState
}

/** Achievements tab data. Empty when there are no achievements to show. */
sealed interface AchievementsUiState {
    data object Loading : AchievementsUiState
    data object Empty : AchievementsUiState
    data class Content(val achievements: List<UiAchievement>, val unlockedCount: Int) : AchievementsUiState
}
