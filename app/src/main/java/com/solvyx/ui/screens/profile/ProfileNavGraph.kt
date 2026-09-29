package com.solvyx.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.navigation.navigateUpFrom
import com.solvyx.ui.screens.profile.legal.AboutScreen
import com.solvyx.ui.screens.profile.legal.PrivacyScreen
import com.solvyx.ui.screens.profile.legal.TermsScreen

private const val ROUTE_MAIN = "profile_main"
private const val ROUTE_PRIVACY = "profile_privacy"
private const val ROUTE_ABOUT = "profile_about"
private const val ROUTE_TERMS = "profile_terms"

/** Own NavHost of Mi perfil: the main screen plus its three information sub-screens. */
@Composable
fun ProfileNavGraph(
    onOpenDrawer: () -> Unit,
    onOpenJourney: () -> Unit,
    onOpenAssessment: () -> Unit,
    onOpenSupportNetwork: () -> Unit,
    onCreateAccount: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            ProfileScreen(
                onOpenDrawer = onOpenDrawer,
                onOpenJourney = onOpenJourney,
                onOpenAssessment = onOpenAssessment,
                onOpenSupportNetwork = onOpenSupportNetwork,
                onOpenPrivacy = { navController.navigate(ROUTE_PRIVACY) },
                onOpenAbout = { navController.navigate(ROUTE_ABOUT) },
                onOpenTerms = { navController.navigate(ROUTE_TERMS) },
                onCreateAccount = onCreateAccount,
                onLoggedOut = onLoggedOut
            )
        }
        // navigateUpFrom: a double back press must not pop past Mi perfil (that relaunches the app).
        composable(ROUTE_PRIVACY) { entry -> PrivacyScreen(onBack = { navController.navigateUpFrom(entry) }) }
        composable(ROUTE_ABOUT) { entry ->
            AboutScreen(
                onBack = { navController.navigateUpFrom(entry) },
                onOpenTerms = { navController.navigate(ROUTE_TERMS) },
                onOpenPrivacy = { navController.navigate(ROUTE_PRIVACY) }
            )
        }
        composable(ROUTE_TERMS) { entry -> TermsScreen(onBack = { navController.navigateUpFrom(entry) }) }
    }
}
