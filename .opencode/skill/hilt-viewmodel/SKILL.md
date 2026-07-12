---
description: Convenciones de Hilt para ViewModels en Solvyx. @HiltViewModel, SavedStateHandle, navegación con parámetros, testing.
---

# Skill: Hilt ViewModel

Esta skill te entrega las convenciones para crear y configurar ViewModels con Hilt en Solvyx. Aplícala cada vez que crees un nuevo ViewModel o necesites navegar con parámetros.

## Principios

1. **Un ViewModel por pantalla o flujo lógico.**
2. **Constructor injection con `@Inject`.** Nunca Service Locator.
3. **`@HiltViewModel` siempre.** Es lo que permite a Compose usar `hiltViewModel()`.
4. **`SavedStateHandle` para parámetros de navegación** que sobreviven a la muerte del proceso.
5. **Sin dependencias de UI en el VM** (no `Context`, `View`, `Activity`).

## Plantilla básica

```kotlin
@HiltViewModel
class EjerciciosViewModel @Inject constructor(
    private val repository: EjerciciosRepository
) : ViewModel() {
    // ...
}
```

## Con SavedStateHandle (para argumentos de nav)

```kotlin
@HiltViewModel
class EjercicioDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EjerciciosRepository
) : ViewModel() {
    
    private val slug: String = savedStateHandle.get<String>("slug")
        ?: error("slug es obligatorio")
    
    val ejercicio: StateFlow<EjercicioDetalleUiState> = repository
        .observeEjercicioBySlug(slug)
        .map { EjercicioDetalleUiState.Loaded(it) }
        .catch { emit(EjercicioDetalleUiState.Error(it.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EjercicioDetalleUiState.Loading)
}
```

## Inyección de varias dependencias

```kotlin
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightsRepository: InsightsRepository,
    private val bitacoraRepository: BitacoraRepository,
    private val preferences: UserPreferencesRepository,
    @ApplicationContext private val appContext: Context  // solo si estrictamente necesario
) : ViewModel() {
    // ...
}
```

## @AssistedInject para dependencias dinámicas

Si una dependencia solo se conoce en tiempo de ejecución (raro en VMs):

```kotlin
@HiltViewModel(assistedFactory = XViewModel.Factory::class)
class XViewModel @AssistedInject constructor(
    @Assisted private val dynamicParam: String,
    private val repository: YRepository
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(dynamicParam: String): XViewModel
    }
}
```

> **Regla:** evita `@AssistedInject` en VMs. Si necesitas un parámetro dinámico, generalmente es mejor pasarlo como método (`fun load(slug: String)`) o vía `SavedStateHandle`.

## Inyección de SavedStateHandle junto con otras deps

```kotlin
@HiltViewModel
class BitacoraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bitacoraRepo: BitacoraRepository,
    private val insightRepo: InsightsRepository,
    private val workScheduler: WorkScheduler
) : ViewModel() {
    // ...
}
```

## ViewModel compartido entre pantallas (Activity scope)

Si necesitas un VM compartido por varias pantallas:

```kotlin
@HiltViewModel
class MainSharedViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel()

// En Compose:
val activity = LocalContext.current as ComponentActivity
val vm: MainSharedViewModel = hiltViewModel(activity)
```

## Uso en Compose

```kotlin
@Composable
fun EjerciciosScreen(
    onNavigateToDetalle: (String) -> Unit,
    viewModel: EjerciciosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

## Testing de ViewModel

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class EjerciciosViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val fakeRepo: EjerciciosRepository = mockk()
    private lateinit var viewModel: EjerciciosViewModel
    
    @Before
    fun setup() {
        viewModel = EjerciciosViewModel(fakeRepo)
    }
    
    @Test
    fun `init loads ejercicios from repo`() = runTest {
        coEvery { fakeRepo.observeEjercicios() } returns flowOf(listOf(ejercicioTest))
        
        viewModel = EjerciciosViewModel(fakeRepo)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.cargando)
        assertEquals(1, state.ejercicios.size)
    }
}
```

### TestRule para Main dispatcher

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

## Errores comunes

| Error | Causa | Solución |
|---|---|---|
| `MissingBinding` | Dependencia no proveída | Agregar `@Provides` o `@Inject constructor`. |
| `Cannot create instance` | Falta `@HiltViewModel` | Agregar la anotación. |
| `IllegalStateException: Hilt Activity must be set` | Falta `HiltAndroidApp` en Application | Agregar `@HiltAndroidApp` a `SolvyxApp.kt`. |
| VM no recibe SavedStateHandle | No se inyectó explícitamente | Agregar a la lista de parámetros. |
| NavController pasa argumentos no leídos | Type mismatch en SavedStateHandle | Usar `savedStateHandle.get<String>("slug")` con null-safety. |

## SavedStateHandle tips

1. **Tipos primitivos** se serializan nativamente: `String`, `Int`, `Long`, `Boolean`, `Float`, `Double`, `Parcelable`, `Serializable`.
2. **Tipos complejos:** usa `@Serializable` con kotlinx.serialization y un getter/setter custom, o usa `getStateFlow("key", default)`.
3. **No guardes objetos grandes** en SavedStateHandle (Bundle tiene límites).
4. **No guardes referencias a Context, View, Fragment.**

```kotlin
val uiState: StateFlow<UiState> = savedStateHandle.getStateFlow("ui_state", UiState.Initial)
fun onAction() {
    savedStateHandle["ui_state"] = UiState.Loading
}
```

## Lifecycle

- `viewModelScope` se cancela en `onCleared()`.
- Si necesitas trabajo que sobreviva al VM (descarga, sync), usa `applicationScope` o WorkManager.

## Anti-patrones prohibidos

1. **`@Inject` con Service Locator** (manual).
2. **ViewModel con `Context` directo** sin qualifier.
3. **`GlobalScope.launch`** dentro del VM.
4. **Estado mutable (`var`) expuesto al UI.**
5. **ViewModels que pasan NavControllers o Views.**
6. **Crear ViewModels dentro de Composables** sin `hiltViewModel()` (rompe saved state).
7. **Mezclar `@HiltViewModel` con `ViewModelProvider.Factory`** manual.