package com.solvyx.ui.screens.chatbot

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.theme.ChatStatusOnline
import com.solvyx.ui.theme.ChatWarmAccent
import com.solvyx.ui.theme.CrisisRed

private fun ChatMode.statusLabel(): String = when (this) {
    ChatMode.CONECTADO -> "Conectado · responde con IA"
    ChatMode.SIN_CONEXION -> "Modo guiado · sin conexión"
    ChatMode.MODO_GUIADO -> "Modo guiado"
}

@Composable
fun ChatTopBar(
    chatMode: ChatMode,
    isTtsMuted: Boolean,
    isSpeaking: Boolean,
    onToggleMute: () -> Unit,
    onBack: () -> Unit,
    onBreathe: () -> Unit,
    onRequestHelp: () -> Unit,
    onShowCapabilities: () -> Unit,
    onClearMessages: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolvyxBackButton(onClick = onBack)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Berto",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            ModeStatus(chatMode)
        }
        VoiceToggle(isTtsMuted = isTtsMuted, isSpeaking = isSpeaking, onToggleMute = onToggleMute)
        ChatOverflowMenu(
            onBreathe = onBreathe,
            onRequestHelp = onRequestHelp,
            onShowCapabilities = onShowCapabilities,
            onClearMessages = onClearMessages
        )
    }
}

@Composable
private fun ModeStatus(chatMode: ChatMode) {
    val pulseAlpha by rememberInfiniteTransition(label = "OnlinePulse").animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "PulseAlpha"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        val dotColor = if (chatMode == ChatMode.CONECTADO) ChatStatusOnline else ChatWarmAccent
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = pulseAlpha))
        )
        Spacer(Modifier.width(5.dp))
        AnimatedContent(
            targetState = chatMode,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 2 }) togetherWith
                    (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
            },
            label = "ModeStatus"
        ) { mode ->
            Text(mode.statusLabel(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun VoiceToggle(isTtsMuted: Boolean, isSpeaking: Boolean, onToggleMute: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(visible = isSpeaking && !isTtsMuted) {
            SoundWaveIndicator(modifier = Modifier.padding(end = 2.dp))
        }
        IconButton(onClick = onToggleMute) {
            Icon(
                painter = painterResource(if (isTtsMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on),
                contentDescription = if (isTtsMuted) "Activar voz" else "Silenciar voz",
                tint = if (isSpeaking && !isTtsMuted) Color.White else Color.White.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChatOverflowMenu(
    onBreathe: () -> Unit,
    onRequestHelp: () -> Unit,
    onShowCapabilities: () -> Unit,
    onClearMessages: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vertical),
                contentDescription = "Más opciones",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            val close = { expanded = false }
            MenuItem("Necesito ayuda ahora", R.drawable.ic_alert_triangle, CrisisRed) { close(); onRequestHelp() }
            MenuDivider()
            MenuItem("Respirar con Berto", R.drawable.ic_wind) { close(); onBreathe() }
            MenuDivider()
            MenuItem("¿Qué puede hacer Berto?", R.drawable.ic_info_circle) { close(); onShowCapabilities() }
            MenuDivider()
            MenuItem("Empezar de nuevo", R.drawable.ic_refresh) { close(); onClearMessages() }
        }
    }
}

@Composable
private fun MenuItem(label: String, @DrawableRes icon: Int, color: Color? = null, onClick: () -> Unit) {
    val tint = color ?: MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(label, style = MaterialTheme.typography.bodyMedium, color = tint) },
        leadingIcon = { Icon(painterResource(icon), null, tint = tint, modifier = Modifier.size(18.dp)) },
        onClick = onClick
    )
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SoundWaveIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "SoundWave")
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Distinct base heights look more organic than three equal bars.
        listOf(6f, 13f, 8f).forEachIndexed { index, base ->
            val height by transition.animateFloat(
                initialValue = base,
                targetValue = base + 7f,
                animationSpec = infiniteRepeatable(tween(300 + index * 90, easing = LinearEasing), RepeatMode.Reverse),
                label = "Bar$index"
            )
            Box(
                Modifier
                    .width(2.5.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}
