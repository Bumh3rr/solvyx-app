package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.MoodEuforico
import com.solvyx.ui.theme.MoodTriste
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Tallest plant, as a fraction of the stage height. */
private const val PLANT_MAX_HEIGHT = 0.27f
private const val GROW_MS = 1_600
/** How many plants overlap while growing: bigger = a softer wave from the center outwards. */
private const val GROW_OVERLAP = 3f
private const val SWAY_DEGREES = 4f
private const val SWAY_CYCLE_MS = 6_000
private const val CELEBRATION_SWAY = 4f
private const val FULL_TURN = 2f * PI.toFloat()

private val Stem = Color(0xFF3E8E5E)
private val Leaf = Color(0xFF58A873)
private val Soil = Color(0xFF8A6A4F)
private val FlowerCenter = Color(0xFFF6C744)
private val SunflowerPetal = Color(0xFFF4B73A)
private val SunflowerCenter = Color(0xFF7A4E2A)
private val DaisyPetal = Color(0xFFFFFDF7)
private val PetalColors = listOf(
    MoodEuforico, Color(0xFFF2856D), MoodTriste, Color(0xFFB39DDB), MoodAnsioso
)
private val WingColors = listOf(Color(0xFFF2856D), Color(0xFF8FB8F0), Color(0xFFF6C744))

/**
 * The front of the stage, drawn over Berto's feet: the near hill and a garden that grows with the
 * streak (see [gardenFor]). Plants grow in a wave from Berto outwards, sway out of phase, and
 * wiggle harder for a moment whenever [celebrationKey] changes.
 */
@Composable
fun StreakGarden(
    streak: Int,
    palette: StagePalette,
    celebrationKey: Int,
    modifier: Modifier = Modifier
) {
    val plants = remember(streak) { gardenFor(streak) }
    val butterflies = remember(streak) { butterfliesFor(streak) }

    val growth = remember(plants) { Animatable(0f) }
    LaunchedEffect(plants) {
        growth.animateTo(1f, tween(GROW_MS, easing = FastOutSlowInEasing))
    }
    val swayBoost = remember { Animatable(1f) }
    LaunchedEffect(celebrationKey) {
        if (celebrationKey == 0) return@LaunchedEffect
        swayBoost.snapTo(CELEBRATION_SWAY)
        swayBoost.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow))
    }
    val time by rememberInfiniteTransition(label = "Garden").animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(tween(SWAY_CYCLE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "GardenTime"
    )

    Canvas(modifier.fillMaxSize()) {
        drawFrontHill(palette.hillFront)
        val plantUnit = size.height * PLANT_MAX_HEIGHT
        plants.forEachIndexed { index, plant ->
            val local = ((growth.value * (plants.size + GROW_OVERLAP)) - index) / GROW_OVERLAP
            val progress = local.coerceIn(0f, 1f)
            if (progress <= 0f) return@forEachIndexed
            val base = Offset(plant.x * size.width, frontHillY(plant.x) * size.height + 2f * density)
            val sway = sin(time + plant.swayPhase) * SWAY_DEGREES * swayBoost.value
            rotate(sway, pivot = base) {
                drawPlant(plant, base, plant.height * plantUnit, progress)
            }
        }
        repeat(butterflies) { drawButterfly(it, time) }
    }
}

private fun DrawScope.drawFrontHill(color: Color) {
    val control = 2f * FRONT_HILL_PEAK_Y - FRONT_HILL_EDGE_Y
    val path = Path().apply {
        moveTo(0f, size.height * FRONT_HILL_EDGE_Y)
        quadraticTo(size.width / 2f, size.height * control, size.width, size.height * FRONT_HILL_EDGE_Y)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(path, color)
}

/** Stem grows during the first 60 % of [progress]; the head opens in the rest. */
private fun DrawScope.drawPlant(plant: GardenPlant, base: Offset, fullHeight: Float, progress: Float) {
    val stemProgress = (progress / 0.6f).coerceAtMost(1f)
    val bloom = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
    val height = fullHeight * stemProgress
    val top = base - Offset(0f, height)
    val petal = PetalColors[plant.colorIndex % PetalColors.size]

    if (plant.kind == PlantKind.SEED) {
        drawSeed(base, fullHeight, progress)
        return
    }
    drawStem(base, top, fullHeight)
    drawLeaves(base, height, fullHeight, plant.kind)
    if (bloom <= 0f) return
    val headSize = fullHeight * 0.16f * bloom
    when (plant.kind) {
        PlantKind.SPROUT -> Unit
        PlantKind.BUD -> drawBud(top, headSize, petal)
        PlantKind.TULIP -> drawTulip(top, headSize * 1.25f, petal)
        PlantKind.DAISY -> drawDaisy(top, headSize * 1.1f, DaisyPetal, FlowerCenter)
        PlantKind.SUNFLOWER -> drawDaisy(top, headSize * 1.5f, SunflowerPetal, SunflowerCenter, petals = 12)
        PlantKind.SEED -> Unit
    }
}

private fun DrawScope.drawSeed(base: Offset, fullHeight: Float, progress: Float) {
    val mound = fullHeight * 0.6f
    drawOval(
        color = Soil,
        topLeft = base - Offset(mound, mound * 0.45f),
        size = Size(mound * 2f, mound * 0.9f)
    )
    drawCircle(Leaf, radius = mound * 0.28f * progress, center = base - Offset(0f, mound * 0.55f))
}

private fun DrawScope.drawStem(base: Offset, top: Offset, fullHeight: Float) {
    val path = Path().apply {
        moveTo(base.x, base.y)
        quadraticTo(base.x + fullHeight * 0.08f, (base.y + top.y) / 2f, top.x, top.y)
    }
    drawPath(path, Stem, style = Stroke(width = 2.6f * density, cap = StrokeCap.Round))
}

/** Two leaves low on the stem; sprouts are all leaves, so theirs are bigger and higher. */
private fun DrawScope.drawLeaves(base: Offset, height: Float, fullHeight: Float, kind: PlantKind) {
    val isSprout = kind == PlantKind.SPROUT
    val length = fullHeight * if (isSprout) 0.42f else 0.26f
    val at = base - Offset(0f, height * if (isSprout) 0.85f else 0.35f)
    drawLeaf(at, length, angle = -35f)
    drawLeaf(at - Offset(0f, height * 0.08f), length * 0.9f, angle = 215f)
}

private fun DrawScope.drawLeaf(from: Offset, length: Float, angle: Float) {
    if (length <= 0f) return
    rotate(angle, pivot = from) {
        val path = Path().apply {
            moveTo(from.x, from.y)
            quadraticTo(from.x + length * 0.5f, from.y - length * 0.32f, from.x + length, from.y)
            quadraticTo(from.x + length * 0.5f, from.y + length * 0.32f, from.x, from.y)
            close()
        }
        drawPath(path, Leaf)
    }
}

private fun DrawScope.drawBud(top: Offset, size: Float, color: Color) {
    drawOval(color, topLeft = top - Offset(size * 0.55f, size * 1.3f), size = Size(size * 1.1f, size * 1.5f))
}

/** A cup of three petals: two sides and a front one. */
private fun DrawScope.drawTulip(top: Offset, size: Float, color: Color) {
    val cup = Path().apply {
        moveTo(top.x - size, top.y - size * 1.1f)
        lineTo(top.x - size * 0.45f, top.y - size * 0.6f)
        lineTo(top.x, top.y - size * 1.25f)
        lineTo(top.x + size * 0.45f, top.y - size * 0.6f)
        lineTo(top.x + size, top.y - size * 1.1f)
        quadraticTo(top.x + size, top.y + size * 0.1f, top.x, top.y + size * 0.1f)
        quadraticTo(top.x - size, top.y + size * 0.1f, top.x - size, top.y - size * 1.1f)
        close()
    }
    drawPath(cup, color)
    drawPath(cup, Color.White.copy(alpha = 0.22f), style = Stroke(width = 1f * density))
}

private fun DrawScope.drawDaisy(
    top: Offset,
    size: Float,
    petalColor: Color,
    centerColor: Color,
    petals: Int = 8
) {
    val center = top - Offset(0f, size * 0.4f)
    repeat(petals) { index ->
        val angle = index * 360f / petals
        withTransform({ rotate(angle, pivot = center) }) {
            drawOval(
                color = petalColor,
                topLeft = center - Offset(size * 0.22f, size * 1.05f),
                size = Size(size * 0.44f, size * 0.85f)
            )
        }
    }
    drawCircle(centerColor, radius = size * 0.34f, center = center)
}

/** Butterflies loop over the garden; integer multipliers of [time] keep the loop seamless. */
private fun DrawScope.drawButterfly(index: Int, time: Float) {
    val phase = index * 2.1f
    val anchorX = when (index) {
        0 -> 0.18f
        1 -> 0.82f
        else -> 0.5f
    }
    val anchorY = if (index == 2) 0.28f else 0.58f
    val position = Offset(
        x = size.width * (anchorX + 0.07f * sin(time + phase)),
        y = size.height * (anchorY + 0.06f * sin(2f * time + phase))
    )
    val flap = abs(cos(24f * time + phase))
    val wing = size.minDimension * 0.028f
    val color = WingColors[index % WingColors.size]
    listOf(-1f, 1f).forEach { side ->
        drawOval(
            color = color,
            topLeft = Offset(position.x + side * wing * flap - wing * flap, position.y - wing),
            size = Size(wing * 2f * flap.coerceAtLeast(0.15f), wing * 1.6f)
        )
    }
    drawLine(
        color = Color(0xFF3B3B3B),
        start = position - Offset(0f, wing * 0.8f),
        end = position + Offset(0f, wing * 0.6f),
        strokeWidth = 1.6f * density,
        cap = StrokeCap.Round
    )
}
