package com.solvyx.ui.screens.profile.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.AnimatedCountText
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark

private const val IconTintAlpha = 0.14f

private data class StatTile(
    @DrawableRes val icon: Int,
    val tint: Color,
    val value: Int,
    val label: String,
    val onClick: () -> Unit
)

/** Three tappable stats whose numbers count up; each one opens where that number comes from. */
@Composable
fun ProfileStatsRow(
    streak: Int,
    bestStreak: Int,
    completedAssessments: Int,
    onOpenJourney: () -> Unit,
    onOpenAssessment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val tiles = listOf(
        StatTile(R.drawable.ic_flame, StreakFlame, streak, "Días de racha", onOpenJourney),
        StatTile(R.drawable.ic_trophy, primary, bestStreak, "Mejor racha", onOpenJourney),
        StatTile(R.drawable.ic_clipboard, primary, completedAssessments, "Diagnósticos", onOpenAssessment)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tiles.forEach { tile ->
            StatTileCard(
                tile = tile,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun StatTileCard(tile: StatTile, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier, onClick = tile.onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(tile.tint.copy(alpha = IconTintAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(tile.icon),
                    contentDescription = null,
                    tint = tile.tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            AnimatedCountText(
                value = tile.value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                text = tile.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
