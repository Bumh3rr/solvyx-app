package com.solvyx.ui.screens.firstaid.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.components.colors
import com.solvyx.ui.screens.firstaid.model.FirstAidRoute
import com.solvyx.ui.screens.firstaid.model.FirstAidSituation

private const val GridColumns = 2

/** "Elige tu situación": two-column grid of situation cards of equal height per row. */
@Composable
fun SituationGrid(
    situations: List<FirstAidSituation>,
    onOpen: (FirstAidRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Elige tu situación",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            situations.chunked(GridColumns).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { situation ->
                        SituationCard(
                            situation = situation,
                            onClick = { onOpen(situation.route) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    repeat(GridColumns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun SituationCard(
    situation: FirstAidSituation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = situation.tone.colors()
    SolvyxCard(modifier = modifier, containerColor = colors.container, onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            ToneIconBadge(icon = situation.icon, tone = situation.tone)
            Spacer(Modifier.height(12.dp))
            Text(
                text = situation.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.content
            )
            Text(
                text = situation.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
