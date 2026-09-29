package com.solvyx.ui.screens.journey.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.backend.common.goals.DayGoalNote
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.journey.diary.components.DayStory
import com.solvyx.ui.screens.journey.diary.components.DiaryEmptyState
import com.solvyx.ui.screens.journey.diary.components.DiaryHero
import com.solvyx.ui.screens.journey.diary.components.DiaryFilters
import com.solvyx.ui.screens.journey.diary.components.MoodCalendar
import com.solvyx.ui.screens.journey.diary.components.TimelineEntry
import com.solvyx.ui.theme.TealDark
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val SpanishMexico = Locale("es", "MX")

/**
 * "Mi diario": every logged day. Hero with Berto and counters, a mood calendar, filters and a
 * timeline grouped by month; any day opens as a full-screen story ([DayStory]).
 */
@Composable
fun DiaryScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onEditToday: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(Modifier.fillMaxSize()) {
                SolvyxTopBar(title = "Mi diario", navigationButton = { SolvyxBackButton(onClick = onBack) })
                when (val state = viewModel.state) {
                    DiaryUiState.Loading -> Unit
                    DiaryUiState.Empty -> DiaryEmptyState(
                        title = "Tu diario está esperando",
                        message = "Cada día que registres aparecerá aquí para que lo recuerdes con Berto.",
                        actionLabel = "Registrar mi día",
                        onAction = onRegister
                    )
                    is DiaryUiState.Content -> DiaryContent(state, viewModel)
                }
            }
        }

        val story = viewModel.story
        // Remembers the last story so the exit animation still has something to show.
        var shownStory by remember { mutableStateOf<DiaryStory?>(null) }
        if (story != null) shownStory = story
        AnimatedVisibility(
            visible = story != null,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.94f),
            exit = fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 0.94f)
        ) {
            shownStory?.let {
                DayStory(
                    story = it,
                    today = viewModel.today,
                    goalNotes = (viewModel.state as? DiaryUiState.Content)?.goalNotes.orEmpty(),
                    onEditToday = {
                        viewModel.closeStory()
                        onEditToday()
                    },
                    onClose = {
                        // Ignores a second tap on "X" while the story is already closing.
                        if (viewModel.story != null) {
                            viewModel.closeStory()
                            if (viewModel.storyClosesScreen) onBack()
                        }
                    },
                    isOpen = story != null
                )
            }
        }
    }
}

@Composable
private fun DiaryContent(state: DiaryUiState.Content, viewModel: DiaryViewModel) {
    val visible = viewModel.visibleEntries
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + navigationBar),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "hero") { DiaryHero(summary = state.summary) }
        item(key = "calendar") {
            MoodCalendar(
                month = viewModel.calendarMonth,
                months = state.months,
                entriesByDate = state.entriesByDate,
                today = viewModel.today,
                onMonthChange = viewModel::showMonth,
                onDaySelected = viewModel::openStory,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item(key = "filters_title") { SectionTitle("Mi línea del tiempo", Modifier.padding(top = 24.dp, bottom = 10.dp)) }
        item(key = "filters") {
            DiaryFilters(
                useFilter = viewModel.useFilter,
                moodFilter = viewModel.moodFilter,
                onUseFilter = viewModel::selectUseFilter,
                onMood = viewModel::toggleMoodFilter,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (visible.isEmpty()) {
            item(key = "no_matches") {
                DiaryEmptyState(
                    title = "Ningún día coincide",
                    message = "Prueba con otro filtro para ver más días.",
                    actionLabel = "Quitar filtros",
                    onAction = viewModel::clearFilters,
                    modifier = Modifier.animateItem()
                )
            }
        } else {
            timeline(visible, state.goalNotes, onOpen = viewModel::openStory)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.timeline(
    entries: List<JournalEntry>,
    goalNotes: Map<LocalDate, DayGoalNote>,
    onOpen: (LocalDate) -> Unit
) {
    groupByMonth(entries).forEach { group ->
        stickyHeader(key = "month_${group.month}") {
            val name = group.month.month.getDisplayName(TextStyle.FULL, SpanishMexico).replaceFirstChar { it.uppercase() }
            Text(
                "$name ${group.month.year}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 8.dp)
                    .animateItem()
            )
        }
        itemsIndexed(group.entries, key = { _, entry -> entry.date.toString() }) { index, entry ->
            TimelineEntry(
                entry = entry,
                goalNote = goalNotes[entry.date],
                isFirst = index == 0,
                isLast = index == group.entries.lastIndex,
                onClick = { onOpen(entry.date) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = TealDark,
        modifier = modifier
    )
}
