package com.solvyx.ui.screens.directory.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.directory.components.DirectoryTransition
import com.solvyx.ui.screens.directory.model.DirectoryEntry

/** Detail of one provider. Orchestrates header, actions, info and Berto's call tip. */
@Composable
fun DirectoryDetailScreen(
    entry: DirectoryEntry,
    isOnline: Boolean,
    transition: DirectoryTransition,
    onBack: () -> Unit,
    onPracticeWithBerto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = entry.category.singularLabel,
            navigationButton = { SolvyxBackButton(onClick = onBack) }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailHeader(entry = entry, transition = transition)
            DetailActions(entry = entry, isOnline = isOnline)
            DetailInfoCard(entry = entry)
            CallScriptCard(category = entry.category, onPracticeWithBerto = onPracticeWithBerto)
        }
    }
}
