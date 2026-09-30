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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.components.drawer.model.CustomDrawerState
import com.solvyx.ui.components.common.GuestLockOverlay
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.components.navigation.SolvyxBottomNavClearance
import com.solvyx.ui.screens.home.stage.HomeStage
import com.solvyx.ui.screens.home.stage.SuggestionAction
import com.solvyx.ui.screens.home.stage.rememberBertoStageState
import com.solvyx.ui.screens.red.RedApoyoViewModel
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * Pantalla de Inicio. Orquesta las secciones (barra superior, el jardín de Berto, banners condicionales, racha,
 * ánimo, accesos rápidos y el cierre de Berto); cada sección vive en su propio archivo `Home*`.
 * Aquí solo queda el estado de la pantalla y el ensamblado.
 */
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    drawerState: CustomDrawerState,
    onNavigateToRedApoyo: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToEjercicio: () -> Unit = {},
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToJourney: () -> Unit = {},
    onNavigateToGuias: () -> Unit = {},
    onNavigateToAssist: () -> Unit = {},
    onNavigateToCrearCuenta: () -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val redApoyoViewModel: RedApoyoViewModel = hiltViewModel()
    val contactCount = redApoyoViewModel.contactos.count { it.name.isNotBlank() }

    var sosBannerDescartado by rememberSaveable { mutableStateOf(false) }
    var assistBannerDescartado by rememberSaveable { mutableStateOf(false) }
    var registroBannerDescartado by rememberSaveable { mutableStateOf(false) }

    val stage = rememberBertoStageState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            HomeStage(
                state = stage,
                streak = viewModel.streak,
                nickname = viewModel.nickname,
                moodToday = viewModel.moodToday,
                introduceBerto = viewModel.introduceBerto,
                onGreeted = viewModel::onBertoGreeted,
                onSuggestionAction = { action ->
                    when (action) {
                        SuggestionAction.BREATHE -> onNavigateToBreathing()
                        SuggestionAction.GROUNDING -> onNavigateToEjercicio()
                        SuggestionAction.TALK -> onNavigateToChat()
                        SuggestionAction.SUPPORT_NETWORK -> onNavigateToRedApoyo()
                        SuggestionAction.JOURNAL -> onNavigateToJourney()
                        SuggestionAction.SELF_CARE -> Unit
                    }
                },
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
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

            GuestLockOverlay(
                locked = viewModel.isAnonymous,
                message = "Crea una cuenta para llevar tu racha de bienestar",
                onUnlock = onNavigateToCrearCuenta
            ) {
                HomeStreakCard(
                    streak = viewModel.streak,
                    bestStreak = viewModel.bestStreak
                )
            }

            Spacer(Modifier.height(12.dp))

            GuestLockOverlay(
                locked = viewModel.isAnonymous,
                message = "Crea una cuenta para registrar cómo te sientes",
                onUnlock = onNavigateToCrearCuenta
            ) {
                HomeMoodCard(
                    moodToday = viewModel.moodToday,
                    onMoodSelected = { mood ->
                        viewModel.logMood(mood)
                        // Berto is at the top: bring him into view, then he reacts.
                        scope.launch {
                            scrollState.animateScrollTo(0)
                            stage.reactTo(mood)
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            HomeQuickAccess(
                onNavigateToPlan = onNavigateToPlan,
                onNavigateToJourney = onNavigateToJourney,
                onNavigateToChat = onNavigateToChat,
                onNavigateToBreathing = onNavigateToBreathing,
                onNavigateToFirstAid = onNavigateToGuias,
                onNavigateToSupportNetwork = onNavigateToRedApoyo
            )

            Spacer(Modifier.height(20.dp))

            HomeBertoFooter(onNavigateToChat = onNavigateToChat)

            Spacer(Modifier.height(SolvyxBottomNavClearance))
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
