package com.solvyx.ui.screens.firstaid.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.screens.firstaid.model.BertoMood

private sealed interface BertoArt {
    data class Animated(val pose: BertoPose, @DrawableRes val fallback: Int) : BertoArt
    data class Still(@DrawableRes val drawable: Int) : BertoArt
}

/**
 * Single mapping point between a mood and its artwork. WORRIED and PROUD use still drawables
 * until `berto_poses.riv` ships matching poses — then switch them to [BertoArt.Animated] here.
 */
private fun BertoMood.art(): BertoArt = when (this) {
    BertoMood.WELCOMING -> BertoArt.Animated(BertoPose.CENTER_IDLE_HELLO, R.drawable.berto_saludando)
    BertoMood.CALMING -> BertoArt.Animated(BertoPose.CENTER_IDLE, R.drawable.berto_tranquilo)
    BertoMood.POINTING -> BertoArt.Animated(BertoPose.RIGHT, R.drawable.berto_dedo_der)
    BertoMood.WORRIED -> BertoArt.Still(R.drawable.berto_preocupado)
    BertoMood.PROUD -> BertoArt.Still(R.drawable.berto_feliz)
}

@Composable
fun FirstAidBerto(mood: BertoMood, modifier: Modifier = Modifier) {
    when (val art = mood.art()) {
        is BertoArt.Animated -> BertoPoseAnimation(
            pose = art.pose,
            riveFileRes = R.raw.berto_poses,
            modifier = modifier,
            fallback = art.fallback
        )
        is BertoArt.Still -> Image(
            painter = painterResource(art.drawable),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}
