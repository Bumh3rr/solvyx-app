package com.solvyx.ui.screens.chatbot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.remote.chat.ChatRemoteRepository
import com.solvyx.ui.theme.CrisisRed

private const val DISABLED_ALPHA = 0.45f
private const val COUNTER_VISIBLE_FROM = 500
private const val COUNTER_RED_REMAINING = 20
private const val SOS_RING_MAX_SCALE = 1.35f
private val SosButtonSize = 44.dp

private fun ChatMode.placeholder(): String =
    if (this == ChatMode.CONECTADO) "Cuéntale a Berto…" else "Escribe o elige un tema…"

@Composable
fun ChatInputBar(
    text: String,
    chatMode: ChatMode,
    inCrisis: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onSosClick: () -> Unit,
    onMicClick: () -> Unit,
    // false mientras responde la IA: se bloquean texto, micrófono y enviar. El SOS nunca se bloquea.
    isInputEnabled: Boolean
) {
    val haptics = LocalHapticFeedback.current
    val canSend = text.isNotBlank() && isInputEnabled
    val send = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onSend()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        CharacterCounter(length = text.length, modifier = Modifier.align(Alignment.End))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SosButton(pulsing = inCrisis, onClick = onSosClick)
            Spacer(Modifier.size(8.dp))
            MessageField(
                text = text,
                placeholder = chatMode.placeholder(),
                enabled = isInputEnabled,
                onTextChange = onTextChange,
                onSend = { if (canSend) send() },
                onMicClick = onMicClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(8.dp))
            SendButton(enabled = canSend, onClick = send)
        }
    }
}

@Composable
private fun CharacterCounter(length: Int, modifier: Modifier) {
    val max = ChatRemoteRepository.MAX_CARACTERES
    AnimatedVisibility(visible = length >= COUNTER_VISIBLE_FROM, modifier = modifier) {
        Text(
            "$length/$max",
            style = MaterialTheme.typography.labelSmall,
            color = if (length >= max - COUNTER_RED_REMAINING) CrisisRed else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, end = 72.dp)
        )
    }
}

/** Always enabled. In crisis support a soft ring pulses around it so it is easy to find. */
@Composable
private fun SosButton(pulsing: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        if (pulsing) SosPulseRing()
        Box(
            modifier = Modifier
                .size(SosButtonSize)
                .clip(CircleShape)
                .background(CrisisRed.copy(alpha = if (pulsing) 0.22f else 0.12f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_triangle),
                contentDescription = "SOS",
                tint = CrisisRed,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SosPulseRing() {
    val ring by rememberInfiniteTransition(label = "SosPulse").animateFloat(
        initialValue = 1f,
        targetValue = SOS_RING_MAX_SCALE,
        animationSpec = infiniteRepeatable(tween(1_100), RepeatMode.Restart),
        label = "SosPulseScale"
    )
    Box(
        Modifier
            .size(SosButtonSize)
            .scale(ring)
            // Fades out as it grows, reaching 0 at the max scale.
            .alpha(SOS_RING_MAX_SCALE - ring)
            .clip(CircleShape)
            .background(CrisisRed.copy(alpha = 0.35f))
    )
}

@Composable
private fun MessageField(
    text: String,
    placeholder: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(end = 36.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 4,
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    AnimatedContent(
                        targetState = placeholder,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                        label = "Placeholder"
                    ) { hint ->
                        Text(
                            hint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                innerTextField()
            }
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-4).dp)
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onMicClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = "Voz",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SendScale"
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_send),
            contentDescription = "Enviar",
            tint = if (enabled) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}
