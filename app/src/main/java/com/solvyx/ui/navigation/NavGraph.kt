package com.solvyx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.screens.auth.choice.AuthChoiceScreen
import com.solvyx.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.solvyx.ui.screens.auth.login.LoginScreen
import com.solvyx.ui.screens.auth.register.RegisterScreen
import com.solvyx.ui.screens.onboarding.OnboardingScreen
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
        composable(Routes.HOME) {
            // Simple home screen for logged-in users
            com.solvyx.ui.screens.home.HomeScreen(navController)
        }
        composable(Routes.DIAGNOSTICO) {
            val diagnosticoNavController = rememberNavController()
            DiagnosticoNavGraph(
                navController = diagnosticoNavController,
                onFinishAssist = { navController.popBackStack() }
            )
        }
    }
}
