package com.solvyx.ui.screens.journey.checkin.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.MoodOption
import com.solvyx.ui.components.common.MoodOptions
import kotlinx.coroutines.delay

private const val EntranceStepMillis = 60L
private const val SelectedScale = 1.18f
private const val DimmedScale = 0.9f
private const val DimmedAlpha = 0.45f
private const val IdleTintAlpha = 0.15f
private const val IdleBorderAlpha = 0.4f

/**
 * Five mood bubbles that pop in one after another. The chosen one grows with a bounce and the rest
 * step back, so the choice is felt, not just shown; a light haptic confirms it.
 */
@Composable
fun MoodStep(selected: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MoodOptions.forEachIndexed { index, option ->
            MoodBubble(
                option = option,
                index = index,
                isSelected = selected == option.id,
                anySelected = selected != null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(option.id)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MoodBubble(
    option: MoodOption,
    index: Int,
    isSelected: Boolean,
    anySelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * EntranceStepMillis)
        entrance.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    val scale by animateFloatAsState(
        targetValue = when {
            isSelected -> SelectedScale
            anySelected -> DimmedScale
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "moodScale"
    )
    val alpha by animateFloatAsState(
        if (anySelected && !isSelected) DimmedAlpha else 1f,
        tween(250),
        label = "moodAlpha"
    )
    val background by animateColorAsState(
        if (isSelected) option.color else option.color.copy(alpha = IdleTintAlpha),
        label = "moodBg"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale * entrance.value
                scaleY = scale * entrance.value
                this.alpha = alpha * entrance.value
            }
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(background, CircleShape)
                .border(
                    width = if (isSelected) 0.dp else 1.5.dp,
                    color = if (isSelected) Color.Transparent else option.color.copy(alpha = IdleBorderAlpha),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = option.label,
                tint = if (isSelected) Color.White else option.color,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = option.label,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (isSelected) option.color else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
