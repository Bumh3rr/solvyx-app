package com.solvyx.ui.screens.journey.achievements.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.AnimatedCountText
import com.solvyx.ui.theme.MedalGold
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

private val HeroShape = RoundedCornerShape(28.dp)
private val RingSize = 112.dp
private const val RING_FILL_MS = 1_200
private const val FULL_CIRCLE = 360f
private const val RING_START_ANGLE = -90f

/**
 * Top of Logros: a ring that fills with the unlocked share of the medals (streak + goals), the
 * counts, and Berto's line.
 */
@Composable
fun TrophyHero(
    unlocked: Int,
    total: Int,
    badgesUnlocked: Int,
    badgesTotal: Int,
    message: String,
    modifier: Modifier = Modifier,
    goalMedalsUnlocked: Int = 0,
    goalMedalsTotal: Int = 0
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeroShape)
            .background(Brush.linearGradient(listOf(TealPrimary, TealDark)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UnlockRing(unlocked = unlocked + goalMedalsUnlocked, total = total + goalMedalsTotal)
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroCount(value = unlocked, total = total, label = "logros de racha")
                if (goalMedalsTotal > 0) {
                    HeroCount(value = goalMedalsUnlocked, total = goalMedalsTotal, label = "medallas de metas")
                }
                HeroCount(value = badgesUnlocked, total = badgesTotal, label = "insignias del diario")
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_TO_RIGHT,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.size(52.dp),
                fallback = R.drawable.berto_dedo_der
            )
            Spacer(Modifier.width(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TealDark, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun UnlockRing(unlocked: Int, total: Int) {
    val sweep = remember { Animatable(0f) }
    val target = if (total == 0) 0f else unlocked.toFloat() / total
    LaunchedEffect(target) { sweep.animateTo(target, tween(RING_FILL_MS, easing = FastOutSlowInEasing)) }
    Box(Modifier.size(RingSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Color.White.copy(alpha = 0.2f), 0f, FULL_CIRCLE, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                Brush.sweepGradient(listOf(MedalGold, Color.White, MedalGold)),
                RING_START_ANGLE,
                FULL_CIRCLE * sweep.value,
                false,
                Offset(inset, inset),
                arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedCountText(
                value = unlocked,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text("/$total", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun HeroCount(value: Int, total: Int, label: String) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedCountText(
                value = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text(" de $total", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}
