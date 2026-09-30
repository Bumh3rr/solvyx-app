package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark

/** Slower than speech: this is Berto reading, eyes moving along each line. */
private const val READING_CHAR_MS = 42L
private const val DOTS_CYCLE_MS = 1_200

/**
 * The note of the day, the text Berto reads on Home. While he reads it types out and reports the
 * caret, which becomes his `lookX`. A tap asks him to read it again.
 */
@Composable
fun BertoReadingNote(
    text: String,
    isReading: Boolean,
    readingKey: Long,
    onCaret: (Float) -> Unit,
    onFinished: () -> Unit,
    onReadAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier
                .clickable(
                    enabled = !isReading,
                    role = Role.Button,
                    onClickLabel = "Pedirle a Berto que la lea otra vez",
                    onClick = onReadAgain
                )
                .height(IntrinsicSize.Min)
        ) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(StreakFlame.copy(alpha = 0.75f))
            )
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_guide),
                        contentDescription = null,
                        tint = StreakFlame,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    AnimatedContent(
                        targetState = isReading,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "NoteHeader",
                        modifier = Modifier.weight(1f)
                    ) { reading ->
                        if (reading) ReadingLabel() else Text(
                            "Nota del día",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TealDark
                        )
                    }
                    if (!isReading && readingKey > 0) {
                        Icon(
                            painterResource(R.drawable.ic_refresh),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                TypewriterText(
                    text = text,
                    playKey = readingKey,
                    charDelayMs = READING_CHAR_MS,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp),
                    started = readingKey > 0,
                    revealAll = readingKey > 0 && !isReading,
                    onCaret = onCaret,
                    onFinished = onFinished
                )
            }
        }
    }
}

@Composable
private fun ReadingLabel() {
    val dots by rememberInfiniteTransition(label = "ReadingDots").animateFloat(
        initialValue = 0f,
        targetValue = 3.99f,
        animationSpec = infiniteRepeatable(tween(DOTS_CYCLE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "ReadingDotsCount"
    )
    Row {
        Text(
            "Berto está leyendo",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = StreakFlame
        )
        repeat(3) { index ->
            Text(
                ".",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = StreakFlame,
                modifier = Modifier.graphicsLayer { alpha = if (dots.toInt() > index) 1f else 0f }
            )
        }
    }
}
