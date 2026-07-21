package com.solvyx.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.components.drawer.model.CustomDrawerState
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.screens.red.RedApoyoViewModel
import dev.chrisbanes.haze.haze

/**
 * Pantalla de Inicio. Orquesta las secciones (barra superior, hero, banners condicionales, racha,
 * ánimo, accesos rápidos y el cierre de Berto); cada sección vive en su propio archivo `Home*`.
 * Aquí solo queda el estado de la pantalla y el ensamblado.
 */
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    drawerState: CustomDrawerState,
    onNavigateToRedApoyo: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToSos: () -> Unit = {},
    onNavigateToEjercicio: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToRegistro: () -> Unit = {},
    onNavigateToGuias: () -> Unit = {},
    onNavigateToAssist: () -> Unit = {},
    onNavigateToCrearCuenta: () -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val redApoyoViewModel: RedApoyoViewModel = hiltViewModel()
    val contactCount = redApoyoViewModel.contactos.count { it.name.isNotBlank() }

    var showSosDialog by remember { mutableStateOf(false) }
    var sosBannerDescartado by remember { mutableStateOf(false) }
    var assistBannerDescartado by remember { mutableStateOf(false) }
    var registroBannerDescartado by remember { mutableStateOf(false) }

    if (showSosDialog) {
        SosConfirmationDialog(
            onConfirm = { showSosDialog = false; onNavigateToSos() },
            onDismiss = { showSosDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTopBar(onOpenDrawer = onOpenDrawer, drawerState = drawerState)

        // El contenido scrolleable es la fuente del blur del bottom nav (ver LocalHazeState).
        val hazeState = LocalHazeState.current
        Column(
            modifier = Modifier
                .weight(1f)
                .haze(
                    hazeState,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    tint = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    blurRadius = 16.dp
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            HomeHeroSection(
                estadoAnimo = viewModel.moodToday ?: "neutral",
                rachaActual = viewModel.streak
            )

            DismissibleBanner(visible = contactCount == 0 && !sosBannerDescartado) {
                SosWarningBanner(
                    onConfigClick = onNavigateToRedApoyo,
                    onDismiss = { sosBannerDescartado = true }
                )
            }

            DismissibleBanner(visible = !viewModel.isAssistCompleted && !assistBannerDescartado) {
                InfoBanner(
                    titulo = "Completa tu diagnóstico",
                    subtitulo = "El ASSIST ayuda a Berto a darte mejores recomendaciones.",
                    accion = "Completar →",
                    onAccionClick = onNavigateToAssist,
                    onDismiss = { assistBannerDescartado = true }
                )
            }

            DismissibleBanner(visible = viewModel.isAnonymous && !registroBannerDescartado) {
                InfoBanner(
                    titulo = "Crea una cuenta",
                    subtitulo = "Guarda tu bitácora, metas y avances para no perderlos.",
                    accion = "Crear cuenta →",
                    onAccionClick = onNavigateToCrearCuenta,
                    onDismiss = { registroBannerDescartado = true }
                )
            }

            HomeStreakCard(
                streak = viewModel.streak,
                bestStreak = viewModel.bestStreak
            )

            Spacer(Modifier.height(12.dp))

            HomeMoodCard(
                moodToday = viewModel.moodToday,
                onMoodSelected = { viewModel.logMood(it) },
                onNavigateToChat = onNavigateToChat,
                onNavigateToEjercicio = onNavigateToEjercicio,
                onNavigateToRegistro = onNavigateToRegistro,
                onNavigateToRedApoyo = onNavigateToRedApoyo
            )

            Spacer(Modifier.height(16.dp))

            HomeQuickAccess(
                onNavigateToPlan = onNavigateToPlan,
                onNavigateToChat = onNavigateToChat,
                onNavigateToGuias = onNavigateToGuias,
                onNavigateToRegistro = onNavigateToRegistro,
                onNavigateToRedApoyo = onNavigateToRedApoyo
            )

            Spacer(Modifier.height(20.dp))

            HomeBertoFooter(onNavigateToChat = onNavigateToChat)

            Spacer(Modifier.height(SolvyxBottomNavHeight))
        }
    }
}

/**
 * Envuelve un banner con su animación de entrada/salida y el espacio inferior cuando está visible.
 * Recoge el patrón repetido de los tres banners condicionales de Home.
 */
@Composable
private fun DismissibleBanner(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
            content()
            Spacer(Modifier.height(16.dp))
        }
    }
}
