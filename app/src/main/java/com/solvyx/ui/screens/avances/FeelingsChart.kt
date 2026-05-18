package com.solvyx.ui.screens.avances

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary

@Composable
fun FeelingsChart(
    data: List<Float>,  // bienestar values 0-10
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
                // Y-axis labels
                drawContext.canvas.nativeCanvas.drawText(
                    v.toInt().toString(),
                    leftPad - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    guidePaint
                )
            }

            // ── Helper: map data index to x coordinate ────────────────────
            val n = data.size
            fun xAt(i: Int): Float =
                leftPad + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartWidth

            // ── Helper: map value (0-10) to y coordinate ─────────────────
            fun yAt(value: Float): Float =
                topPad + chartHeight - (value / 10f) * chartHeight

            // ── Build smooth Path using cubic bezier ──────────────────────
            fun buildSmoothPath(values: List<Float>): Path {
                val path = Path()
                if (values.isEmpty()) return path
                path.moveTo(xAt(0), yAt(values[0]))
                for (i in 0 until values.size - 1) {
                    val x0 = xAt(i)
                    val y0 = yAt(values[i])
                    val x1 = xAt(i + 1)
                    val y1 = yAt(values[i + 1])
                    val midX = (x0 + x1) / 2f
                    path.cubicTo(midX, y0, midX, y1, x1, y1)
                }
                return path
            }

            // ── Draw BIENESTAR line (solid, TealPrimary) ───────────────────
            val bienestarValues = data
            val bienestarPath   = buildSmoothPath(bienestarValues)
            drawPath(
                path        = bienestarPath,
                color       = TealPrimary,
                style       = Stroke(
                    width   = 2.dp.toPx(),
                    cap     = StrokeCap.Round
                )
            )

            // ── Draw data points for bienestar ─────────────────────────────
            bienestarValues.forEachIndexed { i, v ->
                drawCircle(
                    color  = TealPrimary,
                    radius = 3.dp.toPx(),
                    center = Offset(xAt(i), yAt(v))
                )
                drawCircle(
                    color  = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = Offset(xAt(i), yAt(v))
                )
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

        // ── Legend ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bienestar dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(TealPrimary)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text  = "Bienestar",
                style = MaterialTheme.typography.labelSmall,
                color = TealDark
            )
        }
    }
}
