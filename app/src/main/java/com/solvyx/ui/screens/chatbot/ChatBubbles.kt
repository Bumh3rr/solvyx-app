package com.solvyx.ui.screens.chatbot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.TealPrimary

private val MaxBubbleWidth = 290.dp
private val AvatarSize = 30.dp
private val AvatarGap = BertoBubbleIndent - AvatarSize

private fun bubbleShape(isUser: Boolean, isFirstOfGroup: Boolean) = RoundedCornerShape(
    topStart = if (!isUser && !isFirstOfGroup) 6.dp else 18.dp,
    topEnd = if (isUser && !isFirstOfGroup) 6.dp else 18.dp,
    bottomStart = if (isUser) 18.dp else 4.dp,
    bottomEnd = if (isUser) 4.dp else 18.dp
)

/**
 * One chat message. Consecutive Berto messages form a group: only the first shows his avatar
 * (in the mood he had), the rest line up under it.
 */
@Composable
fun MessageBubble(message: ChatMessage, isFirstOfGroup: Boolean) {
    val isUser = !message.isFromBerto
    val shape = bubbleShape(isUser, isFirstOfGroup)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            if (isFirstOfGroup) BertoAvatar(message.bertoState, size = AvatarSize) else Spacer(Modifier.width(AvatarSize))
            Spacer(Modifier.width(AvatarGap))
        }
        Box(
            modifier = Modifier
                .widthIn(max = MaxBubbleWidth)
                .clip(shape)
                .background(if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                .then(if (isUser) Modifier else Modifier.border(0.8.dp, message.bertoState.accentColor(), shape))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                // Las respuestas de la IA traen Markdown (**negritas**, viñetas); el usuario escribe texto plano.
                val content = remember(message.content, isUser) {
                    if (isUser) AnnotatedString(message.content) else formatearMarkdown(message.content)
                }
                Text(
                    content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

/** "Berto is typing" as his own bubble at the end of the conversation. */
@Composable
fun TypingBubble(state: BertoState, isThinkingLong: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BertoAvatar(state, size = AvatarSize)
        Spacer(Modifier.width(AvatarGap))
        Row(
            modifier = Modifier
                .clip(bubbleShape(isUser = false, isFirstOfGroup = true))
                .background(MaterialTheme.colorScheme.surface)
                .border(0.8.dp, state.accentColor(), bubbleShape(isUser = false, isFirstOfGroup = true))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypingDots()
            AnimatedContent(
                targetState = isThinkingLong,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "TypingLabel"
            ) { long ->
                if (long) {
                    Text(
                        "  Pensando…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "TypingDots")
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(3) { index ->
            val lift by transition.animateFloat(
                initialValue = 0f,
                targetValue = -5f,
                animationSpec = infiniteRepeatable(tween(420, delayMillis = index * 140), RepeatMode.Reverse),
                label = "TypingDotLift$index"
            )
            val alpha by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(420, delayMillis = index * 140), RepeatMode.Reverse),
                label = "TypingDotAlpha$index"
            )
            Box(
                Modifier
                    .offset(y = lift.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = alpha))
            )
        }
    }
}

// ── Aviso del sistema (conexión perdida/recuperada) ─────
@Composable
fun SystemNoticeRow(aviso: AvisoSistema, text: String) {
    val iconRes = when (aviso) {
        AvisoSistema.SIN_CONEXION,
        AvisoSistema.SERVIDOR_NO_DISPONIBLE -> R.drawable.ic_wifi_off
        AvisoSistema.CONEXION_RECUPERADA,
        AvisoSistema.SERVIDOR_RECUPERADO -> R.drawable.ic_check_circle
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
