package com.solvyx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.screens.sos.SosOverlayScreen
import com.solvyx.ui.screens.auth.choice.AuthChoiceScreen
import com.solvyx.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.solvyx.ui.screens.auth.login.LoginScreen
import com.solvyx.ui.screens.auth.register.RegisterScreen
import com.solvyx.ui.screens.chatbot.BertoScreen
import com.solvyx.ui.screens.main.MainScreen
import com.solvyx.ui.screens.onboarding.OnboardingScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.screens.splash.SplashScreen

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

        composable(Routes.HOME) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.AUTH_CHOICE) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT)
                },
                onNavigateToSos = {
                    navController.navigate(Routes.SOS_OVERLAY)
                }
            )
        }

        composable(Routes.CHAT) {
            BertoScreen(
                onBack = { navController.navigateUp() },
                onNavigateToSos = { navController.navigate(Routes.SOS_OVERLAY) }
            )
        }

        composable(Routes.SOS_OVERLAY) {
            SosOverlayScreen(
                contactos = listOf("Mamá", "Dr. Reyes", "Carlos"),
                telefonos = emptyList(),
                onCancel = { navController.navigateUp() },
                onHablarConBerto = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.SOS_OVERLAY) { inclusive = true }
                    }
                },
                onClose = { navController.navigateUp() }
            )
        }
    }
}
