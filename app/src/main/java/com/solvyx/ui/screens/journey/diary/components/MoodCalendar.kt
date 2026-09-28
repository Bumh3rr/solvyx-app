package com.solvyx.ui.screens.journey.diary.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.screens.journey.diary.CalendarDay
import com.solvyx.ui.screens.journey.diary.calendarDays
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_PER_WEEK = 7
private const val WAVE_STEP_MS = 14L
private const val PREVIOUS_MONTH_ICON_ROTATION = 180f
private val WeekdayInitials = listOf("L", "M", "X", "J", "V", "S", "D")
private val SpanishMexico = Locale("es", "MX")

/**
 * "Mapa de mi ánimo": one month at a time, each logged day painted with its mood color (a small
 * red dot marks use). Months slide in the direction you move, and days appear in a wave.
 */
@Composable
fun MoodCalendar(
    month: YearMonth,
    months: List<YearMonth>,
    entriesByDate: Map<LocalDate, JournalEntry>,
    today: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            MonthHeader(month = month, months = months, onMonthChange = onMonthChange)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                WeekdayInitials.forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            AnimatedContent(
                targetState = month,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(320)) { direction * it / 2 } + fadeIn(tween(320))) togetherWith
                        (slideOutHorizontally(tween(260)) { -direction * it / 2 } + fadeOut(tween(200)))
                },
                label = "MoodCalendarMonth"
            ) { shown ->
                val days = remember(shown, entriesByDate) { calendarDays(shown, entriesByDate) }
                MonthGrid(days = days, today = today, onDaySelected = onDaySelected)
            }
            Spacer(Modifier.height(10.dp))
            CalendarLegend()
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, months: List<YearMonth>, onMonthChange: (YearMonth) -> Unit) {
    val index = months.indexOf(month)
    Row(verticalAlignment = Alignment.CenterVertically) {
        MonthArrow(
            enabled = index > 0,
            rotation = PREVIOUS_MONTH_ICON_ROTATION,
            description = "Mes anterior",
            onClick = { months.getOrNull(index - 1)?.let(onMonthChange) }
        )
        AnimatedContent(
            targetState = month,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            modifier = Modifier.weight(1f),
            label = "MoodCalendarTitle"
        ) { shown ->
            Text(
                text = "${shown.month.getDisplayName(TextStyle.FULL, SpanishMexico).replaceFirstChar { it.uppercase() }} ${shown.year}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        MonthArrow(
            enabled = index in 0 until months.lastIndex,
            rotation = 0f,
            description = "Mes siguiente",
            onClick = { months.getOrNull(index + 1)?.let(onMonthChange) }
        )
    }
}

@Composable
private fun MonthArrow(enabled: Boolean, rotation: Float, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = description,
            tint = if (enabled) TealPrimary else TealLight,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation)
        )
    }
}

@Composable
private fun MonthGrid(days: List<CalendarDay?>, today: LocalDate, onDaySelected: (LocalDate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        days.chunked(DAYS_PER_WEEK).forEachIndexed { weekIndex, week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEachIndexed { dayIndex, day ->
                    Box(Modifier.weight(1f)) {
                        day?.let {
                            val waveIndex = weekIndex * DAYS_PER_WEEK + dayIndex
                            DayCell(day = it, isToday = it.date == today, waveIndex = waveIndex, onClick = onDaySelected)
                        }
                    }
                }
                // Pads the last week so its cells keep the same width as the others.
                repeat(DAYS_PER_WEEK - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, isToday: Boolean, waveIndex: Int, onClick: (LocalDate) -> Unit) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(waveIndex * WAVE_STEP_MS)
        appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
    }
    val entry = day.entry
    val moodColor = entry?.let { moodOption(it.mood).color }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                alpha = appear.value.coerceIn(0f, 1f)
                scaleX = 0.6f + 0.4f * appear.value
                scaleY = 0.6f + 0.4f * appear.value
            }
            .clip(CircleShape)
            .background(moodColor?.copy(alpha = 0.85f) ?: MaterialTheme.colorScheme.surfaceDim)
            .then(if (entry != null) Modifier.clickable(role = Role.Button) { onClick(day.date) } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isToday) TodayRing()
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (entry != null) FontWeight.ExtraBold else FontWeight.Normal),
            color = if (entry != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (entry?.consumed == true) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, CrisisRed, CircleShape)
            )
        }
    }
}

/** Today breathes gently so it is easy to find in the month. */
@Composable
private fun TodayRing() {
    val pulse by rememberInfiniteTransition(label = "TodayRing").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_100), RepeatMode.Reverse),
        label = "TodayRingAlpha"
    )
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .alpha(pulse)
            .scale(0.92f + 0.08f * pulse)
            .border(2.dp, TealDark, CircleShape)
    )
}

@Composable
private fun CalendarLegend() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, CrisisRed, CircleShape)
        )
        Text(
            "Día con consumo · toca un día para recordarlo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
