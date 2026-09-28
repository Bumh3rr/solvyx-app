package com.solvyx.ui.screens.journey.achievements.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.screens.journey.achievements.DiaryBadge
import com.solvyx.ui.theme.TealDark

private const val BADGES_PER_ROW = 3
private const val BADGE_STAGGER_MS = 60L

/** Diary badges in rows of three: medal, name and "3/5" while it is still locked. */
@Composable
fun DiaryBadgesGrid(badges: List<DiaryBadge>, onSelect: (DiaryBadge) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        badges.chunked(BADGES_PER_ROW).forEachIndexed { row, rowBadges ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowBadges.forEachIndexed { column, badge ->
                    StaggeredAppear(
                        index = row * BADGES_PER_ROW + column,
                        stepMs = BADGE_STAGGER_MS,
                        modifier = Modifier.weight(1f)
                    ) {
                        BadgeTile(badge = badge, onClick = { onSelect(badge) })
                    }
                }
                repeat(BADGES_PER_ROW - rowBadges.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BadgeTile(badge: DiaryBadge, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Medal(
            icon = badge.icon,
            color = DiaryBadgeColor,
            unlocked = badge.unlocked,
            progress = badge.progress,
            size = 64.dp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            badge.title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = if (badge.unlocked) TealDark else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        if (!badge.unlocked) {
            Text(
                "${badge.current}/${badge.target}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DiaryBadgeColor
            )
        }
    }
}
