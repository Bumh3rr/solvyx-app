package com.solvyx.ui.screens.plan

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val PLAN_HUB = "planHub"
private const val SUBSTANCE_INFO = "info_sustancia"

/**
 * Own NavHost of "Mi plan". The quick tools open full-screen routes of the main graph (craving
 * guide, breathing, 5-4-3-2-1, chat), so going back from them returns here.
 */
@Composable
fun PlanNavGraph(
    onOpenDrawer: () -> Unit,
    onOpenCravingGuide: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenGrounding: () -> Unit,
    onNavigateToChat: () -> Unit,
    onOpenDirectory: () -> Unit,
    onCreateAccount: () -> Unit,
    onOpenCheckIn: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = PLAN_HUB) {
        composable(PLAN_HUB) {
            MiPlanHubScreen(
                onOpenDrawer = onOpenDrawer,
                onOpenCravingGuide = onOpenCravingGuide,
                onOpenBreathing = onOpenBreathing,
                onOpenGrounding = onOpenGrounding,
                onOpenChat = onNavigateToChat,
                onOpenSubstanceInfo = { navController.navigate(SUBSTANCE_INFO) },
                onOpenDirectory = onOpenDirectory,
                onCreateAccount = onCreateAccount,
                onOpenCheckIn = onOpenCheckIn
            )
        }
        composable(SUBSTANCE_INFO) {
            InfoSustanciaScreen(onBack = { navController.navigateUp() })
        }
    }
}
