package com.solvyx.ui.screens.directory.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.directory.components.CategoryIconBadge
import com.solvyx.ui.screens.directory.components.DirectoryTransition
import com.solvyx.ui.screens.directory.components.EntryFactPills
import com.solvyx.ui.screens.directory.components.SharedIcon
import com.solvyx.ui.screens.directory.components.SharedName
import com.solvyx.ui.screens.directory.components.accent
import com.solvyx.ui.screens.directory.components.sharedEntryBounds
import com.solvyx.ui.screens.directory.components.sharedEntryElement
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import com.solvyx.ui.theme.TealDark

/** Who they are: the list card morphs into this header (shared bounds + name + icon). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailHeader(entry: DirectoryEntry, transition: DirectoryTransition, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier.fillMaxWidth().sharedEntryBounds(transition, entry.id)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    category = entry.category,
                    size = 56.dp,
                    modifier = Modifier.sharedEntryElement(transition, entry.id, SharedIcon)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = entry.category.singularLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = entry.category.accent
                    )
                    Text(
                        text = entry.name,
                        modifier = Modifier.sharedEntryElement(transition, entry.id, SharedName),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EntryFactPills(entry)
            }
        }
    }
}
