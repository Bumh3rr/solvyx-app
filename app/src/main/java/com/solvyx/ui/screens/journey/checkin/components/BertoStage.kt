package com.solvyx.ui.screens.journey.checkin.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.solvyx.ui.components.berto.BertoReactTrigger
import com.solvyx.ui.components.berto.BertoReactsAnimation
import com.solvyx.ui.screens.journey.checkin.BertoGesture
import com.solvyx.ui.screens.journey.checkin.BertoReaction
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private val RiveCanvasSize = 150.dp
private const val MaxLeanDegrees = 7f
private val MaxLeanShift = 12.dp
private const val ReactionPopScale = 0.9f
private const val ReadingNodMillis = 520
private val ReadingNodDepth = 3.dp
private val BubbleTail = 6.dp
private const val TailRotationDegrees = 45f

/**
 * Berto at the top of the check-in, from `reacts_berto.riv`: each gesture fires its Rive reaction
 * (see [toReactTrigger]) and, while the user types, Rive lowers his head and his eyes follow the
 * cursor ([lookX], 0 = left edge, 1 = right edge). Compose adds on top a pop every time his reaction
 * changes and, while reading, a lean toward the cursor with a small floating nod.
 */
@Composable
fun BertoStage(
    reaction: BertoReaction,
    lookX: Float?,
    bertoSize: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val pop = remember { Animatable(1f) }
    LaunchedEffect(reaction.message) {
        pop.snapTo(ReactionPopScale)
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    val lean by animateFloatAsState(
        targetValue = lookX?.let { lerp(-1f, 1f, it) } ?: 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bertoLean"
    )
    val isReading = reaction.gesture == BertoGesture.READING
    val nod = if (isReading) readingNod() else 0f
    val animatedSize by animateDpAsState(bertoSize, label = "bertoSize")

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // The Rive view keeps one fixed size and is only scaled: resizing its layout crops the
        // artboard instead of shrinking Berto.
        val shrink = animatedSize / RiveCanvasSize
        Box(modifier = Modifier.size(animatedSize), contentAlignment = Alignment.Center) {
            BertoReactsAnimation(
                reaction = reaction.gesture.toReactTrigger(),
                isReading = isReading,
                lookX = lookX,
                modifier = Modifier
                    .requiredSize(RiveCanvasSize)
                    .graphicsLayer {
                        scaleX = pop.value * shrink
                        scaleY = pop.value * shrink
                        translationX = with(density) { MaxLeanShift.toPx() } * lean
                        translationY = with(density) { ReadingNodDepth.toPx() } * nod
                        rotationZ = MaxLeanDegrees * lean
                    }
            )
        }
        SpeechBubble(message = reaction.message)
    }
}

/**
 * Rive reaction in `reacts_berto.riv` for each gesture; `null` leaves Berto in Idle (or reading,
 * which `isReading` drives). Moments without their own animation yet reuse the closest one: a
 * clean day is celebrated like euphoria and "no consumí" like a good mood. GREET needs no trigger:
 * the state machine greets on its own when it starts.
 */
private fun BertoGesture.toReactTrigger(): BertoReactTrigger? = when (this) {
    BertoGesture.REACT_SAD -> BertoReactTrigger.SAD
    BertoGesture.REACT_ANXIOUS -> BertoReactTrigger.ANXIOUS
    BertoGesture.REACT_NEUTRAL -> BertoReactTrigger.NEUTRAL
    BertoGesture.REACT_GOOD,
    BertoGesture.PROUD -> BertoReactTrigger.GOOD
    BertoGesture.REACT_EUPHORIC,
    BertoGesture.CELEBRATE -> BertoReactTrigger.EUPHORIC
    BertoGesture.GREET,
    BertoGesture.READING,
    BertoGesture.ATTENTIVE,
    BertoGesture.SUPPORTIVE,
    BertoGesture.COMFORT -> null
}

/** 0..1..0 loop, only composed (and running) while Berto is reading. */
@Composable
private fun readingNod(): Float {
    val value by rememberInfiniteTransition(label = "bertoNod").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(ReadingNodMillis), RepeatMode.Reverse),
        label = "bertoNodValue"
    )
    return value
}

@Composable
private fun SpeechBubble(message: String) {
    val shape = RoundedCornerShape(18.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Tail: a rotated square half-hidden under the bubble, pointing up at Berto.
        Box(
            Modifier
                .offset(y = BubbleTail)
                .size(BubbleTail * 2)
                .rotate(TailRotationDegrees)
                .background(MaterialTheme.colorScheme.surfaceDim)
        )
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInVertically { it / 3 }) togetherWith
                    (fadeOut(tween(150)) + slideOutVertically { -it / 3 })
            },
            label = "bertoBubble",
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceDim, shape)
                .border(1.dp, TealLight, shape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TealDark,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}
