package com.solvyx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.solvyx.backend.router.Destino
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.screens.auth.choice.AuthChoiceScreen
import com.solvyx.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.solvyx.ui.screens.auth.login.LoginScreen
import com.solvyx.ui.screens.auth.onboarding.OnboardingScreen
import com.solvyx.ui.screens.auth.register.RegisterScreen
import com.solvyx.ui.screens.breathing.BreathingScreen
import com.solvyx.ui.screens.chatbot.BertoScreen
import com.solvyx.ui.screens.chatbot.CHAT_SUBSTANCE_ARG
import com.solvyx.ui.screens.chatbot.CHAT_TOPIC_ARG
import com.solvyx.ui.screens.chatbot.TopicIntent
import com.solvyx.ui.screens.firstaid.guides.CravingGuideScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoViewModel
import com.solvyx.ui.screens.journey.checkin.CHECK_IN_EDIT_ARG
import com.solvyx.ui.screens.journey.diary.DIARY_DAY_ARG
import com.solvyx.ui.screens.journey.diary.DiaryScreen
import com.solvyx.ui.screens.journey.checkin.CheckInScreen
import com.solvyx.ui.screens.main.MainScreen
import com.solvyx.ui.screens.profile.legal.PrivacyScreen
import com.solvyx.ui.screens.profile.legal.TermsScreen
import com.solvyx.ui.screens.profilesetup.ProfileSetupNavGraph
import com.solvyx.ui.screens.profilesetup.ProfileSetupStep
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.screens.sos.SosOverlayScreen
import com.solvyx.ui.screens.splash.SplashScreen

fun Destino.aRuta(): String = when (this) {
    is Destino.AuthChoice   -> Routes.AUTH_CHOICE
    is Destino.HomeDirecto  -> Routes.HOME
    is Destino.ProfileSetup -> "${Routes.PROFILE_SETUP}?step=${step.name}"
}

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
@Composable
fun SolvyxNavGraph(
    navController: NavHostController,
    pendingShortcut: ShortcutDestination? = null,
    onShortcutHandled: () -> Unit = {}
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
        composable(
            route = "${Routes.PROFILE_SETUP}?step={step}",
            arguments = listOf(navArgument("step") { defaultValue = ProfileSetupStep.SUBSTANCES.name })
        ) { backStackEntry ->
            val step = ProfileSetupStep.entries.firstOrNull {
                it.name == backStackEntry.arguments?.getString("step")
            } ?: ProfileSetupStep.SUBSTANCES
            ProfileSetupNavGraph(
                startStep = step,
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.TERMINOS) { entry ->
            TermsScreen(onBack = { navController.navigateUpFrom(entry) })
        }
        composable(Routes.PRIVACIDAD) { entry ->
            PrivacyScreen(onBack = { navController.navigateUpFrom(entry) })
        }

        composable(Routes.DIAGNOSTICO) {
            val diagnosticoNavController = rememberNavController()
            DiagnosticoNavGraph(
                navController = diagnosticoNavController,
                isOnboarding = false, // explicit: this call site is always a retake, never the wizard
                onFinishAssist = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                },
                onNavigateToRedApoyo = {
                    navController.navigate("${Routes.RED_APOYO_SETUP}?omitible=true") {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                },
                onNavigateToJourney = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set("initialTab", NavigationItem.Journey)
                },
                onNavigateToDirectorio = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DIAGNOSTICO) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set("initialTab", NavigationItem.Directorio)
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
            val initialTab by backStackEntry.savedStateHandle
                .getStateFlow<NavigationItem?>("initialTab", null)
                .collectAsState()

            MainScreen(
                openDrawerOnReturn = openDrawer,
                onDrawerOpened = { backStackEntry.savedStateHandle["openDrawer"] = false },
                initialTab = initialTab,
                onInitialTabConsumed = { backStackEntry.savedStateHandle["initialTab"] = null },
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
                onNavigateToChatAbout = { substanceId ->
                    val topic = TopicIntent.INFO.name.lowercase()
                    navController.navigate("${Routes.CHAT}?$CHAT_TOPIC_ARG=$topic&$CHAT_SUBSTANCE_ARG=$substanceId")
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
                onNavigateToCravingGuide = {
                    navController.navigate(Routes.CRAVING_GUIDE)
                },
                onNavigateToBreathing = {
                    navController.navigate(Routes.BREATHING)
                },
                onNavigateToCheckIn = { edit ->
                    navController.navigate("${Routes.CHECK_IN}?$CHECK_IN_EDIT_ARG=$edit")
                },
                onNavigateToDiary = { day ->
                    navController.navigate(if (day == null) Routes.DIARY else "${Routes.DIARY}?$DIARY_DAY_ARG=$day")
                },
                onNavigateToCrearCuenta = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(
            // topic + substance open the chat straight in a guide (e.g. "Pregúntale a Berto" in Plan).
            route = "${Routes.CHAT}?source={source}&$CHAT_TOPIC_ARG={$CHAT_TOPIC_ARG}&$CHAT_SUBSTANCE_ARG={$CHAT_SUBSTANCE_ARG}",
            arguments = listOf(
                navArgument("source") { defaultValue = "" },
                navArgument(CHAT_TOPIC_ARG) { defaultValue = "" },
                navArgument(CHAT_SUBSTANCE_ARG) { defaultValue = "" }
            )
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

        composable(
            route = "${Routes.CHECK_IN}?$CHECK_IN_EDIT_ARG={$CHECK_IN_EDIT_ARG}",
            arguments = listOf(navArgument(CHECK_IN_EDIT_ARG) {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { entry ->
            CheckInScreen(
                onClose = { navController.navigateUpFrom(entry) },
                onOpenFirstAid = {
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set("initialTab", NavigationItem.GuiasPrimerosAuxilios)
                    navController.navigateUpFrom(entry)
                }
            )
        }

        composable(
            route = "${Routes.DIARY}?$DIARY_DAY_ARG={$DIARY_DAY_ARG}",
            arguments = listOf(navArgument(DIARY_DAY_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { entry ->
            DiaryScreen(
                onBack = { navController.navigateUpFrom(entry) },
                onRegister = { navController.navigate("${Routes.CHECK_IN}?$CHECK_IN_EDIT_ARG=false") },
                onEditToday = { navController.navigate("${Routes.CHECK_IN}?$CHECK_IN_EDIT_ARG=true") }
            )
        }

        composable(Routes.EJERCICIO_GUIADO) {
            val viewModel: EjercicioGuiadoViewModel = hiltViewModel()
            EjercicioGuiadoScreen(
                viewModel = viewModel,
                onFinish = { navController.navigateUp() }
            )
        }

        // "Ganas muy fuertes" opened straight from Mi plan: full screen, so back returns to the plan.
        composable(Routes.CRAVING_GUIDE) { entry ->
            var showSosDialog by rememberSaveable { mutableStateOf(false) }
            if (showSosDialog) {
                SosConfirmationDialog(
                    onConfirm = {
                        showSosDialog = false
                        navController.navigate(Routes.SOS_OVERLAY)
                    },
                    onDismiss = { showSosDialog = false }
                )
            }
            CravingGuideScreen(
                onBack = { navController.navigateUpFrom(entry) },
                onSos = { showSosDialog = true }
            )
        }

        composable(Routes.BREATHING) { entry ->
            BreathingScreen(onFinish = { navController.navigateUpFrom(entry) })
        }
    }
}
