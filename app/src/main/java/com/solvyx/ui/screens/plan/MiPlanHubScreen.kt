package com.solvyx.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxMenuButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.components.navigation.SolvyxBottomNavClearance
import com.solvyx.ui.theme.TealLight
import dev.chrisbanes.haze.haze

/**
 * "Mi plan": Berto's contextual message, the active goals, Berto's suggestions, the quick tools,
 * the entry to substance info and the completed goals. Anonymous users see an account invite in
 * place of the goals; the tools and substance info work for everyone.
 */
@Composable
fun MiPlanHubScreen(
    onOpenDrawer: () -> Unit,
    onOpenCravingGuide: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenGrounding: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenSubstanceInfo: () -> Unit,
    onOpenDirectory: () -> Unit,
    onCreateAccount: () -> Unit,
    onOpenCheckIn: () -> Unit,
    viewModel: PlanViewModel = hiltViewModel()
) {
    val goalsState = viewModel.goalsState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = "Mi plan",
            navigationButton = { SolvyxMenuButton(onClick = onOpenDrawer) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                // El contenido es la fuente del blur del bottom nav (ver LocalHazeState).
                .haze(
                    LocalHazeState.current,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    tint = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    blurRadius = 16.dp
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                // El bottom nav flota encima del contenido: sin este espacio tapa las últimas tarjetas.
                .padding(top = 16.dp, bottom = SolvyxBottomNavClearance),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                BertoSpeechRow(
                    message = viewModel.bertoLine.message,
                    supporting = viewModel.bertoLine.supporting
                ) {
                    BertoPoseAnimation(
                        pose = BertoPose.CENTER_IDLE_HELLO,
                        riveFileRes = R.raw.berto_poses,
                        modifier = Modifier.fillMaxSize(),
                        fallback = R.drawable.berto_feliz
                    )
                }
                if (viewModel.bertoLine.opensCheckIn) {
                    TextButton(onClick = onOpenCheckIn, modifier = Modifier.align(Alignment.End)) {
                        Text(
                            "Registrar mi día →",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            when (goalsState) {
                PlanGoalsState.Loading -> Unit
                PlanGoalsState.AccountRequired -> GoalsAccountInvite(onCreateAccount = onCreateAccount)
                is PlanGoalsState.Content -> {
                    ActiveGoalsSection(goals = goalsState.active, onOpenGoal = viewModel::openGoal)
                    SuggestionsSection(
                        suggestions = goalsState.suggestions,
                        suggestProfessional = goalsState.suggestProfessional,
                        onAccept = viewModel::acceptSuggestion,
                        onOpenDirectory = onOpenDirectory
                    )
                    CreateGoalButton(enabled = viewModel.canCreateGoal, onClick = viewModel::openCreateGoal)
                }
            }

            PlanQuickToolsGrid(
                onOpenCravingGuide = onOpenCravingGuide,
                onOpenBreathing = onOpenBreathing,
                onOpenGrounding = onOpenGrounding,
                onOpenChat = onOpenChat
            )

            SubstanceInfoEntry(onClick = onOpenSubstanceInfo)

            (goalsState as? PlanGoalsState.Content)?.let { CompletedGoalsSection(goals = it.completed) }
        }
    }

    // A dialog so the celebration also covers the bottom nav, which floats above this screen.
    viewModel.celebrations.firstOrNull()?.let { goal ->
        Dialog(
            onDismissRequest = viewModel::celebrationDone,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            GoalCompletedCelebration(goal = goal, onDone = viewModel::celebrationDone)
        }
    }

    viewModel.selectedGoal?.let { card ->
        GoalDetailSheet(
            card = card,
            journal = viewModel.journal,
            onArchive = { viewModel.archiveGoal(card.goal.id) },
            onDismiss = viewModel::closeGoal
        )
    }

    viewModel.createDraft?.let { draft ->
        CreateGoalSheet(
            draft = draft,
            substanceChoices = viewModel::substanceChoicesFor,
            showWithdrawalWarning = viewModel.needsWithdrawalWarning(draft),
            onSelectType = viewModel::selectGoalType,
            onSelectSubstance = viewModel::selectGoalSubstance,
            onDaysChange = viewModel::setGoalDays,
            onWeeklyLimitChange = viewModel::setGoalWeeklyLimit,
            onWeeksChange = viewModel::setGoalWeeks,
            onBack = viewModel::createGoalBack,
            onCreate = viewModel::confirmCreateGoal,
            onDismiss = viewModel::closeCreateGoal
        )
    }
}

@Composable
private fun SubstanceInfoEntry(onClick: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(TealLight.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_guide),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Conoce tu sustancia",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Efectos y cómo cuidarte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
