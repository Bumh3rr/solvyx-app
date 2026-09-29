package com.solvyx.ui.screens.journey.progress.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.AnimatedCountText
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.screens.journey.progress.WeekDay
import com.solvyx.ui.screens.journey.progress.WeekView
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary
import java.time.LocalDate

private val WeekdayInitials = listOf("L", "M", "X", "J", "V", "S", "D")
private val DayCircle = 40.dp
private const val DAY_STAGGER_MS = 55L
private const val PREVIOUS_ICON_ROTATION = 180f
private const val DAYS_IN_WEEK = 7

/**
 * "Mi semana": one column per day with the mood face in its color (an empty dashed ring when the
 * day was not written, never a zero), a small mark for clean day or use, arrows to other weeks and
 * the week's numbers. Tapping a written day opens its story.
 */
@Composable
fun WeekCard(
    week: WeekView,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Mi semana", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = TealDark)
                    Text(week.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                WeekArrow(enabled = week.canGoBack, rotation = PREVIOUS_ICON_ROTATION, description = "Semana anterior", onClick = onPrevious)
                WeekArrow(enabled = week.canGoForward, rotation = 0f, description = "Semana siguiente", onClick = onNext)
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                targetState = week,
                // Same week (e.g. a new entry arrived) updates in place; another week slides.
                contentKey = { it.days.first().date },
                transitionSpec = {
                    val direction = if (targetState.days.first().date > initialState.days.first().date) 1 else -1
                    (slideInHorizontally(tween(300)) { direction * it / 2 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(250)) { -direction * it / 2 } + fadeOut(tween(200)))
                },
                label = "WeekDays"
            ) { shown ->
                Column {
                    Row(Modifier.fillMaxWidth()) {
                        shown.days.forEachIndexed { index, day ->
                            StaggeredAppear(index = index, stepMs = DAY_STAGGER_MS, modifier = Modifier.weight(1f)) {
                                DayColumn(day = day, initial = WeekdayInitials[index % DAYS_IN_WEEK], onClick = onDaySelected)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    WeekNumbers(shown)
                }
            }
            Spacer(Modifier.height(10.dp))
            WeekLegend()
        }
    }
}

@Composable
private fun WeekArrow(enabled: Boolean, rotation: Float, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = description,
            tint = if (enabled) TealPrimary else TealLight,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation)
        )
    }
}

@Composable
private fun DayColumn(day: WeekDay, initial: String, onClick: (LocalDate) -> Unit) {
    val entry = day.entry
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (entry != null) Modifier.clickable(role = Role.Button) { onClick(day.date) } else Modifier)
            .padding(vertical = 4.dp)
    ) {
        Text(
            initial,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Normal),
            color = if (day.isToday) TealDark else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        DayFace(day)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.height(16.dp), contentAlignment = Alignment.Center) {
            when {
                entry == null -> Unit
                // A plain warm dot: substance icons (e.g. the vape one) can read as "crossed out".
                entry.consumed == true -> UseDot()
                else -> Icon(
                    painterResource(R.drawable.ic_check_circle),
                    contentDescription = "Día limpio",
                    tint = TealPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun DayFace(day: WeekDay) {
    val entry = day.entry
    val ringColor = if (day.isToday) TealDark else TealLight
    when {
        entry != null -> {
            val mood = moodOption(entry.mood)
            Box(
                Modifier
                    .size(DayCircle)
                    .clip(CircleShape)
                    .background(mood.color)
                    .then(if (day.isToday) Modifier.border(2.dp, TealDark, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(mood.icon), contentDescription = mood.label, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        day.isFuture -> Box(
            Modifier
                .size(DayCircle)
                .padding(16.dp)
                .clip(CircleShape)
                .background(TealLight.copy(alpha = 0.5f))
        )
        // Not written: an empty dashed ring, clearly "no data" rather than a low mood.
        else -> Box(
            Modifier
                .size(DayCircle)
                .drawBehind {
                    val stroke = 2.dp.toPx()
                    drawCircle(
                        color = ringColor,
                        radius = size.minDimension / 2 - stroke,
                        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )
                }
        )
    }
}

@Composable
private fun WeekNumbers(week: WeekView) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberChip(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedCountText(week.summary.written, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = TealDark)
                Text("/$DAYS_IN_WEEK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("escritos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        NumberChip(modifier = Modifier.weight(1f)) {
            AnimatedCountText(week.summary.clean, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = TealPrimary)
            Text("limpios", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        NumberChip(modifier = Modifier.weight(1.3f)) {
            val topMood = week.summary.topMoodId?.let { moodOption(it) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (topMood != null) {
                    Icon(painterResource(topMood.icon), null, tint = topMood.color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    topMood?.label ?: "—",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = topMood?.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("ánimo frecuente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NumberChip(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { content() }
}

@Composable
private fun UseDot() {
    Box(
        Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(StreakFlame)
            .semantics { contentDescription = "Día con consumo" }
    )
}

@Composable
private fun WeekLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_check_circle), null, tint = TealPrimary, modifier = Modifier.size(12.dp))
        Text(" limpio   ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        UseDot()
        Text(" con consumo   ·   toca un día para recordarlo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
