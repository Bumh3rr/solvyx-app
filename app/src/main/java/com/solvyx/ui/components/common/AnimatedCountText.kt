package com.solvyx.ui.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private const val CountUpMillis = 900

/**
 * A number that counts up from its previous value to [value] (from 0 on first show), so stats
 * feel earned instead of just appearing.
 */
@Composable
fun AnimatedCountText(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(value) {
        animated.animateTo(value.toFloat(), tween(CountUpMillis, easing = FastOutSlowInEasing))
    }
    Text(text = animated.value.toInt().toString(), modifier = modifier, style = style, color = color)
}
