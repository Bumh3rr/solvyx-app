---
description: Navigation Compose para Solvyx. NavHost, rutas, argumentos tipados, deep links, integración con Hilt.
---

# Skill: Navigation Compose

Esta skill te entrega las convenciones del proyecto Solvyx para implementar navegación con Jetpack Navigation Compose. Aplícala al agregar pantallas nuevas, conectar deep links o pasar argumentos entre destinos.

## Principios

1. **Rutas en `Routes.kt`** como sealed class. Nunca `String` mágicos.
2. **Argumentos tipados** vía `navArgument` con su tipo NavType.
3. **ViewModel por destino** con `hiltViewModel()`.
4. **Deep links** registrados para notificaciones y enlaces externos.
5. **Back stack controlado.** `popUpTo` y `launchSingleTop` cuando aplique.
6. **No lógica en callbacks de navegación.** Solo invocar VM y navegar.

## Setup

```kotlin
implementation("androidx.navigation:navigation-compose:2.8.0")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```

## Estructura de rutas

```kotlin
// app/src/main/java/com/solvyx/ui/navigation/Routes.kt
sealed class SolvyxRoutes(val route: String) {
    object Splash : SolvyxRoutes("splash")
    object Onboarding : SolvyxRoutes("onboarding")
    object AuthChoice : SolvyxRoutes("auth/choice")
    object Login : SolvyxRoutes("auth/login")
    object Register : SolvyxRoutes("auth/register")
    
    object Home : SolvyxRoutes("home")
    object Bitacora : SolvyxRoutes("bitacora")
    object Avances : SolvyxRoutes("avances")
    
    // Pantallas con argumento
    object EjercicioDetalle : SolvyxRoutes("ejercicios/{slug}") {
        const val ARG_SLUG = "slug"
        fun build(slug: String) = "ejercicios/$slug"
    }
    
    object LeccionDetalle : SolvyxRoutes("lecciones/{sustancia}/{slug}") {
        const val ARG_SUSTANCIA = "sustancia"
        const val ARG_SLUG = "slug"
        fun build(sustancia: String, slug: String) = "lecciones/$sustancia/$slug"
    }
}
```

## NavHost

```kotlin
@Composable
fun SolvyxNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = SolvyxRoutes.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(SolvyxRoutes.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(SolvyxRoutes.Onboarding.route) {
                        popUpTo(SolvyxRoutes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(SolvyxRoutes.Home.route) {
                        popUpTo(SolvyxRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(SolvyxRoutes.Home.route) {
            MainScreen(/* ... */)
        }
        
        composable(SolvyxRoutes.EjercicioDetalle.route) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString(SolvyxRoutes.EjercicioDetalle.ARG_SLUG).orEmpty()
            EjercicioDetalleScreen(
                slug = slug,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = SolvyxRoutes.LeccionDetalle.route,
            arguments = listOf(
                navArgument(SolvyxRoutes.LeccionDetalle.ARG_SUSTANCIA) { type = NavType.StringType },
                navArgument(SolvyxRoutes.LeccionDetalle.ARG_SLUG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sustancia = backStackEntry.arguments?.getString(SolvyxRoutes.LeccionDetalle.ARG_SUSTANCIA).orEmpty()
            val slug = backStackEntry.arguments?.getString(SolvyxRoutes.LeccionDetalle.ARG_SLUG).orEmpty()
            LeccionDetalleScreen(
                sustancia = sustancia,
                slug = slug,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

## Tipos de argumentos

| Tipo Kotlin | NavType |
|---|---|
| String | `NavType.StringType` |
| Int | `NavType.IntType` |
| Long | `NavType.LongType` |
| Float | `NavType.FloatType` |
| Boolean | `NavType.BoolType` |
| Parcelable | `NavType.ParcelableType` |
| Serializable | custom |

```kotlin
navArgument("id") {
    type = NavType.LongType
    defaultValue = -1L
}
navArgument("slug") {
    type = NavType.StringType
    nullable = false
}
```

## Navegación entre pantallas

### Navegación básica

```kotlin
navController.navigate(SolvyxRoutes.EjercicioDetalle.build(slug = "respiracion-4-7-8"))
```

### Reemplazar destino actual

```kotlin
navController.navigate(SolvyxRoutes.Home.route) {
    popUpTo(SolvyxRoutes.AuthChoice.route) { inclusive = true }
}
```

### Limpiar back stack al logout

```kotlin
navController.navigate(SolvyxRoutes.AuthChoice.route) {
    popUpTo(navController.graph.id) { inclusive = true }
    launchSingleTop = true
}
```

### Back stack con guardado

```kotlin
navController.navigate(SolvyxRoutes.Home.route) {
    saveState = true
    popUpTo(SolvyxRoutes.Home.route) { inclusive = false }
    launchSingleTop = true
    restoreState = true
}
```

## Deep links

### Definir

```kotlin
composable(
    route = SolvyxRoutes.LeccionDetalle.route,
    deepLinks = listOf(
        navDeepLink { uriPattern = "solvyx://lecciones/{sustancia}/{slug}" },
        navDeepLink { uriPattern = "https://solvyx.app/lecciones/{sustancia}/{slug}" }
    )
) { /* ... */ }
```

### Manejar deep link en MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compose maneja el deep link automáticamente si está registrado
        setContent {
            SolvyxTheme {
                SolvyxNavGraph()
            }
        }
    }
}
```

### Disparar deep link programáticamente

```kotlin
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("solvyx://lecciones/alcohol/efectos")).apply {
    setPackage(context.packageName)
}
context.startActivity(intent)
```

## Integración con Hilt

```kotlin
@Composable
fun LeccionDetalleScreen(
    sustancia: String,
    slug: String,
    viewModel: LeccionDetalleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(sustancia, slug) {
        viewModel.cargar(sustancia, slug)
    }
    
    // ...
}
```

`hiltViewModel()` infiere el ViewModel scope según la ruta del NavGraph. Para ViewModels compartidos entre pantallas:

```kotlin
val parentEntry = remember(backStackEntry) {
    navController.getBackStackEntry(SolvyxRoutes.Home.route)
}
val sharedVm: SharedViewModel = hiltViewModel(parentEntry)
```

## Navegación programática desde VM

```kotlin
@HiltViewModel
class LeccionDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val sustancia: String = savedStateHandle.get<String>("sustancia").orEmpty()
    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()
    
    private val _effect = Channel<LeccionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    
    fun onSiguienteLeccion() {
        viewModelScope.launch {
            _effect.send(LeccionEffect.NavigateToNext(sustancia, nextSlug()))
        }
    }
}

sealed class LeccionEffect {
    data class NavigateToNext(val sustancia: String, val slug: String) : LeccionEffect()
}
```

En el composable:

```kotlin
val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

LaunchedEffect(effect) {
    when (val e = effect) {
        is LeccionEffect.NavigateToNext -> onNavigateToDetalle(e.sustancia, e.slug)
        null -> Unit
    }
}
```

## Testing

```kotlin
@Test
fun `navigate_to_ejercicio_detalle_pushes_route`() {
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    navController.setGraph(SolvyxNavGraph.createGraph())
    
    navController.navigate(SolvyxRoutes.EjercicioDetalle.build("test"))
    
    assertEquals("ejercicios/test", navController.currentDestination?.route)
}
```

## Anti-patrones prohibidos

1. **Rutas como `String` mágicos.** Usar sealed class.
2. **Pasar objetos complejos sin serializar.** Bundle tiene límites.
3. **`popBackStack()` sin verificar.** Usar `popBackStack(route, inclusive)`.
4. **Deep links sin verificar esquema.** Validar antes de navegar.
5. **ViewModels compartidos sin scope claro.** Definir padre explícito.
6. **Navegación desde Composables hijos.** Solo root.
7. **Sin `launchSingleTop` cuando aplica.** Duplicar destinos en back stack.
8. **`startDestination` hardcoded** que no respeta el flow de onboarding.