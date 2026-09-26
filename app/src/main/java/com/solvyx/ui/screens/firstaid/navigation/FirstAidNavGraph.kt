package com.solvyx.ui.screens.firstaid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.screens.firstaid.crisischeck.CrisisCheckScreen
import com.solvyx.ui.screens.firstaid.guides.CravingGuideScreen
import com.solvyx.ui.screens.firstaid.guides.CrisisGuideScreen
import com.solvyx.ui.screens.firstaid.guides.OveruseGuideScreen
import com.solvyx.ui.screens.firstaid.guides.PanicGuideScreen
import com.solvyx.ui.screens.firstaid.hub.FirstAidHubScreen
import com.solvyx.ui.screens.firstaid.model.FirstAidRoute
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoScreen
import com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoViewModel

/**
 * Own NavHost of "Primeros auxilios" (hub + guides + grounding exercise). The SOS confirmation
 * lives here once, shared by every screen that offers "Avisar a mi red de apoyo".
 */
@Composable
fun FirstAidNavGraph(
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToDirectory: () -> Unit
) {
    val navController = rememberNavController()
    var showSosDialog by rememberSaveable { mutableStateOf(false) }
    val requestSos = { showSosDialog = true }
    val navigate = { route: FirstAidRoute -> navController.navigate(route.path) }
    val back: () -> Unit = { navController.navigateUp() }

    if (showSosDialog) {
        SosConfirmationDialog(
            onConfirm = {
                showSosDialog = false
                onNavigateToSos()
            },
            onDismiss = { showSosDialog = false }
        )
    }

    NavHost(navController = navController, startDestination = FirstAidRoute.HUB.path) {
        route(FirstAidRoute.HUB) {
            FirstAidHubScreen(
                onOpenDrawer = onOpenDrawer,
                onOpen = navigate,
                onSos = requestSos,
                onOpenDirectory = onNavigateToDirectory
            )
        }
        route(FirstAidRoute.CRISIS) {
            CrisisGuideScreen(onBack = back, onSos = requestSos, onTalkToBerto = onNavigateToChat)
        }
        route(FirstAidRoute.PANIC) {
            PanicGuideScreen(
                onBack = back,
                onSos = requestSos,
                onStartGrounding = { navigate(FirstAidRoute.GROUNDING) }
            )
        }
        route(FirstAidRoute.CRAVING) {
            CravingGuideScreen(onBack = back, onSos = requestSos)
        }
        route(FirstAidRoute.OVERUSE) {
            OveruseGuideScreen(onBack = back, onSos = requestSos)
        }
        route(FirstAidRoute.CRISIS_CHECK) {
            CrisisCheckScreen(
                onBack = back,
                onSos = requestSos,
                onOpenCrisisGuide = { navigate(FirstAidRoute.CRISIS) },
                onStartGrounding = { navigate(FirstAidRoute.GROUNDING) }
            )
        }
        route(FirstAidRoute.GROUNDING) {
            val viewModel: EjercicioGuiadoViewModel = hiltViewModel()
            EjercicioGuiadoScreen(viewModel = viewModel, onFinish = back)
        }
    }
}

private fun NavGraphBuilder.route(route: FirstAidRoute, content: @Composable () -> Unit) {
    composable(route.path) { content() }
}
