package com.solvyx.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary
import kotlinx.coroutines.delay

private data class BreathPhase(val name: String, val durationMs: Int, val targetSize: Dp)

private val BREATH_PHASES = listOf(
    BreathPhase("Inhala", 4_000, 150.dp),
    BreathPhase("Mantén", 7_000, 150.dp),
    BreathPhase("Exhala", 8_000, 80.dp)
)

/**
 * 4-7-8 breathing guide for dark backgrounds: the circle grows while inhaling, holds, and shrinks
 * while exhaling, with the phase name and a countdown inside. [onPhaseChange] fires with each phase
 * name ("Inhala", "Mantén", "Exhala") so the caller can speak it or count cycles.
 */
@Composable
fun BreathingCircle(onPhaseChange: (String) -> Unit = {}) {
    var phaseIndex by remember { mutableIntStateOf(0) }
    var currentPhase by remember { mutableStateOf(BREATH_PHASES[0]) }
    var countdown by remember { mutableIntStateOf(BREATH_PHASES[0].durationMs / 1000) }

    LaunchedEffect(phaseIndex) {
        val phase = BREATH_PHASES[phaseIndex]
        currentPhase = phase
        onPhaseChange(phase.name)
        for (s in phase.durationMs / 1000 downTo 1) {
            countdown = s
            delay(1000L)
        }
        phaseIndex = (phaseIndex + 1) % BREATH_PHASES.size
    }

    val animatedDiameter by animateDpAsState(
        targetValue = currentPhase.targetSize,
        animationSpec = tween(
            durationMillis = currentPhase.durationMs,
            easing = when (phaseIndex) {
                1 -> LinearEasing
                else -> EaseInOut
            }
        ),
        label = "BreathDiameter"
    )

    val isExpanded = animatedDiameter > 110.dp
    val circleColor by animateColorAsState(
        targetValue = if (isExpanded) TealMedium.copy(alpha = 0.30f)
        else TealPrimary.copy(alpha = 0.40f),
        animationSpec = tween(600),
        label = "CircleColor"
    )

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Anillo exterior fijo de referencia
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White.copy(alpha = 0.30f), CircleShape)
        )

        // Círculo animado de respiración
        Box(
            modifier = Modifier
                .size(animatedDiameter)
                .clip(CircleShape)
                .background(circleColor)
                .border(2.dp, Color.White.copy(alpha = 0.60f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = BREATH_PHASES[phaseIndex].name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.02.sp
                )
                Text(
                    text = countdown.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
