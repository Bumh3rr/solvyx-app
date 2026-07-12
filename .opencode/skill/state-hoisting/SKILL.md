---
description: Patrones de state hoisting en Compose para Solvyx. Composables stateless, unidirectional data flow, eventos arriba estado abajo.
---

# Skill: State Hoisting

Esta skill te entrega los patrones de state hoisting (elevación de estado) en Jetpack Compose para Solvyx. Aplícala al diseñar Composables y su relación con ViewModels.

## Principios

1. **Stateless por defecto.** Composables no mantienen estado interno salvo que sea UI-only (ej. animaciones, focus).
2. **Estado abajo, eventos arriba.** El padre posee el estado; los hijos lo reciben y emiten eventos.
3. **Unidirectional Data Flow (UDF).** Estado fluye hacia abajo; eventos hacia arriba.
4. **Previews fáciles.** Composables stateless son 100% previsualizables sin mocks.
5. **Testeo fácil.** Composables stateless se testean sin lifecycle ni VMs.

## Stateless vs Stateful

### Stateless (preferido)

```kotlin
@Composable
fun Counter(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text("Count: $count")
        Button(onClick = onIncrement) {
            Text("+")
        }
    }
}
```

### Stateful (solo para UI puramente local)

```kotlin
@Composable
fun Counter(modifier: Modifier = Modifier) {
    var count by rememberSaveable { mutableStateOf(0) }
    
    Column(modifier = modifier) {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("+")
        }
    }
}
```

**Regla:** si un Composable necesita `mutableStateOf`, casi siempre el estado debe vivir en el padre (ViewModel u otro Composable).

## Patrones de elevación

### Patrón básico

```kotlin
// Stateless: el padre controla todo
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
        }
    )
}

// Uso:
val query by viewModel.query.collectAsStateWithLifecycle()
SearchBar(
    query = query,
    onQueryChange = viewModel::onQueryChange,
    onSearch = viewModel::onSearch
)
```

### Patrón con ViewModel implícito

```kotlin
@Composable
fun SearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    
    TextField(
        value = query,
        onValueChange = { query = it },
        trailingIcon = {
            IconButton(onClick = { onSearch(query) }) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
        },
        modifier = modifier
    )
}
```

Esto se usa solo cuando el query es puramente UI (no necesita sobrevivir a muerte del proceso). Si quieres persistencia, eleva al ViewModel.

## Estado derivado

Si el Composable calcula algo del estado, usa `derivedStateOf`:

```kotlin
@Composable
fun ListaConFiltro(
    items: List<Item>,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    
    val filtered by remember(items) {
        derivedStateOf {
            if (query.isBlank()) items
            else items.filter { it.nombre.contains(query, ignoreCase = true) }
        }
    }
    
    Column(modifier = modifier) {
        TextField(value = query, onValueChange = { query = it })
        LazyColumn {
            items(filtered, key = { it.id }) { ItemRow(it) }
        }
    }
}
```

`derivedStateOf` evita recalcular si el query no cambió (solo cambia cuando `items` cambia).

## Patrones para ViewModels

### Recibir todo el UI state desde VM

```kotlin
@Composable
fun EjerciciosScreen(
    onNavigateToDetalle: (String) -> Unit,
    viewModel: EjerciciosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    EjerciciosContent(
        uiState = uiState,
        onItemClick = viewModel::onItemClick,
        onNavigateToDetalle = onNavigateToDetalle
    )
}

@Composable
private fun EjerciciosContent(
    uiState: EjerciciosUiState,
    onItemClick: (String) -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ...
}
```

### Acciones vía callback simple

```kotlin
@Composable
fun EjercicioDetalleScreen(
    slug: String,
    onNavigateBack: () -> Unit,
    onIniciarEjercicio: (String) -> Unit,
    viewModel: EjercicioDetalleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(slug) {
        viewModel.cargar(slug)
    }
    
    EjercicioDetalleContent(
        uiState = uiState,
        onBack = onNavigateBack,
        onIniciar = { onIniciarEjercicio(uiState.ejercicio?.slug.orEmpty()) }
    )
}
```

### Eventos one-shot con Channel

```kotlin
sealed interface EjerciciosEffect {
    data class NavigateToDetalle(val slug: String) : EjerciciosEffect
    data class ShowError(val mensaje: String) : EjerciciosEffect
}

// VM
private val _effects = Channel<EjerciciosEffect>(Channel.BUFFERED)
val effects: Flow<EjerciciosEffect> = _effects.receiveAsFlow()

fun onItemClick(slug: String) {
    viewModelScope.launch {
        _effects.send(EjerciciosEffect.NavigateToDetalle(slug))
    }
}

// Composable
val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

LaunchedEffect(effect) {
    when (val e = effect) {
        is EjerciciosEffect.NavigateToDetalle -> onNavigateToDetalle(e.slug)
        is EjerciciosEffect.ShowError -> snackbarHostState.showSnackbar(e.mensaje)
        null -> Unit
    }
}
```

## Buenas prácticas

### 1. Un solo `UiState` por pantalla

```kotlin
// Bien
data class EjerciciosUiState(
    val cargando: Boolean = true,
    val ejercicios: List<Ejercicio> = emptyList(),
    val filtro: TipoEjercicio? = null
)

// Mal: múltiples StateFlows sueltos
val cargando: StateFlow<Boolean>
val ejercicios: StateFlow<List<Ejercicio>>
val filtro: StateFlow<TipoEjercicio?>
```

### 2. Callbacks como lambdas o method references

```kotlin
// Bien
EjerciciosContent(
    items = state.ejercicios,
    onItemClick = viewModel::onItemClick
)

// Bien
EjerciciosContent(
    items = state.ejercicios,
    onItemClick = { slug -> viewModel.onItemClick(slug) }
)

// Mal: pasar el VM al hijo
EjerciciosContent(
    items = state.ejercicios,
    viewModel = viewModel  // ❌ nunca
)
```

### 3. Composables hijos sin acceso a VMs

```kotlin
// Bien: hijo recibe todo por parámetros
@Composable
private fun EjerciciosContent(
    ejercicios: List<Ejercicio>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) { /* ... */ }

// Mal: hijo accede al VM
@Composable
private fun EjerciciosContent(viewModel: EjerciciosViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

### 4. Modifier siempre como último parámetro con default

```kotlin
@Composable
fun MiComponente(
    titulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier  // siempre último y con default
) { /* ... */ }
```

### 5. Previews sin mocks

```kotlin
@Preview
@Composable
private fun EjerciciosContentPreview() {
    SolvyxTheme {
        EjerciciosContent(
            uiState = EjerciciosUiState.Loaded(
                ejercicios = listOf(testEjercicio1, testEjercicio2)
            ),
            onItemClick = {},
            onNavigateToDetalle = {}
        )
    }
}
```

## Anti-patrones prohibidos

1. **Composables hijos que inyectan VMs.** Solo el root.
2. **State mutable compartido sin elevación.** Cada hijo debe tener su propio estado o recibirlo.
3. **`mutableStateOf` en Composables que necesitan persistencia.** Usa ViewModel.
4. **Pasar el VM completo como parámetro.** Solo estado y callbacks.
5. **Mezclar UI state local con UI state global.** Decidir dónde vive cada uno.
6. **No usar `derivedStateOf` cuando aplica.** Recalcular en cada recomposición.
7. **Side effects sin `LaunchedEffect` o `DisposableEffect`.**
8. **Composables que saben de ViewModels.** Solo el root.