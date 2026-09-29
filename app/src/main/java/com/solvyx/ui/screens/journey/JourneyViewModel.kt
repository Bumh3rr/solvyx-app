package com.solvyx.ui.screens.journey

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.solvyx.R
import com.solvyx.backend.common.streak.StreakCalculator
import com.solvyx.backend.data.model.Achievement
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.repository.GoalRepository
import com.solvyx.backend.repository.ProgressRepository
import com.solvyx.ui.screens.journey.diary.diaryEntries
import com.solvyx.ui.screens.journey.progress.WeekView
import com.solvyx.ui.screens.journey.progress.browsableWeeks
import com.solvyx.ui.screens.journey.progress.weekDays
import com.solvyx.ui.screens.journey.progress.weekLabel
import com.solvyx.ui.screens.journey.progress.weekStartOf
import com.solvyx.ui.screens.journey.progress.weekSummary
import com.solvyx.ui.screens.journey.progress.weeklyInsights
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.solvyx.ui.screens.journey.achievements.diaryBadges
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private const val DIARY_PREVIEW_DAYS = 7

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val repository: ProgressRepository,
    private val goalRepository: GoalRepository,
    private val streakCalculator: StreakCalculator,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val isAnonymous: Boolean get() = firebaseAuth.currentUser?.isAnonymous == true

    var progressState by mutableStateOf<ProgressUiState>(ProgressUiState.Loading)
        private set
    var achievementsState by mutableStateOf<AchievementsUiState>(AchievementsUiState.Loading)
        private set

    // Survives the check-in wizard opening/closing, so JourneyScreen can still show the
    // full-screen unlock celebration once the user is back instead of never celebrating.
    var justUnlockedIds by mutableStateOf<Set<String>>(emptySet())
        private set

    fun consumeJustUnlocked(id: String) {
        justUnlockedIds = justUnlockedIds - id
    }

    private val zone = ZoneId.systemDefault()
    val today: LocalDate get() = LocalDate.now(zone)

    /** Monday of the week shown in "Mi semana". */
    var selectedWeek by mutableStateOf(weekStartOf(today))
        private set

    /** The week on screen, recomputed only when the journal or the selected week change. */
    val week: WeekView? by derivedStateOf {
        (progressState as? ProgressUiState.Content)?.let { content ->
            val days = weekDays(selectedWeek, content.entriesByDate, today)
            val index = content.weeks.indexOf(selectedWeek)
            WeekView(
                days = days,
                summary = weekSummary(days),
                label = weekLabel(selectedWeek),
                insights = weeklyInsights(content.entries, selectedWeek),
                canGoBack = index > 0,
                canGoForward = index in 0 until content.weeks.lastIndex
            )
        }
    }


    // Exposed so CheckInViewModel doesn't need its own separate live listener on the same
    // journal collection just to know whether today is already logged (was a 3rd redundant
    // Firestore subscription for this one screen; collapsed to the one this ViewModel already
    // holds for progressState/achievementsState).
    var todayEntry by mutableStateOf<JournalEntry?>(null)
        private set

    init {
        viewModelScope.launch {
            combine(
                repository.observeJournal(),
                repository.observeAchievements(),
                goalRepository.observeGoals()
            ) { journalEntries, achievementEntities, goals -> Triple(journalEntries, achievementEntities, goals) }
            .catch { emit(Triple(emptyList(), emptyList(), emptyList())) }
            .collect { (journalEntries, achievementEntities, goals) ->
                val today = LocalDate.now(zone)
                todayEntry = journalEntries.firstOrNull { it.date == today }

                val stats = streakCalculator.compute(journalEntries, today)
                val registered = diaryEntries(journalEntries)

                progressState = ProgressUiState.Content(
                    streak = stats.current,
                    entries = registered,
                    entriesByDate = registered.associateBy { it.date },
                    weeks = browsableWeeks(registered, today),
                    hasHistory = registered.isNotEmpty(),
                    recentMoods = registered.take(DIARY_PREVIEW_DAYS).map { it.mood }.reversed(),
                    registeredDays = registered.size
                )

                val completedGoals = goals.count { it.completed }
                val (streakEntities, goalEntities) = achievementEntities.partition { it.id in Achievement.STREAK_THRESHOLDS }
                achievementsState = achievementsStateFrom(
                    list = streakEntities.map { mapAchievement(it, stats.current) },
                    currentStreak = stats.current,
                    badges = diaryBadges(journalEntries),
                    goalMedals = goalEntities.filter { it.id in Achievement.GOAL_THRESHOLDS }.map { mapGoalMedal(it, completedGoals) },
                    completedGoals = completedGoals
                )
                autoUnlock(streakEntities, Achievement.STREAK_THRESHOLDS, stats.current)
                autoUnlock(goalEntities, Achievement.GOAL_THRESHOLDS, completedGoals)
            }
        }
    }

    /** Unlocks (and queues the celebration of) every locked achievement whose [thresholds] [count] reached. */
    private fun autoUnlock(achievements: List<Achievement>, thresholds: Map<String, Int>, count: Int) {
        achievements.filter { !it.unlocked }.forEach { achievement ->
            val threshold = thresholds[achievement.id] ?: return@forEach
            if (count >= threshold) {
                justUnlockedIds = justUnlockedIds + achievement.id
                viewModelScope.launch { repository.unlockAchievement(achievement.id) }
            }
        }
    }

    private fun mapGoalMedal(entity: Achievement, completedGoals: Int): UiAchievement {
        val (icon, title, description) = when (entity.id) {
            "metas_completadas_1"  -> Triple(R.drawable.ic_target,      "Primera meta", "Cumpliste tu primera meta")
            "metas_completadas_5"  -> Triple(R.drawable.ic_trending_up, "Cinco metas",  "Cumpliste 5 metas")
            "metas_completadas_10" -> Triple(R.drawable.ic_trophy,      "Diez metas",   "Cumpliste 10 metas")
            else                   -> Triple(R.drawable.ic_target,      entity.id,      "")
        }
        val threshold = Achievement.GOAL_THRESHOLDS[entity.id] ?: 1
        val progress = progressToward(completedGoals, threshold, entity.unlocked)
        val unlockedOn = entity.unlockDate?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        return UiAchievement(entity.id, icon, title, description, entity.unlocked, progress, threshold, unlockedOn)
    }

    private fun mapAchievement(entity: Achievement, currentStreak: Int): UiAchievement {
        val (icon, title, description) = when (entity.id) {
            "racha_3"  -> Triple(R.drawable.ic_trophy, "Primeros pasos", "3 días consecutivos")
            "racha_7"  -> Triple(R.drawable.ic_flame,  "Primera semana", "7 días sin consumo")
            "racha_10" -> Triple(R.drawable.ic_brain,  "Mente clara",    "10 días consecutivos")
            "racha_15" -> Triple(R.drawable.ic_flag,   "2 semanas",      "15 días consecutivos")
            "racha_30" -> Triple(R.drawable.ic_gem,    "Un mes",         "30 días consecutivos")
            else       -> Triple(R.drawable.ic_trophy, entity.id,        "")
        }
        val threshold = Achievement.STREAK_THRESHOLDS[entity.id] ?: 1
        val progress = progressToward(currentStreak, threshold, entity.unlocked)
        val unlockedOn = entity.unlockDate?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        return UiAchievement(entity.id, icon, title, description, entity.unlocked, progress, threshold, unlockedOn)
    }

    fun showPreviousWeek() = moveWeek(-1)

    fun showNextWeek() = moveWeek(1)

    private fun moveWeek(step: Int) {
        val weeks = (progressState as? ProgressUiState.Content)?.weeks ?: return
        weeks.getOrNull(weeks.indexOf(selectedWeek) + step)?.let { selectedWeek = it }
    }
}
