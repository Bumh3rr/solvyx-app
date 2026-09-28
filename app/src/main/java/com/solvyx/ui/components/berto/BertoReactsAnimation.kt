package com.solvyx.ui.components.berto

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.rive.Result
import app.rive.Rive
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
    EUPHORIC("triggerEuphoric")
}

/**
 * Berto from `reacts_berto.riv`: greets on load, plays a [reaction] every time it changes to a new
 * value, and while [isReading] lowers his head and follows [lookX] (0 = left edge of the text, 1 =
 * right edge) with his eyes. `lookX` is smoothed here, so the jump back to 0 on a new line glides
 * instead of snapping. See "Berto — Guía animación Reading" in the vault for the Rive side.
 */
@Composable
fun BertoReactsAnimation(
    reaction: BertoReactTrigger?,
    isReading: Boolean,
    lookX: Float?,
    modifier: Modifier = Modifier,
    @DrawableRes fallback: Int = R.drawable.berto_saludando
) {
    val riveWorker = rememberRiveWorker()
    when (val riveFileResult = rememberRiveFile(RiveFileSource.RawRes.from(R.raw.reacts_berto), riveWorker)) {
        is Result.Loading -> BertoReactsFallback(fallback, modifier)
        is Result.Error -> {
            Log.w(TAG, "No se pudo cargar reacts_berto.riv, usando fallback estático", riveFileResult.throwable)
            BertoReactsFallback(fallback, modifier)
        }
        is Result.Success -> {
            val riveFile = riveFileResult.value
            val viewModel = rememberViewModelInstance(riveFile)
            val smoothLookX by animateFloatAsState(
                targetValue = (lookX ?: LOOK_X_CENTER) * LOOK_X_SCALE,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "bertoLookX"
            )

            LaunchedEffect(reaction) {
                reaction?.let { viewModel.fireTrigger(it.riveName) }
            }
            LaunchedEffect(isReading) {
                viewModel.setBoolean(IS_READING, isReading)
            }
            LaunchedEffect(viewModel) {
                snapshotFlow { smoothLookX }.collect { viewModel.setNumber(LOOK_X, it) }
            }

            Rive(file = riveFile, viewModelInstance = viewModel, modifier = modifier)
        }
    }
}

@Composable
private fun BertoReactsFallback(@DrawableRes fallback: Int, modifier: Modifier) {
    Image(
        painter = painterResource(fallback),
        contentDescription = "Berto",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
