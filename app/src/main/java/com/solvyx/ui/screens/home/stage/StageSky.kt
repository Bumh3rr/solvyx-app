package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Colors of the stage at each time of day. [hillBack] sits behind Berto, [hillFront] under his feet. */
data class StagePalette(
    val skyTop: Color,
    val skyBottom: Color,
    val hillBack: Color,
    val hillFront: Color,
    val celestial: Color,
    val isNight: Boolean
)

fun stagePaletteFor(timeOfDay: TimeOfDay): StagePalette = when (timeOfDay) {
    TimeOfDay.MORNING -> StagePalette(
        skyTop = Color(0xFFCDEFE2), skyBottom = Color(0xFFFFF4E2),
        hillBack = Color(0xFFB1DCC4), hillFront = Color(0xFF86C6A2),
        celestial = Color(0xFFFFCF73), isNight = false
    )
    TimeOfDay.AFTERNOON -> StagePalette(
        skyTop = Color(0xFFBDE6EE), skyBottom = Color(0xFFEAF8F1),
        hillBack = Color(0xFFA3D5B8), hillFront = Color(0xFF79BF97),
        celestial = Color(0xFFFFE08A), isNight = false
    )
    TimeOfDay.EVENING -> StagePalette(
        skyTop = Color(0xFFF5C4A6), skyBottom = Color(0xFFF1E1EE),
        hillBack = Color(0xFF9FC2AB), hillFront = Color(0xFF6FA88A),
        celestial = Color(0xFFFF9C6B), isNight = false
    )
    TimeOfDay.NIGHT -> StagePalette(
        skyTop = Color(0xFF14303D), skyBottom = Color(0xFF2B535E),
        hillBack = Color(0xFF244F49), hillFront = Color(0xFF1E6652),
        celestial = Color(0xFFF4F0D8), isNight = true
    )
}

/** Where the front hill's top edge sits, as a fraction of the stage height (left, middle, right). */
internal const val FRONT_HILL_EDGE_Y = 0.9f
internal const val FRONT_HILL_PEAK_Y = 0.87f

/** Height of the front hill at [x] (0..1): a quadratic curve through the three points above. */
internal fun frontHillY(x: Float): Float {
    val t = x.coerceIn(0f, 1f)
    val oneMinus = 1f - t
    // Control point so the curve passes through FRONT_HILL_PEAK_Y at the middle.
    val control = 2f * FRONT_HILL_PEAK_Y - FRONT_HILL_EDGE_Y
    return oneMinus * oneMinus * FRONT_HILL_EDGE_Y + 2f * oneMinus * t * control + t * t * FRONT_HILL_EDGE_Y
}

private const val STAR_COUNT = 22
private const val TWINKLE_MS = 3_200

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float)

/** Sky, sun or moon, stars at night and the far hill. Everything behind Berto. */
@Composable
fun StageSky(palette: StagePalette, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)))
        if (palette.isNight) drawMoon(palette) else drawSun(palette)
        drawBackHill(palette.hillBack)
    }
    if (palette.isNight) TwinklingStars(modifier)
}

/** Only mounted at night, so the day sky doesn't redraw every frame for stars nobody sees. */
@Composable
private fun TwinklingStars(modifier: Modifier = Modifier) {
    val stars = remember {
        val random = Random(seed = 11)
        List(STAR_COUNT) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat() * 0.55f,
                radius = 0.8f + random.nextFloat() * 1.6f,
                phase = random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }
    val twinkle by rememberInfiniteTransition(label = "SkyTwinkle").animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(TWINKLE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "SkyTwinklePhase"
    )

    Canvas(modifier.fillMaxSize()) {
        stars.forEach { star ->
            val alpha = 0.35f + 0.65f * ((sin(twinkle + star.phase) + 1f) / 2f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.radius * density,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}

private fun DrawScope.drawSun(palette: StagePalette) {
    val center = Offset(size.width * 0.86f, size.height * 0.36f)
    val radius = size.minDimension * 0.08f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.celestial.copy(alpha = 0.45f), Color.Transparent),
            center = center,
            radius = radius * 2.6f
        ),
        radius = radius * 2.6f,
        center = center
    )
    drawCircle(palette.celestial, radius, center)
}

/** A crescent: the moon, then a sky-colored disc slightly offset over it. */
private fun DrawScope.drawMoon(palette: StagePalette) {
    val center = Offset(size.width * 0.86f, size.height * 0.36f)
    val radius = size.minDimension * 0.065f
    drawCircle(palette.celestial.copy(alpha = 0.18f), radius * 2f, center)
    drawCircle(palette.celestial, radius, center)
    drawCircle(palette.skyTop, radius * 0.86f, center + Offset(radius * 0.45f, -radius * 0.2f))
}

private fun DrawScope.drawBackHill(color: Color) {
    val path = Path().apply {
        moveTo(0f, size.height * 0.78f)
        quadraticTo(size.width * 0.3f, size.height * 0.66f, size.width * 0.62f, size.height * 0.76f)
        quadraticTo(size.width * 0.85f, size.height * 0.83f, size.width, size.height * 0.72f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(path, color)
}
