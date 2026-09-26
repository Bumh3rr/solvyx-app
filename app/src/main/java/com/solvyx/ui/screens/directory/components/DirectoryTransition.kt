@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.solvyx.ui.screens.directory.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val EnterMillis = 350
private const val ExitMillis = 250
private val CardCornerRadius = 20.dp

/**
 * Scopes for the list → detail shared transition, bundled so screens pass one value instead of
 * two. Keys are per entry id: the card morphs into the detail header, and the name and icon fly
 * between them.
 */
class DirectoryTransition(
    val sharedScope: SharedTransitionScope,
    val visibilityScope: AnimatedVisibilityScope
)

@Composable
fun Modifier.sharedEntryBounds(
    transition: DirectoryTransition,
    entryId: String
): Modifier = with(transition.sharedScope) {
    sharedBounds(
        sharedContentState = rememberSharedContentState(key = "card_$entryId"),
        animatedVisibilityScope = transition.visibilityScope,
        enter = fadeIn(tween(EnterMillis)),
        exit = fadeOut(tween(ExitMillis)),
        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(CardCornerRadius))
    )
}

@Composable
fun Modifier.sharedEntryElement(
    transition: DirectoryTransition,
    entryId: String,
    part: String
): Modifier = with(transition.sharedScope) {
    sharedElement(
        sharedContentState = rememberSharedContentState(key = "${part}_$entryId"),
        animatedVisibilityScope = transition.visibilityScope
    )
}

const val SharedName = "name"
const val SharedIcon = "icon"
