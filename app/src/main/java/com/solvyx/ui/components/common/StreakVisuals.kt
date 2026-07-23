package com.solvyx.ui.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

/**
 * Visual pieces shared between Home's compact streak card ([com.solvyx.ui.screens
 * .home.HomeStreakCard]) and Progress's full journey card (`StreakJourneyCard`): the flame
 * badge, the traveling marker, the thermal color scale, and the copy. For these pieces both
 * cards are, alike, "a horizontal track from 0 to 1" — they only differ in how many milestones
 * they draw on top of it.
 */

/** Shared height of the streak track. */
val StreakTrackHeight: Dp = 34.dp

/** Diameter of an already-reached milestone node — also the start/end margin of any track. */
val StreakNodeReached: Dp = 13.dp

/** Halo of the marker traveling over the rail. It contains the flame, so it wraps it with room to breathe. */
val StreakHeadSize: Dp = 26.dp

/** Lottie flame inside the marker. Smaller than the halo so the pulse shows behind it. */
val StreakFlameHeadSize: Dp = 18.dp

/** Flame badge. Only breathes with an active streak: the pulse communicates "still alive". */
@Composable
fun StreakFlameBadge(isActive: Boolean, tint: Color) {
    val breathing = rememberInfiniteTransition(label = "StreakBreath")
    val pulse by breathing.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StreakBreathScale"
    )
    val halo by animateFloatAsState(
        targetValue = if (isActive) 0.35f else 0.10f,
        animationSpec = tween(500),
        label = "StreakHalo"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(if (isActive) pulse else 1f)
            .clip(CircleShape)
            .background(tint.copy(alpha = halo)),
        contentAlignment = Alignment.Center
    ) {
        FireAnimation(size = 24, isActive = isActive)
    }
}

/** Best-streak badge, shown in the header when it says something the current streak doesn't already say. */
@Composable
fun BestStreakBadge(bestStreak: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = "Récord $bestStreak",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Marker head over the rail: the Lottie flame traveling along the path, at [progress]
 * (0f-1f) of the available horizontal track (with the same [StreakNodeReached] margin on both
 * ends that the containing track's `Canvas` uses). Only mounted with an active streak, so the
 * flame always animates here.
 */
@Composable
fun TrackFlameHead(progress: Float, tint: Color) {
    val breathing = rememberInfiniteTransition(label = "TrackHead")
    val glow by breathing.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TrackHeadGlow"
    )

    BoxWithConstraints(Modifier.fillMaxWidth().height(StreakTrackHeight)) {
        val usable = maxWidth - StreakNodeReached
        val headX = StreakNodeReached / 2 + usable * progress.coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .offset(x = headX - StreakHeadSize / 2)
                .size(StreakHeadSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(StreakHeadSize)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.30f * glow))
            )
            FireAnimation(size = StreakFlameHeadSize.value.toInt(), isActive = true)
        }
    }
}

/**
 * Color of the thermal scale at [ratio] (0..1 of the journey). Interpolates between adjacent
 * stops the same way the rail's gradient would, so a node or segment painted with this lands
 * exactly on the tone the line underneath has.
 */
fun heatAt(ratio: Float, stops: List<Color>): Color {
    if (stops.isEmpty()) return Color.Transparent
    if (stops.size == 1) return stops.first()
    val t = ratio.coerceIn(0f, 1f) * (stops.size - 1)
    val i = t.toInt().coerceAtMost(stops.size - 2)
    return lerp(stops[i], stops[i + 1], t - i)
}

/** Shared thermal color scale for both streak tracks (Home's compact segment and Progress's
 * full journey) — calm green at the start, warming through mood greens/amber, ending in the
 * flame's orange. Both cards must read this same list so a point on the journey always renders
 * the same color regardless of which card is showing it. */
val StreakHeatStops: List<Color> = listOf(TealDark, TealPrimary, MoodBien, MoodAnsioso, StreakFlame)

// ── Shared copy ─────────────────────────────────────────────────────────

/** Avoids the "1 días" the previous version showed. */
fun diasLabel(value: Int): String = if (value == 1) "día" else "días"

fun subtituloRacha(streak: Int, bestStreak: Int): String = when {
    streak == 0 && bestStreak == 0 -> "Tu primer día empieza cuando tú decidas."
    streak == 0 -> "Puedes volver a empezar hoy."
    streak == bestStreak -> "Estás en tu mejor racha."
    else -> "Vas construyendo tu constancia."
}

fun textoAvanceRacha(
    streak: Int,
    nextMilestone: Int,
    milestoneProgress: Float,
    hasSurpassedAll: Boolean
): String {
    if (hasSurpassedAll) return "Superaste todos los hitos. $streak ${diasLabel(streak)} de constancia."
    val restantes = (nextMilestone - streak).coerceAtLeast(0)
    if (streak == 0) return "El primer hito son $nextMilestone días."
    val pct = (milestoneProgress * 100).toInt().coerceIn(0, 100)
    return if (restantes == 1) "Te falta 1 día para el hito de $nextMilestone · $pct%"
           else "Te faltan $restantes días para el hito de $nextMilestone · $pct%"
}
