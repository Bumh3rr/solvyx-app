---
description: Convenciones de Jetpack Compose para construir pantallas de Solvyx. Layouts, estado, animaciones, performance.
---

# Skill: Jetpack Compose Screens

Esta skill te entrega las convenciones del proyecto Solvyx para construir pantallas en Jetpack Compose. Aplícala cada vez que crees una pantalla nueva o modifiques una existente.

## Principios

1. **Stateless por defecto.** Composables reciben estado y callbacks. La lógica vive en el ViewModel.
2. **Previews obligatorias.** Cada Composable nuevo con `@Preview`.
3. **`Modifier` siempre presente** como último parámetro con default `Modifier`.
4. **`collectAsStateWithLifecycle()`, no `collectAsState()`.**
5. **`Material 3` siempre.** Evita APIs deprecadas.
6. **Sin side effects en composición.** Side effects solo en `LaunchedEffect`, `DisposableEffect`, etc.
7. **Performance:** usa `key` en `LazyColumn items()` y `remember` para cálculos pesados.

## Plantilla de pantalla

```kotlin
@Composable
fun MiPantallaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiPantallaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            GuiaTopBar(
                title = "Mi Pantalla",
                onBack = onNavigateBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is MiPantallaUiState.Loading -> LoadingState()
                is MiPantallaUiState.Loaded -> MiPantallaContent(
                    items = state.items,
                    onItemClick = viewModel::onItemClick
                )
                is MiPantallaUiState.Error -> ErrorState(
                    mensaje = state.mensaje,
                    onRetry = viewModel::retry
                )
            }
        }
    }
}
```

## Layouts

### Box

Para apilar elementos o centrar uno solo:

```kotlin
Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Image(/* ... */)
}
```

### Column / Row

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    Text(/* ... */)
    Text(/* ... */)
}
```

### LazyColumn (listas)

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(items = lista, key = { it.id }) { item ->
        ItemCard(item = item, onClick = { onItemClick(item.id) })
    }
}
```

### LazyVerticalGrid

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(items = lista, key = { it.id }) { item ->
        ItemCard(item = item)
    }
}
```

### Scaffold con TopBar y BottomBar

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("Título") },
            navigationIcon = { BackButton(onBack = onBack) }
        )
    },
    bottomBar = {
        SolvyxBottomNavigationBar(/* ... */)
    }
) { padding ->
    // contenido con Modifier.padding(padding)
}
```

## Estado

### `remember` y `rememberSaveable`

```kotlin
// remember: estado perdido al rotar o matar proceso
var counter by remember { mutableStateOf(0) }

// rememberSaveable: estado preservado en rotación y proceso (con Bundle)
var counter by rememberSaveable { mutableStateOf(0) }
```

### `derivedStateOf` para estado derivado

```kotlin
val filteredList by remember(uiState.items) {
    derivedStateOf {
        uiState.items.filter { it.activo }
    }
}
```

### `rememberCoroutineScope` para acciones en composables

```kotlin
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        // side effect
    }
}) {
    Text("Click me")
}
```

## LaunchedEffect para side effects

```kotlin
@Composable
fun MiPantalla(viewModel: MiPantallaViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.cargar()
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // ...
}
```

### DisposableEffect para cleanup

```kotlin
DisposableEffect(Unit) {
    val listener = SomeListener { /* ... */ }
    addListener(listener)
    
    onDispose {
        removeListener(listener)
    }
}
```

## Animaciones

### `animate*AsState`

```kotlin
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(durationMillis = 300),
    label = "alpha"
)
```

### `AnimatedVisibility`

```kotlin
AnimatedVisibility(
    visible = expanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut()
) {
    Text("Contenido expandible")
}
```

### `Crossfade` para cambiar contenido

```kotlin
Crossfade(targetState = uiState, label = "state") { state ->
    when (state) {
        is Loading -> LoadingState()
        is Loaded -> ContentState(state.data)
    }
}
```

## Performance

### Usar `key` en `items()`

```kotlin
LazyColumn {
    items(items = lista, key = { it.id }) { item ->
        ItemCard(item)
    }
}
```

Sin `key`, Compose no puede optimizar el recompose. Con `key`, evita recomposiciones innecesarias.

### `derivedStateOf` para listas filtradas

```kotlin
// MAL: filtra en cada recomposición
val filtered = items.filter { it.activo }

// BIEN: solo recalcula si items cambia
val filtered by remember(items) {
    derivedStateOf { items.filter { it.activo } }
}
```

### `remember` para objetos pesados

```kotlin
val complexObject = remember(data) {
    parseData(data)
}
```

### Estabilidad de parámetros

Compose optimiza mejor si los parámetros son estables. Si pasas un `List<X>` que cambia de instancia en cada recomposición, Compose no puede skipear.

```kotlin
// Usar ImmutableList o kotlinx.collections.immutable
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MyList(items: ImmutableList<Item>) {
    // ...
}
```

## Themes

```kotlin
@Composable
fun MiPantalla() {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Text(
        text = "Hola",
        color = colorScheme.onSurface,
        style = typography.bodyLarge
    )
}
```

## Previews

```kotlin
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MiPantallaPreview() {
    SolvyxTheme {
        MiPantallaContent(
            items = listOf(/* ... */),
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MiPantallaDarkPreview() {
    SolvyxTheme {
        MiPantallaContent(items = emptyList(), onItemClick = {})
    }
}
```

## Click handlers

```kotlin
Button(onClick = { /* click */ }) {
    Text("Aceptar")
}

// Con callback tipado
val onItemClick: (Item) -> Unit = { item -> /* ... */ }
```

## Strings (internacionalización)

```kotlin
import androidx.compose.ui.res.stringResource

Text(text = stringResource(R.string.welcome))
```

**Nunca hardcodear strings de UI.** Todo en `strings.xml`.

## Errores comunes

| Error | Solución |
|---|---|
| `LazyColumn items()` sin `key` | Agregar `key = { it.id }`. |
| Hardcoded strings | Extraer a `strings.xml`. |
| Color hardcoded `Color(0xFF...)` | Usar `MaterialTheme.colorScheme.primary`. |
| `collectAsState()` | Usar `collectAsStateWithLifecycle()`. |
| Side effect en composición (no `LaunchedEffect`) | Envolver en `LaunchedEffect(key) { ... }`. |
| Composables gigantes (>200 líneas) | Dividir en sub-Composables. |
| Estado mutable compartido | Mover al ViewModel. |
| `for` para listas | Usar `LazyColumn` o `Column` con iteradores. |
| Imports innecesarios de Material 2 | Usar `androidx.compose.material3.*`. |

## Anti-patrones prohibidos

1. **`collectAsState()`** en lugar de `collectAsStateWithLifecycle()`.
2. **Strings hardcoded** en Composables.
3. **Colores hardcoded** sin pasar por MaterialTheme.
4. **Side effects** en cuerpo de `@Composable` sin `LaunchedEffect`.
5. **`for`/`while` para listas.** Usar `LazyColumn` o `Column`.
6. **`key = null` en `LazyColumn`.** Optimización rota.
7. **Recomposiciones globales.** Usar `derivedStateOf` y `remember` para aislar.
8. **Composables >300 líneas.** Dividir.
9. **ViewModel directamente en sub-Composables.** Solo en root.
10. **Previews sin `SolvyxTheme`.** Pierdes el contexto de diseño.