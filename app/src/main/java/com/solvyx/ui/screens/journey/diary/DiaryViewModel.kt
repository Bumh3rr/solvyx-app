package com.solvyx.ui.screens.journey.diary

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.common.goals.DayGoalNote
import com.solvyx.backend.common.goals.GoalDays
import com.solvyx.backend.common.streak.StreakCalculator
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.repository.GoalRepository
import com.solvyx.backend.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

sealed interface DiaryUiState {
    data object Loading : DiaryUiState
    data object Empty : DiaryUiState
    data class Content(
        val entries: List<JournalEntry>,
        val entriesByDate: Map<LocalDate, JournalEntry>,
        val summary: DiarySummary,
        val months: List<YearMonth>,
        // Days that added to a goal ("Avanzaste en tu meta") or completed one.
        val goalNotes: Map<LocalDate, DayGoalNote> = emptyMap()
    ) : DiaryUiState
}

/** "Mi diario": every logged day, live from Firestore, with filters and a story view per day. */
/** Optional nav argument: open the story of this day (yyyy-MM-dd) as soon as the diary loads. */
const val DIARY_DAY_ARG = "day"

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: ProgressRepository,
    private val goalRepository: GoalRepository,
    private val streakCalculator: StreakCalculator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // A day tapped in "Mi semana" arrives here; its story opens once, and closing it goes back.
    private var pendingDay: LocalDate? = savedStateHandle.get<String>(DIARY_DAY_ARG)?.let(LocalDate::parse)

    /** True when the story was opened from outside: closing it should leave the diary too. */
    val storyClosesScreen: Boolean = pendingDay != null

    private val zone = ZoneId.systemDefault()
    val today: LocalDate get() = LocalDate.now(zone)

    var state by mutableStateOf<DiaryUiState>(DiaryUiState.Loading)
        private set
    var useFilter by mutableStateOf(UseFilter.ALL)
        private set
    var moodFilter by mutableStateOf<String?>(null)
        private set
    var calendarMonth by mutableStateOf(YearMonth.now(zone))
        private set

    /** Days the open story swipes through, and where it starts; `null` when closed. */
    var story by mutableStateOf<DiaryStory?>(null)
        private set

    /** Recomputed only when the entries or a filter change, not on every recomposition. */
    val visibleEntries: List<JournalEntry> by derivedStateOf {
        filterEntries((state as? DiaryUiState.Content)?.entries.orEmpty(), useFilter, moodFilter)
    }

    init {
        viewModelScope.launch {
            combine(repository.observeJournal(), goalRepository.observeGoals()) { all, goals -> all to goals }
                .catch { emit(emptyList<JournalEntry>() to emptyList()) }
                .collect { (all, goals) ->
                    val entries = diaryEntries(all)
                    val loaded = if (entries.isEmpty()) {
                        DiaryUiState.Empty
                    } else {
                        DiaryUiState.Content(
                            entries = entries,
                            entriesByDate = entries.associateBy { it.date },
                            summary = summarize(entries, streakCalculator.compute(all, today).best),
                            months = browsableMonths(entries, YearMonth.now(zone)),
                            goalNotes = GoalDays.dayNotes(goals, all, today)
                        )
                    }
                    state = loaded
                    pendingDay?.let { day ->
                        pendingDay = null
                        openStory(day)
                    }
                }
        }
    }

    fun selectUseFilter(filter: UseFilter) { useFilter = filter }

    /** Tapping the selected mood again clears it. */
    fun toggleMoodFilter(moodId: String) { moodFilter = if (moodFilter == moodId) null else moodId }

    fun clearFilters() {
        useFilter = UseFilter.ALL
        moodFilter = null
    }

    fun showMonth(month: YearMonth) {
        val months = (state as? DiaryUiState.Content)?.months ?: return
        if (month in months) calendarMonth = month
    }

    /**
     * Opens the story on [date]. It swipes through the filtered days when the day is among them;
     * otherwise (tapped on the calendar while a filter hides it) through all days.
     */
    fun openStory(date: LocalDate) {
        val all = (state as? DiaryUiState.Content)?.entries ?: return
        val days = visibleEntries.takeIf { list -> list.any { it.date == date } } ?: all
        val index = days.indexOfFirst { it.date == date }
        if (index >= 0) story = DiaryStory(days, index)
    }

    fun closeStory() { story = null }
}

data class DiaryStory(val days: List<JournalEntry>, val startIndex: Int)
