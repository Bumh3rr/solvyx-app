package com.solvyx.ui.screens.firstaid.guides

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.screens.firstaid.components.FirstAidBerto
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import kotlinx.coroutines.delay
import java.util.Locale

private const val WaveMinutes = 15
private const val WaveMillis = WaveMinutes * 60_000L
private const val TickMillis = 1_000L
private const val RisingPhaseEnd = 1f / 3f
private const val PeakPhaseEnd = 2f / 3f
private val TimerRingSize = 148.dp

private enum class WavePhase { IDLE, RUNNING, DONE }

/**
 * "Aguanta la ola": a 15-minute countdown that walks the user through a craving rising, peaking
 * and fading. The start time is saved (not the remaining time), so the countdown stays correct
 * across rotation and while the user visits another guide.
 */
@Composable
fun CravingWaveTimerCard(modifier: Modifier = Modifier) {
    var startedAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(startedAt) {
        val start = startedAt ?: return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            if (now - start >= WaveMillis) break
            delay(TickMillis)
        }
    }

    val elapsed = startedAt?.let { (now - it).coerceIn(0L, WaveMillis) } ?: 0L
    val phase = when {
        startedAt == null -> WavePhase.IDLE
        elapsed >= WaveMillis -> WavePhase.DONE
        else -> WavePhase.RUNNING
    }
    val start = {
        now = System.currentTimeMillis()
        startedAt = now
    }

    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "cravingWavePhase",
            modifier = Modifier.padding(16.dp)
        ) { current ->
            when (current) {
                WavePhase.IDLE -> IdleContent(onStart = start)
                WavePhase.RUNNING -> RunningContent(
                    elapsedMillis = elapsed,
                    onStop = { startedAt = null }
                )
                WavePhase.DONE -> DoneContent(onRestart = start)
            }
        }
    }
}

@Composable
private fun IdleContent(onStart: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ToneIconBadge(icon = R.drawable.ic_clock, tone = GuideTone.CALM)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Aguanta la ola",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    text = "Las ganas suben, llegan a su punto más alto y bajan. Te acompaño " +
                        "los próximos $WaveMinutes minutos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        SolvyxButton(
            text = "Empezar $WaveMinutes minutos",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun RunningContent(elapsedMillis: Long, onStop: () -> Unit) {
    val fraction = elapsedMillis.toFloat() / WaveMillis
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { fraction },
                modifier = Modifier.size(TimerRingSize),
                color = MaterialTheme.colorScheme.primary,
                trackColor = TealLight.copy(alpha = 0.4f),
                strokeWidth = 10.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatRemaining(WaveMillis - elapsedMillis),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    text = "restantes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = waveMessage(fraction),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TealDark,
            textAlign = TextAlign.Center
        )
        SolvyxTextButton(text = "Detener", onClick = onStop)
    }
}

@Composable
private fun DoneContent(onRestart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FirstAidBerto(mood = BertoMood.PROUD, modifier = Modifier.size(88.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "La ola pasó. Lo lograste.",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
        Text(
            text = "Si las ganas regresan, puedes volver a empezar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        SolvyxOutlinedButton(
            text = "Empezar otra vez",
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun waveMessage(fraction: Float): String = when {
    fraction < RisingPhaseEnd -> "Las ganas están subiendo. Respira lento, no tienes que hacer nada más."
    fraction < PeakPhaseEnd -> "Estás en lo más alto de la ola. Desde aquí empieza a bajar."
    else -> "La ola ya va de bajada. Lo estás logrando."
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = millis / 1_000
    return String.format(Locale.ROOT, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}
