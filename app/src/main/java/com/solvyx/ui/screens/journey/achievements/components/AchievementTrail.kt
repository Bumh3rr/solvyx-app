package com.solvyx.ui.screens.journey.achievements.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.screens.journey.UiAchievement
import com.solvyx.ui.screens.journey.achievements.trailPosition
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary
import kotlin.math.roundToInt

private val RowHeight = 132.dp
private val MedalSize = 76.dp
private val StartDotSize = 26.dp
private val BertoSize = 64.dp
private val TrailWidth = 7.dp
private val LabelWidth = 132.dp
private const val LEFT_COLUMN = 0.27f
private const val RIGHT_COLUMN = 0.73f
private const val DRAW_TRAIL_MS = 1_400
private const val MEDAL_STAGGER_MS = 110L

/**
 * The streak achievements as a winding trail: a start point, then one medal per milestone,
 * zig-zagging down. The walked part of the trail is drawn in color (animated on open) and Berto
 * stands exactly where the current streak is. Tapping a medal opens its detail.
 */
@Composable
fun AchievementTrail(
    achievements: List<UiAchievement>,
    streak: Int,
    onSelect: (UiAchievement) -> Unit,
    modifier: Modifier = Modifier
) {
    val sorted = remember(achievements) { achievements.sortedBy { it.threshold } }
    val position = trailPosition(streak, sorted.map { it.threshold })
    val walked = remember { Animatable(0f) }
    LaunchedEffect(position) { walked.animateTo(position, tween(DRAW_TRAIL_MS, easing = FastOutSlowInEasing)) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight * (sorted.size + 1))
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val rowPx = with(density) { RowHeight.toPx() }
        // Node 0 is the start; nodes alternate columns so the trail zig-zags.
        val centers = remember(widthPx, sorted.size) {
            (0..sorted.size).map { i ->
                val column = if (i == 0) 0.5f else if (i % 2 == 1) LEFT_COLUMN else RIGHT_COLUMN
                Offset(widthPx * column, rowPx * i + rowPx / 2)
            }
        }
        val trail = remember(centers) { trailPath(centers) }
        val measure = remember(trail) { PathMeasure().apply { setPath(trail, false) } }
        val segments = sorted.size.coerceAtLeast(1)

        Canvas(Modifier.fillMaxSize()) {
            val stroke = TrailWidth.toPx()
            drawPath(
                trail,
                color = TealLight,
                style = Stroke(width = stroke, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 1.6f, stroke * 1.6f)))
            )
            val walkedPath = Path()
            measure.getSegment(0f, measure.length * walked.value / segments, walkedPath, true)
            drawPath(walkedPath, color = TealPrimary, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }

        StartPoint(center = centers[0])
        sorted.forEachIndexed { index, achievement ->
            TrailStop(
                achievement = achievement,
                tierIndex = index,
                center = centers[index + 1],
                labelOnRight = centers[index + 1].x < widthPx / 2,
                appearIndex = index,
                onClick = { onSelect(achievement) }
            )
        }

        val bertoAt = measure.getPosition(measure.length * walked.value / segments)
        BertoOnTrail(at = bertoAt, streak = streak)
    }
}

/** Smooth S-curves between consecutive stops. */
private fun trailPath(centers: List<Offset>): Path = Path().apply {
    moveTo(centers.first().x, centers.first().y)
    centers.zipWithNext().forEach { (from, to) ->
        val midY = (from.y + to.y) / 2
        cubicTo(from.x, midY, to.x, midY, to.x, to.y)
    }
}

@Composable
private fun Modifier.centeredAt(center: Offset, size: Dp): Modifier {
    val half = with(LocalDensity.current) { size.toPx() / 2 }
    return this.offset { IntOffset((center.x - half).roundToInt(), (center.y - half).roundToInt()) }
}

@Composable
private fun StartPoint(center: Offset) {
    Box(
        Modifier
            .centeredAt(center, StartDotSize)
            .size(StartDotSize)
            .clip(CircleShape)
            .background(TealPrimary)
            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
    )
}

@Composable
private fun TrailStop(
    achievement: UiAchievement,
    tierIndex: Int,
    center: Offset,
    labelOnRight: Boolean,
    appearIndex: Int,
    onClick: () -> Unit
) {
    val color = streakTierColor(tierIndex)
    StaggeredAppear(index = appearIndex, stepMs = MEDAL_STAGGER_MS, modifier = Modifier.centeredAt(center, MedalSize)) {
        Medal(
            icon = achievement.icon,
            color = color,
            unlocked = achievement.unlocked,
            progress = achievement.progress,
            size = MedalSize,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onClick)
        )
    }
    val density = LocalDensity.current
    val labelX = with(density) {
        if (labelOnRight) center.x + MedalSize.toPx() / 2 + 8.dp.toPx() else center.x - MedalSize.toPx() / 2 - 8.dp.toPx() - LabelWidth.toPx()
    }
    val labelY = with(density) { center.y - 22.dp.toPx() }
    Column(
        modifier = Modifier
            .offset { IntOffset(labelX.roundToInt(), labelY.roundToInt()) }
            .width(LabelWidth)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = if (labelOnRight) Alignment.Start else Alignment.End
    ) {
        Text(
            achievement.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = if (achievement.unlocked) TealDark else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = if (labelOnRight) TextAlign.Start else TextAlign.End
        )
        Text(
            "${achievement.threshold} días",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

/** Berto standing on the trail, just above the point that matches the current streak. */
@Composable
private fun BertoOnTrail(at: Offset, streak: Int) {
    val density = LocalDensity.current
    val x = with(density) { at.x - BertoSize.toPx() / 2 }
    val y = with(density) { at.y - BertoSize.toPx() * 0.95f }
    BertoPoseAnimation(
        pose = BertoPose.CENTER_IDLE_HELLO,
        riveFileRes = R.raw.berto_poses,
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .size(BertoSize),
        fallback = R.drawable.berto_saludando
    )
    val chipY = with(density) { at.y + 10.dp.toPx() }
    val chipX = with(density) { at.x - 28.dp.toPx() }
    Text(
        text = if (streak == 0) "Inicio" else "Día $streak",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .offset { IntOffset(chipX.roundToInt(), chipY.roundToInt()) }
            .width(56.dp)
            .clip(RoundedCornerShape(50))
            .background(TealDark)
            .padding(vertical = 3.dp)
    )
}
