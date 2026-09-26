package com.solvyx.ui.screens.directory.detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.directory.components.InfoPill
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import com.solvyx.ui.theme.TealDark

private data class InfoLine(@DrawableRes val icon: Int, val label: String, val value: String)

private fun DirectoryEntry.infoLines(): List<InfoLine> = listOfNotNull(
    specialty?.let { InfoLine(R.drawable.ic_user_check, "Especialidad", it) },
    schedule?.let { InfoLine(R.drawable.ic_clock, "Horario", it) },
    appointment?.let { InfoLine(R.drawable.ic_clock, "Citas", it) },
    InfoLine(R.drawable.ic_tag, "Costo", cost ?: if (isFree) "Sin costo" else "Pregunta al llamar"),
    address?.let { InfoLine(R.drawable.ic_map_pin, "Dirección", it) }
)

/** Everything to know before going: labeled lines plus the entry's topics. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailInfoCard(entry: DirectoryEntry, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            entry.infoLines().forEach { InfoLineRow(it) }
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.tags.forEach { InfoPill(it) }
                }
            }
        }
    }
}

@Composable
private fun InfoLineRow(line: InfoLine) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            painter = painterResource(line.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = line.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = line.value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TealDark
            )
        }
    }
}
