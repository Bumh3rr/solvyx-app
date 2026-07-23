package com.solvyx.ui.components.berto

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private const val TAG = "BertoPoseAnimation"

/**
 * Reusable Rive-animated Berto for a single static pose/angle (left/right/center/etc.), as
 * opposed to [BertoRiveAnimation]'s mood+streak hero. Takes its own `.riv` resource since poses
 * live in a file separate from `bertovm.riv` — reusable across any future pose file, not just one.
 */
@Composable
fun BertoPoseAnimation(
    pose: BertoPose,
    @RawRes riveFileRes: Int,
    modifier: Modifier = Modifier,
    @DrawableRes fallback: Int = R.drawable.berto_tranquilo
) {
    val riveWorker = rememberRiveWorker()
    val riveFileResult = rememberRiveFile(
        RiveFileSource.RawRes.from(riveFileRes),
        riveWorker
    )

    when (riveFileResult) {
        is Result.Loading -> BertoPoseStaticFallback(fallback, modifier)
        is Result.Error -> {
            Log.w(TAG, "No se pudo cargar el .riv de Berto ($riveFileRes), usando fallback estático", riveFileResult.throwable)
            BertoPoseStaticFallback(fallback, modifier)
        }
        is Result.Success -> {
            val riveFile = riveFileResult.value
            val vmi = rememberViewModelInstance(riveFile)

            LaunchedEffect(pose) {
                vmi.setEnum("typePose", pose.riveValue)
            }

            Rive(
                file = riveFile,
                viewModelInstance = vmi,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BertoPoseStaticFallback(@DrawableRes fallback: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(fallback),
        contentDescription = "Berto",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
