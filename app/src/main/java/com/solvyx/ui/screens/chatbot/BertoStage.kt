package com.solvyx.ui.screens.chatbot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.ConfettiBurst
import com.solvyx.ui.theme.TealDark
import kotlinx.coroutines.delay

private const val CAPTION_ROTATION_MS = 6_000L
private val ExpandedBertoSize = 112.dp
private val CollapsedBertoSize = 46.dp
private val CaptionShape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)

/**
 * Berto's stage above the conversation: he reacts to how the chat is going (listening while the user
 * types, thinking while he answers, worried, happy, in support mode). The background takes his visor
 * color. [collapsed] (keyboard open) shrinks it to a strip so the conversation keeps its room.
 */
@Composable
fun BertoStage(
    expression: BertoExpression,
    state: BertoState,
    celebrationCount: Int,
    collapsed: Boolean,
    modifier: Modifier = Modifier
) {
    val stateColor by animateColorAsState(state.visorColor(), tween(600), label = "StageColor")
    val background = MaterialTheme.colorScheme.background
    val bertoSize by animateDpAsState(
        targetValue = if (collapsed) CollapsedBertoSize else ExpandedBertoSize,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "StageBertoSize"
    )
    val verticalPadding by animateDpAsState(if (collapsed) 6.dp else 14.dp, label = "StagePadding")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(stateColor, background)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                StageHalo(state = state, size = bertoSize)
                BertoChatFigure(expression = expression, modifier = Modifier.size(bertoSize))
            }
            Spacer(Modifier.width(12.dp))
            StageCaption(expression = expression, collapsed = collapsed, modifier = Modifier.weight(1f))
        }
        // One burst per new celebration; the key restarts the one-shot animation.
        if (celebrationCount > 0 && state == BertoState.CELEBRANDO) {
            key(celebrationCount) { ConfettiBurst(Modifier.matchParentSize()) }
        }
    }
}

/** A soft glow that breathes behind Berto in his accent color. */
@Composable
private fun StageHalo(state: BertoState, size: Dp) {
    val glow by rememberInfiniteTransition(label = "StageHalo").animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1_800), RepeatMode.Reverse),
        label = "StageHaloAlpha"
    )
    Box(
        Modifier
            .size(size * 0.92f)
            .alpha(glow)
            .clip(CircleShape)
            .background(state.accentColor().copy(alpha = 0.35f))
    )
}

/**
 * The line Berto is saying: starts on the expression's main line and moves to the next one every
 * [CAPTION_ROTATION_MS], so he does not repeat the same sentence for as long as the mood lasts.
 */
@Composable
private fun rememberRotatingCaption(expression: BertoExpression): String {
    var index by remember(expression) { mutableIntStateOf(0) }
    LaunchedEffect(expression) {
        val count = expression.captions.size
        while (count > 1) {
            delay(CAPTION_ROTATION_MS)
            index = (index + 1) % count
        }
    }
    return expression.captions[index]
}

@Composable
private fun StageCaption(expression: BertoExpression, collapsed: Boolean, modifier: Modifier) {
    AnimatedContent(
        targetState = rememberRotatingCaption(expression),
        transitionSpec = {
            (fadeIn(tween(250)) + slideInVertically { it / 3 }) togetherWith
                (fadeOut(tween(150)) + slideOutVertically { -it / 3 })
        },
        modifier = modifier,
        label = "StageCaption"
    ) { caption ->
        Box(
            modifier = Modifier
                .clip(CaptionShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = if (collapsed) 0f else 0.9f))
                .border(1.dp, TealDark.copy(alpha = if (collapsed) 0f else 0.12f), CaptionShape)
                .padding(horizontal = if (collapsed) 0.dp else 14.dp, vertical = if (collapsed) 0.dp else 10.dp)
        ) {
            Text(
                text = caption,
                style = if (collapsed) {
                    MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                },
                color = TealDark
            )
        }
    }
}
