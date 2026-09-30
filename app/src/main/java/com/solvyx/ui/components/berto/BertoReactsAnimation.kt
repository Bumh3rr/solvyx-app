package com.solvyx.ui.components.berto

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.rive.Result
import app.rive.Rive
import app.rive.RivePointerInputMode
import app.rive.RiveFileSource
import app.rive.rememberRiveFile
import app.rive.rememberRiveWorker
import app.rive.rememberViewModelInstance
import com.solvyx.R

private const val TAG = "BertoReactsAnimation"
private const val IS_READING = "isReading"
private const val LOOK_X = "lookX"
private const val LOOK_X_CENTER = 0.5f
private const val LOOK_X_SCALE = 100f

/**
 * One-shot reactions of `reacts_berto.riv`, fired as ViewModel triggers. [riveName] is the exact
 * trigger name in `ViewModel1`; each reaction plays once and the state machine returns to Idle.
 */
enum class BertoReactTrigger(val riveName: String) {
    SAD("triggerSad"),
    ANXIOUS("triggerAnxious"),
    NEUTRAL("triggerNeutral"),
    GOOD("triggerGood"),
    EUPHORIC("triggerEuphoric");

    companion object {
        /** Reaction for a journal mood id ("triste", "ansioso"…); `null` for unknown ids. */
        fun forMood(moodId: String?): BertoReactTrigger? = when (moodId) {
            "triste" -> SAD
            "ansioso" -> ANXIOUS
            "neutral" -> NEUTRAL
            "bien" -> GOOD
            "euforico" -> EUPHORIC
            else -> null
        }
    }
}

/**
 * Berto from `reacts_berto.riv`: greets on load, plays a [reaction] every time it (or
 * [reactionKey]) changes — the key replays the same reaction, e.g. on two days with the same mood —, and while [isReading] lowers his head and follows [lookX] (0 = left edge of the text, 1 =
 * right edge) with his eyes. See "Berto — Guía animación Reading" in the vault for the Rive side.
 */
@Composable
fun BertoReactsAnimation(
    reaction: BertoReactTrigger?,
    isReading: Boolean,
    lookX: Float?,
    modifier: Modifier = Modifier,
    reactionKey: Any? = null,
    @DrawableRes fallback: Int = R.drawable.berto_saludando
) {
    BertoRiveHost(
        riveRes = R.raw.reacts_berto,
        trigger = reaction?.riveName,
        triggerKey = reactionKey,
        isReading = isReading,
        lookX = lookX,
        modifier = modifier,
        fallback = fallback
    )
}

/**
 * Shared player for Berto's `.riv` files that follow the reacts rig (`ViewModel1` with triggers,
 * `isReading` and `lookX`). Fires [trigger] each time it or [triggerKey] changes. `lookX` is
 * smoothed here, so the jump back to 0 on a new line glides instead of snapping.
 * [initialBooleans] are set before the first frame, so the transitions out of Entry already see
 * them. [pointerInputMode] is PassThrough where Berto sits inside a scrolling or tappable parent.
 */
@Composable
internal fun BertoRiveHost(
    @RawRes riveRes: Int,
    trigger: String?,
    triggerKey: Any?,
    isReading: Boolean,
    lookX: Float?,
    modifier: Modifier,
    @DrawableRes fallback: Int,
    initialBooleans: Map<String, Boolean> = emptyMap(),
    pointerInputMode: RivePointerInputMode = RivePointerInputMode.Consume
) {
    val riveWorker = rememberRiveWorker()
    when (val riveFileResult = rememberRiveFile(RiveFileSource.RawRes.from(riveRes), riveWorker)) {
        is Result.Loading -> BertoRiveFallback(fallback, modifier)
        is Result.Error -> {
            Log.w(TAG, "No se pudo cargar el .riv de Berto ($riveRes), usando fallback estático", riveFileResult.throwable)
            BertoRiveFallback(fallback, modifier)
        }
        is Result.Success -> {
            val riveFile = riveFileResult.value
            val viewModel = rememberViewModelInstance(riveFile)
            val smoothLookX by animateFloatAsState(
                targetValue = (lookX ?: LOOK_X_CENTER) * LOOK_X_SCALE,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "bertoLookX"
            )

            // DisposableEffect runs while the composition is applied, before Rive's first advance.
            DisposableEffect(viewModel) {
                initialBooleans.forEach { (name, value) -> viewModel.setBoolean(name, value) }
                onDispose { }
            }
            LaunchedEffect(trigger, triggerKey) {
                trigger?.let { viewModel.fireTrigger(it) }
            }
            LaunchedEffect(isReading) {
                viewModel.setBoolean(IS_READING, isReading)
            }
            LaunchedEffect(viewModel) {
                snapshotFlow { smoothLookX }.collect { viewModel.setNumber(LOOK_X, it) }
            }

            Rive(
                file = riveFile,
                viewModelInstance = viewModel,
                modifier = modifier,
                pointerInputMode = pointerInputMode
            )
        }
    }
}

@Composable
private fun BertoRiveFallback(@DrawableRes fallback: Int, modifier: Modifier) {
    Image(
        painter = painterResource(fallback),
        contentDescription = "Berto",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
