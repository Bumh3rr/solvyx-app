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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.navigation.SolvyxBottomNavigationBar
import com.solvyx.ui.components.navigation.SolvyxBottomTab
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.components.drawer.CustomDrawer
import com.solvyx.ui.components.drawer.model.CustomDrawerState
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.components.drawer.model.isOpened
import com.solvyx.ui.components.drawer.model.opposite
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.screens.bitacora.RegistroEmocionalScreen
import com.solvyx.ui.screens.home.InicioViewModel
import com.solvyx.ui.screens.directorio.DirectorioRootScreen
import com.solvyx.ui.screens.perfil.PerfilNavGraph
import com.solvyx.ui.screens.guias.navigation.GuiasNavGraph
import com.solvyx.ui.screens.home.InicioScreen
import com.solvyx.ui.screens.plan.PlanNavGraph
import com.solvyx.ui.screens.avances.MisAvancesScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToChatFromDrawer: (() -> Unit)? = null,
    onNavigateToSos: () -> Unit = {},
    onNavigateToAssist: () -> Unit = {},
    onNavigateToEjercicio: () -> Unit = {},
    openDrawerOnReturn: Boolean = false,
    onDrawerOpened: () -> Unit = {}
) {
    val inicioViewModel: InicioViewModel = hiltViewModel()
    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    var selectedItem by remember { mutableStateOf(NavigationItem.Inicio) }

    LaunchedEffect(openDrawerOnReturn) {
        if (openDrawerOnReturn) {
            drawerState = CustomDrawerState.Opened
            onDrawerOpened()
        }
    }

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val offsetValue = with(density) {
        (windowInfo.containerSize.width * 0.60f).toDp()
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
            contentScale = ContentScale.FillWidth
        )

        // ── Capa 1: drawer (fijo, debajo en z-order) ──────
        CustomDrawer(
            selectedNavigationItem = selectedItem,
            userNickname = inicioViewModel.apodo,
            onNavigationItemClick = { item ->
                when (item) {
                    NavigationItem.CerrarSesion -> onLogout()
                    NavigationItem.Berto -> {
                        drawerState = CustomDrawerState.Closed
                        (onNavigateToChatFromDrawer ?: onNavigateToChat)()
                    }
                    else -> {
                        selectedItem = item
                        drawerState = CustomDrawerState.Closed
                    }
                }
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            onProfileClick = {
                selectedItem = NavigationItem.MiPerfil
                drawerState = CustomDrawerState.Closed
            }
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
            onNavigateToChat = onNavigateToChat,
            onNavigateToSos = onNavigateToSos,
            onNavigateToAssist = onNavigateToAssist,
            onNavigateToEjercicio = onNavigateToEjercicio,
            onBottomNavNavigate = { item -> selectedItem = item },
            onLogout = onLogout
        )
    }
}

@Composable
private fun SolvyxMainContent(
    modifier: Modifier = Modifier,
    selectedItem: NavigationItem,
    drawerState: CustomDrawerState,
    onDrawerClick: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToAssist: () -> Unit,
    onNavigateToEjercicio: () -> Unit = {},
    onLogout: () -> Unit,
    onBottomNavNavigate: (NavigationItem) -> Unit
) {
    var showSosDialog by remember { mutableStateOf(false) }

    val showBottomBar = selectedItem in listOf(
        NavigationItem.Inicio, NavigationItem.Plan,
        NavigationItem.RegistroEmocional, NavigationItem.Avances
    )

    val selectedTab = when (selectedItem) {
        NavigationItem.Plan    -> SolvyxBottomTab.PLAN
        NavigationItem.Avances -> SolvyxBottomTab.AVANCES
        else                   -> SolvyxBottomTab.INICIO
    }

    if (showSosDialog) {
        SosConfirmationDialog(
            onConfirm = {
                showSosDialog = false
                onNavigateToSos()
            },
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
                            SolvyxBottomTab.CHATBOT  -> onNavigateToChat()
                            SolvyxBottomTab.PLAN     -> onBottomNavNavigate(NavigationItem.Plan)
                            SolvyxBottomTab.AVANCES  -> onBottomNavNavigate(NavigationItem.Avances)
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
                        drawerState = drawerState,
                        onNavigateToRedApoyo  = { onBottomNavNavigate(NavigationItem.RedApoyo) },
                        onNavigateToChat      = onNavigateToChat,
                        onNavigateToSos       = onNavigateToSos,
                        onNavigateToDirectorio = { onBottomNavNavigate(NavigationItem.Directorio) },
                        onNavigateToEjercicio = onNavigateToEjercicio,
                        onNavigateToPlan      = { onBottomNavNavigate(NavigationItem.Plan) },
                        onNavigateToRegistro  = { onBottomNavNavigate(NavigationItem.RegistroEmocional) },
                        onNavigateToGuias     = { onBottomNavNavigate(NavigationItem.GuiasPrimerosAuxilios) }
                    )
                NavigationItem.Plan ->
                    PlanNavGraph(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToSos = onNavigateToSos,
                        onNavigateToRedApoyo = { onBottomNavNavigate(NavigationItem.RedApoyo) }
                    )
                NavigationItem.RegistroEmocional ->
                    RegistroEmocionalScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.Avances ->
                    MisAvancesScreen(onOpenDrawer = onDrawerClick)
                NavigationItem.GuiasPrimerosAuxilios ->
                    GuiasNavGraph(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToSos = onNavigateToSos
                    )
                NavigationItem.RedApoyo ->
                    RedApoyoScreen(
                        isSetupMode = false,
                        onBack = {},
                        onOpenDrawer = onDrawerClick,
                        onFinishSetup = {}
                    )
                NavigationItem.Directorio ->
                    DirectorioRootScreen(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToChat = onNavigateToChat
                    )
                NavigationItem.MiPerfil ->
                    PerfilNavGraph(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToAssist = onNavigateToAssist,
                        onNavigateToRedApoyo = { onBottomNavNavigate(NavigationItem.RedApoyo) },
                        onLogout = onLogout
                    )
                NavigationItem.Berto -> { /* navega fuera del MainScreen via onNavigateToChat */ }
                NavigationItem.CerrarSesion -> { /* manejado en MainScreen */ }
            }
        }
    }
}
