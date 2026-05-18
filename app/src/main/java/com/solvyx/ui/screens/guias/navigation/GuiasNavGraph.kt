package com.solvyx.ui.screens.guias.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.components.SosConfirmationDialog
import com.solvyx.ui.screens.guias.screens.hub.GuiasHubScreen
import com.solvyx.ui.screens.guias.screens.GuiaCrisisIdScreen
import com.solvyx.ui.screens.guias.screens.panico.GuiaPanicoScreen
import com.solvyx.ui.screens.guias.screens.GuiaCravingIntensoScreen
import com.solvyx.ui.screens.guias.screens.GuiaConsumiDeMasScreen
import com.solvyx.ui.screens.guias.screens.GuiaEstoyEnCrisisScreen

@Composable
fun GuiasNavGraph(
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSos: () -> Unit = {}
) {
    val navController = rememberNavController()
    var showSosDialog by remember { mutableStateOf(false) }

    if (showSosDialog) {
        SosConfirmationDialog(
            onConfirm = {
                showSosDialog = false
                onNavigateToSos()
            },
            onDismiss = { showSosDialog = false }
        )
    }

    NavHost(navController = navController, startDestination = "guiasHub") {
        composable("guiasHub") {
            GuiasHubScreen(
                onOpenDrawer = onOpenDrawer,
                onOpenGuide = { id -> navController.navigate(id) }
            )
        }
        composable("crisisId") {
            GuiaCrisisIdScreen(
                onBack = { navController.navigateUp() },
                onSos = { showSosDialog = true }
            )
        }
        composable("panic") {
            GuiaPanicoScreen(
                onBack = { navController.navigateUp() },
                onSos = { showSosDialog = true }
            )
        }
        composable("craving") {
            GuiaCravingIntensoScreen(
                onBack = { navController.navigateUp() },
                onSos = { showSosDialog = true }
            )
        }
        composable("overuse") {
            GuiaConsumiDeMasScreen(
                onBack = { navController.navigateUp() },
                onSos = { showSosDialog = true }
            )
        }
        composable("crisis") {
            GuiaEstoyEnCrisisScreen(
                onBack = { navController.navigateUp() },
                onSos = { showSosDialog = true },
                onHablarConBerto = onNavigateToChat
            )
        }
    }
}
