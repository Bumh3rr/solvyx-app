package com.solvyx.ui.screens.journey.diary.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.MoodOption
import com.solvyx.ui.components.common.MoodOptions
import com.solvyx.ui.screens.journey.diary.UseFilter
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary

private const val SELECTED_MOOD_SCALE = 1.15f
private const val DIMMED_MOOD_ALPHA = 0.35f

/** Use filter chips plus the five moods; both combine ("días sin consumo" + "bien"). */
@Composable
fun DiaryFilters(
    useFilter: UseFilter,
    moodFilter: String?,
    onUseFilter: (UseFilter) -> Unit,
    onMood: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UseFilter.entries.forEach { filter ->
                FilterChip(label = filter.label, selected = filter == useFilter, onClick = { onUseFilter(filter) })
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MoodOptions.forEach { mood ->
                MoodToggle(
                    mood = mood,
                    selected = mood.id == moodFilter,
                    dimmed = moodFilter != null && mood.id != moodFilter,
                    onClick = { onMood(mood.id) }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(if (selected) TealPrimary else Color.Transparent, label = "ChipBg")
    val content by animateColorAsState(if (selected) Color.White else TealDark, label = "ChipText")
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(1.dp, if (selected) TealPrimary else TealLight, RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun MoodToggle(mood: MoodOption, selected: Boolean, dimmed: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) SELECTED_MOOD_SCALE else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "MoodScale"
    )
    val alpha by animateFloatAsState(if (dimmed) DIMMED_MOOD_ALPHA else 1f, label = "MoodAlpha")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(44.dp)
                .clip(CircleShape)
                .background(mood.color.copy(alpha = if (selected) 1f else 0.15f * alpha + 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(mood.icon),
                contentDescription = mood.label,
                tint = if (selected) Color.White else mood.color.copy(alpha = alpha),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            mood.label,
            style = MaterialTheme.typography.labelSmall,
            color = TealDark.copy(alpha = alpha),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
