package com.solvyx.ui.screens.profilesetup.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxTopBarButtonSize
import com.solvyx.ui.components.common.SolvyxTopBarButtonStyle
import com.solvyx.ui.screens.profilesetup.ProfileSetupStep
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val SegmentFillMillis = 450
private const val TrackAlpha = 0.5f

/**
 * One header for all three setup steps: "Paso X de 3", a segment per step and each step's name, so
 * the user always knows where they are. [onBack] is null on the first reachable step.
 */
@Composable
fun SetupStepHeader(
    step: ProfileSetupStep,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val steps = ProfileSetupStep.entries
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                SolvyxBackButton(onClick = onBack, style = SolvyxTopBarButtonStyle.OnSurface)
            } else {
                Spacer(Modifier.size(SolvyxTopBarButtonSize))
            }
            Text(
                text = "Paso ${step.ordinal + 1} de ${steps.size}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(SolvyxTopBarButtonSize))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEach { item ->
                StepSegment(
                    label = item.label,
                    state = when {
                        item.ordinal < step.ordinal -> SegmentState.DONE
                        item == step -> SegmentState.CURRENT
                        else -> SegmentState.UPCOMING
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private enum class SegmentState { DONE, CURRENT, UPCOMING }

@Composable
private fun StepSegment(label: String, state: SegmentState, modifier: Modifier = Modifier) {
    val fill by animateFloatAsState(
        targetValue = if (state == SegmentState.UPCOMING) 0f else 1f,
        animationSpec = tween(SegmentFillMillis),
        label = "setupSegment"
    )
    Column(modifier = modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(TealLight.copy(alpha = TrackAlpha))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (state == SegmentState.CURRENT) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (state == SegmentState.UPCOMING) MaterialTheme.colorScheme.onSurfaceVariant else TealDark,
            maxLines = 1
        )
    }
}
