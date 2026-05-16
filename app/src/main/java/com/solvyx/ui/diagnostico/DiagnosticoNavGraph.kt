package com.solvyx.ui.diagnostico

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun DiagnosticoNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "selection") {
        composable("selection") {
            SubstanceSelectionScreen(navController = navController)
        }

        composable(
            route = "questions/{sustancia}",
            arguments = listOf(navArgument("sustancia") { type = NavType.StringType })
        ) { backStackEntry ->
            val sustancia = backStackEntry.arguments?.getString("sustancia")
            QuestionsScreen(navController = navController, sustanciaArg = sustancia)
        }

        composable("result") {
            ResultScreen(navController = navController)
        }

        composable("history") {
            HistoryScreen(navController = navController)
        }
    }
}