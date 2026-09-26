package com.solvyx.ui.screens.firstaid.crisischeck

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.components.common.WizardProgressDots
import com.solvyx.ui.screens.firstaid.components.BertoSpeechHero
import com.solvyx.ui.screens.firstaid.components.EmergencySignsCard
import com.solvyx.ui.screens.firstaid.components.GuideScaffold
import com.solvyx.ui.screens.firstaid.content.EmergencySigns
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.screens.firstaid.model.SosEmphasis
import com.solvyx.ui.theme.CrisisRed

private val QuestionCount = CrisisSignalGroups.size
private val ResultStepIndex = QuestionCount
private val AllSignals = CrisisSignalGroups.flatMap { it.signals }

/**
 * "¿Estoy en crisis?" as three short questions Berto asks one at a time, then a result screen
 * that recaps what the user marked and points to the next step. Red flags get help offered on the
 * spot, without waiting for the end.
 */
@Composable
fun CrisisCheckScreen(
    onBack: () -> Unit,
    onSos: () -> Unit,
    onOpenCrisisGuide: () -> Unit,
    onStartGrounding: () -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    // Labels (not objects) are saved so the answers survive rotation and process death.
    var selectedLabels by rememberSaveable { mutableStateOf(setOf<String>()) }
    val selected by remember {
        derivedStateOf { AllSignals.filter { it.label in selectedLabels }.toSet() }
    }
    val scrollState = rememberScrollState()
    val previousStep = { step -= 1 }

    LaunchedEffect(step) { scrollState.scrollTo(0) }
    BackHandler(enabled = step > 0, onBack = previousStep)

    GuideScaffold(
        title = "¿Estoy en crisis?",
        onBack = onBack,
        onSos = onSos,
        sosEmphasis = SosEmphasis.FILLED,
        scrollState = scrollState,
        header = {
            WizardProgressDots(
                currentStep = step,
                totalSteps = QuestionCount + 1,
                onBack = if (step > 0) previousStep else null
            )
        }
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally { if (forward) it else -it } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (forward) -it else it } + fadeOut())
            },
            label = "crisisCheckStep"
        ) { current ->
            if (current < ResultStepIndex) {
                QuestionStep(
                    index = current,
                    selected = selected,
                    onToggle = { signal ->
                        selectedLabels = if (signal.label in selectedLabels) {
                            selectedLabels - signal.label
                        } else {
                            selectedLabels + signal.label
                        }
                    },
                    onNext = { step += 1 }
                )
            } else {
                ResultStep(
                    selected = AllSignals.filter { it in selected },
                    onGrounding = onStartGrounding,
                    onCrisisGuide = onOpenCrisisGuide,
                    onRestart = {
                        selectedLabels = emptySet()
                        step = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun QuestionStep(
    index: Int,
    selected: Set<CrisisSignal>,
    onToggle: (CrisisSignal) -> Unit,
    onNext: () -> Unit
) {
    val group = CrisisSignalGroups[index]
    val markedHere = group.signals.any { it in selected }
    val nextLabel = when {
        index == QuestionCount - 1 -> "Ver qué puedo hacer"
        markedHere -> "Siguiente"
        else -> "Ninguna de estas, siguiente"
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BertoSpeechHero(hero = questionHero(group, index))
        SignalQuestionCard(group = group, selected = selected, onToggle = onToggle)
        RedFlagNotice(visible = group.signals.any { it.isRedFlag && it in selected })
        SolvyxButton(text = nextLabel, onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ResultStep(
    selected: List<CrisisSignal>,
    onGrounding: () -> Unit,
    onCrisisGuide: () -> Unit,
    onRestart: () -> Unit
) {
    val copy = assessCrisisCheck(selected).toResultCopy()
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BertoSpeechHero(hero = copy.hero)
        SelectedSignalsCard(selected = selected, tone = copy.tone)
        SolvyxButton(
            text = copy.actionLabel,
            onClick = when (copy.action) {
                ResultAction.GROUNDING -> onGrounding
                ResultAction.CRISIS_GUIDE -> onCrisisGuide
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (copy.tone == GuideTone.URGENT) CrisisRed else MaterialTheme.colorScheme.primary
        )
        SolvyxTextButton(text = "Volver a empezar", onClick = onRestart)
        EmergencySignsCard(signs = EmergencySigns)
    }
}
