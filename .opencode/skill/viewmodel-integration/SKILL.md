---
description: Integración de ViewModels con Jetpack Compose en Solvyx. hiltViewModel, collectAsStateWithLifecycle, SavedStateHandle, eventos one-shot.
---

# Skill: ViewModel Integration

Esta skill te entrega los patrones para conectar ViewModels de Hilt con Composables en Solvyx. Aplícala al crear pantallas nuevas o conectar flujos existentes.

## Principios

1. **`hiltViewModel()` solo en el Composable root de la pantalla.**
2. **`collectAsStateWithLifecycle()`, no `collectAsState()`.**
3. **Estado como `StateFlow<T>` inmutable.** Nunca exponer `MutableStateFlow`.
4. **Eventos one-shot vía `Channel` o `SharedFlow`**, no `StateFlow`.
5. **`SavedStateHandle`** para parámetros que vienen de navegación.
6. **El Composable no conoce al VM por tipo**; el VM solo se inyecta al root.

## Setup

```kotlin
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
```

## Inyección del VM

### Patrón básico

```kotlin
@Composable
fun EjerciciosScreen(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EjerciciosViewModel = hiltViewModel()
) {
    // ...
}
```

### VM compartido entre pantallas (Activity scope)

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    // VM scope: la entrada más cercana en el back stack que tenga NavGraph
}
```

Para VM con scope mayor (compartido entre varias pantallas):

```kotlin
@Composable
fun HomeChildScreen(
    parentEntry: NavBackStackEntry,
    viewModel: HomeSharedViewModel = hiltViewModel(parentEntry)
) {
    // ...
}

// Uso:
val parentEntry = remember(navBackStackEntry) {
    navController.getBackStackEntry(SolvyxRoutes.Home.route)
}
HomeChildScreen(parentEntry = parentEntry)
```

### Pasar argumentos de navegación al VM

```kotlin
@HiltViewModel
class EjercicioDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EjerciciosRepository
) : ViewModel() {
    
    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()
    
    // ...
}

// En el composable:
@Composable
fun EjercicioDetalleScreen(
    slug: String,  // pasado por NavGraph
    onNavigateBack: () -> Unit,
    viewModel: EjercicioDetalleViewModel = hiltViewModel()
) {
    // El VM ya recibió slug vía SavedStateHandle
    // Pero también lo recibimos aquí para evitar doble lookup
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(slug) {
        viewModel.cargar(slug)  // idempotente
    }
}
```

## Colección de estado

### collectAsStateWithLifecycle

```kotlin
@Composable
fun MiPantallaScreen(viewModel: MiPantallaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is MiPantallaUiState.Loading -> LoadingState()
        is MiPantallaUiState.Loaded -> ContentState(state.data)
        is MiPantallaUiState.Error -> ErrorState(state.mensaje)
    }
}
```

### collectAsStateWithLifecycle con initialValue

Para Flows one-shot (eventos):

```kotlin
@Composable
fun MiPantallaScreen(viewModel: MiPantallaViewModel = hiltViewModel()) {
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    
    LaunchedEffect(effect) {
        when (val e = effect) {
            is MiPantallaEffect.NavigateTo -> onNavigateTo(e.route)
            is MiPantallaEffect.ShowError -> snackbar.showSnackbar(e.mensaje)
            null -> Unit
        }
    }
}
```

## Eventos one-shot

### Patrón con Channel

```kotlin
// VM
private val _effect = Channel<MiPantallaEffect>(Channel.BUFFERED)
val effect = Flow<MiPantallaEffect> = _effect.receiveAsFlow()

fun onAceptar() {
    viewModelScope.launch {
        _effect.send(MiPantallaEffect.NavigateTo(Routes.Home))
    }
}

// Composable
@Composable
fun MiPantallaScreen(viewModel: MiPantallaViewModel = hiltViewModel()) {
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    
    LaunchedEffect(effect) {
        when (val e = effect) {
            is MiPantallaEffect.NavigateTo -> onNavigate(e.route)
            null -> Unit
        }
    }
}
```

### Snackbar / Toast / Dialog

```kotlin
@Composable
fun MiPantallaScreen(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: MiPantallaViewModel = hiltViewModel()
) {
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    
    LaunchedEffect(effect) {
        when (val e = effect) {
            is MiPantallaEffect.ShowError -> snackbarHostState.showSnackbar(e.mensaje)
            else -> Unit
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // contenido
    }
}
```

## Init del VM

### Llamar `cargar()` en LaunchedEffect del root

```kotlin
@Composable
fun MiPantallaScreen(viewModel: MiPantallaViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.cargar()  // se llama una vez al primer compose
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

### O en `init` del VM

```kotlin
@HiltViewModel
class MiPantallaViewModel @Inject constructor(
    private val repository: MiRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MiPantallaUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    init {
        cargar()
    }
    
    private fun cargar() {
        viewModelScope.launch {
            repository.observeData()
                .catch { /* error */ }
                .collect { /* update state */ }
        }
    }
}
```

**Regla:** si la carga depende de un argumento de navegación, usa `LaunchedEffect(argumento)` en el composable. Si no depende, `init { }` del VM está bien.

## Re-composición eficiente

### `LaunchedEffect(key)` para reaccionar a cambios

```kotlin
@Composable
fun DetalleScreen(slug: String, viewModel: DetalleViewModel = hiltViewModel()) {
    LaunchedEffect(slug) {
        viewModel.cargar(slug)
    }
    // ...
}
```

Si `slug` cambia, `LaunchedEffect` se cancela y relanza.

### `DisposableEffect` para cleanup

```kotlin
@Composable
fun PantallaConListener(viewModel: PantallaViewModel = hiltViewModel()) {
    DisposableEffect(Unit) {
        val listener = viewModel.registrarListener()
        
        onDispose {
            viewModel.removerListener(listener)
        }
    }
}
```

## Manejo de errores en el VM

```kotlin
@HiltViewModel
class MiPantallaViewModel @Inject constructor(
    private val repository: MiRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MiPantallaUiState>(MiPantallaUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    init { cargar() }
    
    private fun cargar() {
        viewModelScope.launch {
            repository.observeData()
                .catch { e ->
                    _uiState.update { 
                        MiPantallaUiState.Error("No pudimos cargar.")
                    }
                }
                .collect { data ->
                    _uiState.update { MiPantallaUiState.Loaded(data) }
                }
        }
    }
    
    fun retry() {
        _uiState.value = MiPantallaUiState.Loading
        cargar()
    }
}
```

## SavedStateHandle para estado persistente

```kotlin
@HiltViewModel
class FormularioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _nombre = savedStateHandle.getStateFlow("nombre", "")
    val nombre: StateFlow<String> = _nombre
    
    fun onNombreChange(value: String) {
        savedStateHandle["nombre"] = value
    }
    
    private val _email = savedStateHandle.getStateFlow("email", "")
    val email: StateFlow<String> = _email
    
    // El StateFlow del SavedStateHandle se reconstruye automáticamente
    // si el proceso muere y se restaura.
}
```

## Testing

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class EjerciciosViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val repo: EjerciciosRepository = mockk()
    
    @Test
    fun `init loads from repo`() = runTest {
        coEvery { repo.observeEjercicios() } returns flowOf(listOf(testEjercicio))
        
        val vm = EjerciciosViewModel(repo)
        advanceUntilIdle()
        
        assertEquals(
            EjerciciosUiState.Loaded(listOf(testEjercicio)),
            vm.uiState.value
        )
    }
}
```

## Patrones comunes

### Pull-to-refresh

```kotlin
@Composable
fun PantallaScreen(viewModel: PantallaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PullToRefreshBox(
        isRefreshing = uiState.refreshing,
        onRefresh = viewModel::refresh
    ) {
        LazyColumn { /* ... */ }
    }
}
```

### Loading más loading inicial

```kotlin
data class MiUiState(
    val cargandoInicial: Boolean = false,
    val refreshing: Boolean = false,
    val datos: List<Item> = emptyList(),
    val error: String? = null
)
```

### Paginación

```kotlin
val items = viewModel.pagingFlow.collectAsLazyPagingItems()

LazyColumn {
    items(items.itemCount) { i ->
        items[i]?.let { ItemRow(it) }
    }
}
```

## Anti-patrones prohibidos

1. **`collectAsState()` en lugar de `collectAsStateWithLifecycle()`.** No respeta lifecycle.
2. **ViewModel inyectado en composables hijos.** Solo en root.
3. **Pasar el VM como parámetro.** Solo estado y callbacks.
4. **`mutableStateOf` en composables** para datos que deben persistir.
5. **Side effects en `init` del composable** sin `LaunchedEffect`.
6. **Channel para estado** (es para eventos, no estado).
7. **`StateFlow` para eventos one-shot** (se pierden al rotar).
8. **Llamar `cargar()` en `init { }` cuando depende de un argumento** de nav.
9. **Olvidar `LaunchedEffect(key)` cuando el VM debe recargarse** al cambiar el argumento.
10. **`hiltViewModel(LocalContext.current as Activity)`** sin razón. Solo si necesitas VM con Activity scope.