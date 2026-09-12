package com.solvyx.ui.diagnostico

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun DiagnosticoNavGraph(
    navController: NavHostController,
    onFinishAssist: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToChat: () -> Unit = onNavigateToHome,
    onNavigateToRedApoyo: () -> Unit = onNavigateToHome,
    onNavigateToJourney: () -> Unit = onNavigateToHome,
    onNavigateToDirectorio: () -> Unit = onNavigateToHome,
    isOnboarding: Boolean = false,
    preselectedSubstances: Set<String> = emptySet()
) {
    val viewModel: DiagnosticoViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        if (shouldPreselectSubstances(isOnboarding, viewModel.sustanciasSeleccionadas, preselectedSubstances)) {
            viewModel.preseleccionar(preselectedSubstances)
            viewModel.iniciarCuestionario()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isOnboarding && preselectedSubstances.isNotEmpty()) "questions" else "selection"
    ) {

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
                isOnboarding = isOnboarding,
                onReiniciar = {
                    navController.navigate("selection") {
                        popUpTo("selection") { inclusive = true }
                    }
                },
                onVerHistorial = {
                    navController.navigate("history")
                },
                onFinish = onFinishAssist,
                onNavigateToChat = onNavigateToChat,
                onNavigateToBitacora = onNavigateToJourney,
                onNavigateToAvances = onNavigateToJourney,
                onNavigateToDirectorio = onNavigateToDirectorio,
                // Berto ya tiene árboles de craving/información por sustancia; no existen pantallas
                // dedicadas separadas, así que Chat es el destino real correcto para ambas.
                onNavigateToManejoCraving = onNavigateToChat,
                onNavigateToInfoSustancia = onNavigateToChat,
                onNavigateToRedApoyo = onNavigateToRedApoyo
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
