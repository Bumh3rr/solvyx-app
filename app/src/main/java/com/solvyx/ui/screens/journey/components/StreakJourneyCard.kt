package com.solvyx.ui.screens.journey.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.backend.data.model.Achievement
import com.solvyx.ui.components.common.BestStreakBadge
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.StreakFlameBadge
import com.solvyx.ui.components.common.StreakHeatStops
import com.solvyx.ui.components.common.StreakNodeReached
import com.solvyx.ui.components.common.StreakTrackHeight
import com.solvyx.ui.components.common.TrackFlameHead
import com.solvyx.ui.components.common.diasLabel
import com.solvyx.ui.components.common.heatAt
import com.solvyx.ui.components.common.subtituloRacha
import com.solvyx.ui.components.common.textoAvanceRacha
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealMedium

/** Diameter of a milestone not yet reached (the full track has intermediate nodes; Home's doesn't). */
private val NodePending: Dp = 9.dp

/** Fixed width of each label's slot, so it can be centered over its node. */
private val LabelSlot: Dp = 24.dp

/**
 * Progress's streak card ("Mi camino"): the **full** journey of the 5 milestones, unlike Home's
 * compact version ([com.solvyx.ui.screens.home.HomeStreakCard]) which only shows the current
 * stretch. Same header (flame, number, subtitle, record) — Progress is where it makes sense to
 * see the whole journey, not just where you are now.
 */
@Composable
fun StreakJourneyCard(
    streak: Int,
    bestStreak: Int,
    modifier: Modifier = Modifier
) {
    val milestones = Achievement.MILESTONE_DAYS
    val maxMilestone = milestones.last()
    val isActive = streak > 0
    val hasSurpassedAll = streak >= maxMilestone

    val targetMilestone = milestones.firstOrNull { it > streak } ?: maxMilestone
    val prevMilestone = milestones.lastOrNull { it <= streak } ?: 0
    val stepProgress = if (hasSurpassedAll) 1f
        else ((streak - prevMilestone).toFloat() /
            (targetMilestone - prevMilestone).coerceAtLeast(1)).coerceIn(0f, 1f)

    // Progress over the FULL route (0..maxMilestone), not toward the next milestone: this is
    // what lets the flame travel the entire path.
    val targetRouteProgress = (streak.toFloat() / maxMilestone).coerceIn(0f, 1f)

    var startedRouteAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startedRouteAnim = true }
    val routeProgress by animateFloatAsState(
        targetValue = if (startedRouteAnim) targetRouteProgress else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "StreakRouteProgress"
    )

    val flameTint by animateColorAsState(
        targetValue = if (isActive) StreakFlame else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(500),
        label = "StreakFlameTint"
    )

    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StreakFlameBadge(isActive = isActive, tint = flameTint)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "Racha de bienestar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = streak,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { it } + fadeIn()) togetherWith
                                (slideOutVertically { -it } + fadeOut())
                        } else {
                            (slideInVertically { -it } + fadeIn()) togetherWith
                                (slideOutVertically { it } + fadeOut())
                        }
                    },
                    label = "StreakCount"
                ) { value ->
                    Text(
                        text = "$value ${diasLabel(value)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = subtituloRacha(streak, bestStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (bestStreak > streak && bestStreak > 0) {
                BestStreakBadge(bestStreak)
            }
        }

        MilestoneTrack(
            milestones = milestones,
            streak = streak,
            routeProgress = routeProgress,
            isActive = isActive,
            flameTint = flameTint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp)
        )

        Text(
            text = textoAvanceRacha(streak, targetMilestone, stepProgress, hasSurpassedAll),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        )
    }
}

/**
 * Milestone route: traveled line + nodes. It's a [Canvas] and not a row of composables because
 * the flame has to be able to stop at any intermediate point of the stroke, not just on a node.
 *
 * The color tells a **thermal journey**: the path is born in the brand's green (calm
 * constancy), warms up as it advances, and ends in the flame's orange. Each reached node takes
 * the temperature of its point in the journey, so the 5 milestones stop being identical dots and
 * read as a scale that progresses.
 */
@Composable
private fun MilestoneTrack(
    milestones: List<Int>,
    streak: Int,
    routeProgress: Float,
    isActive: Boolean,
    flameTint: Color,
    modifier: Modifier = Modifier
) {
    val maxMilestone = milestones.last()
    val trackColor = MaterialTheme.colorScheme.primaryContainer
    val pendingNode = TealMedium.copy(alpha = 0.45f)

    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(StreakTrackHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(StreakTrackHeight)) {
                val y = size.height / 2f
                val startX = StreakNodeReached.toPx() / 2f
                val endX = size.width - StreakNodeReached.toPx() / 2f
                val usable = endX - startX

                drawLine(
                    color = trackColor,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val headX = startX + usable * routeProgress
                if (routeProgress > 0f) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = StreakHeatStops,
                            startX = startX,
                            endX = headX.coerceAtLeast(startX + 1f)
                        ),
                        start = Offset(startX, y),
                        end = Offset(headX, y),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                milestones.forEach { day ->
                    val ratio = day.toFloat() / maxMilestone
                    val x = startX + usable * ratio
                    val reached = streak >= day

                    if (reached) {
                        val heat = heatAt(ratio, StreakHeatStops)
                        drawCircle(
                            color = trackColor,
                            radius = StreakNodeReached.toPx() / 2f + 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = heat,
                            radius = StreakNodeReached.toPx() / 2f,
                            center = Offset(x, y)
                        )
                    } else {
                        drawCircle(
                            color = trackColor,
                            radius = NodePending.toPx() / 2f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = pendingNode,
                            radius = NodePending.toPx() / 2f,
                            center = Offset(x, y),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }

            if (isActive) {
                TrackFlameHead(progress = routeProgress, tint = flameTint)
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            val usable = maxWidth - StreakNodeReached
            milestones.forEach { day ->
                val reached = streak >= day
                val x = StreakNodeReached / 2 + usable * (day.toFloat() / maxMilestone)
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (reached) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(LabelSlot)
                        .offset(x = x - LabelSlot / 2),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
