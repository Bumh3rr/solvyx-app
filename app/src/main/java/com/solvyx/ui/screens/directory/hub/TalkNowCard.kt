package com.solvyx.ui.screens.directory.hub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.formatMexicanPhone
import com.solvyx.ui.screens.directory.components.RoundCallButton
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import com.solvyx.ui.theme.TealDark

private const val SecondaryTextAlpha = 0.85f
private const val DividerAlpha = 0.25f

/**
 * "¿Necesitas hablar ya?" — the lines that answer 24/7, pinned above the list so the fastest
 * path to a human never depends on searching or filtering.
 */
@Composable
fun TalkNowCard(lines: List<DirectoryEntry>, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.primary) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "¿Necesitas hablar ya?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text(
                text = "Estas líneas contestan las 24 horas, sin costo y de forma confidencial.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = SecondaryTextAlpha)
            )
            Spacer(Modifier.height(8.dp))
            lines.forEachIndexed { index, line ->
                if (index > 0) HorizontalDivider(color = Color.White.copy(alpha = DividerAlpha))
                TalkNowRow(line)
            }
        }
    }
}

@Composable
private fun TalkNowRow(line: DirectoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = line.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = formatMexicanPhone(line.phone),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = SecondaryTextAlpha)
            )
        }
        Spacer(Modifier.width(12.dp))
        RoundCallButton(phone = line.phone, contactName = line.name, containerColor = TealDark)
    }
}
