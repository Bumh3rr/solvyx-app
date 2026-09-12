package com.solvyx.ui.screens.profilesetup

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.screens.red.RedApoyoScreen

/**
 * 3-step wizard that runs once after creating an email account or converting an anonymous one:
 * Substances → ASSIST → Red de Apoyo (skippable). `startStep` comes from
 * `PostAuthRouter.resolver()` — lets the user resume at the right step if they closed the app
 * mid-wizard. The substances ViewModel is requested here (not inside each child composable) so
 * the "assist" step can read `viewModel.selectedSubstances` without asking again.
 */
@Composable
fun ProfileSetupNavGraph(
    startStep: ProfileSetupStep,
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: ProfileSetupViewModel = hiltViewModel()
    val startRoute = when (startStep) {
        ProfileSetupStep.SUBSTANCES -> "substances"
        ProfileSetupStep.ASSIST     -> "assist"
        ProfileSetupStep.RED_APOYO  -> "red_apoyo"
    }

    NavHost(navController = navController, startDestination = startRoute) {
        composable("substances") {
            SubstancesStepScreen(
                viewModel = viewModel,
                onContinue = {
                    viewModel.saveSubstancesAndContinue {
                        navController.navigate("assist") { popUpTo("substances") { inclusive = true } }
                    }
                }
            )
        }
        composable("assist") {
            // DiagnosticoNavGraph already requests its own DiagnosticoViewModel internally (see
            // Task 7) — no need to request it here too.
            val diagnosticoNavController = rememberNavController()
            DiagnosticoNavGraph(
                navController = diagnosticoNavController,
                onFinishAssist = {
                    navController.navigate("red_apoyo") { popUpTo("assist") { inclusive = true } }
                },
                isOnboarding = true,
                preselectedSubstances = viewModel.selectedSubstances
            )
        }
        composable("red_apoyo") {
            RedApoyoScreen(
                isSetupMode = true,
                esOmitible = true,
                onBack = { navController.navigateUp() },
                onFinishSetup = onFinish
            )
        }
    }
}
