---
description: Convenciones de Kotlin Coroutines y Flow para Solvyx. viewModelScope, Dispatchers, operadores de Flow, manejo de errores.
---

# Skill: Coroutines Patterns

Esta skill te entrega las convenciones para usar Kotlin Coroutines y Flow en Solvyx. Aplícala en cualquier ViewModel, Repository, Worker o UseCase que ejecute trabajo asíncrono.

## Principios

1. **Nunca `GlobalScope`.** Siempre un scope con ciclo de vida definido.
2. **Structured concurrency.** Si una coroutine falla, sus hijas se cancelan.
3. **Dispatchers explícitos.** No asumas el dispatcher.
4. **Cancelación cooperativa.** Trabajo cancelable siempre que sea posible.
5. **Errores manejados.** Excepciones se capturan; no se propagan al UI sin mapear.

## Scopes del proyecto

| Scope | Cuándo |
|---|---|
| `viewModelScope` | Dentro de ViewModels. Se cancela en `onCleared()`. |
| `lifecycleScope` | Dentro de Activity/Fragment. Se cancela al destruir. |
| `Lifecycle.coroutineScope` | Dentro de un `LifecycleOwner` específico. |
| `rememberCoroutineScope()` | Dentro de Composables. Se cancela al salir de composición. |
| `CoroutineWorker` | Dentro de Workers. |
| `applicationScope` | Singleton, ciclo de la app. Solo para tareas que deban sobrevivir a VMs (ej. seed inicial). |

## Dispatchers

| Dispatcher | Para qué |
|---|---|
| `Dispatchers.Main` | UI. Acciones de Compose, mostrar SnackBars. |
| `Dispatchers.IO` | Red, archivos, base de datos (Room ya inyecta su propio pool, pero `Dispatchers.IO` está bien). |
| `Dispatchers.Default` | CPU-intensive: parsing JSON, cálculos, ordenamiento. |
| `Dispatchers.Unconfined` | **Evitar.** Solo en tests. |

### Plantilla

```kotlin
suspend fun loadEjercicios(): List<Ejercicio> = withContext(Dispatchers.IO) {
    val json = context.assets.open("seed/v1/ejercicios.json")
        .bufferedReader()
        .use { it.readText() }
    SolvyxJson.instance.decodeFromString<SeedFile<EjercicioSeed>>(json).items
}
```

## launch vs async

- **`launch`:** fire-and-forget. No devuelve valor. Para side effects.
- **`async`:** devuelve `Deferred<T>`. Para trabajo paralelo que luego se `await()`.

```kotlin
// launch: efectos
viewModelScope.launch {
    repository.guardar(entry)
    _effects.send(FormularioEffect.Exito)
}

// async: paralelo
val userDeferred = async { userRepo.getUser() }
val prefsDeferred = async { prefsRepo.getPrefs() }
val (user, prefs) = Pair(userDeferred.await(), prefsDeferred.await())
```

## Operadores de Flow esenciales

### Transformación

- `map { T -> R }` — uno a uno.
- `mapNotNull { T -> R? }` — filtra nulos resultantes.
- `transform { value -> emit(...) }` — control fino de emisiones.
- `flatMapLatest { T -> Flow<R> }` — cambia a un nuevo Flow cancelando el anterior.

### Filtrado

- `filter { T -> Boolean }`
- `filterNotNull()`
- `distinctUntilChanged()` — deduplica valores consecutivos iguales.
- `debounce(300.milliseconds)` — espera N ms sin nuevas emisiones.
- `sample(1.seconds)` — emite el último valor cada N segundos.

### Combinación

- `combine(flow1, flow2) { v1, v2 -> ... }` — combina el último valor de cada Flow.
- `zip(flow1, flow2)` — emite pares en orden.
- `merge(flow1, flow2)` — intercala emisiones.

### Side effects

- `onEach { ... }` — ejecuta acción sin transformar.
- `onStart { emit(initial) }` — emite al suscribirse.
- `onCompletion { cause -> ... }` — al completarse (con o sin error).
- `catch { e -> ... }` — captura excepciones upstream.

## Manejo de errores

```kotlin
repository.observeEjercicios()
    .catch { e ->
        // Log + emitir error como estado
        _uiState.update { it.copy(error = "No pudimos cargar.") }
    }
    .onEach { lista ->
        _uiState.update { it.copy(ejercicios = lista, error = null) }
    }
    .launchIn(viewModelScope)
```

### Cuándo usar `try/catch` vs `.catch`

- **`try/catch`** dentro de `suspend fun` para errores esperados (parsing, validation).
- **`.catch`** en el lado del Flow para errores de producción.

## Cancelación

```kotlin
var searchJob: Job? = null

fun onQueryChange(query: String) {
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        delay(300)
        repository.buscar(query).collect { /* ... */ }
    }
}
```

```kotlin
suspend fun loadLargeFile(): String = withContext(Dispatchers.IO) {
    // cooperativo
    file.bufferedReader().useLines { lines ->
        lines.forEachIndexed { i, line ->
            ensureActive()  // verifica cancelación
            processLine(line)
            if (i % 100 == 0) yield()  // cede el hilo
        }
    }
}
```

## Structured concurrency

```kotlin
suspend fun cargarResumen(): Resumen = coroutineScope {
    val ejercicios = async { repository.getEjercicios() }
    val bitacora = async { repository.getBitacora() }
    val insights = async { repository.getInsights() }
    
    Resumen(
        ejercicios = ejercicios.await(),
        bitacora = bitacora.await(),
        insights = insights.await()
    )
}
```

Si cualquier `async` falla, `coroutineScope` cancela a los hermanos.

## Testing con runTest

```kotlin
@Test
fun cargarEjercicios_emiteLoading_ySuccess() = runTest {
    val vm = EjerciciosViewModel(fakeRepo)
    
    // Estado inicial: cargando
    assertEquals(true, vm.uiState.value.cargando)
    
    advanceUntilIdle()  // ejecuta coroutines pendientes
    
    val final = vm.uiState.value
    assertEquals(false, final.cargando)
    assertEquals(6, final.ejercicios.size)
}
```

## Anti-patrones prohibidos

1. **`GlobalScope.launch`** — sin lifecycle.
2. **`runBlocking` en producción** — solo tests.
3. **Bloquear el hilo principal** con operaciones síncronas.
4. **Olvidar `cancel()`** en Jobs reemplazables.
5. **`Flow.collect { ... }` sin scope** en producción — siempre `launchIn`.
6. **`try/catch` que solo loguea** sin emitir estado de error.
7. **`async` sin `await`** — `async` malgasta recursos.
8. **Mezclar Dispatchers sin razón** — cada cambio de dispatcher tiene costo.