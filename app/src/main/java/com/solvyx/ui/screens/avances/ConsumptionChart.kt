package com.solvyx.ui.screens.avances

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary

@Composable
fun ConsumptionChart(
    data: List<Float>,       // 0 = no use, 1 = light, 2 = heavy
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val bottomPad = 24.dp.toPx()
        val topPad    = 8.dp.toPx()
        val sidePad   = 8.dp.toPx()

        val chartWidth  = size.width - sidePad * 2
        val chartHeight = size.height - topPad - bottomPad

        val n         = data.size
        val barSlot   = chartWidth / n
        val barWidth  = (barSlot * 0.55f).coerceAtLeast(4.dp.toPx())
        val gap       = barSlot - barWidth

        // Determine label step to avoid crowding
        val labelStep = when {
            n <= 7  -> 1
            n <= 14 -> 2
            else    -> 7
        }

        val labelPaint = Paint().apply {
            textSize    = 8.5.sp.toPx()
            isAntiAlias = true
            color       = TealMedium.copy(alpha = 0.85f).toArgb()
            textAlign   = Paint.Align.CENTER
        }

        data.forEachIndexed { i, value ->
            val slotStart = sidePad + i * barSlot
            val barLeft   = slotStart + gap / 2f

            if (value > 0f) {
                val heightFraction = (value / 2f).coerceIn(0.15f, 1f)
                val barH           = chartHeight * heightFraction
                val barTop         = topPad + chartHeight - barH

                val barColor = if (value >= 2f) CrisisRed else TealMedium

                drawRoundRect(
                    color        = barColor,
                    topLeft      = Offset(barLeft, barTop),
                    size         = Size(barWidth, barH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Small indicator dot above each bar
                drawCircle(
                    color  = TealPrimary,
                    radius = 3.dp.toPx(),
                    center = Offset(barLeft + barWidth / 2f, barTop - 5.dp.toPx())
                )
            } else {
                // Draw a thin zero-line for empty days
                drawLine(
                    color       = TealMedium.copy(alpha = 0.15f),
                    start       = Offset(barLeft + barWidth / 2f, topPad + chartHeight - 2.dp.toPx()),
                    end         = Offset(barLeft + barWidth / 2f, topPad + chartHeight),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // X-axis label
            if (i % labelStep == 0 && i < labels.size) {
                val labelX = slotStart + barSlot / 2f
                val labelY = size.height - 6.dp.toPx()
                drawContext.canvas.nativeCanvas.drawText(
                    labels[i],
                    labelX,
                    labelY,
                    labelPaint
                )
            }
        }

        // Bottom axis line
        drawLine(
            color       = TealMedium.copy(alpha = 0.2f),
            start       = Offset(sidePad, topPad + chartHeight),
            end         = Offset(size.width - sidePad, topPad + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}
