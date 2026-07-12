---
description: Manejo de estados de borde (loading, error, vacío) en pantallas Solvyx. Patrones de UI feedback, retry, animaciones de transición.
---

# Skill: Edge Cases

Esta skill te entrega los patrones para manejar estados de borde en pantallas de Solvyx: loading, error, vacío, sin conexión, estados parciales. Aplícala cada vez que una pantalla lea datos externos (BD, red, sensores).

## Principios

1. **Toda pantalla con datos externos debe tener al menos 3 estados:** Loading, Loaded, Error.
2. **El usuario nunca debe ver una pantalla en blanco.** Siempre feedback visual.
3. **Mensajes en español, sin tecnicismos.** "No pudimos cargar" no "NetworkException 503".
4. **Botón de reintentar** en estados de error cuando aplique.
5. **Estados vacíos amables** con Berto o ilustración, no pantallas frías.
6. **Distinción entre "no hay datos" y "no se pudo cargar".** Son diferentes.

## Tipos de estado de borde

| Estado | Cuándo mostrar |
|---|---|
| Loading inicial | La carga inicial tarda >100ms. |
| Refreshing | Pull-to-refresh o reintento. |
| Error | La carga falló (excepción, timeout). |
| Vacío | La carga exitosa pero no hay datos. |
| Sin conexión | No hay red (cuando aplica). |
| Permiso denegado | Faltan permisos para mostrar datos (notificaciones, ubicación). |
| Parcial | Algunos datos cargaron, otros fallaron. |

## Loading inicial

### Indicador centrado

```kotlin
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }
}
```

### Loading con Berto

```kotlin
@Composable
fun LoadingStateBerto(mensaje: String = "Cargando...", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.berto_tranquilo),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}
```

### Loading con skeleton

Para listas, muestra placeholders:

```kotlin
@Composable
fun SkeletonList(count: Int = 5) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmer()  // efecto shimmer
                )
            }
        }
    }
}
```

Para el efecto shimmer, usa `Modifier.drawBehind` o librería como `accompanist-shimmer` (deprecated) o `compose-shimmer`.

## Error

### Error simple con retry

```kotlin
@Composable
fun ErrorState(
    mensaje: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.berto_preocupado),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onRetry != null) {
            Spacer(Modifier.height(24.dp))
            SolvyxButton(
                text = "Reintentar",
                onClick = onRetry
            )
        }
    }
}
```

### Tipos de error y mensajes

```kotlin
@Composable
fun ErrorStateFromException(
    ex: Throwable,
    onRetry: (() -> Unit)? = null
) {
    val mensaje = when (ex) {
        is IOException -> "Sin conexión a internet. Revisa tu red."
        is HttpException -> "El servidor tuvo un problema. Intenta más tarde."
        is SQLiteException -> "No pudimos acceder a tu información local."
        is JsonDecodingException -> "Hubo un error procesando los datos."
        else -> "Algo salió mal. Estamos trabajando en ello."
    }
    
    ErrorState(mensaje = mensaje, onRetry = onRetry)
}
```

## Estado vacío

### Vacío con Berto y mensaje

```kotlin
@Composable
fun EmptyState(
    titulo: String,
    mensaje: String,
    accion: SolvyxAction? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.berto_tranquilo),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (accion != null) {
            Spacer(Modifier.height(24.dp))
            SolvyxButton(
                text = accion.label,
                onClick = accion.onClick
            )
        }
    }
}

data class SolvyxAction(val label: String, val onClick: () -> Unit)
```

### Ejemplo de uso

```kotlin
EmptyState(
    titulo = "Aún no hay ejercicios",
    mensaje = "Cuando agreguemos contenido aquí, lo verás en esta lista.",
    accion = SolvyxAction("Explorar contenido") { /* ... */ }
)
```

## Sin conexión

```kotlin
@Composable
fun SinRedState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_wifi_off),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Estás sin internet",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Solvyx funciona completo sin red. Solo algunas funciones necesitan conexión.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

## Sin permisos

```kotlin
@Composable
fun SinPermisoState(
    permiso: String,
    onSolicitarPermiso: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Necesitamos tu permiso",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Para $permiso, necesitamos que nos des acceso. Puedes cambiar esto en cualquier momento.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        SolvyxButton(
            text = "Otorgar permiso",
            onClick = onSolicitarPermiso
        )
    }
}
```

## Pull-to-refresh

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaScreen(viewModel: PantallaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PullToRefreshBox(
        isRefreshing = uiState.refreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is PantallaUiState.Loading -> LoadingState()
            is PantallaUiState.Loaded -> Contenido(state.items)
            is PantallaUiState.Error -> ErrorState(
                mensaje = state.mensaje,
                onRetry = viewModel::refresh
            )
            is PantallaUiState.Empty -> EmptyState(
                titulo = state.titulo,
                mensaje = state.mensaje
            )
        }
    }
}
```

## UiState completo

```kotlin
sealed interface PantallaUiState {
    object Loading : PantallaUiState
    data class Loaded(
        val items: List<Item>,
        val refreshing: Boolean = false
    ) : PantallaUiState
    data class Empty(
        val titulo: String,
        val mensaje: String
    ) : PantallaUiState
    data class Error(
        val mensaje: String,
        val canRetry: Boolean = true
    ) : PantallaUiState
}
```

## Transiciones entre estados

Usa `Crossfade` o `AnimatedContent` para suavizar cambios:

```kotlin
@Composable
fun PantallaContent(uiState: PantallaUiState, viewModel: PantallaViewModel) {
    Crossfade(targetState = uiState, label = "state") { state ->
        when (state) {
            is PantallaUiState.Loading -> LoadingState()
            is PantallaUiState.Loaded -> Contenido(state.items)
            is PantallaUiState.Empty -> EmptyState(state.titulo, state.mensaje)
            is PantallaUiState.Error -> ErrorState(state.mensaje, onRetry = viewModel::refresh)
        }
    }
}
```

## Mensajes de error (estándar del proyecto)

| Excepción | Mensaje user-friendly |
|---|---|
| `IOException` | "Sin conexión a internet. Revisa tu red." |
| `SQLiteException` | "No pudimos acceder a tu información. Intenta de nuevo." |
| `JsonDecodingException` | "Hubo un error procesando los datos." |
| `TimeoutCancellationException` | "La operación tardó demasiado. Intenta de nuevo." |
| `UnknownHostException` | "No encontramos el servidor. Revisa tu conexión." |
| Otras | "Algo salió mal. Estamos trabajando en ello." |

## Estado parcial (algunos datos OK, otros fallaron)

```kotlin
data class PantallaUiState(
    val cargando: Boolean = false,
    val items: List<Item> = emptyList(),
    val advertencias: List<String> = emptyList()  // ej. "Algunos datos no se pudieron cargar"
)
```

Mostrar un banner arriba si hay advertencias:

```kotlin
if (uiState.advertencias.isNotEmpty()) {
    AdvertenciaBanner(mensajes = uiState.advertencias)
}
```

## Anti-patrones prohibidos

1. **Pantalla en blanco** sin feedback.
2. **Stacktrace técnico** en el mensaje de error.
3. **Loading permanente** cuando algo falló.
4. **Sin botón de retry** en errores recuperables.
5. **Empty state frío** ("No hay datos") sin contexto ni acción.
6. **Imágenes decorativas sin `contentDescription = null`** explícito.
7. **Estados de borde sin `@Preview`.** Si no puedes ver el estado vacío, no sabes cómo se ve.
8. **Mensaje genérico "Algo salió mal" sin acción posible.**
9. **Loading + error simultáneos** sin transicionar.
10. **Estado `Empty == Error`.** Son diferentes: uno es "no hay datos", otro es "no se pudieron cargar".