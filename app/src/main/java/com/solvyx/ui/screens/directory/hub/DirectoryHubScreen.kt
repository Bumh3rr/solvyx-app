package com.solvyx.ui.screens.directory.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxMenuButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.directory.DirectoryViewModel
import com.solvyx.ui.screens.directory.components.DirectoryTransition
import com.solvyx.ui.screens.directory.model.DirectoryEntry

/**
 * List side of the directory. Orchestrates hero, "hablar ahora", filters and results; each piece
 * lives in its own file under `hub/`. With no category selected, results are grouped by category
 * (lines first: they're the quickest way to talk to someone).
 */
@Composable
fun DirectoryHubScreen(
    viewModel: DirectoryViewModel,
    listState: LazyListState,
    transition: DirectoryTransition,
    onOpenDrawer: () -> Unit,
    onOpenEntry: (DirectoryEntry) -> Unit
) {
    val filter = viewModel.filter
    val results = viewModel.results
    val sections = remember(results, filter.category) {
        if (filter.category != null) listOf(null to results)
        else results.groupBy { it.category }.toSortedMap().map { (category, entries) -> category to entries }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = "Directorio profesional",
            navigationButton = { SolvyxMenuButton(onClick = onOpenDrawer) }
        )
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") { DirectoryHero() }
            if (filter.query.isBlank()) {
                item(key = "talkNow") { TalkNowCard(lines = viewModel.availableNow) }
            }
            item(key = "filters") {
                DirectoryFilters(
                    filter = filter,
                    onQueryChange = viewModel::onQueryChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onToggleOnlyFree = viewModel::onToggleOnlyFree,
                    resultCount = results.size
                )
            }
            if (results.isEmpty()) {
                item(key = "empty") { DirectoryEmptyState(onClearFilters = viewModel::clearFilters) }
            }
            sections.forEach { (category, entries) ->
                category?.let {
                    item(key = "section_${it.name}") {
                        Text(
                            text = it.sectionTitle,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(entries, key = { it.id }) { entry ->
                    DirectoryEntryCard(
                        entry = entry,
                        transition = transition,
                        onOpen = { onOpenEntry(entry) }
                    )
                }
            }
        }
    }
}
