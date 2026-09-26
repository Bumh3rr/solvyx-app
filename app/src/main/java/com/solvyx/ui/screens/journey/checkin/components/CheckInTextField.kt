package com.solvyx.ui.screens.journey.checkin.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import kotlinx.coroutines.delay

/** After this long without a keystroke, the user counts as "not typing" anymore. */
private const val TypingIdleMillis = 1_200L

/**
 * Rounded text area for the check-in that also reports where the cursor is, so Berto can follow
 * the letters: [onCaretMoved] gets the caret's horizontal position (0 = left, 1 = right) while
 * the user is typing, and null once they stop for a moment.
 */
@Composable
fun CheckInTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    minHeight: Dp = 110.dp,
    singleLine: Boolean = false,
    onCaretMoved: (Float?) -> Unit = {}
) {
    // Keep the caret where the user put it while the ViewModel owns the plain text.
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    if (fieldValue.text != value) fieldValue = TextFieldValue(value, TextRange(value.length))

    var keystrokes by remember { mutableIntStateOf(0) }
    var isTyping by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val border by animateColorAsState(
        if (focused) MaterialTheme.colorScheme.primary else TealLight,
        label = "checkInFieldBorder"
    )

    // Typing only reports the caret from onTextLayout (below), where the layout is guaranteed to
    // match the text; this just says "stopped typing" after a short pause.
    LaunchedEffect(keystrokes) {
        if (keystrokes == 0) return@LaunchedEffect
        delay(TypingIdleMillis)
        isTyping = false
        onCaretMoved(null)
    }

    val shape = RoundedCornerShape(16.dp)
    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { updated ->
                if (updated.text.length <= maxLength) {
                    val textChanged = updated.text != fieldValue.text
                    fieldValue = updated
                    onValueChange(updated.text)
                    if (textChanged) {
                        isTyping = true
                        keystrokes++
                    }
                }
            },
            onTextLayout = { result ->
                if (isTyping) caretFraction(result, fieldValue.selection.end)?.let(onCaretMoved)
            },
            singleLine = singleLine,
            interactionSource = interaction,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TealDark),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .background(MaterialTheme.colorScheme.surfaceDim, shape)
                .border(if (focused) 2.dp else 1.dp, border, shape)
                .padding(16.dp),
            decorationBox = { inner ->
                Box {
                    if (fieldValue.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            }
        )
        Text(
            text = "${value.length}/$maxLength",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Horizontal position (0..1) of the caret at [offset] within [layout]. The offset is clamped to the
 * text [layout] was built for: a keystroke can update the selection before the new layout exists,
 * and asking the old layout about the new offset throws.
 */
private fun caretFraction(layout: TextLayoutResult, offset: Int): Float? {
    val width = layout.size.width.takeIf { it > 0 } ?: return null
    val safeOffset = offset.coerceIn(0, layout.layoutInput.text.length)
    return (layout.getCursorRect(safeOffset).center.x / width).coerceIn(0f, 1f)
}
