package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.screens.guias.components.CardLabel
import com.solvyx.ui.screens.guias.components.GuiaPanel
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.HeroSideBerto
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest

@Composable
fun MiPlanHubScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToManejoCraving: () -> Unit,
    onNavigateToInfoSustancia: () -> Unit,
    viewModel: PlanViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Mi Plan",
            onBack = onOpenDrawer,
            isMenuButton = true
        )

        HeroSideBerto(
            mascot = R.drawable.berto_feliz,
            title = "Tu plan de reducción",
            subtitle = "Pequeños pasos, grandes cambios"
        )

        GuiaPanel(modifier = Modifier.weight(1f)) {

            // ── Carrusel de metas ─────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_flag),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Metas sugeridas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TealDark
                    )
                }
                Text(
                    "${viewModel.metaIndex + 1} / ${viewModel.metasList.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            BorderCard {
                AnimatedContent(
                    targetState = viewModel.metaIndex,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
                    },
                    label = "MetaPlanCarousel"
                ) { idx ->
                    Text(
                        text = viewModel.metasList[idx],
                        style = MaterialTheme.typography.bodyMedium,
                        color = TealDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        viewModel.metasList.indices.forEach { i ->
                            Box(
                                Modifier
                                    .size(if (i == viewModel.metaIndex) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == viewModel.metaIndex)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.siguienteMeta() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            "Siguiente →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Mi progreso esta semana ───────────────────────────────────
            CardLabel(iconRes = R.drawable.ic_calendar, text = "Mi progreso esta semana")
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(
                        width = 0.5.dp,
                        color = TealLight,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                val days = listOf("L", "M", "X", "J", "V", "S", "D")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    days.forEachIndexed { index, day ->
                        val completed = index < 3
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (completed) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check_circle),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceDim)
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        )
                                )
                            }
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (completed) TealDark
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Herramientas para este momento ────────────────────────────
            CardLabel(iconRes = R.drawable.ic_brain, text = "Herramientas para este momento")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HerramientaCard(
                    iconRes = R.drawable.ic_flame,
                    titulo = "Manejo del craving",
                    subtitulo = "Técnicas para momentos difíciles",
                    onClick = onNavigateToManejoCraving,
                    modifier = Modifier.weight(1f)
                )
                HerramientaCard(
                    iconRes = R.drawable.ic_info,
                    titulo = "Info por sustancia",
                    subtitulo = "Efectos y reducción de daños",
                    onClick = onNavigateToInfoSustancia,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HerramientaCard(
    @DrawableRes iconRes: Int,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
            .border(
                width = 0.5.dp,
                color = TealLight,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TealLightest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
