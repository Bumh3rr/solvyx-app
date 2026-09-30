package com.solvyx.ui.screens.home.stage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay

/** Extra pause after punctuation, so the text breathes like someone speaking. */
private const val PUNCTUATION_PAUSE_MS = 140L
private val PausingPunctuation = setOf('.', ',', '!', '?', ':')

/**
 * Text that types itself out once per [playKey]. The hidden part is laid out but transparent, so
 * the box never jumps while it grows. [onCaret] reports where the caret is (0 = left edge, 1 =
 * right edge of the text): it jumps back to 0 on each new line, like eyes reading.
 * [revealAll] shows the whole text at once, e.g. when the reading was interrupted.
 */
@Composable
fun TypewriterText(
    text: String,
    playKey: Any?,
    charDelayMs: Long,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    started: Boolean = true,
    revealAll: Boolean = false,
    onCaret: ((Float) -> Unit)? = null,
    onFinished: (() -> Unit)? = null
) {
    var shown by remember(text) { mutableIntStateOf(0) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val caretListener by rememberUpdatedState(onCaret)
    val finishListener by rememberUpdatedState(onFinished)

    LaunchedEffect(text, playKey, started, revealAll) {
        if (revealAll) {
            shown = text.length
            return@LaunchedEffect
        }
        if (!started) {
            shown = 0
            return@LaunchedEffect
        }
        shown = 0
        while (shown < text.length) {
            val typed = text[shown]
            delay(if (typed in PausingPunctuation) charDelayMs + PUNCTUATION_PAUSE_MS else charDelayMs)
            shown++
            layout?.let { caretListener?.invoke(it.caretFraction(shown)) }
        }
        finishListener?.invoke()
    }

    Text(
        text = buildAnnotatedString {
            append(text.take(shown))
            withStyle(SpanStyle(color = Color.Transparent)) { append(text.drop(shown)) }
        },
        style = style,
        color = color,
        modifier = modifier,
        onTextLayout = { layout = it }
    )
}

private fun TextLayoutResult.caretFraction(offset: Int): Float {
    val width = size.width
    if (width <= 0) return 0f
    val safeOffset = offset.coerceIn(0, layoutInput.text.length)
    return (getHorizontalPosition(safeOffset, usePrimaryDirection = true) / width).coerceIn(0f, 1f)
}
