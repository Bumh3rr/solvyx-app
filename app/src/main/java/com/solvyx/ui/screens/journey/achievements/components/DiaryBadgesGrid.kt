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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.screens.journey.UiAchievement
import com.solvyx.ui.screens.journey.achievements.DiaryBadge
import com.solvyx.ui.theme.TealDark

private const val BADGES_PER_ROW = 3
private const val BADGE_STAGGER_MS = 60L

/** Diary badges in rows of three: medal, name and "3/5" while it is still locked. */
@Composable
fun DiaryBadgesGrid(badges: List<DiaryBadge>, onSelect: (DiaryBadge) -> Unit, modifier: Modifier = Modifier) {
    MedalTilesGrid(badges, modifier) { badge ->
        MedalTile(
            title = badge.title,
            icon = badge.icon,
            color = DiaryBadgeColor,
            unlocked = badge.unlocked,
            progress = badge.progress,
            counter = "${badge.current}/${badge.target}",
            onClick = { onSelect(badge) }
        )
    }
}

/** Goal medals (1, 5 and 10 goals completed), same tiles as the diary badges. */
@Composable
fun GoalMedalsGrid(
    medals: List<UiAchievement>,
    completedGoals: Int,
    onSelect: (UiAchievement) -> Unit,
    modifier: Modifier = Modifier
) {
    val tiers = medals.sortedBy { it.threshold }
    MedalTilesGrid(tiers, modifier) { medal ->
        MedalTile(
            title = medal.title,
            icon = medal.icon,
            color = goalTierColor(tiers.indexOf(medal)),
            unlocked = medal.unlocked,
            progress = medal.progress,
            counter = "${completedGoals.coerceAtMost(medal.threshold)}/${medal.threshold}",
            onClick = { onSelect(medal) }
        )
    }
}

@Composable
private fun <T> MedalTilesGrid(items: List<T>, modifier: Modifier, tile: @Composable (T) -> Unit) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(BADGES_PER_ROW).forEachIndexed { row, rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEachIndexed { column, item ->
                    StaggeredAppear(
                        index = row * BADGES_PER_ROW + column,
                        stepMs = BADGE_STAGGER_MS,
                        modifier = Modifier.weight(1f)
                    ) {
                        tile(item)
                    }
                }
                repeat(BADGES_PER_ROW - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MedalTile(
    title: String,
    icon: Int,
    color: Color,
    unlocked: Boolean,
    progress: Float,
    counter: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Medal(
            icon = icon,
            color = color,
            unlocked = unlocked,
            progress = progress,
            size = 64.dp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = if (unlocked) TealDark else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        if (!unlocked) {
            Text(
                counter,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
