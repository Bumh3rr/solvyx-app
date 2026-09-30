package com.solvyx.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.backend.common.goals.GoalDay
import com.solvyx.backend.common.goals.GoalProgressCalculator
import com.solvyx.backend.common.goals.GoalWeek
import com.solvyx.backend.common.goals.GoalWeekStatus
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary
import java.time.LocalDate

private const val DAYS_IN_WEEK = 7
private val WeekdayInitials = listOf("L", "M", "X", "J", "V", "S", "D")

/**
 * A goal's detail: its progress, which days or weeks added up (no red, no crosses: a day that
 * didn't add is just "no sumó") and "Archivar meta" behind a confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailSheet(
    card: GoalCardUi,
    journal: List<JournalEntry>,
    onArchive: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmArchive by rememberSaveable { mutableStateOf(false) }
    val goal = card.goal
    val today = LocalDate.now()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCapsule(goalIcon(goal.type))
                Spacer(Modifier.width(12.dp))
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                "${originLabel(goal)} · ${startLabel(goal)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(20.dp))
            GoalProgressBar(card.fraction)
            Spacer(Modifier.height(8.dp))
            GoalProgressTexts(card)

            Spacer(Modifier.height(24.dp))
            when (goal.type) {
                GoalType.SIN_CONSUMO -> {
                    val days = remember(goal, journal) { GoalProgressCalculator.dayBreakdown(goal, journal, today) }
                    DetailTitle("Día por día")
                    DayGrid(days, today)
                }
                GoalType.REDUCIR_FRECUENCIA -> {
                    val weeks = remember(goal, journal) { GoalProgressCalculator.weekBreakdown(goal, journal, today) }
                    DetailTitle("Semana por semana")
                    WeekList(weeks, weeklyLimit = goal.weeklyLimit ?: 0)
                }
            }
            Text(
                ruleLabel(goal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )

            SolvyxOutlinedButton(
                text = "Archivar meta",
                onClick = { confirmArchive = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            )
        }
    }

    if (confirmArchive) {
        ArchiveGoalDialog(
            onConfirm = {
                confirmArchive = false
                onArchive()
            },
            onDismiss = { confirmArchive = false }
        )
    }
}

@Composable
private fun DetailTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

/** Days since the start in rows of 7 (the same 7-day blocks as the weeks), with a legend. */
@Composable
private fun DayGrid(days: List<GoalDay>, today: LocalDate) {
    if (days.isEmpty()) return
    val firstWeekday = days.first().date.dayOfWeek.value - 1
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                repeat(DAYS_IN_WEEK) { index ->
                    Text(
                        WeekdayInitials[(firstWeekday + index) % DAYS_IN_WEEK],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            days.chunked(DAYS_IN_WEEK).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { day ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            DayDot(day, isToday = day.date == today)
                        }
                    }
                    repeat(DAYS_IN_WEEK - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(filled = true, text = "Sumó")
                LegendItem(filled = false, text = "No sumó")
            }
        }
    }
}

@Composable
private fun DayDot(day: GoalDay, isToday: Boolean) {
    val shape = CircleShape
    val base = Modifier.size(34.dp)
    val styled = when {
        day.counted -> base.background(TealPrimary, shape)
        else -> base.border(1.5.dp, TealLight, shape)
    }.then(if (isToday) Modifier.border(2.dp, TealDark, shape) else Modifier)
    Box(styled, contentAlignment = Alignment.Center) {
        Text(
            day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isToday || day.counted) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (day.counted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(filled: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .then(
                    if (filled) Modifier.background(TealPrimary, CircleShape)
                    else Modifier.border(1.5.dp, TealLight, CircleShape)
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeekList(weeks: List<GoalWeek>, weeklyLimit: Int) {
    if (weeks.isEmpty()) return
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Semana ${week.number}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            weekRangeLabel(week),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        weekStatusLabel(week, weeklyLimit),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (week.status == GoalWeekStatus.COUNTED) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (week.status == GoalWeekStatus.COUNTED) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveGoalDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        title = {
            Text(
                "¿Archivar esta meta?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
        },
        text = {
            Text(
                "Dejará de contar y liberará un espacio.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Archivar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = TealDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = TealDark)
            }
        }
    )
}
