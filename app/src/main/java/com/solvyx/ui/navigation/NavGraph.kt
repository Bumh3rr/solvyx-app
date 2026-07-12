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
import com.solvyx.ui.screens.auth.choice.AuthChoiceScreen
import com.solvyx.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.solvyx.ui.screens.auth.login.LoginScreen
import com.solvyx.ui.screens.auth.onboarding.OnboardingScreen
import com.solvyx.ui.screens.auth.register.RegisterScreen
import com.solvyx.ui.screens.chatbot.BertoScreen
import com.solvyx.ui.screens.ejercicios.EjercicioActivoScreen
import com.solvyx.ui.screens.ejercicios.EjercicioDetalleScreen
import com.solvyx.ui.screens.ejercicios.EjerciciosScreen
import com.solvyx.ui.screens.guias_extendidas.GuiaDetalleScreen
import com.solvyx.ui.screens.guias_extendidas.GuiasExtendidasScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoViewModel
import com.solvyx.ui.screens.insights.InsightsScreen
import com.solvyx.ui.screens.journaling.JournalingEditorScreen
import com.solvyx.ui.screens.journaling.JournalingScreen
import com.solvyx.ui.screens.lecciones.LeccionDetalleScreen
import com.solvyx.ui.screens.lecciones.LeccionesScreen
import com.solvyx.ui.screens.main.MainScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.screens.rutinas.RutinaDetalleScreen
import com.solvyx.ui.screens.rutinas.RutinasScreen
import com.solvyx.ui.screens.sos.SosOverlayScreen
import com.solvyx.ui.screens.splash.SplashScreen

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
@Composable
fun SolvyxNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SolvyxRoutes.Splash.route
    ) {
        // ── Flujo de autenticación y onboarding ───────────────────────────

        composable(SolvyxRoutes.Splash.route) {
            SplashScreen(navController)
        }
        composable(SolvyxRoutes.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(SolvyxRoutes.AuthChoice.route) {
            AuthChoiceScreen(navController)
        }
        composable(SolvyxRoutes.Login.route) {
            LoginScreen(navController)
        }
        composable(SolvyxRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
        composable(SolvyxRoutes.Register.route) {
            RegisterScreen(navController)
        }

        composable(SolvyxRoutes.Diagnostico.route) {
            val diagnosticoNavController = rememberNavController()
            DiagnosticoNavGraph(
                navController = diagnosticoNavController,
                onFinishAssist = {
                    navController.navigate(SolvyxRoutes.RedApoyoSetup.route) {
                        popUpTo(SolvyxRoutes.Diagnostico.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(SolvyxRoutes.Home.route) {
                        popUpTo(SolvyxRoutes.Diagnostico.route) { inclusive = true }
                    }
                }
            )
        }

        composable(SolvyxRoutes.RedApoyoSetup.route) {
            RedApoyoScreen(
                isSetupMode = true,
                onBack = { navController.navigateUp() },
                onOpenDrawer = {},
                onFinishSetup = {
                    navController.navigate(SolvyxRoutes.Home.route) {
                        popUpTo(SolvyxRoutes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── HOME (shell con drawer + bottom nav) ──────────────────────────
        composable(SolvyxRoutes.Home.route) { backStackEntry ->
            val openDrawer by backStackEntry.savedStateHandle
                .getStateFlow("openDrawer", false)
                .collectAsState()

            MainScreen(
                openDrawerOnReturn = openDrawer,
                onDrawerOpened = { backStackEntry.savedStateHandle["openDrawer"] = false },
                onLogout = {
                    navController.navigate(SolvyxRoutes.AuthChoice.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(SolvyxRoutes.Chat.build())
                },
                onNavigateToChatFromDrawer = {
                    navController.navigate(SolvyxRoutes.Chat.build("drawer"))
                },
                onNavigateToSos = {
                    navController.navigate(SolvyxRoutes.SosOverlay.route)
                },
                onNavigateToAssist = {
                    navController.navigate(SolvyxRoutes.Diagnostico.route)
                },
                onNavigateToEjercicio = {
                    navController.navigate(SolvyxRoutes.EjercicioGuiado.route)
                },
                // ── Callbacks para las pantallas del drawer (Fase 1) ──
                onNavigateToDetalleEjercicio = { slug ->
                    navController.navigate(SolvyxRoutes.EjercicioDetalle.build(slug))
                },
                onNavigateToActivoEjercicio = { slug ->
                    navController.navigate(SolvyxRoutes.EjercicioActivo.build(slug))
                },
                onNavigateToDetalleGuia = { slug ->
                    navController.navigate(SolvyxRoutes.GuiaDetalle.build(slug))
                },
                onNavigateToDetalleLeccion = { sustancia, slug ->
                    navController.navigate(SolvyxRoutes.LeccionDetalle.build(sustancia, slug))
                },
                onNavigateToJournalingEditor = { promptSlug, promptTexto ->
                    navController.navigate(
                        SolvyxRoutes.JournalingEditor.build(promptSlug, promptTexto)
                    )
                },
                onNavigateToDetalleRutina = { slug ->
                    navController.navigate(SolvyxRoutes.RutinaDetalle.build(slug))
                },
                onNavigateToDescubrir = {
                    navController.navigate(SolvyxRoutes.Descubrir.route)
                }
            )
        }

        // ── Chat de Berto ────────────────────────────────────────────────
        composable(
            route = SolvyxRoutes.Chat.route,
            arguments = listOf(
                navArgument(SolvyxRoutes.Chat.ARG_SOURCE) { defaultValue = "" }
            )
        ) { backStackEntry ->
            val source = backStackEntry.arguments?.getString(SolvyxRoutes.Chat.ARG_SOURCE).orEmpty()
            BertoScreen(
                onBack = {
                    if (source == "drawer") {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("openDrawer", true)
                    }
                    navController.navigateUp()
                },
                onNavigateToSos = { navController.navigate(SolvyxRoutes.SosOverlay.route) }
            )
        }

        composable(SolvyxRoutes.SosOverlay.route) {
            SosOverlayScreen(
                onCancel = { navController.navigateUp() },
                onHablarConBerto = {
                    navController.navigate(SolvyxRoutes.Chat.build()) {
                        popUpTo(SolvyxRoutes.SosOverlay.route) { inclusive = true }
                    }
                },
                onClose = { navController.navigateUp() }
            )
        }

        composable(SolvyxRoutes.EjercicioGuiado.route) {
            val viewModel: EjercicioGuiadoViewModel = hiltViewModel()
            EjercicioGuiadoScreen(
                viewModel = viewModel,
                onFinish = { navController.navigateUp() }
            )
        }

        // ── Pantallas nuevas (Fase 1) ────────────────────────────────────

        // Ejercicios: listado, detalle, activo
        composable(SolvyxRoutes.Ejercicios.route) {
            EjerciciosScreen(
                onNavigateToDetalle = { slug ->
                    navController.navigate(SolvyxRoutes.EjercicioDetalle.build(slug))
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = SolvyxRoutes.EjercicioDetalle.route,
            arguments = listOf(navArgument(SolvyxRoutes.EjercicioDetalle.ARG_SLUG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString(
                SolvyxRoutes.EjercicioDetalle.ARG_SLUG
            ).orEmpty()
            EjercicioDetalleScreen(
                onNavigateToActivo = { _ ->
                    navController.navigate(SolvyxRoutes.EjercicioActivo.build(slug))
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = SolvyxRoutes.EjercicioActivo.route,
            arguments = listOf(navArgument(SolvyxRoutes.EjercicioActivo.ARG_SLUG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            // `slug` ya está disponible vía SavedStateHandle; la pantalla lo lee.
            EjercicioActivoScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Guías Extendidas: listado + detalle
        composable(SolvyxRoutes.GuiasExtendidas.route) {
            GuiasExtendidasScreen(
                onNavigateToDetalle = { slug ->
                    navController.navigate(SolvyxRoutes.GuiaDetalle.build(slug))
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = SolvyxRoutes.GuiaDetalle.route,
            arguments = listOf(navArgument(SolvyxRoutes.GuiaDetalle.ARG_SLUG) {
                type = NavType.StringType
            })
        ) {
            GuiaDetalleScreen(onNavigateBack = { navController.navigateUp() })
        }

        // Lecciones: listado por sustancia + detalle
        composable(SolvyxRoutes.Lecciones.route) {
            LeccionesScreen(
                onNavigateToDetalle = { sustancia, slug ->
                    navController.navigate(
                        SolvyxRoutes.LeccionDetalle.build(sustancia, slug)
                    )
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = SolvyxRoutes.LeccionDetalle.route,
            arguments = listOf(
                navArgument(SolvyxRoutes.LeccionDetalle.ARG_SUSTANCIA) {
                    type = NavType.StringType
                },
                navArgument(SolvyxRoutes.LeccionDetalle.ARG_SLUG) {
                    type = NavType.StringType
                }
            )
        ) {
            LeccionDetalleScreen(onNavigateBack = { navController.navigateUp() })
        }

        // Journaling: listado + editor
        composable(SolvyxRoutes.Journaling.route) {
            JournalingScreen(
                onNavigateToEditor = { promptSlug, promptTexto ->
                    navController.navigate(
                        SolvyxRoutes.JournalingEditor.build(promptSlug, promptTexto)
                    )
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = SolvyxRoutes.JournalingEditor.route,
            arguments = listOf(
                navArgument(SolvyxRoutes.JournalingEditor.ARG_PROMPT_SLUG) {
                    defaultValue = ""
                },
                navArgument(SolvyxRoutes.JournalingEditor.ARG_PROMPT_TEXTO) {
                    defaultValue = ""
                }
            )
        ) {
            JournalingEditorScreen(onNavigateBack = { navController.navigateUp() })
        }

        // Rutinas: listado + detalle
        composable(SolvyxRoutes.Rutinas.route) {
            RutinasScreen(
                onNavigateToDetalle = { slug ->
                    navController.navigate(SolvyxRoutes.RutinaDetalle.build(slug))
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = SolvyxRoutes.RutinaDetalle.route,
            arguments = listOf(navArgument(SolvyxRoutes.RutinaDetalle.ARG_SLUG) {
                type = NavType.StringType
            })
        ) {
            RutinaDetalleScreen(onNavigateBack = { navController.navigateUp() })
        }

        // Insights (sin detalle: top-level)
        composable(SolvyxRoutes.Insights.route) {
            InsightsScreen(onNavigateBack = { navController.navigateUp() })
        }

        // Descubrir (hub de features nuevas)
        composable(SolvyxRoutes.Descubrir.route) {
            com.solvyx.ui.screens.descubrir.DescubrirScreen(
                onNavigateToEjercicios = { navController.navigate(SolvyxRoutes.Ejercicios.route) },
                onNavigateToRutinas = { navController.navigate(SolvyxRoutes.Rutinas.route) },
                onNavigateToPsicoeducacion = { navController.navigate(SolvyxRoutes.Lecciones.route) },
                onNavigateToInsights = { navController.navigate(SolvyxRoutes.Insights.route) },
                onNavigateToJournaling = { navController.navigate(SolvyxRoutes.Journaling.route) },
                onNavigateToGuiasExtendidas = { navController.navigate(SolvyxRoutes.GuiasExtendidas.route) },
                onBack = { navController.navigateUp() }
            )
        }
    }
}
