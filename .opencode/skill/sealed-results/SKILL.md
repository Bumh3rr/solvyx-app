---
description: Uso de sealed classes y Result para modelar estados UI y errores en Solvyx. Patrones consistentes para ViewModels.
---

# Skill: Sealed Results

Esta skill te entrega los patrones para modelar estados UI y resultados de operaciones en Solvyx usando `sealed interface`, `sealed class` y `Result`. Aplícala al diseñar UiState y los retornos de operaciones en ViewModels y Repositories.

## Principios

1. **Un `sealed` por dominio.** No crear sealed genéricos que se reutilizan para todo.
2. **Todos los casos de éxito/error explícitos.** No uses `null` para indicar error.
3. **Mapea excepciones a casos del sealed.** No propagues `Exception` al UI.
4. **`Result<T>` para errores donde el tipo no importa.** `sealed` cuando el VM necesita actuar diferente según el error.

## Estados UI con sealed

### Patrón básico

```kotlin
sealed interface EjerciciosUiState {
    object Loading : EjerciciosUiState
    data class Loaded(
        val ejercicios: List<Ejercicio>,
        val filtroActivo: TipoEjercicio? = null
    ) : EjerciciosUiState
    data class Error(val mensaje: String) : EjerciciosUiState
}
```

### Con sub-estados

```kotlin
sealed interface BitacoraFormState {
    object Editando : BitacoraFormState
    object Guardando : BitacoraFormState
    data class Exito(val entry: BitacoraEntry) : BitacoraFormState
    data class Error(val tipo: ErrorForm) : BitacoraFormState
}

enum class ErrorForm {
    FECHA_INVALIDA,
    NOTA_MUY_LARGA,
    SUSTANCIA_REQUERIDA_SI_CONSUMO,
    DB_ERROR
}
```

### Estados complejos con sealed

```kotlin
sealed interface DetalleEjercicioState {
    object Loading : DetalleEjercicioState
    data class Loaded(val ejercicio: Ejercicio) : DetalleEjercicioState
    object NoEncontrado : DetalleEjercicioState  // 404-like
    data class Error(val mensaje: String) : DetalleEjercicioState
}
```

## Uso en ViewModel

```kotlin
@HiltViewModel
class EjerciciosViewModel @Inject constructor(
    private val repo: EjerciciosRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<EjerciciosUiState>(EjerciciosUiState.Loading)
    val state: StateFlow<EjerciciosUiState> = _state.asStateFlow()
    
    init {
        cargar()
    }
    
    private fun cargar() {
        viewModelScope.launch {
            repo.observeEjercicios()
                .catch { e -> 
                    _state.value = EjerciciosUiState.Error("No pudimos cargar los ejercicios.")
                }
                .collect { lista ->
                    _state.value = EjerciciosUiState.Loaded(lista)
                }
        }
    }
}
```

## Uso en Compose

```kotlin
@Composable
fun EjerciciosScreen(viewModel: EjerciciosViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    when (val current = state) {
        is EjerciciosUiState.Loading -> LoadingState()
        is EjerciciosUiState.Loaded -> EjerciciosList(current.ejercicios)
        is EjerciciosUiState.Error -> ErrorState(current.mensaje)
    }
}
```

## Result tipado en Repository

### Usando `Result<T>` estándar

```kotlin
override suspend fun guardarBitacora(entry: BitacoraEntry): Result<BitacoraEntry> = runCatching {
    withContext(Dispatchers.IO) {
        val id = dao.upsert(entry.toEntity())
        entry.copy(id = id)
    }
}
```

Uso en VM:

```kotlin
fun onGuardar() {
    viewModelScope.launch {
        val result = repo.guardarBitacora(state.value.entry)
        result.fold(
            onSuccess = { _effects.send(Effect.Exito) },
            onFailure = { _state.update { it.copy(error = "No pudimos guardar.") } }
        )
    }
}
```

### Usando sealed para errores tipados

```kotlin
sealed class GuardarBitacoraResultado {
    data class Exito(val entryGuardada: BitacoraEntry) : GuardarBitacoraResultado()
    sealed class Error : GuardarBitacoraResultado() {
        object FechaInvalida : Error()
        object NotaMuyLarga : Error()
        data class DbError(val original: Throwable) : Error()
    }
}

override suspend fun guardarBitacora(entry: BitacoraEntry): GuardarBitacoraResultado {
    if (entry.fecha <= 0) return GuardarBitacoraResultado.Error.FechaInvalida
    if ((entry.nota?.length ?: 0) > 100) return GuardarBitacoraResultado.Error.NotaMuyLarga
    
    return try {
        val id = withContext(Dispatchers.IO) { dao.upsert(entry.toEntity()) }
        GuardarBitacoraResultado.Exito(entry.copy(id = id))
    } catch (e: SQLiteException) {
        GuardarBitacoraResultado.Error.DbError(e)
    }
}
```

VM con manejo diferenciado:

```kotlin
fun onGuardar() {
    viewModelScope.launch {
        when (val result = repo.guardarBitacora(state.value.entry)) {
            is GuardarBitacoraResultado.Exito -> _effects.send(Effect.Exito(result.entryGuardada))
            is GuardarBitacoraResultado.Error.FechaInvalida -> _state.update { it.copy(errorForm = "La fecha no es válida.") }
            is GuardarBitacoraResultado.Error.NotaMuyLarga -> _state.update { it.copy(errorForm = "La nota no puede pasar de 100 caracteres.") }
            is GuardarBitacoraResultado.Error.DbError -> _state.update { it.copy(errorForm = "No pudimos guardar. Intenta de nuevo.") }
        }
    }
}
```

## Mapping de excepciones a mensajes

Función helper en el VM:

```kotlin
private fun Throwable.toUserMessage(): String = when (this) {
    is SQLiteConstraintException -> "Ya existe un registro con esos datos."
    is SQLiteException -> "No pudimos guardar. Intenta de nuevo."
    is IOException -> "Problema de almacenamiento. Revisa el espacio disponible."
    is JsonDecodingException -> "Hubo un error cargando el contenido."
    else -> "Algo salió mal. Estamos trabajando en ello."
}
```

## Cuándo NO usar sealed

- Errores donde solo necesitas saber "falló o no". Usa `Result<T>`.
- Errores donde el VM siempre hace lo mismo (mostrar mensaje genérico). No tipes.
- Estados donde solo hay éxito o error sin estados intermedios (ej. `Result<T>`).

## Cuándo SÍ usar sealed

- Múltiples tipos de error con UI diferente.
- Múltiples estados (Loading, Empty, Loaded, Error).
- Resultados polimórficos (éxito con diferentes variantes).

## Anti-patrones prohibidos

1. **`sealed class` con un solo caso.** Es una `data class`.
2. **`Any` o `Object` genérico** como tipo de retorno cuando hay errores tipados.
3. **Throwable escapado al UI.** Mapear siempre.
4. **Estado nullable** (`UiState?`) en lugar de sealed.
5. **Mezclar `Result<T>` con sealed** sin razón clara.
6. **Mismo sealed para UiState y Effect.** Son conceptos distintos.