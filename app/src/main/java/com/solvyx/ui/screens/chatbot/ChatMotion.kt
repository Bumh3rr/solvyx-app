package com.solvyx.ui.screens.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Berto's bubbles start after his avatar; cards under them line up with the bubble, not the avatar. */
internal val BertoBubbleIndent = 38.dp

private const val DEFAULT_STAGGER_MS = 70L

/** Pop-in used by chips, cards and tiles: fade, a little bounce and a short rise. */
internal val PopInTransition: EnterTransition =
    fadeIn(tween(220)) +
        scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.85f) +
        slideInVertically { it / 4 }

/** Shows [content] after `index × stepMs`, so a group of items appears one after another. */
@Composable
internal fun StaggeredAppear(
    index: Int,
    modifier: Modifier = Modifier,
    stepMs: Long = DEFAULT_STAGGER_MS,
    enter: EnterTransition = PopInTransition,
    // For items in a row of equal-height cards (the row measures IntrinsicSize.Min).
    fillHeight: Boolean = false,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * stepMs)
        visible = true
    }
    Box(modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = enter,
            modifier = if (fillHeight) Modifier.fillMaxHeight() else Modifier
        ) { content() }
    }
}
