package com.solvyx.ui.screens.journey.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.navigation.SolvyxBottomNavClearance
import com.solvyx.ui.screens.journey.JourneyViewModel
import com.solvyx.ui.screens.journey.ProgressUiState
import com.solvyx.ui.screens.journey.components.CheckInCard
import com.solvyx.ui.screens.journey.components.DiaryEntryCard
import com.solvyx.ui.screens.journey.components.ProgressEmptyState
import com.solvyx.ui.screens.journey.progress.components.BertoInsights
import com.solvyx.ui.screens.journey.progress.components.WeekCard
import java.time.LocalDate

/**
 * Progreso reads top to bottom as a weekly story: today (with the streak shortcut), "Mi semana",
 * what Berto notices, and the door to the full diary. The streak trail lives in Logros and the
 * month view in the diary's calendar, so nothing here repeats them.
 */
@Composable
fun ProgressTab(
    viewModel: JourneyViewModel,
    progressState: ProgressUiState,
    todayEntry: JournalEntry?,
    onRegister: () -> Unit,
    onEdit: () -> Unit,
    onOpenDiary: (day: LocalDate?) -> Unit,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streak = (progressState as? ProgressUiState.Content)?.streak ?: 0
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = SolvyxBottomNavClearance)
    ) {
        CheckInCard(
            todayEntry = todayEntry,
            streak = streak,
            onRegister = onRegister,
            onEdit = onEdit,
            onOpenStreak = onOpenAchievements
        )
        Spacer(Modifier.height(16.dp))
        when (progressState) {
            ProgressUiState.Loading -> ProgressLoading()
            is ProgressUiState.Content -> ProgressContent(progressState, viewModel, onOpenDiary)
        }
    }
}

@Composable
private fun ProgressContent(
    content: ProgressUiState.Content,
    viewModel: JourneyViewModel,
    onOpenDiary: (day: LocalDate?) -> Unit
) {
    if (!content.hasHistory) {
        ProgressEmptyState(modifier = Modifier.fillMaxWidth())
        return
    }
    viewModel.week?.let { week ->
        WeekCard(
            week = week,
            onPrevious = viewModel::showPreviousWeek,
            onNext = viewModel::showNextWeek,
            // The story is full screen above the bottom bar, so it lives in "Mi diario".
            onDaySelected = { onOpenDiary(it) }
        )
        Spacer(Modifier.height(24.dp))
        BertoInsights(insights = week.insights)
    }
    Spacer(Modifier.height(24.dp))
    DiaryEntryCard(
        recentMoods = content.recentMoods,
        registeredDays = content.registeredDays,
        onClick = { onOpenDiary(null) }
    )
}

@Composable
private fun ProgressLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.berto_dedo_der),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Cargando tu progreso…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
