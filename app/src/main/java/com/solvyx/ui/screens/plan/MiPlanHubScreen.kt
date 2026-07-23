package com.solvyx.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.screens.guias.components.GuiaPanel
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.HeroSideBerto

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
            PlanGoalCard(
                metaIndex = viewModel.metaIndex,
                metasList = viewModel.metasList,
                metaLogradaHoy = viewModel.metaLogradaHoy,
                onSiguienteMeta = { viewModel.siguienteMeta() },
                onToggleMetaLograda = { viewModel.toggleMetaLograda() }
            )

            Spacer(Modifier.height(20.dp))

            PlanQuickTools(
                onNavigateToManejoCraving = onNavigateToManejoCraving,
                onNavigateToInfoSustancia = onNavigateToInfoSustancia
            )
        }
    }
}
