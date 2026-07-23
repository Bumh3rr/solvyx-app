package com.solvyx.ui.screens.journey.tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.screens.journey.WizardStep
import com.solvyx.ui.screens.journey.CheckInViewModel
import com.solvyx.ui.screens.journey.components.CheckInSuccessDialog
import com.solvyx.ui.screens.journey.components.SubstanceBottomSheet

@Composable
fun CheckInWizard(
    viewModel: CheckInViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (viewModel.isSaved) {
        CheckInSuccessDialog(
            mood = viewModel.mood,
            used = viewModel.used == true,
            substance = viewModel.substance,
            onDismiss = { viewModel.reset(); onFinish() }
        )
    }
    if (viewModel.showSubstanceSheet) {
        SubstanceBottomSheet(
            selectedSubstance = viewModel.substance,
            onSubstanceSelected = { viewModel.updateSubstance(it); viewModel.toggleSubstanceSheet() },
            onDismiss = { viewModel.toggleSubstanceSheet() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Progress (dots) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val total = viewModel.totalSteps()
            repeat(total) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i <= viewModel.wizardStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                )
            }
        }

        // ── Berto + step content (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.berto_saludando),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            // Steps slide in the direction of travel (forward = from the right, back = from the left).
            AnimatedContent(
                targetState = viewModel.wizardStep,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(280)) { dir * it } + fadeIn(tween(280))) togetherWith
                        (slideOutHorizontally(tween(280)) { -dir * it } + fadeOut(tween(200)))
                },
                label = "wizard_step"
            ) { stepIndex ->
                val currentStep = WizardStep.entries[stepIndex]
                Column {
                    Text(
                        text = stepTitle(currentStep),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    when (currentStep) {
                        WizardStep.MOOD -> MoodStep(viewModel)
                        WizardStep.NOTE -> NoteStep(viewModel)
                        WizardStep.USE -> UseStep(viewModel)
                        WizardStep.SUBSTANCE -> SubstanceStep(viewModel)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Back / Next|Save bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = SolvyxBottomNavHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (viewModel.wizardStep > 0) {
                SolvyxButton(
                    text = "Atrás",
                    onClick = { viewModel.prevStep() },
                    modifier = Modifier.weight(1f)
                )
            }
            SolvyxButton(
                text = if (viewModel.isLastStep()) "Guardar" else "Siguiente",
                onClick = {
                    if (viewModel.isLastStep()) viewModel.save() else viewModel.nextStep()
                },
                enabled = viewModel.canAdvance(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun stepTitle(step: WizardStep): String = when (step) {
    WizardStep.MOOD -> "¿Cómo te sientes hoy?"
    WizardStep.NOTE -> "¿Quieres agregar una nota?"
    WizardStep.USE -> "¿Consumiste alguna sustancia hoy?"
    WizardStep.SUBSTANCE -> "Cuéntame un poco más"
}

@Composable
private fun MoodStep(viewModel: CheckInViewModel) {
    val moods = listOf(
        "triste" to R.drawable.ic_face_sad, "ansioso" to R.drawable.ic_face_anxious,
        "neutral" to R.drawable.ic_face_neutral, "bien" to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val labels = listOf("Triste", "Ansioso", "Neutral", "Bien", "Eufórico")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        moods.forEachIndexed { idx, (id, icon) ->
            val selected = viewModel.mood == id
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { viewModel.updateMood(id) }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .then(
                            if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = labels[idx],
                        tint = if (selected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = labels[idx],
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NoteStep(viewModel: CheckInViewModel) {
    OutlinedTextField(
        value = viewModel.note,
        onValueChange = { viewModel.updateNote(it) },
        placeholder = {
            Text(
                "Escribe cómo te sientes (opcional)...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background
        ),
        suffix = {
            Text(
                "${viewModel.note.length}/100",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        maxLines = 4
    )
}

@Composable
private fun UseStep(viewModel: CheckInViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val noSelected = viewModel.used == false
        UseOption(
            text = "No",
            icon = R.drawable.ic_circle_x,
            selected = noSelected,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        ) { viewModel.updateUsed(false) }

        val yesSelected = viewModel.used == true
        val sosRed = Color(0xFFE24B4A)
        UseOption(
            text = "Sí",
            icon = R.drawable.ic_alert_circle,
            selected = yesSelected,
            color = sosRed,
            modifier = Modifier.weight(1f)
        ) { viewModel.updateUsed(true) }
    }
}

@Composable
private fun UseOption(
    text: String,
    icon: Int,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) color else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun SubstanceStep(viewModel: CheckInViewModel) {
    Column(Modifier.fillMaxWidth()) {
        // Substance selector (opens the bottom sheet)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { viewModel.toggleSubstanceSheet() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.substance?.replaceFirstChar { it.uppercase() }
                    ?: "Elige la sustancia",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        // cantidad_aprox
        OutlinedTextField(
            value = viewModel.amount,
            onValueChange = { viewModel.updateAmount(it) },
            placeholder = { Text("Cantidad aprox. (ej. 2 cervezas)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background
            )
        )
        Spacer(Modifier.height(12.dp))
        // nota_contexto
        OutlinedTextField(
            value = viewModel.context,
            onValueChange = { viewModel.updateContext(it) },
            placeholder = { Text("¿Qué pasó? Contexto (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background
            ),
            suffix = {
                Text(
                    "${viewModel.context.length}/200",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            maxLines = 4
        )
    }
}
