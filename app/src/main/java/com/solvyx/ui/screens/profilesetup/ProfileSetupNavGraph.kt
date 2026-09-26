package com.solvyx.ui.screens.profilesetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.screens.profilesetup.components.SetupStepHeader
import com.solvyx.ui.screens.profilesetup.steps.AssistIntroScreen
import com.solvyx.ui.screens.profilesetup.steps.SetupDoneScreen
import com.solvyx.ui.screens.profilesetup.steps.SubstancesStepScreen
import com.solvyx.ui.screens.profilesetup.steps.WelcomeScreen
import com.solvyx.ui.screens.red.RedApoyoScreen

private const val ROUTE_WELCOME = "welcome"
private const val ROUTE_SUBSTANCES = "substances"
private const val ROUTE_ASSIST_INTRO = "assist_intro"
private const val ROUTE_ASSIST = "assist"
private const val ROUTE_SUPPORT_NETWORK = "support_network"
private const val ROUTE_DONE = "done"

/**
 * Setup wizard that runs once after creating an email account or converting an anonymous one:
 * welcome → Substances → ASSIST (intro + questions) → Red de apoyo (skippable) → done.
 * `startStep` comes from `PostAuthRouter.resolver()` so the user resumes at the first missing
 * step; the welcome screen only shows when starting from the beginning. Steps stay on the back
 * stack, so "atrás" goes to the previous step (the ASSIST already saved keeps its result).
 */
@Composable
fun ProfileSetupNavGraph(
    startStep: ProfileSetupStep,
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: ProfileSetupViewModel = hiltViewModel()
    val startRoute = when (startStep) {
        ProfileSetupStep.SUBSTANCES -> ROUTE_WELCOME
        ProfileSetupStep.ASSIST -> ROUTE_ASSIST_INTRO
        ProfileSetupStep.RED_APOYO -> ROUTE_SUPPORT_NETWORK
    }
    val backOrNull: (String) -> (() -> Unit)? = { route ->
        if (route == startRoute) null else ({ navController.navigateUp() })
    }

    NavHost(navController = navController, startDestination = startRoute) {
        composable(ROUTE_WELCOME) {
            WelcomeScreen(
                greeting = viewModel.greeting,
                onStart = { navController.navigate(ROUTE_SUBSTANCES) }
            )
        }
        composable(ROUTE_SUBSTANCES) {
            SubstancesStepScreen(
                selected = viewModel.selectedSubstances,
                onToggle = viewModel::toggleSubstance,
                onContinue = { viewModel.saveSubstancesAndContinue { navController.navigate(ROUTE_ASSIST_INTRO) } },
                onBack = backOrNull(ROUTE_SUBSTANCES)
            )
        }
        composable(ROUTE_ASSIST_INTRO) {
            AssistIntroScreen(
                substanceCount = viewModel.selectedSubstances.size,
                onStart = { navController.navigate(ROUTE_ASSIST) },
                onBack = backOrNull(ROUTE_ASSIST_INTRO)
            )
        }
        composable(ROUTE_ASSIST) {
            AssistStep(
                preselectedSubstances = viewModel.selectedSubstances,
                onBack = { navController.navigateUp() },
                onFinished = { navController.navigate(ROUTE_SUPPORT_NETWORK) }
            )
        }
        composable(ROUTE_SUPPORT_NETWORK) {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                SetupStepHeader(step = ProfileSetupStep.RED_APOYO, onBack = backOrNull(ROUTE_SUPPORT_NETWORK))
                RedApoyoScreen(
                    isSetupMode = true,
                    esOmitible = true,
                    showTopBar = false,
                    onBack = { navController.navigateUp() },
                    onFinishSetup = { navController.navigateToDone() }
                )
            }
        }
        composable(ROUTE_DONE) {
            SetupDoneScreen(
                substanceCount = viewModel.selectedSubstances.size,
                contactCount = viewModel.contactCount,
                onFinish = onFinish
            )
        }
    }
}

/** The ASSIST questionnaire under the shared step header (its own top bars are hidden). */
@Composable
private fun AssistStep(
    preselectedSubstances: Set<String>,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SetupStepHeader(step = ProfileSetupStep.ASSIST, onBack = onBack)
        DiagnosticoNavGraph(
            navController = rememberNavController(),
            onFinishAssist = onFinished,
            isOnboarding = true,
            preselectedSubstances = preselectedSubstances
        )
    }
}

/** "Done" replaces the whole wizard stack: going back from it would re-enter finished steps. */
private fun NavHostController.navigateToDone() {
    navigate(ROUTE_DONE) { popUpTo(graph.id) { inclusive = true } }
}
