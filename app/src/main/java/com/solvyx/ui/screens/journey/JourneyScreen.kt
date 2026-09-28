package com.solvyx.ui.screens.journey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.components.common.SolvyxMenuButton
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.common.SolvyxSegmentedDefaults
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.screens.journey.components.AccountRequiredState
import com.solvyx.ui.screens.journey.components.DayDetailSheet
import com.solvyx.ui.screens.journey.tabs.AchievementsTab
import com.solvyx.ui.screens.journey.tabs.ProgressTab
import dev.chrisbanes.haze.haze

/**
 * "Mi camino": Progreso + Logros. Logging the day opens the check-in as its own full-screen
 * route ([onOpenCheckIn], `true` to edit today's entry) instead of living inside this tab.
 */
@Composable
fun JourneyScreen(
    onOpenDrawer: () -> Unit,
    onCreateAccount: () -> Unit,
    onOpenCheckIn: (edit: Boolean) -> Unit,
    onOpenDiary: () -> Unit
) {
    val journeyVM: JourneyViewModel = hiltViewModel()

    TabsScreen(
        journeyVM = journeyVM,
        onOpenDrawer = onOpenDrawer,
        onCreateAccount = onCreateAccount,
        onRegister = { onOpenCheckIn(false) },
        onEdit = { onOpenCheckIn(true) },
        onOpenDiary = onOpenDiary
    )

    journeyVM.selectedDay?.let { entry ->
        DayDetailSheet(entry = entry, onDismiss = { journeyVM.dismissDayDetail() })
    }
}

@Composable
private fun TabsScreen(
    journeyVM: JourneyViewModel,
    onOpenDrawer: () -> Unit,
    onCreateAccount: () -> Unit,
    onRegister: () -> Unit,
    onEdit: () -> Unit,
    onOpenDiary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = "Mi camino",
            navigationButton = { SolvyxMenuButton(onClick = onOpenDrawer) }
        )

        if (journeyVM.isAnonymous) {
            AccountRequiredState(onCreateAccount = onCreateAccount)
            return@Column
        }

        val todayEntry = journeyVM.todayEntry

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
                    onEdit = onEdit,
                    onOpenDiary = onOpenDiary
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
