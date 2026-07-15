package com.solvyx.ui.components.berto

import android.util.Log
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

private const val TAG = "BertoRiveAnimation"

@Composable
fun BertoRiveAnimation(
    estadoAnimo: String,
    rachaActual: Int,
    modifier: Modifier = Modifier
) {
    val riveWorker = rememberRiveWorker()
    val riveFileResult = rememberRiveFile(
        RiveFileSource.RawRes.from(R.raw.bertovm),
        riveWorker
    )

    when (riveFileResult) {
        is Result.Loading -> BertoStaticFallback(modifier)
        is Result.Error -> {
            Log.w(TAG, "No se pudo cargar bertovm.riv, usando fallback estático", riveFileResult.throwable)
            BertoStaticFallback(modifier)
        }
        is Result.Success -> {
            val riveFile = riveFileResult.value
            val vmi = rememberViewModelInstance(riveFile)

            LaunchedEffect(estadoAnimo) {
                vmi.setEnum("estado_animo", estadoAnimo)
            }
            LaunchedEffect(rachaActual) {
                vmi.setNumber("racha_actual", rachaActual.toFloat())
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
private fun BertoStaticFallback(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.berto_tranquilo),
        contentDescription = "Berto",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
