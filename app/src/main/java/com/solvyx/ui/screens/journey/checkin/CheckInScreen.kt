package com.solvyx.ui.screens.journey.checkin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.screens.journey.WizardStep
import com.solvyx.ui.screens.journey.checkin.components.BertoStage
import com.solvyx.ui.screens.journey.checkin.components.CheckInResult
import com.solvyx.ui.screens.journey.checkin.components.CheckInTopBar
import com.solvyx.ui.screens.journey.checkin.steps.MoodStep
import com.solvyx.ui.screens.journey.checkin.steps.NoteStep
import com.solvyx.ui.screens.journey.checkin.steps.SubstanceStep
import com.solvyx.ui.screens.journey.checkin.steps.UseStep
import com.solvyx.ui.screens.plan.GoalCompletedCelebration
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark

private val BertoSizeResting = 150.dp
private val BertoSizeWithKeyboard = 88.dp
private const val MoodTintAlpha = 0.22f
private const val StepSlideMillis = 320

/**
 * "Registrar mi día" as its own full-screen experience (no bottom nav): Berto reacts to every
 * answer, the background takes the color of the chosen mood, and the steps slide in the direction
 * of travel. Saving swaps the whole screen for a closing moment.
 */
@Composable
fun CheckInScreen(
    onClose: () -> Unit,
    onOpenFirstAid: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    var caretX by remember { mutableStateOf<Float?>(null) }
    val background = MaterialTheme.colorScheme.background
    val moodColor = viewModel.mood?.let { moodOption(it).color } ?: MaterialTheme.colorScheme.primary
    val topTint by animateColorAsState(
        moodColor.copy(alpha = MoodTintAlpha).compositeOver(background),
        tween(600),
        label = "moodTint"
    )
    val saved = viewModel.saveState as? SaveState.Saved

    BackHandler(enabled = saved == null && viewModel.step > 0, onBack = viewModel::back)

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(topTint, background)))
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = saved,
                transitionSpec = { (fadeIn(tween(400)) + scaleIn(initialScale = 0.96f)) togetherWith fadeOut(tween(200)) },
                label = "checkInResult"
            ) { result ->
                if (result != null) {
                    CheckInResult(
                        used = viewModel.used == true,
                        streak = result.streak,
                        pendingSync = result.pendingSync,
                        advancedTitle = viewModel.advanced?.message,
                        advancedDetail = viewModel.advanced?.supporting,
                        onDone = onClose,
                        onOpenFirstAid = onOpenFirstAid
                    )
                } else {
                    CheckInFlow(
                        viewModel = viewModel,
                        accent = moodColor,
                        caretX = caretX,
                        onCaretMoved = { caretX = it },
                        onClose = onClose
                    )
                }
            }
        }
        // A goal completed by this check-in is celebrated over the result, one at a time.
        if (saved != null) {
            viewModel.celebrations.firstOrNull()?.let { goal ->
                GoalCompletedCelebration(goal = goal, onDone = viewModel::celebrationDone)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckInFlow(
    viewModel: CheckInViewModel,
    accent: Color,
    caretX: Float?,
    onCaretMoved: (Float?) -> Unit,
    onClose: () -> Unit
) {
    val step = viewModel.currentStep
    val reaction = reactionFor(step, viewModel.mood, viewModel.used, viewModel.substance, isTyping = caretX != null)
    val bertoSize = if (WindowInsets.isImeVisible) BertoSizeWithKeyboard else BertoSizeResting

    Column(Modifier.fillMaxSize().imePadding()) {
        CheckInTopBar(
            step = viewModel.step,
            totalSteps = viewModel.totalSteps,
            accent = accent,
            onClose = onClose
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            BertoStage(reaction = reaction, lookX = caretX, bertoSize = bertoSize)
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(tween(StepSlideMillis)) { direction * it } + fadeIn(tween(StepSlideMillis))) togetherWith
                        (slideOutHorizontally(tween(StepSlideMillis)) { -direction * it } + fadeOut(tween(200)))
                },
                label = "checkInStep"
            ) { current ->
                Column {
                    Text(
                        text = stepTitle(current),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Spacer(Modifier.height(16.dp))
                    StepContent(step = current, viewModel = viewModel, onCaretMoved = onCaretMoved)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        SaveErrorBanner(visible = viewModel.saveState == SaveState.Failed)
        CheckInActions(viewModel = viewModel)
    }
}

@Composable
private fun StepContent(step: WizardStep, viewModel: CheckInViewModel, onCaretMoved: (Float?) -> Unit) {
    when (step) {
        WizardStep.MOOD -> MoodStep(selected = viewModel.mood, onSelect = viewModel::selectMood)
        WizardStep.NOTE -> NoteStep(
            mood = viewModel.mood,
            note = viewModel.note,
            onNoteChange = viewModel::updateNote,
            onIdeaTap = viewModel::appendToNote,
            onCaretMoved = onCaretMoved
        )
        WizardStep.USE -> UseStep(used = viewModel.used, onSelect = viewModel::updateUsed)
        WizardStep.SUBSTANCE -> SubstanceStep(
            substance = viewModel.substance,
            amount = viewModel.amount,
            context = viewModel.context,
            onSubstanceSelect = viewModel::updateSubstance,
            onAmountChange = viewModel::updateAmount,
            onContextChange = viewModel::updateContext,
            onCaretMoved = onCaretMoved
        )
    }
}

@Composable
private fun CheckInActions(viewModel: CheckInViewModel) {
    val isSaving = viewModel.saveState == SaveState.Saving
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (viewModel.step > 0) {
            SolvyxOutlinedButton(
                text = "Atrás",
                onClick = viewModel::back,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            )
        }
        SolvyxButton(
            text = when {
                isSaving -> "Guardando…"
                viewModel.isLastStep -> "Guardar"
                else -> "Siguiente"
            },
            onClick = if (viewModel.isLastStep) viewModel::save else viewModel::next,
            enabled = viewModel.canAdvance && !isSaving,
            modifier = Modifier.weight(1f),
            leadingIcon = if (isSaving) {
                { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) }
            } else null
        )
    }
}

@Composable
private fun SaveErrorBanner(visible: Boolean) {
    AnimatedVisibility(visible = visible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Text(
            text = "No se pudo guardar. Revisa tu sesión e intenta de nuevo.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(CrisisRedLight, RoundedCornerShape(12.dp))
                .padding(12.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = CrisisRed
        )
    }
}

private fun stepTitle(step: WizardStep): String = when (step) {
    WizardStep.MOOD -> "¿Cómo te sientes hoy?"
    WizardStep.NOTE -> "¿Quieres agregar una nota?"
    WizardStep.USE -> "¿Consumiste alguna sustancia hoy?"
    WizardStep.SUBSTANCE -> "Cuéntame un poco más"
}
