package com.solvyx.ui.screens.journey

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.MoodEuforico
import com.solvyx.ui.theme.MoodNeutral
import com.solvyx.ui.theme.MoodTriste
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium

/**
 * Real mood color for a value in the series. The values are exactly the 5 from
 * `JourneyViewModel.moodScale` (1/3/5/7/10) plus 0f for "no check-in that day" — they're never
 * computed or summed, so the exact Float comparison is safe here.
 */
private fun moodColor(value: Float): Color = when (value) {
    1f -> MoodTriste
    3f -> MoodAnsioso
    5f -> MoodNeutral
    7f -> MoodBien
    10f -> MoodEuforico
    else -> TealMedium.copy(alpha = 0.3f)
}

/**
 * Face icon for a value in the series, or null for "no check-in that day" (0f) — that case keeps
 * the plain dot look, since there's no mood to show a face for.
 */
private fun moodPainter(
    value: Float,
    sad: Painter,
    anxious: Painter,
    neutral: Painter,
    happy: Painter,
    euphoric: Painter
): Painter? = when (value) {
    1f -> sad
    3f -> anxious
    5f -> neutral
    7f -> happy
    10f -> euphoric
    else -> null
}

@Composable
fun FeelingsChart(
    data: List<Float>,  // bienestar values 0-10
    labels: List<String>,
    modifier: Modifier = Modifier,
    onPointSelected: ((Int) -> Unit)? = null
) {
    if (data.isEmpty()) return

    val density = LocalDensity.current
    val sadPainter = painterResource(R.drawable.ic_face_sad)
    val anxiousPainter = painterResource(R.drawable.ic_face_anxious)
    val neutralPainter = painterResource(R.drawable.ic_face_neutral)
    val happyPainter = painterResource(R.drawable.ic_face_happy)
    val euphoricPainter = painterResource(R.drawable.ic_face_euphoric)
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .then(
                    if (onPointSelected == null) Modifier
                    else Modifier.pointerInput(data.size) {
                        detectTapGestures { offset ->
                            val leftPad = with(density) { 32.dp.toPx() }
                            val rightPad = with(density) { 8.dp.toPx() }
                            val n = data.size
                            val chartWidth = size.width - leftPad - rightPad
                            if (n <= 0 || chartWidth <= 0f) return@detectTapGestures
                            val rel = ((offset.x - leftPad) / chartWidth).coerceIn(0f, 1f)
                            val index = Math.round(rel * (n - 1).coerceAtLeast(1)).coerceIn(0, n - 1)
                            onPointSelected(index)
                        }
                    }
                )
        ) {
            val leftPad   = 32.dp.toPx()
            val rightPad  = 8.dp.toPx()
            val bottomPad = 24.dp.toPx()
            val topPad    = 12.dp.toPx()

            val chartWidth  = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad

            // ── Y-axis guide lines at 0, 5, 10 ──────────────────────────────
            val guideValues = listOf(0f, 5f, 10f)
            val guidePaint = Paint().apply {
                color = TealLightest.toArgb()
                textSize = 9.sp.toPx()
                isAntiAlias = true
                this.color = TealMedium.copy(alpha = 0.5f).toArgb()
            }
            guideValues.forEach { v ->
                val y = topPad + chartHeight - (v / 10f) * chartHeight
                drawLine(
                    color = TealLightest,
                    start = Offset(leftPad, y),
                    end   = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    v.toInt().toString(),
                    leftPad - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    guidePaint
                )
            }

            val n = data.size
            fun xAt(i: Int): Float =
                leftPad + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartWidth
            fun yAt(value: Float): Float =
                topPad + chartHeight - (value / 10f) * chartHeight

            for (i in 0 until n - 1) {
                val x0 = xAt(i); val y0 = yAt(data[i])
                val x1 = xAt(i + 1); val y1 = yAt(data[i + 1])
                val midX = (x0 + x1) / 2f
                val segmentPath = Path().apply {
                    moveTo(x0, y0)
                    cubicTo(midX, y0, midX, y1, x1, y1)
                }
                drawPath(
                    path = segmentPath,
                    brush = Brush.linearGradient(
                        colors = listOf(moodColor(data[i]), moodColor(data[i + 1])),
                        start = Offset(x0, y0),
                        end = Offset(x1, y1)
                    ),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // ── Points as a mood chip, matching HomeMoodCard's look ────────
            // Opaque white backdrop FIRST: the line above is drawn before the points, so
            // without a fully opaque circle underneath, the semi-transparent chip tint let the
            // line color show through right where it crosses a point — looking like the line
            // was "poking out" over the icon instead of sitting cleanly behind it.
            val pointRadius = 8.dp.toPx()
            val faceIconSize = 18.dp.toPx()
            data.forEachIndexed { i, v ->
                val center = Offset(xAt(i), yAt(v))
                val painter = moodPainter(v, sadPainter, anxiousPainter, neutralPainter, happyPainter, euphoricPainter)
                if (painter != null) {
                    val color = moodColor(v)
                    drawCircle(color = Color.White, radius = pointRadius, center = center)
                    drawCircle(color = color.copy(alpha = 0.18f), radius = pointRadius, center = center)
                    drawCircle(
                        color = color.copy(alpha = 0.6f),
                        radius = pointRadius,
                        center = center,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                    translate(left = center.x - faceIconSize / 2f, top = center.y - faceIconSize / 2f) {
                        with(painter) {
                            draw(size = Size(faceIconSize, faceIconSize), colorFilter = ColorFilter.tint(color))
                        }
                    }
                } else {
                    // No check-in that day: keep the plain dim dot, no mood to show a chip for.
                    drawCircle(color = Color.White, radius = pointRadius, center = center)
                    drawCircle(color = moodColor(v), radius = 4.dp.toPx(), center = center)
                    drawCircle(color = Color.White, radius = 1.8.dp.toPx(), center = center)
                }
            }

            // ── X-axis labels ─────────────────────────────────────────────
            val labelPaint = Paint().apply {
                textSize    = 8.5.sp.toPx()
                isAntiAlias = true
                color       = TealMedium.copy(alpha = 0.85f).toArgb()
                textAlign   = Paint.Align.CENTER
            }
            val step = if (n <= 7) 1 else if (n <= 14) 2 else 4
            data.indices.forEach { i ->
                if (i % step == 0 && i < labels.size) {
                    val x = xAt(i)
                    val y = size.height - 4.dp.toPx()
                    drawContext.canvas.nativeCanvas.drawText(labels[i], x, y, labelPaint)
                }
            }
        }

        // ── Legend: an icon (not just color) per mood, since 2 of the 5 mood colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoodLegendIcon(MoodTriste, R.drawable.ic_face_sad, "Triste")
            MoodLegendIcon(MoodAnsioso, R.drawable.ic_face_anxious, "Ansioso")
            MoodLegendIcon(MoodNeutral, R.drawable.ic_face_neutral, "Neutral")
            MoodLegendIcon(MoodBien, R.drawable.ic_face_happy, "Bien")
            MoodLegendIcon(MoodEuforico, R.drawable.ic_face_euphoric, "Eufórico")
        }
    }
}

@Composable
private fun MoodLegendIcon(color: Color, icon: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TealDark
        )
    }
}
