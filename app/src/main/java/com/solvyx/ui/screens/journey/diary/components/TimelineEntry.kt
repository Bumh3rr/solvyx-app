package com.solvyx.ui.screens.journey.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.common.goals.DayGoalNote
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.components.common.substanceLabel
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary
import java.time.format.TextStyle
import java.util.Locale

private val SpanishMexico = Locale("es", "MX")
private val DateColumnWidth = 44.dp
private val ThreadWidth = 3.dp
private val DotSize = 14.dp
private val DotTop = 22.dp
private val CardShape = RoundedCornerShape(18.dp)

/**
 * One day of the timeline: the date, a thread that joins the days (colored by mood) and a card
 * with the mood, use and the note. The thread starts at the first day and ends at the last one.
 */
@Composable
fun TimelineEntry(
    entry: JournalEntry,
    // Whether this day added to a goal or completed one; null when it touched none.
    goalNote: DayGoalNote?,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mood = moodOption(entry.mood)
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        DateColumn(entry)
        Thread(color = mood.color, isFirst = isFirst, isLast = isLast)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp)
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, mood.color.copy(alpha = 0.35f), CardShape)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(mood.icon), null, tint = mood.color, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    mood.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark,
                    modifier = Modifier.weight(1f)
                )
                UseBadge(entry)
            }
            entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    "“$note”",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val goalText = when {
                goalNote == null -> null
                goalNote.completedTitles.isNotEmpty() -> "Cumpliste una meta"
                goalNote.advanced -> "Avanzaste en tu meta"
                else -> null
            }
            goalText?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_check_circle), null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it, style = MaterialTheme.typography.labelSmall, color = TealPrimary)
                }
            }
        }
    }
}

@Composable
private fun DateColumn(entry: JournalEntry) {
    Column(
        modifier = Modifier
            .width(DateColumnWidth)
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            entry.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
        Text(
            entry.date.dayOfWeek.getDisplayName(TextStyle.SHORT, SpanishMexico).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Vertical thread with the day's dot; cut above the first day and below the last one. */
@Composable
private fun Thread(color: Color, isFirst: Boolean, isLast: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .fillMaxHeight()
            .drawBehind {
                val x = size.width / 2
                val dotCenter = DotTop.toPx() + DotSize.toPx() / 2
                val top = if (isFirst) dotCenter else 0f
                val bottom = if (isLast) dotCenter else size.height
                drawLine(color.copy(alpha = 0.35f), Offset(x, top), Offset(x, bottom), strokeWidth = ThreadWidth.toPx())
            }
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = DotTop)
                .size(DotSize)
                .clip(CircleShape)
                .background(color)
                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
        )
    }
}

@Composable
private fun UseBadge(entry: JournalEntry) {
    val used = entry.consumed == true
    val text = if (used) entry.substance?.let(::substanceLabel) ?: "Con consumo" else "Día limpio"
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = if (used) CrisisRedDark else TealPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (used) CrisisRedLight else TealPrimary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
