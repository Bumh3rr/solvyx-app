package com.solvyx.ui.screens.journey

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.solvyx.backend.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/** Minimum number of logged days before claiming any pattern in "Berto dice". */
private const val MIN_DAYS_FOR_PATTERN = 5

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val repository: ProgressRepository,
    private val streakCalculator: StreakCalculator,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val isAnonymous: Boolean get() = firebaseAuth.currentUser?.isAnonymous == true

    /** 0 = week, 1 = month (Semana/Mes toggle inside Progress). */
    var selectedPeriod by mutableStateOf(0)
        private set

    var progressState by mutableStateOf<ProgressUiState>(ProgressUiState.Loading)
        private set
    var achievementsState by mutableStateOf<AchievementsUiState>(AchievementsUiState.Loading)
        private set

    // Survives the check-in wizard opening/closing (which disposes the whole Achievements tab),
    // so AchievementCard can still play its unlock celebration once the user is back on the tab
    // instead of just recomposing already-unlocked and never celebrating.
    var justUnlockedIds by mutableStateOf<Set<String>>(emptySet())
        private set

    fun consumeJustUnlocked(id: String) {
        justUnlockedIds = justUnlockedIds - id
    }

    var selectedDay by mutableStateOf<JournalEntry?>(null)
        private set

    // Visible days per period and date->entry lookup, for the day detail sheet.
    private var daysWeek: List<LocalDate> = emptyList()
    private var daysMonth: List<LocalDate> = emptyList()
    private var entriesByDate: Map<LocalDate, JournalEntry> = emptyMap()

    val milestoneDays = Achievement.MILESTONE_DAYS
    val labelsWeek = listOf("L", "M", "X", "J", "V", "S", "D")
    val labelsMonth = (1..28).map { it.toString() }

    private val moodScale = mapOf(
        "triste" to 1f, "ansioso" to 3f, "neutral" to 5f, "bien" to 7f, "euforico" to 10f
    )
    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            combine(
                repository.observeJournal(),
                repository.observeAchievements()
            ) { journalEntries, achievementEntities -> Pair(journalEntries, achievementEntities) }
            .catch { emit(emptyList<JournalEntry>() to emptyList()) }
            .collect { (journalEntries, achievementEntities) ->
                val today = LocalDate.now(zone)
                val entryMap = journalEntries.groupBy { it.date }

                val stats = streakCalculator.compute(journalEntries, today)

                // Chart data
                val weekDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val monthDays = (27 downTo 0).map { today.minusDays(it.toLong()) }
                daysWeek = weekDays
                daysMonth = monthDays
                entriesByDate = journalEntries.associateBy { it.date }
                fun moodSeries(days: List<LocalDate>) = days.map { d ->
                    entryMap[d]?.firstOrNull()?.mood?.let { m -> moodScale[m] } ?: 0f
                }
                // -1 = no entry that day at all, 0 = clean (an entry exists, no consumption),
                // 1 = consumption. Keeping "no entry" distinct from "clean" matters: without it,
                // the consumption chart couldn't tell "nothing was ever registered" apart from
                // "registered and clean" — both used to collapse to 0f.
                fun useSeries(days: List<LocalDate>) = days.map { d ->
                    val dayEntries = entryMap[d]
                    when {
                        dayEntries == null -> -1f
                        dayEntries.any { it.consumed == true } -> 1f
                        else -> 0f
                    }
                }

                progressState = ProgressUiState.Content(
                    streak = stats.current,
                    bestStreak = stats.best,
                    nextMilestone = stats.nextMilestone,
                    milestoneProgress = stats.progress,
                    feelingsWeek = moodSeries(weekDays),
                    feelingsMonth = moodSeries(monthDays),
                    useWeek = useSeries(weekDays),
                    useMonth = useSeries(monthDays),
                    insight = buildInsight(entryMap),
                    hasHistory = entryMap.isNotEmpty()
                )

                achievementsState = achievementsStateFrom(achievementEntities.map { mapAchievement(it, stats.current) })
                autoUnlock(achievementEntities, stats.current)
            }
        }
    }

    /**
     * Text for the "Berto dice" card. Derived only from the real journal: if there isn't
     * enough data it says so instead of claiming a pattern that doesn't exist.
     */
    private fun buildInsight(entryMap: Map<LocalDate, List<JournalEntry>>): String {
        val totalDays = entryMap.size
        if (totalDays < MIN_DAYS_FOR_PATTERN) {
            return "Aún no tengo suficientes registros para ver patrones. " +
                "Registra unos días más y aquí te muestro lo que encuentre."
        }

        val daysWithUse = entryMap.filterValues { day -> day.any { it.consumed == true } }.keys
        if (daysWithUse.isEmpty()) {
            return "En $totalDays días registrados no reportaste consumo. " +
                "Ese es un patrón que vale la pena sostener."
        }

        val byWeekday = daysWithUse.groupingBy { it.dayOfWeek }.eachCount()
        val (topDay, count) = byWeekday.maxByOrNull { it.value }!!
        if (count >= 2) {
            val dayName = topDay.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
            return "Los $dayName concentran tu mayor consumo registrado " +
                "($count de ${daysWithUse.size} días con consumo). " +
                "Planear algo distinto ese día puede ayudarte."
        }

        return "Llevas ${daysWithUse.size} de $totalDays días registrados con consumo, " +
            "sin repetirse en un mismo día de la semana. Sigue registrando para ver tus patrones."
    }

    private fun autoUnlock(achievements: List<Achievement>, currentStreak: Int) {
        achievements.filter { !it.unlocked }.forEach { achievement ->
            val threshold = Achievement.STREAK_THRESHOLDS[achievement.id] ?: return@forEach
            if (currentStreak >= threshold) {
                justUnlockedIds = justUnlockedIds + achievement.id
                viewModelScope.launch { repository.unlockAchievement(achievement.id) }
            }
        }
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
        return UiAchievement(entity.id, icon, title, description, entity.unlocked, progress)
    }

    fun selectPeriod(index: Int) { selectedPeriod = index }

    /** Opens the detail of the day tapped on the chart. Ignores days with no entry. */
    fun onChartPointSelected(index: Int) {
        val days = if (selectedPeriod == 0) daysWeek else daysMonth
        val date = dateForChartIndex(index, days) ?: return
        entriesByDate[date]?.let { selectedDay = it }
    }

    fun dismissDayDetail() { selectedDay = null }
}
