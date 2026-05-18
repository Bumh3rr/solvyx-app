package com.solvyx.ui.screens.plan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.components.dialog.SosConfirmationDialog

@Composable
fun PlanNavGraph(
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToSos: () -> Unit = {},
    onNavigateToRedApoyo: () -> Unit = {}
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

    NavHost(navController = navController, startDestination = "planHub") {
        composable("planHub") {
            MiPlanHubScreen(
                onOpenDrawer = onOpenDrawer,
                onNavigateToManejoCraving = { navController.navigate("manejo_craving") },
                onNavigateToInfoSustancia = { navController.navigate("info_sustancia") }
            )
        }
        composable("manejo_craving") {
            ManejoCravingScreen(
                onBack = { navController.navigateUp() },
                onNavigateToEjercicio = { /* ejercicio route comes later */ },
                onNavigateToRedApoyo = onNavigateToRedApoyo
            )
        }
        composable("info_sustancia") {
            InfoSustanciaScreen(onBack = { navController.navigateUp() })
        }
    }
}
