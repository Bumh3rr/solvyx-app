package com.solvyx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.screens.sos.SosOverlayScreen
import com.solvyx.ui.screens.auth.choice.AuthChoiceScreen
import com.solvyx.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.solvyx.ui.screens.auth.login.LoginScreen
import com.solvyx.ui.screens.auth.register.RegisterScreen
import com.solvyx.ui.screens.chatbot.BertoScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoViewModel
import com.solvyx.ui.screens.main.MainScreen
import com.solvyx.ui.screens.auth.onboarding.OnboardingScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.screens.splash.SplashScreen

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
@Composable
fun SolvyxNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(navController)
        }
        composable(Routes.AUTH_CHOICE) {
            AuthChoiceScreen(navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController)
        }

        composable(Routes.DIAGNOSTICO) {
            val diagnosticoNavController = rememberNavController()
            DiagnosticoNavGraph(
                navController = diagnosticoNavController,
                onFinishAssist = {
                    navController.navigate(Routes.RED_APOYO_SETUP) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RED_APOYO_SETUP) {
            RedApoyoScreen(
                isSetupMode = true,
                onBack = { navController.navigateUp() },
                onOpenDrawer = {},
                onFinishSetup = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) { backStackEntry ->
            val openDrawer by backStackEntry.savedStateHandle
                .getStateFlow("openDrawer", false)
                .collectAsState()

            MainScreen(
                openDrawerOnReturn = openDrawer,
                onDrawerOpened = { backStackEntry.savedStateHandle["openDrawer"] = false },
                onLogout = {
                    navController.navigate(Routes.AUTH_CHOICE) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT)
                },
                onNavigateToChatFromDrawer = {
                    navController.navigate("${Routes.CHAT}?source=drawer")
                },
                onNavigateToSos = {
                    navController.navigate(Routes.SOS_OVERLAY)
                },
                onNavigateToAssist = {
                    navController.navigate(Routes.DIAGNOSTICO)
                },
                onNavigateToEjercicio = {
                    navController.navigate(Routes.EJERCICIO_GUIADO)
                }
            )
        }

        composable(
            route = "${Routes.CHAT}?source={source}",
            arguments = listOf(navArgument("source") { defaultValue = "" })
        ) { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: ""
            BertoScreen(
                onBack = {
                    if (source == "drawer") {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("openDrawer", true)
                    }
                    navController.navigateUp()
                },
                onNavigateToSos = { navController.navigate(Routes.SOS_OVERLAY) }
            )
        }

        composable(Routes.SOS_OVERLAY) {
            SosOverlayScreen(
                onCancel = { navController.navigateUp() },
                onHablarConBerto = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.SOS_OVERLAY) { inclusive = true }
                    }
                },
                onClose = { navController.navigateUp() }
            )
        }

        composable(Routes.EJERCICIO_GUIADO) {
            val viewModel: EjercicioGuiadoViewModel = hiltViewModel()
            EjercicioGuiadoScreen(
                viewModel = viewModel,
                onFinish = { navController.navigateUp() }
            )
        }
    }
}
