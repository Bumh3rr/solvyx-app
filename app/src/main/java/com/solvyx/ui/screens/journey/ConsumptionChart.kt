package com.solvyx.ui.screens.journey

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium

@Composable
fun ConsumptionChart(
    data: List<Float>,       // -1 = no entry that day, 0 = clean day, 1 = with consumption
    labels: List<String>,
    modifier: Modifier = Modifier,
    onPointSelected: ((Int) -> Unit)? = null
) {
    if (data.isEmpty()) return

    val density = LocalDensity.current
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .then(
                    if (onPointSelected == null) Modifier
                    else Modifier.pointerInput(data.size) {
                        detectTapGestures { offset ->
                            val sidePad = with(density) { 8.dp.toPx() }
                            val n = data.size
                            val chartWidth = size.width - sidePad * 2
                            if (n <= 0 || chartWidth <= 0f) return@detectTapGestures
                            val barSlot = chartWidth / n
                            val index = (((offset.x - sidePad) / barSlot).toInt()).coerceIn(0, n - 1)
                            onPointSelected(index)
                        }
                    }
                )
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

                val dotCenter = Offset(barLeft + barWidth / 2f, topPad + chartHeight - 6.dp.toPx())

                when {
                    value > 0f -> {
                        val heightFraction = (value / 2f).coerceIn(0.15f, 1f)
                        val barH           = chartHeight * heightFraction
                        val barTop         = topPad + chartHeight - barH

                        drawRoundRect(
                            color        = StreakFlame,
                            topLeft      = Offset(barLeft, barTop),
                            size         = Size(barWidth, barH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                    value == 0f -> {
                        // Clean day — an entry exists, no consumption. Celebrates the day, same
                        // green as the wellbeing chart's "Bien" mood.
                        drawCircle(color = MoodBien, radius = 4.dp.toPx(), center = dotCenter)
                    }
                    else -> {
                        // No entry at all that day — same dim, neutral treatment as the
                        // wellbeing chart's "no check-in" days. Doesn't claim the day was clean;
                        // there's simply no data for it.
                        drawCircle(color = TealMedium.copy(alpha = 0.3f), radius = 3.dp.toPx(), center = dotCenter)
                    }
                }

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

            drawLine(
                color       = TealMedium.copy(alpha = 0.2f),
                start       = Offset(sidePad, topPad + chartHeight),
                end         = Offset(size.width - sidePad, topPad + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        // ── Legend ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ConsumptionLegendDot(MoodBien, "Día limpio")
            ConsumptionLegendDot(StreakFlame, "Día con consumo")
            ConsumptionLegendDot(TealMedium.copy(alpha = 0.3f), "Sin registro")
        }
    }
}

@Composable
private fun ConsumptionLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TealDark
        )
    }
}
