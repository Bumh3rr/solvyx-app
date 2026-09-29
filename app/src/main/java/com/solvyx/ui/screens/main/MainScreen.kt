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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.solvyx.ui.components.haze.LocalHazeState
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.screens.home.HomeViewModel
import com.solvyx.ui.screens.directory.DirectoryScreen
import com.solvyx.ui.screens.profile.ProfileNavGraph
import com.solvyx.ui.screens.firstaid.navigation.FirstAidNavGraph
import com.solvyx.ui.screens.home.HomeScreen
import com.solvyx.ui.screens.plan.PlanNavGraph
import com.solvyx.ui.screens.journey.JourneyScreen
import com.solvyx.ui.screens.red.RedApoyoScreen
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary
import dev.chrisbanes.haze.HazeState
import java.time.LocalDate

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToChatFromDrawer: (() -> Unit)? = null,
    onNavigateToSos: () -> Unit = {},
    onNavigateToAssist: () -> Unit = {},
    onNavigateToEjercicio: () -> Unit = {},
    onNavigateToCheckIn: (edit: Boolean) -> Unit = {},
    onNavigateToDiary: (day: LocalDate?) -> Unit = {},
    onNavigateToCrearCuenta: () -> Unit = {},
    openDrawerOnReturn: Boolean = false,
    onDrawerOpened: () -> Unit = {},
    initialTab: NavigationItem? = null,
    onInitialTabConsumed: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    var drawerState by rememberSaveable { mutableStateOf(CustomDrawerState.Closed) }
    var selectedItem by rememberSaveable { mutableStateOf(NavigationItem.Inicio) }
    var previousItem by rememberSaveable { mutableStateOf<NavigationItem?>(null) }
    fun navigateToTab(item: NavigationItem) {
        previousItem = selectedItem
        selectedItem = item
    }
    val hazeState = remember { HazeState() }

    LaunchedEffect(openDrawerOnReturn) {
        if (openDrawerOnReturn) {
            drawerState = CustomDrawerState.Opened
            onDrawerOpened()
        }
    }

    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            selectedItem = initialTab
            onInitialTabConsumed()
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

    BackHandler(enabled = !drawerState.isOpened() && selectedItem != NavigationItem.Inicio) {
        navigateToTab(NavigationItem.Inicio)
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
            userNickname = homeViewModel.nickname,
            onNavigationItemClick = { item ->
                when (item) {
                    NavigationItem.Berto -> {
                        drawerState = CustomDrawerState.Closed
                        (onNavigateToChatFromDrawer ?: onNavigateToChat)()
                    }
                    else -> {
                        navigateToTab(item)
                        drawerState = CustomDrawerState.Closed
                    }
                }
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            onProfileClick = {
                navigateToTab(NavigationItem.MiPerfil)
                drawerState = CustomDrawerState.Closed
            }
        )

        // ── Capa 2: contenido principal (animado, encima) ──
        CompositionLocalProvider(LocalHazeState provides hazeState) {
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
                previousItem = previousItem,
                drawerState = drawerState,
                onDrawerClick = { drawerState = drawerState.opposite() },
                onNavigateToChat = onNavigateToChat,
                onNavigateToSos = onNavigateToSos,
                onNavigateToAssist = onNavigateToAssist,
                onNavigateToEjercicio = onNavigateToEjercicio,
                onNavigateToCheckIn = onNavigateToCheckIn,
                onNavigateToDiary = onNavigateToDiary,
                onNavigateToCrearCuenta = onNavigateToCrearCuenta,
                onBottomNavNavigate = { item -> navigateToTab(item) },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun SolvyxMainContent(
    modifier: Modifier = Modifier,
    selectedItem: NavigationItem,
    previousItem: NavigationItem?,
    drawerState: CustomDrawerState,
    onDrawerClick: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToAssist: () -> Unit,
    onNavigateToEjercicio: () -> Unit = {},
    onNavigateToCheckIn: (edit: Boolean) -> Unit = {},
    onNavigateToDiary: (day: LocalDate?) -> Unit = {},
    onNavigateToCrearCuenta: () -> Unit = {},
    onLogout: () -> Unit,
    onBottomNavNavigate: (NavigationItem) -> Unit
) {
    var showSosDialog by remember { mutableStateOf(false) }

    val showBottomBar = selectedItem in listOf(
        NavigationItem.Inicio, NavigationItem.Plan, NavigationItem.Journey
    )

    val selectedTab = when (selectedItem) {
        NavigationItem.Plan    -> SolvyxBottomTab.PLAN
        NavigationItem.Journey -> SolvyxBottomTab.JOURNEY
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
                            SolvyxBottomTab.INICIO   -> onBottomNavNavigate(NavigationItem.Inicio)
                            SolvyxBottomTab.PLAN     -> onBottomNavNavigate(NavigationItem.Plan)
                            SolvyxBottomTab.JOURNEY  -> onBottomNavNavigate(NavigationItem.Journey)
                        }
                    },
                    onSosClick = { showSosDialog = true }
                )
            }
        }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (selectedItem) {
                NavigationItem.Inicio ->
                    HomeScreen(
                        onOpenDrawer = onDrawerClick,
                        drawerState = drawerState,
                        onNavigateToRedApoyo  = { onBottomNavNavigate(NavigationItem.RedApoyo) },
                        onNavigateToChat      = onNavigateToChat,
                        onNavigateToEjercicio = onNavigateToEjercicio,
                        onNavigateToPlan      = { onBottomNavNavigate(NavigationItem.Plan) },
                        onNavigateToJourney   = { onBottomNavNavigate(NavigationItem.Journey) },
                        onNavigateToGuias     = { onBottomNavNavigate(NavigationItem.GuiasPrimerosAuxilios) },
                        onNavigateToAssist    = onNavigateToAssist,
                        onNavigateToCrearCuenta = onNavigateToCrearCuenta
                    )
                NavigationItem.Plan ->
                    PlanNavGraph(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToSos = onNavigateToSos,
                        onNavigateToRedApoyo = { onBottomNavNavigate(NavigationItem.RedApoyo) }
                    )
                NavigationItem.Journey ->
                    JourneyScreen(
                        onOpenDrawer = onDrawerClick,
                        onCreateAccount = onNavigateToCrearCuenta,
                        onOpenCheckIn = onNavigateToCheckIn,
                        onOpenDiary = onNavigateToDiary
                    )
                NavigationItem.GuiasPrimerosAuxilios ->
                    FirstAidNavGraph(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToSos = onNavigateToSos,
                        onNavigateToDirectory = { onBottomNavNavigate(NavigationItem.Directorio) }
                    )
                NavigationItem.RedApoyo ->
                    RedApoyoScreen(
                        isSetupMode = false,
                        onBack = { onBottomNavNavigate(previousItem ?: NavigationItem.Inicio) },
                        onFinishSetup = {}
                    )
                NavigationItem.Directorio ->
                    DirectoryScreen(
                        onOpenDrawer = onDrawerClick,
                        onNavigateToChat = onNavigateToChat
                    )
                NavigationItem.MiPerfil ->
                    ProfileNavGraph(
                        onOpenDrawer = onDrawerClick,
                        onOpenJourney = { onBottomNavNavigate(NavigationItem.Journey) },
                        onOpenAssessment = onNavigateToAssist,
                        onOpenSupportNetwork = { onBottomNavNavigate(NavigationItem.RedApoyo) },
                        onCreateAccount = onNavigateToCrearCuenta,
                        onLoggedOut = onLogout
                    )
                NavigationItem.Berto -> { /* navega fuera del MainScreen via onNavigateToChat */ }
            }
        }
    }
}
