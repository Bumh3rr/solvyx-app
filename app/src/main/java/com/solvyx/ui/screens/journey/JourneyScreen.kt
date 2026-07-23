package com.solvyx.ui.screens.journey

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.haze.LocalHazeState
import dev.chrisbanes.haze.haze
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.common.SolvyxSegmentedDefaults
import com.solvyx.ui.screens.journey.components.AccountRequiredState
import com.solvyx.ui.screens.journey.components.DayDetailSheet
import com.solvyx.ui.screens.journey.tabs.AchievementsTab
import com.solvyx.ui.screens.journey.tabs.ProgressTab
import com.solvyx.ui.screens.journey.tabs.CheckInWizard

@Composable
fun JourneyScreen(
    onOpenDrawer: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val journeyVM: JourneyViewModel = hiltViewModel()
    val checkInVM: CheckInViewModel = hiltViewModel()

    var showingWizard by rememberSaveable { mutableStateOf(false) }
    fun closeWizard() { checkInVM.reset(); showingWizard = false }

    BackHandler(enabled = showingWizard) { closeWizard() }

    AnimatedContent(
        targetState = showingWizard,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(320)) { it } + fadeIn(tween(320))) togetherWith
                    fadeOut(tween(200))
            } else {
                fadeIn(tween(320)) togetherWith
                    (slideOutHorizontally(tween(320)) { it } + fadeOut(tween(200)))
            }
        },
        label = "journey_wizard"
    ) { wizard ->
        if (wizard) {
            WizardScreen(viewModel = checkInVM, onClose = { closeWizard() }, onFinish = { showingWizard = false })
        } else {
            TabsScreen(
                journeyVM = journeyVM,
                checkInVM = checkInVM,
                onOpenDrawer = onOpenDrawer,
                onCreateAccount = onCreateAccount,
                onRegister = { checkInVM.reset(); showingWizard = true },
                onEdit = { checkInVM.loadToday(); showingWizard = true }
            )
        }
    }

    journeyVM.selectedDay?.let { entry ->
        DayDetailSheet(entry = entry, onDismiss = { journeyVM.dismissDayDetail() })
    }
}

@Composable
private fun WizardScreen(
    viewModel: CheckInViewModel,
    onClose: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SolvyxBackButton(onClick = onClose)
            Text(
                text = "Registrar mi día",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(44.dp))
        }
        CheckInWizard(
            viewModel = viewModel,
            onFinish = onFinish,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabsScreen(
    journeyVM: JourneyViewModel,
    checkInVM: CheckInViewModel,
    onOpenDrawer: () -> Unit,
    onCreateAccount: () -> Unit,
    onRegister: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Mi camino",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        if (journeyVM.isAnonymous) {
            AccountRequiredState(onCreateAccount = onCreateAccount)
            return@Column
        }

        val todayEntry = checkInVM.entries.collectAsState().value
            .firstOrNull { it.date == checkInVM.today }

        var selectedTab by rememberSaveable { mutableIntStateOf(TAB_PROGRESS) }

        SolvyxSegmentedControl(
            options = listOf("Progreso", "Logros"),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            colors = SolvyxSegmentedDefaults.onPrimary(),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp)
        )

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            modifier = Modifier
                .weight(1f)
                .haze(
                    LocalHazeState.current,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    tint = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    blurRadius = 16.dp
                ),
            label = "journey_tab"
        ) { tab ->
            when (tab) {
                TAB_PROGRESS -> ProgressTab(
                    viewModel = journeyVM,
                    progressState = journeyVM.progressState,
                    todayEntry = todayEntry,
                    onRegister = onRegister,
                    onEdit = onEdit
                )
                TAB_ACHIEVEMENTS -> AchievementsTab(
                    state = journeyVM.achievementsState,
                    justUnlockedIds = journeyVM.justUnlockedIds,
                    onConsumeJustUnlocked = { journeyVM.consumeJustUnlocked(it) }
                )
            }
        }
    }
}
