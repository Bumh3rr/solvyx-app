package com.solvyx.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.solvyx.ui.screens.auth.AuthChoiceScreen
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
            // TODO: LoginScreen(navController)
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }
        composable(Routes.REGISTER) {
            // TODO: RegisterScreen(navController)
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }
    }
}
