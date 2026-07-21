package com.solvyx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.solvyx.ui.screens.perfil.PrivacidadDatosScreen
import com.solvyx.ui.screens.perfil.TerminosCondicionesScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.screens.splash.SplashScreen
import com.solvyx.backend.router.Destino

fun Destino.aRuta(): String = when (this) {
    is Destino.AuthChoice -> Routes.AUTH_CHOICE
    is Destino.HomeDirecto -> Routes.HOME
    is Destino.AssistPendiente -> Routes.DIAGNOSTICO
    is Destino.RedApoyoSetupOmitible -> "${Routes.RED_APOYO_SETUP}?omitible=true"
}

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
@Composable
fun SolvyxNavGraph(
    navController: NavHostController,
    pendingShortcut: ShortcutDestination? = null,
    onShortcutHandled: () -> Unit = {}
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Un App Shortcut se apila sobre la pantalla base en vez de reemplazarla, para que
    // "Cancelar" en el SOS o "atrás" en Berto regresen a donde el usuario estaría normalmente
    // en vez de tirarlo fuera de la app.
    //
    // Cubre los dos casos con una sola regla:
    //  - Arranque en frío: espera a que el Splash resuelva la base y recién ahí apila.
    //  - App ya abierta (onNewIntent): la base ya existe, así que apila de inmediato.
    LaunchedEffect(pendingShortcut, currentRoute) {
        val destino = pendingShortcut ?: return@LaunchedEffect
        if (currentRoute == null || currentRoute == Routes.SPLASH) return@LaunchedEffect
        if (currentRoute != destino.route) {
            navController.navigate(destino.route)
        }
        onShortcutHandled()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController, skipBranding = pendingShortcut != null)
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
        composable(Routes.TERMINOS) {
            TerminosCondicionesScreen(onBack = { navController.navigateUp() })
        }
        composable(Routes.PRIVACIDAD) {
            PrivacidadDatosScreen(onBack = { navController.navigateUp() })
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

        composable(
            route = "${Routes.RED_APOYO_SETUP}?omitible={omitible}",
            arguments = listOf(navArgument("omitible") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val omitible = backStackEntry.arguments?.getBoolean("omitible") ?: false
            RedApoyoScreen(
                isSetupMode = true,
                esOmitible = omitible,
                onBack = { navController.navigateUp() },
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
                },
                onNavigateToCrearCuenta = {
                    navController.navigate(Routes.REGISTER)
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
