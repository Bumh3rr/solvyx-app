package com.solvyx.ui.screens.journey.achievements.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.berto.BertoReactTrigger
import com.solvyx.ui.components.berto.BertoReactsAnimation
import com.solvyx.ui.components.common.ConfettiBurst
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary
import kotlinx.coroutines.launch

private const val SPIN_DEGREES = 720f
private const val SPIN_MS = 1_300
private const val CAMERA_DISTANCE = 12f

/**
 * Full-screen moment for a streak achievement that was just unlocked: the medal spins in over a
 * dark backdrop, confetti falls and Berto celebrates (euphoric reaction in `reacts_berto.riv`).
 */
@Composable
fun UnlockCelebration(info: MedalInfo, onDone: () -> Unit) {
    val spin = remember { Animatable(SPIN_DEGREES) }
    val scale = remember { Animatable(0.2f) }
    LaunchedEffect(info.title) {
        launch { spin.animateTo(0f, tween(SPIN_MS, easing = FastOutSlowInEasing)) }
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
    }
    BackHandler(onBack = onDone)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealPrimary, TealDark)))
            // Swallows taps so nothing behind reacts.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center
    ) {
        ConfettiBurst(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "¡Nuevo logro!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.85f)
            )
            Medal(
                icon = info.icon,
                color = info.color,
                unlocked = true,
                progress = 1f,
                size = 180.dp,
                modifier = Modifier.graphicsLayer {
                    rotationY = spin.value
                    scaleX = scale.value
                    scaleY = scale.value
                    cameraDistance = CAMERA_DISTANCE * density
                }
            )
            Text(
                info.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                info.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            BertoReactsAnimation(
                reaction = BertoReactTrigger.EUPHORIC,
                isReading = false,
                lookX = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(4.dp))
            SolvyxButton(text = "¡Lo logré!", onClick = onDone, modifier = Modifier.fillMaxWidth())
        }
    }
}
