package com.solvyx.ui.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R

/**
 * Wraps a piece of guest-facing content that requires a real account to be meaningful (e.g. a
 * streak, a mood log) and shows it as a locked, frosted card when [locked] is true — instead of
 * letting the anonymous user interact with a feature that silently does nothing (writes are
 * no-ops for anonymous users at the repository layer, so without this the card just looked
 * broken: taps registered no feedback, the streak stayed frozen at 0 forever).
 *
 * The "frosted" look is two layered effects, not one: a real Gaussian blur on the content
 * (`Modifier.blur`, which only renders on API 31+ — a known Compose limitation, not a bug here)
 * plus a translucent scrim on top of it that works on every API level this app supports (26+).
 * On newer devices both effects stack (blurred AND veiled); on older ones the scrim alone still
 * reads clearly as "locked", so the result never regresses to sharp, readable content underneath.
 *
 * The whole locked area is one big tap target: any tap calls [onUnlock] (typically "create
 * account"), and a transparent capture layer on top means no tap ever reaches the real content
 * underneath.
 */
@Composable
fun GuestLockOverlay(
    locked: Boolean,
    message: String,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!locked) {
        content()
        return
    }

    val shape = RoundedCornerShape(20.dp) // matches SolvyxCard's corner radius
    val pulseTransition = rememberInfiniteTransition(label = "GuestLockPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GuestLockPulseScale"
    )

    Box(modifier =modifier.clip(shape)) {
        Box(modifier = Modifier.blur(10.dp)) {
            content()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Crear cuenta →",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onUnlock() }
        )
    }
}
