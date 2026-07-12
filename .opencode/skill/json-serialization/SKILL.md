---
description: Convenciones de kotlinx.serialization para seed data y DTOs de Solvyx. JSON, configuración, polymorphic.
---

# Skill: JSON Serialization

Esta skill te entrega las convenciones para serialización JSON con kotlinx.serialization en Solvyx. Aplícala al crear assets de seed, DTOs, respuestas de API (futuras), y cualquier modelo que se serialice a JSON.

## Setup del proyecto

Ya integrado en `app/build.gradle.kts`:

```kotlin
plugins {
    kotlin("plugin.serialization") version "2.x.x"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.x.x")
}
```

## Objeto Json compartido

Un solo `Json` configurado en el proyecto:

```kotlin
object SolvyxJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true       // tolerante a campos nuevos
        explicitNulls = false          // omite nulls en encode
        prettyPrint = false            // false para storage, true para debug
        encodeDefaults = true          // serializa defaults
        coerceInputValues = true       // null → default si existe
    }
}
```

Usa `SolvyxJson.instance` siempre. No crees instancias locales.

## Data classes serializables

### Regla base

```kotlin
@Serializable
data class EjercicioSeed(
    val slug: String,
    val nombre: String,
    val tipo: String,                  // enum como String, convertido en mapper
    val duracionMinutos: Int,
    val pasos: List<String>,
    val copyBienvenida: String,
    val copyCierre: String
)
```

### Serialización de enums

Por defecto, los enums se serializan como `String` (nombre). Si necesitas ordinal (no recomendado), configura:

```kotlin
@Serializable
enum class TipoEjercicio { RESPIRACION, GROUNDING, BODY_SCAN, ACTIVACION }
```

Para mapear entre String del JSON y enum:

```kotlin
fun TipoEjercicio.toStringValue(): String = when (this) {
    TipoEjercicio.RESPIRACION -> "respiracion"
    TipoEjercicio.GROUNDING -> "grounding"
    // ...
}

fun String.toTipoEjercicio(): TipoEjercicio? = entries.firstOrNull { it.toStringValue() == this }
```

## Listas y mapas

```kotlin
@Serializable
data class LeccionSeed(
    val slug: String,
    val sustancia: String,
    val titulo: String,
    val bloques: List<BloqueLeccion>     // data class polimórfico o simple
)

@Serializable
data class BloqueLeccion(
    val tipo: String,                    // "texto", "lista", "linea_ayuda"
    val contenido: String,
    val items: List<String>? = null,
    val telefono: String? = null
)
```

## Polimorfismo

Si necesitas tipos diferentes en una misma lista:

```kotlin
@Serializable
sealed class BloqueContenido {
    abstract val key: String
    
    @Serializable
    @SerialName("texto")
    data class Texto(val contenido: String) : BloqueContenido() {
        override val key = "texto"
    }
    
    @Serializable
    @SerialName("linea_ayuda")
    data class LineaAyuda(val nombre: String, val telefono: String, val horario: String?) : BloqueContenido() {
        override val key = "linea_ayuda"
    }
}

// Uso:
val json = SolvyxJson.instance
val bloques: List<BloqueContenido> = json.decodeFromString(jsonString)
```

## Encoding a JSON para assets

```kotlin
fun encodeSeed(entities: List<EjercicioSeed>): String {
    val map = mapOf(
        "_version" to 1,
        "_created_at" to System.currentTimeMillis(),
        "items" to entities
    )
    return SolvyxJson.instance.encodeToString(map)
}

fun decodeSeed(jsonString: String): SeedFile {
    return SolvyxJson.instance.decodeFromString<SeedFile>(jsonString)
}

@Serializable
data class SeedFile(
    @SerialName("_version") val version: Int,
    @SerialName("_created_at") val createdAt: Long,
    val items: List<EjercicioSeed>
)
```

## Lectura desde assets

```kotlin
suspend fun loadEjerciciosFromAssets(context: Context): List<EjercicioSeed> = withContext(Dispatchers.IO) {
    val json = context.assets.open("seed/v1/ejercicios.json")
        .bufferedReader()
        .use { it.readText() }
    
    val seedFile = SolvyxJson.instance.decodeFromString<SeedFile<EjercicioSeed>>(json)
    seedFile.items
}
```

## Versionado de seed

Cada archivo de seed tiene metadata:

```json
{
  "_version": 1,
  "_created_at": "2026-07-15T00:00:00Z",
  "_app_min_version": "1.2.0",
  "items": [ /* ... */ ]
}
```

Al cargar, comparar `_version` con la versión actual en BD. Si difiere, ejecutar migración de seed (no de schema).

## Errores comunes

| Error | Solución |
|---|---|
| `MissingFieldException` | Campo obligatorio sin default. Agregar `= null` o default. |
| `JsonDecodingException` | JSON malformado. Loguear el archivo problemático. |
| Enum ordinal cambia al reordenar | Usar `SerialName` o `String` con mapper manual. |
| NullPointerException en deserialización | `explicitNulls = false` en Json config, o agregar `= null`. |
| Polimorfismo no reconocido | Verificar que `@SerialName` coincida con el JSON. |

## Performance

1. **`encodeToString` es CPU-intensive.** Hazlo en `Dispatchers.Default` para datasets grandes.
2. **`decodeFromString` también.** Para seed inicial (puede ser grande), usa `withContext(Dispatchers.IO)`.
3. **Reutiliza el objeto `Json`** — es thread-safe y costoso de construir.
4. **Para datasets grandes:** considera usar streaming (`Json.decodeFromJsonTree` con iteradores).

## Testing

```kotlin
@Test
fun ejercicio_seed_roundtrip() {
    val original = EjercicioSeed(
        slug = "respiracion-4-7-8",
        nombre = "Respiración 4-7-8",
        tipo = "respiracion",
        duracionMinutos = 3,
        pasos = listOf("Inhala 4 segundos", "Sostén 7", "Exhala 8"),
        copyBienvenida = "Vamos a hacerlo juntos",
        copyCierre = "Lo lograste"
    )
    
    val json = SolvyxJson.instance.encodeToString(original)
    val decoded = SolvyxJson.instance.decodeFromString<EjercicioSeed>(json)
    
    assertEquals(original, decoded)
}
```

## Anti-patrones prohibidos

1. **`@Serializable` con `var`** — campos mutables rompen inmutabilidad.
2. **Hardcodear el `Json` formatter** — usa siempre `SolvyxJson.instance`.
3. **Mezclar `kotlinx.serialization` con `Gson` o `Moshi`** — incompatibles.
4. **`encodeToString` en `Dispatchers.Main`** con datasets grandes.
5. **JSON en duro en código Kotlin** — usa assets.
6. **Olvidar `@SerialName`** cuando el nombre del campo en JSON difiere del Kotlin.
