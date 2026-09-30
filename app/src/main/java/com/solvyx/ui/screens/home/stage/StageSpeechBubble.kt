package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.theme.TealDark

private const val SPEECH_CHAR_MS = 26L
private val BubbleShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
private val BubbleColor = Color.White.copy(alpha = 0.95f)

/**
 * What Berto is saying, in a bubble whose tail points down at him. Each new line ([lineKey])
 * pops in from the tail and types itself out.
 */
@Composable
fun StageSpeechBubble(text: String, lineKey: Long, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = lineKey,
        transitionSpec = {
            val fromTail = TransformOrigin(1f, 1f)
            (scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), 0.6f, fromTail) + fadeIn()) togetherWith
                (scaleOut(tween(140), 0.9f, fromTail) + fadeOut(tween(140)))
        },
        modifier = modifier,
        label = "SpeechBubble"
    ) { key ->
        Column(horizontalAlignment = Alignment.End) {
            Box(
                Modifier
                    .shadow(4.dp, BubbleShape)
                    .background(BubbleColor, BubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                TypewriterText(
                    text = text,
                    playKey = key,
                    charDelayMs = SPEECH_CHAR_MS,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TealDark
                )
            }
            BubbleTail(Modifier.padding(end = 18.dp))
        }
    }
}

@Composable
private fun BubbleTail(modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = 14.dp, height = 10.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width * 0.85f, size.height)
            close()
        }
        drawPath(path, BubbleColor)
    }
}
