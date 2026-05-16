package com.solvyx.ui.diagnostico

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel

@Composable
fun DiagnosticoNavGraph() {
    val navController = rememberNavController()

    // Obtain a single shared DiagnosticoViewModel instance and pass it to all destinations
    val sharedViewModel: DiagnosticoViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "selection") {
        composable("selection") {
            SubstanceSelectionScreen(navController = navController, viewModel = sharedViewModel)
        }

        composable(
            route = "questions/{sustancia}",
            arguments = listOf(navArgument("sustancia") { type = NavType.StringType })
        ) { backStackEntry ->
            val sustancia = backStackEntry.arguments?.getString("sustancia")
            QuestionsScreen(navController = navController, sustanciaArg = sustancia, viewModel = sharedViewModel)
        }

        composable("result") {
            ResultScreen(navController = navController, viewModel = sharedViewModel)
        }

        composable("history") {
            HistoryScreen(navController = navController, viewModel = sharedViewModel)
        }
    }
}