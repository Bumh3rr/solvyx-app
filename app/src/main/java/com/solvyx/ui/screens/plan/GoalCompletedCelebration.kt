package com.solvyx.ui.screens.plan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.backend.data.model.Goal
import com.solvyx.ui.components.berto.BertoReactTrigger
import com.solvyx.ui.components.berto.BertoReactsAnimation
import com.solvyx.ui.components.common.ConfettiBurst
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.theme.MedalGold
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

/**
 * Full-screen moment for a goal just completed, wherever it was detected (the check-in that
 * completed it, or Plan when a reduce week closed). Same look as the medal celebration of Mi
 * camino: green backdrop, confetti and Berto euphoric.
 */
@Composable
fun GoalCompletedCelebration(goal: Goal, onDone: () -> Unit) {
    val scale = remember(goal.id) { Animatable(0.2f) }
    LaunchedEffect(goal.id) {
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
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
                    .background(MedalGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(goalIcon(goal.type)),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(
                "¡Cumpliste tu meta!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "“${goal.title}”",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Lo lograste paso a paso. Estoy muy orgulloso de ti.",
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
