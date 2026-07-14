package com.solvyx.ui.screens.perfil

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun PerfilNavGraph(
    onOpenDrawer: () -> Unit,
    onNavigateToAssist: () -> Unit,
    onNavigateToRedApoyo: () -> Unit,
    onNavigateToAgregarCuenta: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "perfil_main") {
        composable("perfil_main") {
            MiPerfilScreen(
                onOpenDrawer = onOpenDrawer,
                onNavigateToAssist = onNavigateToAssist,
                onNavigateToRedApoyo = onNavigateToRedApoyo,
                onNavigateToPrivacidad = { navController.navigate("perfil_privacidad") },
                onNavigateToAcercaDe = { navController.navigate("perfil_acerca") },
                onNavigateToTerminos = { navController.navigate("perfil_terminos") },
                onNavigateToAgregarCuenta = onNavigateToAgregarCuenta,
                onLogout = onLogout
            )
        }
        composable("perfil_privacidad") {
            PrivacidadDatosScreen(onBack = { navController.navigateUp() })
        }
        composable("perfil_acerca") {
            AcercaDeSolvyxScreen(onBack = { navController.navigateUp() })
        }
        composable("perfil_terminos") {
            TerminosCondicionesScreen(onBack = { navController.navigateUp() })
        }
    }
}
