package com.solvyx.ui.screens.journey.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.screens.journey.JourneyViewModel
import com.solvyx.ui.screens.journey.ConsumptionChart
import com.solvyx.ui.screens.journey.FeelingsChart
import com.solvyx.ui.screens.journey.ProgressUiState
import com.solvyx.ui.screens.journey.components.CheckInCard
import com.solvyx.ui.screens.journey.components.ProgressEmptyState
import com.solvyx.ui.screens.journey.components.StreakJourneyCard
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium

@Composable
fun ProgressTab(
    viewModel: JourneyViewModel,
    progressState: ProgressUiState,
    todayEntry: JournalEntry?,
    onRegister: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streak = (progressState as? ProgressUiState.Content)?.streak ?: 0
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = SolvyxBottomNavHeight)
    ) {
        // ── Day check-in (replaces the old "Hoy" tab) ───────────
        Spacer(Modifier.height(16.dp))
        CheckInCard(
            todayEntry = todayEntry,
            streak = streak,
            onRegister = onRegister,
            onEdit = onEdit
        )
        Spacer(Modifier.height(20.dp))

        when (progressState) {
            ProgressUiState.Loading -> ProgressLoading()
            is ProgressUiState.Content -> ProgressContent(progressState, viewModel)
        }

    }
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

@Composable
private fun ProgressContent(content: ProgressUiState.Content, viewModel: JourneyViewModel) {
    // ── Streak hero (viaje completo de hitos) ──────────────────────
    StreakJourneyCard(
        streak = content.streak,
        bestStreak = content.bestStreak,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    if (!content.hasHistory) {
        ProgressEmptyState(modifier = Modifier.fillMaxWidth())
        return
    }

    // ── Week / Month selector ─────────────────────────────────────
    SolvyxSegmentedControl(
        options = listOf("Semana", "Mes"),
        selectedIndex = viewModel.selectedPeriod,
        onSelect = { viewModel.selectPeriod(it) }
    )

    Spacer(Modifier.height(20.dp))

    val isWeek = viewModel.selectedPeriod == 0
    val labels = if (isWeek) viewModel.labelsWeek else viewModel.labelsMonth

    // ── Wellbeing chart ──────────────────────────────────────
    SectionHeader(icon = R.drawable.ic_heart_pulse, text = "Mi bienestar")
    Spacer(Modifier.height(8.dp))
    FeelingsChart(
        data = if (isWeek) content.feelingsWeek else content.feelingsMonth,
        labels = labels,
        modifier = Modifier.fillMaxWidth(),
        onPointSelected = { viewModel.onChartPointSelected(it) }
    )

    Spacer(Modifier.height(20.dp))

    // ── Use chart ────────────────────────────────────────
    SectionHeader(icon = R.drawable.ic_droplet, text = "Días de consumo")
    Spacer(Modifier.height(8.dp))
    ConsumptionChart(
        data = if (isWeek) content.useWeek else content.useMonth,
        labels = labels,
        modifier = Modifier.fillMaxWidth(),
        onPointSelected = { viewModel.onChartPointSelected(it) }
    )

    Spacer(Modifier.height(20.dp))

    // ── "Berto dice" ──────────────────────────────────────────────
    BorderCard(
        leftBorderColor = TealMedium,
        bg = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_TO_RIGHT,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.size(44.dp),
                fallback = R.drawable.berto_dedo_der
            )
            Column {
                Text(
                    text = "Berto dice",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = content.insight,
                    style = MaterialTheme.typography.bodySmall,
                    color = TealDark
                )
            }
        }
    }
}

/** Encabezado de sección con un ícono decorativo pequeño junto al texto. */
@Composable
private fun SectionHeader(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TealDark,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
    }
}
