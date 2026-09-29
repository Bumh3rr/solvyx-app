package com.solvyx.ui.screens.journey.achievements.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.MedalGold
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.MoodEuforico
import com.solvyx.ui.theme.MoodTriste
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val RAY_COUNT = 12
private const val RAYS_TURN_MS = 9_000
private const val FULL_TURN_DEGREES = 360f
private const val MEDAL_CORE_RATIO = 0.74f
private const val ICON_RATIO = 0.36f
private const val HIGHLIGHT_MIX = 0.45f

/** One color per streak tier, from the first milestone to the last (gold). */
private val TierColors = listOf(MoodBien, StreakFlame, MoodTriste, MoodEuforico, MedalGold)

fun streakTierColor(tierIndex: Int): Color = TierColors[tierIndex.coerceIn(0, TierColors.lastIndex)]

/** Diary badges all share the brand teal, so they read as a different family from streak medals. */
val DiaryBadgeColor: Color = TealPrimary

/** Goal medals (1, 5 and 10 goals): green, blue and gold. */
private val GoalTierColors = listOf(MoodBien, MoodTriste, MedalGold)

fun goalTierColor(tierIndex: Int): Color = GoalTierColors[tierIndex.coerceIn(0, GoalTierColors.lastIndex)]

/**
 * A medal. Unlocked: a glossy disc in [color] with light rays turning slowly behind it. Locked: a
 * grey disc with a lock and a ring showing [progress] toward unlocking it.
 */
@Composable
fun Medal(
    @DrawableRes icon: Int,
    color: Color,
    unlocked: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    showRays: Boolean = true
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (unlocked && showRays) MedalRays(color)
        if (!unlocked) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(size * MEDAL_CORE_RATIO + 8.dp),
                strokeWidth = 3.dp,
                strokeCap = StrokeCap.Round,
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
        Box(
            modifier = Modifier
                .size(size * MEDAL_CORE_RATIO)
                .clip(CircleShape)
                .background(
                    if (unlocked) {
                        Brush.radialGradient(listOf(lerp(color, Color.White, HIGHLIGHT_MIX), color))
                    } else {
                        Brush.radialGradient(listOf(MaterialTheme.colorScheme.surfaceDim, MaterialTheme.colorScheme.surfaceDim))
                    }
                )
                .border(3.dp, if (unlocked) Color.White.copy(alpha = 0.8f) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (unlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(size * ICON_RATIO)
            )
        }
        if (!unlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_lock),
                    contentDescription = "Bloqueado",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.15f)
                )
            }
        }
    }
}

/** Soft rays turning behind an unlocked medal. */
@Composable
private fun MedalRays(color: Color) {
    val angle by rememberInfiniteTransition(label = "MedalRays").animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN_DEGREES,
        animationSpec = infiniteRepeatable(tween(RAYS_TURN_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "MedalRaysAngle"
    )
    Canvas(
        Modifier
            .fillMaxSize()
            .rotate(angle)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val inner = size.minDimension * 0.3f
        val outer = size.minDimension / 2
        repeat(RAY_COUNT) { i ->
            val theta = (2 * PI * i / RAY_COUNT).toFloat()
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = center + Offset(cos(theta) * inner, sin(theta) * inner),
                end = center + Offset(cos(theta) * outer, sin(theta) * outer),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
