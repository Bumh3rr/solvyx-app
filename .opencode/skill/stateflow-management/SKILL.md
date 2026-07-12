---
description: Convenciones de StateFlow y SharedFlow para ViewModels de Solvyx. Estado inmutable expuesto al UI.
---

# Skill: StateFlow Management

Esta skill te entrega las convenciones para manejar estado reactivo en ViewModels de Solvyx usando StateFlow de Kotlin Coroutines. Aplícala en cada ViewModel que cree o modifiques.

## Principios

1. **Inmutabilidad.** El estado expuesto al UI es inmutable. Nunca expongas `MutableStateFlow` directo.
2. **Un solo StateFlow por pantalla.** Si la pantalla tiene sub-estados (formulario + lista), usa un `data class` que los combine.
3. **Loading inicial explícito.** Si la carga puede tardar >100ms, emite `Loading` antes que `Success`.
4. **Errores como estado.** No lances excepciones que escapen al UI. Mapea a `UiState.Error`.

## Plantilla base

```kotlin
data class EjerciciosUiState(
    val cargando: Boolean = true,
    val ejercicios: List<Ejercicio> = emptyList(),
    val error: String? = null,
    val filtroActivo: TipoEjercicio? = null
)

@HiltViewModel
class EjerciciosViewModel @Inject constructor(
    private val repository: EjerciciosRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EjerciciosUiState())
    val uiState: StateFlow<EjerciciosUiState> = _uiState.asStateFlow()
    
    init {
        cargarEjercicios()
    }
    
    private fun cargarEjercicios() {
        viewModelScope.launch {
            repository.observeEjercicios()
                .catch { e ->
                    _uiState.update {
                        it.copy(cargando = false, error = "No pudimos cargar los ejercicios.")
                    }
                }
                .collect { lista ->
                    _uiState.update {
                        it.copy(cargando = false, ejercicios = lista, error = null)
                    }
                }
        }
    }
    
    fun aplicarFiltro(tipo: TipoEjercicio?) {
        _uiState.update { it.copy(filtroActivo = tipo) }
    }
}
```

## Reglas clave

### Exposición

```kotlin
// BIEN: inmutable
private val _uiState = MutableStateFlow(EjerciciosUiState())
val uiState: StateFlow<EjerciciosUiState> = _uiState.asStateFlow()

// MAL: mutable expuesto
val uiState = MutableStateFlow(...)
```

### Update

```kotlin
// BIEN: update atómico
_uiState.update { it.copy(error = null) }

// MAL: get + set no atómico
_uiState.value = _uiState.value.copy(error = null)
```

### Init

```kotlin
init {
    cargarEjercicios()  // lanza una coroutine en viewModelScope
}
```

### Errores

```kotlin
.catch { e ->
    _uiState.update { it.copy(cargando = false, error = mapError(e)) }
}
```

## Side effects con Channel

Para eventos one-shot (mostrar SnackBar, navegar, mostrar diálogo), usa `Channel`:

```kotlin
sealed interface EjerciciosEffect {
    object NavegarADetalle : EjerciciosEffect
    data class MostrarError(val mensaje: String) : EjerciciosEffect
}

private val _effects = Channel<EjerciciosEffect>(Channel.BUFFERED)
val effects: Flow<EjerciciosEffect> = _effects.receiveAsFlow()

fun onEjercicioClick(slug: String) {
    viewModelScope.launch {
        _effects.send(EjerciciosEffect.NavegarADetalle)
    }
}
```

En Compose:

```kotlin
val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)
LaunchedEffect(effect) {
    when (effect) {
        is EjerciciosEffect.MostrarError -> snackbar.show()
        else -> {}
    }
    effect  // consume
}
```

## SharedFlow para eventos multi-suscriptor

Si múltiples UI components necesitan escuchar (ej. un badge en Home + un banner en Bitácora):

```kotlin
private val _nuevoInsight = MutableSharedFlow<Insight>(replay = 0, extraBufferCapacity = 4)
val nuevoInsight: SharedFlow<Insight> = _nuevoInsight.asSharedFlow()

fun publicarInsight(insight: Insight) {
    viewModelScope.launch {
        _nuevoInsight.emit(insight)
    }
}
```

## Estado derivado

Si necesitas filtrar o transformar el estado, usa `map` o `stateIn`:

```kotlin
val ejerciciosFiltrados: StateFlow<List<Ejercicio>> = _uiState
    .map { state ->
        if (state.filtroActivo == null) state.ejercicios
        else state.ejercicios.filter { it.tipo == state.filtroActivo }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

## Compose integration

```kotlin
@Composable
fun EjerciciosScreen(viewModel: EjerciciosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.cargando -> LoadingState()
        uiState.error != null -> ErrorState(uiState.error!!)
        uiState.ejercicios.isEmpty() -> EmptyState()
        else -> EjerciciosList(uiState.ejercicios, viewModel::aplicarFiltro)
    }
}
```

## Patrones comunes

### Lista con búsqueda

```kotlin
data class BusquedaUiState(
    val cargando: Boolean = true,
    val query: String = "",
    val resultados: List<Item> = emptyList(),
    val error: String? = null
)

fun onQueryChange(query: String) {
    _uiState.update { it.copy(query = query) }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        delay(300)  // debounce
        repository.buscar(query).collect { /* ... */ }
    }
}
```

### Formulario

```kotlin
data class FormularioState(
    val cargando: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val exito: Boolean = false
)
```

### Paginación

Usa `Pager` + `PagingData` + `Flow<PagingData<T>>`. Ver skill `kotlin-collections-advanced` para referencia.

## Lifecycle

- `viewModelScope` se cancela automáticamente en `onCleared()`.
- No guardes referencias a `Context`, `Activity`, `View` dentro del VM.
- `StateFlow` mantiene el último valor; `SharedFlow` no (a menos que `replay > 0`).
- `Channel` consume cada valor una vez; úsalo para eventos one-shot.

## Anti-patrones prohibidos

1. **Exponer `MutableStateFlow` al UI.**
2. **Estado mutable (`var`) dentro de un VM que el UI puede leer.**
3. **Mezclar LiveData con StateFlow en el mismo VM.**
4. **Lanzar excepciones al UI** sin mapear a `Error`.
5. **`GlobalScope.launch`** desde un VM.
6. **Bloquear el hilo principal** con operaciones síncronas dentro de `update {}`.