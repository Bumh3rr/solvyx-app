package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.FireAnimation
import com.solvyx.ui.components.common.diasLabel
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val EMBER_CYCLE_MS = 5_200
private const val GLOW_BREATH_MS = 2_400
private const val BADGE_BOB_MS = 1_800
private const val BADGE_BOB_DP = 3f
private const val FULL_TURN = 2f * PI.toFloat()

/** Where Berto's chest sits in the stage, as fractions: the glow and embers start there. */
private const val BERTO_CENTER_X = 0.5f
private const val BERTO_CENTER_Y = 0.6f

private data class Ember(val x: Float, val speed: Int, val offset: Float, val drift: Float, val radius: Float)

/**
 * The streak burning behind Berto: a warm glow that breathes and embers that rise and fade. Both
 * scale with the streak ([glowFor], [embersFor]) and disappear with no streak, so a day of
 * consumption never gets a celebratory fire.
 */
@Composable
fun StreakGlow(streak: Int, modifier: Modifier = Modifier) {
    val strength = glowFor(streak)
    if (strength <= 0f) return
    val count = embersFor(streak)
    val embers = remember(count) {
        val random = Random(seed = 5)
        List(count) {
            Ember(
                x = BERTO_CENTER_X + (random.nextFloat() - 0.5f) * 0.42f,
                speed = 1 + random.nextInt(2),
                offset = random.nextFloat(),
                drift = random.nextFloat() * FULL_TURN,
                radius = 1.4f + random.nextFloat() * 2.2f
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "StreakGlow")
    val cycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(EMBER_CYCLE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "EmberCycle"
    )
    val breath by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(GLOW_BREATH_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "GlowBreath"
    )

    Canvas(modifier.fillMaxSize()) {
        val center = Offset(size.width * BERTO_CENTER_X, size.height * BERTO_CENTER_Y)
        val radius = size.minDimension * (0.34f + 0.1f * strength) * breath
        drawCircle(
            brush = Brush.radialGradient(
                listOf(StreakFlame.copy(alpha = 0.42f * strength), MoodAnsioso.copy(alpha = 0.12f * strength), Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
        embers.forEach { ember ->
            // Each ember rises from Berto's middle to the top, fading as it goes.
            val rise = (cycle * ember.speed + ember.offset) % 1f
            val y = center.y - rise * center.y * 0.95f
            val x = size.width * ember.x + sin(rise * FULL_TURN + ember.drift) * 8f * density
            val alpha = (1f - rise) * (0.55f + 0.45f * strength)
            drawCircle(
                color = if (ember.speed == 1) StreakFlame.copy(alpha = alpha) else MoodAnsioso.copy(alpha = alpha),
                radius = ember.radius * density * (1f - rise * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}

/** "N días" with a living flame, bobbing gently. Only shown while there's a streak. */
@Composable
fun StageFlameBadge(streak: Int, modifier: Modifier = Modifier) {
    val bob by rememberInfiniteTransition(label = "FlameBadge").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(BADGE_BOB_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "FlameBadgeBob"
    )
    Row(
        modifier = modifier
            .offset { IntOffset(0, (-BADGE_BOB_DP * bob).dp.roundToPx()) }
            .shadow(6.dp, RoundedCornerShape(50), ambientColor = StreakFlame, spotColor = StreakFlame)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FireAnimation(size = 24, isActive = true)
        Spacer(Modifier.width(2.dp))
        AnimatedContent(
            targetState = streak,
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "FlameBadgeCount"
        ) { value ->
            Text(
                text = "$value ${diasLabel(value)}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
        }
    }
}
