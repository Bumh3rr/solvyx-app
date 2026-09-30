package com.solvyx.ui.screens.plan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.GoalType
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.WizardProgressDots
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark

private const val TOTAL_STEPS = 3
private const val DisabledAlpha = 0.45f

/**
 * "Crear mi propia meta": type → substance → number, all with buttons. Nothing is saved until
 * "Crear meta"; closing the sheet halfway just discards the draft.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalSheet(
    draft: CreateGoalDraft,
    substanceChoices: (GoalType) -> List<SubstanceChoice>,
    showWithdrawalWarning: Boolean,
    onSelectType: (GoalType) -> Unit,
    onSelectSubstance: (String?) -> Unit,
    onDaysChange: (Int) -> Unit,
    onWeeklyLimitChange: (Int) -> Unit,
    onWeeksChange: (Int) -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        WizardProgressDots(
            currentStep = draft.step.ordinal,
            totalSteps = TOTAL_STEPS,
            onBack = if (draft.step == CreateGoalStep.TYPE) null else onBack
        )
        AnimatedContent(
            targetState = draft.step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "createGoalStep"
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (step) {
                    CreateGoalStep.TYPE -> TypeStep(selected = draft.type, onSelect = onSelectType)
                    CreateGoalStep.SUBSTANCE -> draft.type?.let { type ->
                        SubstanceStep(
                            choices = substanceChoices(type),
                            selected = draft.substance,
                            anySelected = draft.anySubstance,
                            onSelect = onSelectSubstance
                        )
                    }
                    CreateGoalStep.AMOUNT -> AmountStep(
                        draft = draft,
                        showWithdrawalWarning = showWithdrawalWarning,
                        onDaysChange = onDaysChange,
                        onWeeklyLimitChange = onWeeklyLimitChange,
                        onWeeksChange = onWeeksChange,
                        onCreate = onCreate
                    )
                }
            }
        }
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = TealDark,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun TypeStep(selected: GoalType?, onSelect: (GoalType) -> Unit) {
    StepTitle("¿Qué quieres lograr?")
    OptionCard(
        icon = goalIcon(GoalType.SIN_CONSUMO),
        title = "Días sin consumir",
        description = "Suma cada día que registras sin consumir. No tienen que ser seguidos.",
        selected = selected == GoalType.SIN_CONSUMO,
        onClick = { onSelect(GoalType.SIN_CONSUMO) }
    )
    OptionCard(
        icon = goalIcon(GoalType.REDUCIR_FRECUENCIA),
        title = "Consumir menos días",
        description = "Ponte un máximo de días por semana.",
        selected = selected == GoalType.REDUCIR_FRECUENCIA,
        onClick = { onSelect(GoalType.REDUCIR_FRECUENCIA) }
    )
}

@Composable
private fun SubstanceStep(
    choices: List<SubstanceChoice>,
    selected: String?,
    anySelected: Boolean,
    onSelect: (String?) -> Unit
) {
    StepTitle("¿Con qué sustancia?")
    choices.forEach { choice ->
        val isSelected = if (choice.id == null) anySelected else choice.id == selected
        OptionCard(
            icon = choice.icon,
            title = choice.label,
            description = if (choice.enabled) null else "Ya tienes una meta así",
            selected = isSelected && choice.enabled,
            enabled = choice.enabled,
            onClick = { onSelect(choice.id) }
        )
    }
}

@Composable
private fun AmountStep(
    draft: CreateGoalDraft,
    showWithdrawalWarning: Boolean,
    onDaysChange: (Int) -> Unit,
    onWeeklyLimitChange: (Int) -> Unit,
    onWeeksChange: (Int) -> Unit,
    onCreate: () -> Unit
) {
    StepTitle("¿Cuántos días?")
    when (draft.type) {
        GoalType.SIN_CONSUMO -> NumberStepper(
            label = "Días",
            value = draft.days,
            range = CreateGoalDraft.DAYS_RANGE,
            onChange = onDaysChange
        )
        GoalType.REDUCIR_FRECUENCIA -> {
            NumberStepper(
                label = "Máximo de días por semana",
                value = draft.weeklyLimit,
                range = CreateGoalDraft.WEEKLY_LIMIT_RANGE,
                onChange = onWeeklyLimitChange
            )
            NumberStepper(
                label = "Durante cuántas semanas",
                value = draft.weeks,
                range = CreateGoalDraft.WEEKS_RANGE,
                onChange = onWeeksChange
            )
        }
        null -> Unit
    }

    confirmationLine(draft)?.let { line ->
        Spacer(Modifier.height(4.dp))
        BertoSpeechRow(message = line.message, supporting = line.supporting, bertoSize = 72.dp) {
            Image(
                painter = painterResource(R.drawable.berto_feliz),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    if (showWithdrawalWarning) WithdrawalWarningCard()

    SolvyxButton(
        text = "Crear meta",
        onClick = onCreate,
        enabled = draft.title != null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

@Composable
private fun OptionCard(
    icon: Int,
    title: String,
    description: String?,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    SolvyxCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DisabledAlpha)
            .then(
                if (selected) Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(20.dp))
                else Modifier
            ),
        onClick = if (enabled) onClick else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCapsule(icon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (enabled) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** "−  3  +" inside the value's range; the buttons turn off at the ends. */
@Composable
private fun NumberStepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            StepButton(text = "−", description = "Menos", enabled = value > range.first) { onChange(value - 1) }
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 48.dp)
            )
            StepButton(text = "+", description = "Más", enabled = value < range.last) { onChange(value + 1) }
        }
    }
}

@Composable
private fun StepButton(text: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(
            1.5.dp,
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        // The symbol alone reads poorly with TalkBack; the description names the action.
        modifier = Modifier
            .size(40.dp)
            .semantics { contentDescription = description }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun WithdrawalWarningCard() {
    SolvyxCard(modifier = Modifier.fillMaxWidth(), containerColor = WarnAmber) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_triangle),
                contentDescription = null,
                tint = WarnAmberDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                WithdrawalWarning,
                style = MaterialTheme.typography.bodyMedium,
                color = WarnAmberDark
            )
        }
    }
}
