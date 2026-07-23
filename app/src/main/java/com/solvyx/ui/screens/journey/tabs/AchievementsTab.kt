package com.solvyx.ui.screens.journey.tabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.screens.journey.AchievementsUiState
import com.solvyx.ui.screens.journey.components.AchievementCard
import kotlin.math.roundToInt

@Composable
fun AchievementsTab(
    state: AchievementsUiState,
    modifier: Modifier = Modifier,
    justUnlockedIds: Set<String> = emptySet(),
    onConsumeJustUnlocked: (String) -> Unit = {}
) {
    when (state) {
        AchievementsUiState.Loading -> CenteredMessage(
            image = R.drawable.berto_dedo_der,
            title = "Cargando tus logros…",
            modifier = modifier
        )
        AchievementsUiState.Empty -> CenteredMessage(
            image = R.drawable.berto_dedo_der,
            title = "Aún no hay logros por aquí",
            subtitle = "Registra tus días sin consumo para desbloquear tu primer logro.",
            modifier = modifier
        )
        is AchievementsUiState.Content -> AchievementsGrid(state, modifier, justUnlockedIds, onConsumeJustUnlocked)
    }
}

@Composable
private fun AchievementsGrid(
    state: AchievementsUiState.Content,
    modifier: Modifier = Modifier,
    justUnlockedIds: Set<String>,
    onConsumeJustUnlocked: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Seeded from the pre-unlock count so returning from the check-in wizard (which disposes
        // this whole tab) still animates the counter up instead of snapping straight to the target.
        val unlockedCount = remember { Animatable((state.unlockedCount - justUnlockedIds.size).toFloat()) }
        LaunchedEffect(state.unlockedCount) {
            unlockedCount.animateTo(state.unlockedCount.toFloat(), tween(600))
        }
        Text(
            text = "${unlockedCount.value.roundToInt()} de ${state.achievements.size} desbloqueados",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = SolvyxBottomNavHeight)
        ) {
            itemsIndexed(state.achievements, key = { _, achievement -> achievement.id }) { index, achievement ->
                // Staggered spring entrance: fade + scale + a short vertical settle.
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 40L)
                    visible = true
                }
                val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(300), label = "card_alpha")
                val scale by animateFloatAsState(
                    if (visible) 1f else 0.85f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "card_scale"
                )
                val offsetY by animateFloatAsState(
                    if (visible) 0f else 20f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "card_offset"
                )
                Box(
                    modifier = Modifier
                        .offset(y = offsetY.dp)
                        .graphicsLayer {
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    AchievementCard(
                        achievement = achievement,
                        justUnlocked = achievement.id in justUnlockedIds,
                        onCelebrated = { onConsumeJustUnlocked(achievement.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    image: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.height(96.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
