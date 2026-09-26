package com.solvyx.ui.screens.journey.checkin.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.MoodEuforico
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val PieceCount = 48
private const val BurstMillis = 1_800
private const val Gravity = 0.9f
private const val FullTurnDegrees = 720f
private val PieceColors = listOf(TealPrimary, MoodBien, MoodEuforico, StreakFlame, TealLight)

private data class Piece(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val color: Color
)

/**
 * One-shot confetti from the top center, for a clean day. Pieces fly out, fall with gravity and
 * fade; purely decorative and seeded, so it looks the same on every run.
 */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val pieces = remember {
        val random = Random(seed = 7)
        List(PieceCount) {
            Piece(
                angle = (random.nextFloat() * Math.PI).toFloat() + Math.PI.toFloat(),
                speed = 0.35f + random.nextFloat() * 0.65f,
                size = 6f + random.nextFloat() * 8f,
                spin = random.nextFloat() * FullTurnDegrees,
                color = PieceColors[random.nextInt(PieceColors.size)]
            )
        }
    }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(BurstMillis, easing = LinearEasing)) }

    Canvas(modifier) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val origin = Offset(size.width / 2, size.height * 0.2f)
        val reach = size.minDimension * 0.7f
        pieces.forEach { piece ->
            val x = origin.x + cos(piece.angle) * piece.speed * reach * t
            val y = origin.y + sin(piece.angle) * piece.speed * reach * t + Gravity * reach * t * t
            rotate(piece.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = piece.color.copy(alpha = 1f - t),
                    topLeft = Offset(x, y),
                    size = Size(piece.size, piece.size * 0.6f)
                )
            }
        }
    }
}
