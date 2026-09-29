package com.solvyx.ui.screens.chatbot

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.BreathingCircle
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

private const val TARGET_CYCLES = 3
private const val EXHALE_PHASE = "Exhala"

/**
 * Full-screen "respira conmigo" over the chat: Berto breathes, the circle guides the 4-7-8 rhythm
 * and Berto says each phase out loud. It can be closed at any time; after [TARGET_CYCLES] the
 * button invites to finish. [onClose] gets how many full cycles were done.
 */
@Composable
fun BreathingSession(onPhase: (String) -> Unit, onClose: (completedCycles: Int) -> Unit) {
    var completedCycles by remember { mutableIntStateOf(0) }
    val currentOnPhase by rememberUpdatedState(onPhase)
    val close = { onClose(completedCycles) }
    BackHandler(onBack = close)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealDark, TealPrimary)))
            // Swallows taps so nothing behind the overlay reacts.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        IconButton(onClick = close, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_circle_x),
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    BertoChatFigure(BertoExpression.BREATHING, Modifier.size(112.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Respira conmigo",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Text(
                    "Sigue el círculo. Si te distraes, solo vuelve a empezar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            BreathingCircle(onPhaseChange = { phase ->
                currentOnPhase(phase)
                if (phase == EXHALE_PHASE) completedCycles++
            })

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CycleDots(completed = completedCycles)
                Spacer(Modifier.height(16.dp))
                AnimatedContent(
                    targetState = completedCycles >= TARGET_CYCLES,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "BreathingFinish"
                ) { reachedTarget ->
                    SolvyxButton(
                        text = if (reachedTarget) "Ya me siento con más calma" else "Terminar",
                        onClick = close,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = if (reachedTarget) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleDots(completed: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(TARGET_CYCLES) { index ->
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (index < completed) 1f else 0.3f))
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            "${completed.coerceAtMost(TARGET_CYCLES)} de $TARGET_CYCLES respiraciones",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
