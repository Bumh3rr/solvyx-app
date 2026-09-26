package com.solvyx.ui.screens.directory.hub

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.directory.components.CategoryIconBadge
import com.solvyx.ui.screens.directory.components.DirectoryTransition
import com.solvyx.ui.screens.directory.components.EntryFactPills
import com.solvyx.ui.screens.directory.components.RoundCallButton
import com.solvyx.ui.screens.directory.components.SharedIcon
import com.solvyx.ui.screens.directory.components.SharedName
import com.solvyx.ui.screens.directory.components.sharedEntryBounds
import com.solvyx.ui.screens.directory.components.sharedEntryElement
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import com.solvyx.ui.theme.TealDark

/**
 * Compact list card: what it is, why it may fit (specialty or short description) and the facts
 * that matter to decide (cost, availability). Calling is one tap; tapping the card opens the detail.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectoryEntryCard(
    entry: DirectoryEntry,
    transition: DirectoryTransition,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(
        modifier = modifier
            .fillMaxWidth()
            .sharedEntryBounds(transition, entry.id),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            CategoryIconBadge(
                category = entry.category,
                modifier = Modifier.sharedEntryElement(transition, entry.id, SharedIcon)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    modifier = Modifier.sharedEntryElement(transition, entry.id, SharedName),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.specialty ?: entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EntryFactPills(entry)
                }
            }
            Spacer(Modifier.width(8.dp))
            RoundCallButton(phone = entry.phone, contactName = entry.name)
        }
    }
}
