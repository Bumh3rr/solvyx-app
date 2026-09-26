package com.solvyx.ui.screens.firstaid.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxMenuButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.firstaid.components.HelpLinesCard
import com.solvyx.ui.screens.firstaid.components.ProfessionalCareDisclaimer
import com.solvyx.ui.screens.firstaid.content.HubContent
import com.solvyx.ui.screens.firstaid.model.FirstAidRoute

/**
 * Entry of "Primeros auxilios". Orchestrates the sections (hero, urgent card, situation grid,
 * quick calm, help lines and professional help); each one lives in its own file under `hub/`.
 */
@Composable
fun FirstAidHubScreen(
    onOpenDrawer: () -> Unit,
    onOpen: (FirstAidRoute) -> Unit,
    onSos: () -> Unit,
    onOpenDirectory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = "Primeros auxilios",
            navigationButton = { SolvyxMenuButton(onClick = onOpenDrawer) }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HubHero()
            UrgentCrisisCard(
                onOpenCrisisGuide = { onOpen(FirstAidRoute.CRISIS) },
                onSos = onSos
            )
            SituationGrid(situations = HubContent.situations, onOpen = onOpen)
            QuickCalmCard(onStartGrounding = { onOpen(FirstAidRoute.GROUNDING) })
            HelpLinesCard()
            ProfessionalHelpCard(onOpenDirectory = onOpenDirectory)
            ProfessionalCareDisclaimer()
        }
    }
}
