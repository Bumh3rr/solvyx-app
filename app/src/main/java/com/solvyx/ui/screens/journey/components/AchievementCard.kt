package com.solvyx.ui.screens.journey.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.journey.UiAchievement
import com.solvyx.ui.theme.TealDark
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun AchievementCard(
    achievement: UiAchievement,
    justUnlocked: Boolean = false,
    onCelebrated: () -> Unit = {}
) {
    val lockedColor = MaterialTheme.colorScheme.surfaceDim
    val unlockedColor = MaterialTheme.colorScheme.primaryContainer
    val targetColor = if (achievement.unlocked) unlockedColor else lockedColor
    // Driven manually (not animateColorAsState) so a fresh mount returning from the check-in
    // wizard can still snap to locked and sweep to unlocked instead of initializing pre-animated.
    val containerColor = remember { Animatable(targetColor, Color.VectorConverter(targetColor.colorSpace)) }
    val iconScale = remember { Animatable(1f) }

    LaunchedEffect(justUnlocked, targetColor) {
        if (justUnlocked) {
            containerColor.snapTo(lockedColor)
            // coroutineScope suspends until both animations finish, so onCelebrated() (which
            // clears justUnlocked and cancels this effect) never fires while the icon pop is
            // still mid-flight — otherwise it gets cancelled and freezes at a non-1.0 scale.
            coroutineScope {
                launch {
                    iconScale.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    iconScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                }
                containerColor.animateTo(unlockedColor, tween(400))
            }
            onCelebrated()
        } else {
            containerColor.snapTo(targetColor)
        }
    }

    SolvyxCard(
        modifier = Modifier.width(100.dp),
        containerColor = containerColor.value
    ) {
        Column(
            modifier             = Modifier.padding(12.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                if (!achievement.unlocked) {
                    CircularProgressIndicator(
                        progress = { achievement.progress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier         = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                            }
                            .clip(CircleShape)
                            .background(
                                if (achievement.unlocked) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter           = painterResource(achievement.icon),
                            contentDescription = null,
                            tint              = if (achievement.unlocked) Color.White
                                               else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier          = Modifier.size(22.dp)
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !achievement.unlocked,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200))
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(16.dp)
                                .offset(x = 2.dp, y = 2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter           = painterResource(R.drawable.ic_lock),
                                contentDescription = null,
                                tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier          = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text      = achievement.title,
                style     = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color     = if (achievement.unlocked) TealDark
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text      = achievement.description,
                style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
        }
    }
}
