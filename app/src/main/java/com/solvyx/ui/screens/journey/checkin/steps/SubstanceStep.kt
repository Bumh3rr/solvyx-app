package com.solvyx.ui.screens.journey.checkin.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SubstanceOption
import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.screens.journey.checkin.CONTEXT_MAX_LENGTH
import com.solvyx.ui.screens.journey.checkin.components.CheckInTextField
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val GridColumns = 2
private const val AmountMaxLength = 40
private const val SelectedTintAlpha = 0.1f
private const val IconCapsuleAlpha = 0.45f

/**
 * Which substance, how much and what happened — all on one screen (it used to hide the substance
 * list in a separate bottom sheet). Berto shows a harm-reduction tip for the chosen substance.
 */
@Composable
fun SubstanceStep(
    substance: String?,
    amount: String,
    context: String,
    onSubstanceSelect: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onContextChange: (String) -> Unit,
    onCaretMoved: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TrackedSubstances.chunked(GridColumns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { option ->
                    SubstanceChoice(
                        option = option,
                        isSelected = substance == option.id,
                        onClick = { onSubstanceSelect(option.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        CheckInTextField(
            value = amount,
            onValueChange = onAmountChange,
            placeholder = "Cantidad aproximada (ej. 2 cervezas)",
            maxLength = AmountMaxLength,
            minHeight = 0.dp,
            singleLine = true,
            onCaretMoved = onCaretMoved
        )
        CheckInTextField(
            value = context,
            onValueChange = onContextChange,
            placeholder = "¿Qué pasó? (opcional)",
            maxLength = CONTEXT_MAX_LENGTH,
            minHeight = 90.dp,
            onCaretMoved = onCaretMoved
        )
    }
}

@Composable
private fun SubstanceChoice(
    option: SubstanceOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val background by animateColorAsState(
        if (isSelected) primary.copy(alpha = SelectedTintAlpha) else MaterialTheme.colorScheme.surfaceDim,
        label = "substanceBg"
    )
    val border by animateColorAsState(if (isSelected) primary else TealLight, label = "substanceBorder")
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(if (isSelected) 2.dp else 1.dp, border, shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isSelected) primary else TealLight.copy(alpha = IconCapsuleAlpha),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = null,
                tint = if (isSelected) Color.White else primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
    }
}
