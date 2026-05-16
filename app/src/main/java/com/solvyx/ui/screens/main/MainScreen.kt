package com.solvyx.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.SolvyxBottomNavigationBar
import com.solvyx.ui.components.SolvyxBottomTab
import com.solvyx.ui.components.SosConfirmationDialog
import com.solvyx.ui.components.drawer.CustomDrawer
import com.solvyx.ui.components.drawer.model.CustomDrawerState
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.components.drawer.model.isOpened
import com.solvyx.ui.components.drawer.model.opposite
import com.solvyx.ui.screens.bitacora.RegistroEmocionalScreen
import com.solvyx.ui.screens.chatbot.BertoScreen
import com.solvyx.ui.screens.configuracion.ConfiguracionScreen
import com.solvyx.ui.screens.guias.GuiasScreen
import com.solvyx.ui.screens.home.InicioScreen
import com.solvyx.ui.screens.plan.PlanReduccionScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    userNickname: String = "Alex"
) {
    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    var selectedItem by remember { mutableStateOf(NavigationItem.Inicio) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density

    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * density).roundToInt() }
    }
    val offsetValue by remember {
        derivedStateOf { (screenWidth.value / 2.8f).dp }
    }

    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp,
        label = "Animated Offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f,
        label = "Animated Scale"
    )
    val animatedRadius by animateDpAsState(
        targetValue = if (drawerState.isOpened()) 26.dp else 0.dp,
        label = "Animated Radius"
    )

    BackHandler(enabled = drawerState.isOpened()) {
        drawerState = CustomDrawerState.Closed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(TealPrimary, TealDark)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Imagen decorativa superior ────────────────────
        Image(
            painter = painterResource(R.drawable.ic_decorations_hero_3_drawer),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            contentScale = ContentScale.FillWidth,
            alpha = 0.18f
        )

        // ── Capa 1: drawer (fijo, debajo en z-order) ──────
        CustomDrawer(
            selectedNavigationItem = selectedItem,
            userNickname = userNickname,
            onNavigationItemClick = { item ->
                if (item == NavigationItem.CerrarSesion) {
                    onLogout()
                } else {
                    selectedItem = item
                    drawerState = CustomDrawerState.Closed
                }
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            onProfileClick = { }
        )

        // ── Capa 2: contenido principal (animado, encima) ──
        SolvyxMainContent(
            modifier = Modifier
                .offset(x = animatedOffset)
                .scale(animatedScale)
                .clip(RoundedCornerShape(animatedRadius))
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(animatedRadius)
                )
                .clickable(
                    enabled = drawerState.isOpened(),
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    drawerState = CustomDrawerState.Closed
                },
            selectedItem = selectedItem,
            drawerState = drawerState,
            onDrawerClick = { drawerState = drawerState.opposite() },
            onBottomNavNavigate = { item -> selectedItem = item }
        )
    }
}

@Composable
private fun SolvyxMainContent(
    modifier: Modifier = Modifier,
    selectedItem: NavigationItem,
    drawerState: CustomDrawerState,
    onDrawerClick: () -> Unit,
    onBottomNavNavigate: (NavigationItem) -> Unit
) {
    var showSosDialog by remember { mutableStateOf(false) }

    val showBottomBar = selectedItem == NavigationItem.Inicio ||
                        selectedItem == NavigationItem.Berto ||
                        selectedItem == NavigationItem.RegistroEmocional

    val selectedTab = when (selectedItem) {
        NavigationItem.Berto            -> SolvyxBottomTab.CHATBOT
        NavigationItem.RegistroEmocional -> SolvyxBottomTab.REGISTRO
        else                             -> SolvyxBottomTab.INICIO
    }

    if (showSosDialog) {
        SosConfirmationDialog(
            onConfirm = { showSosDialog = false },
            onDismiss = { showSosDialog = false }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                SolvyxBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        when (tab) {
                            SolvyxBottomTab.CHATBOT  -> onBottomNavNavigate(NavigationItem.Berto)
                            SolvyxBottomTab.REGISTRO -> onBottomNavNavigate(NavigationItem.RegistroEmocional)
                            SolvyxBottomTab.INICIO   -> onBottomNavNavigate(NavigationItem.Inicio)
                        }
                    },
                    onSosClick = { showSosDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                NavigationItem.Inicio ->
                    InicioScreen(
                        onOpenDrawer = onDrawerClick,
                        drawerState = drawerState
                    )
                NavigationItem.Berto ->
                    BertoScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.PlanReduccion ->
                    PlanReduccionScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.RegistroEmocional ->
                    RegistroEmocionalScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.GuiasPrimerosAuxilios ->
                    GuiasScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.RedApoyo ->
                    RedApoyoScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.Configuracion ->
                    ConfiguracionScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.CerrarSesion -> { /* manejado en MainScreen */ }
            }
        }
    }
}
