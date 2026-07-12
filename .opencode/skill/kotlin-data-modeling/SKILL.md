---
description: Convenciones de modelado de datos en Kotlin para Solvyx: data classes, sealed classes, enums, value objects y nullability.
---

# Skill: Kotlin Data Modeling

Esta skill te entrega las convenciones para modelar datos en Kotlin en Solvyx. Aplícala al crear entidades, modelos de dominio, DTOs y cualquier estructura de datos.

## Principios

1. **Inmutabilidad por defecto.** Los datos no mutan; se reemplazan.
2. **Tipos expresivos.** Si un campo solo acepta ciertos valores, usa `enum` o `sealed class`, no `String` libre.
3. **Null safety explícita.** Si un campo puede no existir, decláralo nullable. Si siempre existe, no nullable.
4. **Validación en construcción.** Si un objeto requiere invariantes (ej. email válido, edad > 0), valida en `init {}`.

## Data classes

### Cuándo usar data class

- Modelos de datos (DTOs, entidades, responses).
- Cualquier estructura que se compare por valor.

### Cuándo NO usar data class

- Lógica de negocio con estado mutable.
- Interfaces con métodos que cambian.

### Plantilla estándar

```kotlin
data class Ejercicio(
    val id: EjercicioId,                  // value object
    val slug: String,
    val nombre: String,
    val tipo: TipoEjercicio,
    val duracionMinutos: Int,
    val pasos: List<PasoEjercicio>,        // no String JSON
    val activo: Boolean = true
)
```

## Value Objects

Encapsula primitivos con semántica de dominio:

```kotlin
@JvmInline
value class EjercicioId(val value: Long)

@JvmInline
value class Fecha(val epochMillis: Long) {
    init {
        require(value > 0) { "Fecha debe ser positiva" }
    }
    
    fun formatoCorto(): String { /* ... */ }
}
```

### Cuándo usar value class

- IDs primitivos que se confunden entre sí (ej. `Long id` para `Bitacora` vs `Ejercicio`).
- Tipos con semántica de dominio (ej. `Email`, `Telefono`).
- Performance crítica (evita allocación de wrapper).

## Enums vs Sealed

### Enum: valores finitos conocidos

```kotlin
enum class TipoEjercicio(val displayName: String) {
    RESPIRACION("Respiración"),
    GROUNDING("Anclaje"),
    BODY_SCAN("Body scan"),
    ACTIVACION_CONDUCTUAL("Activación conductual");
    
    companion object {
        fun fromSlug(slug: String): TipoEjercicio? = entries.firstOrNull { it.name == slug }
    }
}
```

### Sealed: jerarquías con datos adicionales

```kotlin
sealed class ContenidoLeccion {
    abstract val texto: String
    
    data class Texto(override val texto: String) : ContenidoLeccion()
    data class ListaPasos(override val texto: String, val pasos: List<String>) : ContenidoLeccion()
    data class LlamadaALinea(override val texto: String, val telefono: String, val nombre: String) : ContenidoLeccion()
}
```

### Reglas

- Enum cuando los valores son fijos y no tienen comportamiento diferenciado.
- Sealed cuando cada variante tiene datos o comportamiento distintos.
- Evita `sealed interface` a menos que necesites multi-herencia.

## Null safety

### Reglas

1. **No nullable sin razón.** Si un campo es opcional, nullable con justificación.
2. **Default values en lugar de null** cuando tenga sentido (`activo: Boolean = true`).
3. **`?.let`, `?:`, `requireNotNull`, `orEmpty()`** según contexto.

```kotlin
// Bien
data class Usuario(
    val nombre: String,
    val email: String,
    val apodo: String? = null   // opcional
)

// Mal
data class Usuario(
    val nombre: String,
    val email: String?,
    val apodo: String           // si es opcional, declaralo nullable
)
```

## Validación en init

```kotlin
data class Nota(
    val texto: String,
    val fecha: Long
) {
    init {
        require(texto.length <= 100) { "Nota máximo 100 caracteres" }
        require(fecha > 0) { "Fecha inválida" }
    }
}
```

## Builder pattern solo cuando hay 5+ campos opcionales

```kotlin
data class BitacoraEntryBuilder(
    var fecha: Long = System.currentTimeMillis(),
    var animo: String = "neutral",
    var consumo: Boolean = false,
    var sustancia: String? = null,
    var nota: String? = null,
    // ... 5 opcionales más
)

fun build(): BitacoraEntry {
    require(nota?.length ?: 0 <= 100) { "..." }
    return BitacoraEntry(fecha, animo, consumo, sustancia, nota, /* ... */)
}
```

Si la entidad tiene menos de 5 opcionales, usa el constructor con defaults directamente.

## Lists, Sets, Maps

1. **Default a inmutable:** `List<T>` no `MutableList<T>` en data classes.
2. **`List<T>` para orden preservado sin unicidad.**
3. **`Set<T>` para unicidad.**
4. **`Map<K, V>` solo cuando necesites lookup por clave.** Si no, prefiere data class con campo.
5. **`emptyList()`, `emptySet()`** en lugar de `null` para listas vacías.

## equals, hashCode, copy

- Data class genera todo automático. Confía en ello.
- `copy()` para cambios inmutables: `entry.copy(consumo = true)`.
- Override de `equals` solo si la igualdad lógica difiere de la igualdad estructural (raro).

## Serialización

- `kotlinx.serialization` para JSON (ya en el stack del proyecto).
- `@Serializable` en data classes que van a JSON.
- `Json.encodeToString()` para serializar, `Json.decodeFromString<T>()` para deserializar.
- Configurar `Json { ignoreUnknownKeys = true; explicitNulls = false }` una sola vez en un objeto compartido.

## Anti-patrones prohibidos

1. **Mutabilidad pública:** `var` en data class. Usa `val` y `copy()`.
2. **Cualquier a `Any`:** evita `Any` salvo que sea justificado.
3. **String libre para enums:** si solo aceptas "alta", "media", "baja", usa enum.
4. **`!!` (non-null assertion):** casi siempre hay una mejor forma.
5. **Data class con lógica en `init`:** si tienes side effects, no es data class.
6. **Herencia abierta:** evita `open class` salvo justificación. Prefiere composición o sealed.
