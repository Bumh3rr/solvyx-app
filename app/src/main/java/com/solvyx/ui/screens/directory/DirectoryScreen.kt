@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.solvyx.ui.screens.directory

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.ui.screens.directory.components.DirectoryTransition
import com.solvyx.ui.screens.directory.data.DirectoryData
import com.solvyx.ui.screens.directory.detail.DirectoryDetailScreen
import com.solvyx.ui.screens.directory.hub.DirectoryHubScreen

private const val EnterMillis = 350
private const val ExitMillis = 250

/**
 * Directorio profesional: list ↔ detail with a shared-element transition. The open entry is kept
 * by id in saved state, so rotation keeps it open and the system back button closes it (instead of
 * leaving the whole section).
 */
@Composable
fun DirectoryScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DirectoryViewModel = hiltViewModel()
) {
    var openEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    BackHandler(enabled = openEntryId != null) { openEntryId = null }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = openEntryId?.let(DirectoryData::findById),
            transitionSpec = { fadeIn(tween(EnterMillis)) togetherWith fadeOut(tween(ExitMillis)) },
            label = "directoryListDetail"
        ) { entry ->
            val transition = DirectoryTransition(this@SharedTransitionLayout, this@AnimatedContent)
            if (entry == null) {
                DirectoryHubScreen(
                    viewModel = viewModel,
                    listState = listState,
                    transition = transition,
                    onOpenDrawer = onOpenDrawer,
                    onOpenEntry = { openEntryId = it.id }
                )
            } else {
                DirectoryDetailScreen(
                    entry = entry,
                    isOnline = isOnline,
                    transition = transition,
                    onBack = { openEntryId = null },
                    onPracticeWithBerto = onNavigateToChat
                )
            }
        }
    }
}
