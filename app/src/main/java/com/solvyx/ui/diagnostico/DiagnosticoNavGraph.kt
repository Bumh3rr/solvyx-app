package com.solvyx.ui.diagnostico

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel

@Composable
fun DiagnosticoNavGraph(
    navController: NavHostController,
    onFinishAssist: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    val viewModel: DiagnosticoViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "selection") {

        composable("selection") {
            SubstanceSelectionScreen(
                viewModel = viewModel,
                onContinuar = {
                    viewModel.iniciarCuestionario()
                    navController.navigate("questions")
                },
                onVerHistorial = {
                    navController.navigate("history")
                }
            )
        }

        composable("questions") {
            QuestionsScreen(
                viewModel = viewModel,
                onAllCompleted = {
                    navController.navigate("result") {
                        popUpTo("questions") { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable("result") {
            ResultScreen(
                viewModel = viewModel,
                onReiniciar = {
                    navController.navigate("selection") {
                        popUpTo("selection") { inclusive = true }
                    }
                },
                onVerHistorial = {
                    navController.navigate("history")
                },
                onFinish = onFinishAssist,
                onNavigateToChat = onNavigateToHome,
                onNavigateToBitacora = onNavigateToHome,
                onNavigateToAvances = onNavigateToHome,
                onNavigateToManejoCraving = onNavigateToHome,
                onNavigateToInfoSustancia = onNavigateToHome,
                onNavigateToDirectorio = onNavigateToHome,
                onNavigateToRedApoyo = onNavigateToHome
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
