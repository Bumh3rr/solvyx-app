package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private const val StepDelayMillis = 70
private const val EnterMillis = 380

/** Delay after which every entrance with index ≤ [count] has finished. */
fun staggeredEntranceDuration(count: Int): Long = (count * StepDelayMillis + EnterMillis).toLong()

/**
 * Fades and slides a section in, [index] steps after the previous one, so the screen assembles
 * top to bottom. With [animate] false (e.g. returning from a sub-screen) it just shows.
 */
@Composable
fun StaggeredEntrance(index: Int, animate: Boolean, content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(!animate).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(EnterMillis, delayMillis = index * StepDelayMillis)) +
            slideInVertically(tween(EnterMillis, delayMillis = index * StepDelayMillis)) { it / 6 }
    ) {
        content()
    }
}
