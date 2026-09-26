package com.solvyx.ui.screens.journey.checkin.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxTopBarButtonStyle
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val SegmentFillMillis = 420
private const val TrackAlpha = 0.5f

/** Close button + one segment per step that fills as the user advances, tinted with [accent]. */
@Composable
fun CheckInTopBar(
    step: Int,
    totalSteps: Int,
    accent: Color,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SolvyxBackButton(onClick = onClose, style = SolvyxTopBarButtonStyle.OnSurface)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Registrar mi día",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    text = "Paso ${step + 1} de $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalSteps) { index ->
                ProgressSegment(filled = index <= step, accent = accent, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProgressSegment(filled: Boolean, accent: Color, modifier: Modifier = Modifier) {
    val fill by animateFloatAsState(if (filled) 1f else 0f, tween(SegmentFillMillis), label = "segmentFill")
    val color by animateColorAsState(accent, tween(SegmentFillMillis), label = "segmentColor")
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(TealLight.copy(alpha = TrackAlpha))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fill)
                .background(color, RoundedCornerShape(50))
        )
    }
}
