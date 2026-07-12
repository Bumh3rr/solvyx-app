---
description: Versionado de seed data para Solvyx. Cuándo crear nueva versión, cómo migrar contenido sin perder datos del usuario, version.json.
---

# Skill: Content Versioning

Esta skill te entrega las convenciones para versionar contenido estático (seed) en Solvyx. Aplícala cuando agregues nuevos items, modifiques copy existente o cambies estructura de un archivo de seed.

## Principios

1. **El contenido versionado protege datos del usuario.** Crear v(N+1) sin migrar puede romper referencias en BD.
2. **Versionado independiente del schema de Room.** Una migración de seed NO requiere bump de `@Database(version)`.
3. **`version.json` es la fuente de verdad.** Los archivos individuales también tienen `_seed_version`.
4. **Soft delete por defecto.** No se borra contenido físicamente; se marca `activo = false`.
5. **Cada nueva versión documenta su origen y propósito.**

## Cuándo crear nueva versión de seed

| Cambio | Versión |
|---|---|
| Agregar nuevo ejercicio, guía, lección o prompt | **Sí**, nueva versión. |
| Modificar copy existente (typo, validación clínica) | **Sí**, nueva versión. |
| Cambiar estructura de campos (nuevo campo en JSON) | **Sí**, nueva versión + schema change si aplica. |
| Reordenar items | **No**, in-place. |
| Eliminar item de la lista activa | Soft delete (in-place con `activo=false`). |
| Cambiar formato de serialización (kebab-case → snake_case) | Migración destructiva justificada. |

## Estructura de versionado

### `version.json` (índice global)

```json
{
  "_format": "solvyx-seed-index",
  "_schema_version": 1,
  "_latest_seed_version": 2,
  "_available_versions": [
    {
      "version": 1,
      "released_at": "2026-01-15T00:00:00Z",
      "files": ["ejercicios.json", "guias.json", "lecciones.json", "prompts_journaling.json", "rutinas.json"],
      "description": "Seed inicial con ASSIST, guías originales, 4 ejercicios base."
    },
    {
      "version": 2,
      "released_at": "2026-07-15T00:00:00Z",
      "app_min_version": "1.3.0",
      "files": ["ejercicios.json", "lecciones.json"],
      "description": "6 nuevos ejercicios, 3 lecciones de vape agregadas, copy revisado clínicamente.",
      "migrates_from": 1
    }
  ]
}
```

### Cada archivo de seed

```json
{
  "_format": "solvyx-seed-ejercicios",
  "_schema_version": 1,
  "_seed_version": 2,
  "_created_at": "2026-07-15T00:00:00Z",
  "_reviewed_by": "psicologo-solvyx 2026-07-15 v1",
  "_previous_version": 1,
  "_migration_notes": "Agregados 6 nuevos ejercicios de regulación. Copy revisado. Sin cambios incompatibles.",
  "items": [ /* ... */ ]
}
```

## Cómo Solvyx decide qué versión cargar

```kotlin
@Singleton
class SeedVersionManager @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val assetsLoader: AssetsLoader
) {
    
    /**
     * Determina la versión de seed a usar.
     * 
     * Lógica:
     * 1. Lee `assets/seed/version.json` para saber qué versiones están disponibles.
     * 2. Lee la versión actualmente aplicada en BD desde preferences.
     * 3. Si la versión actual < latest disponible, aplica migración.
     * 4. Si el usuario tiene una versión mayor (downgrade), no rompe nada.
     */
    suspend fun resolveTargetVersion(): Int {
        val indexJson = assetsLoader.readSeedIndex()
        val index = SolvyxJson.instance.decodeFromString<SeedIndex>(indexJson)
        val current = preferences.getSeedVersion()
        
        return when {
            current == 0 -> index.latestSeedVersion  // primera instalación
            current < index.latestSeedVersion -> index.latestSeedVersion
            else -> current  // ya está en latest o superior (downgrade)
        }
    }
    
    suspend fun applySeed(version: Int) {
        val files = listSeedFiles(version)
        for (file in files) {
            applySeedFile(version, file)
        }
        preferences.setSeedVersion(version)
    }
}
```

## Migraciones de contenido

### Caso A: Solo nuevos items

```kotlin
// ejercicios.json v2 incluye los 6 originales + 6 nuevos.
// Estrategia: upsert por slug. Los nuevos se insertan, los existentes se actualizan.
fun migrateEjercicios(fromVersion: Int, toVersion: Int) {
    val fromList = loadSeed(fromVersion, "ejercicios.json")
    val toList = loadSeed(toVersion, "ejercicios.json")
    
    val newSlugs = toList.map { it.slug }.toSet() - fromList.map { it.slug }.toSet()
    
    toList.forEach { item ->
        if (item.slug in newSlugs || item.slug in fromList.map { it.slug }.toSet()) {
            dao.upsert(item.toEntity().copy(updatedAt = now()))
        }
    }
}
```

### Caso B: Item eliminado

```kotlin
// En v2, "ejercicio-1" fue removido de la lista activa.
// Estrategia: marcar activo = false, no borrar.
fun softDeleteEjercicios(removedSlugs: List<String>) {
    removedSlugs.forEach { slug ->
        dao.softDeleteBySlug(slug)
    }
}
```

### Caso C: Cambio estructural

Si el modelo de dominio cambió (nuevo campo `origenTts`), se requiere migración de schema Room Y de seed. Coordinar con `backend-data-architect`.

```kotlin
// 1. Migración Room (4 → 5) agrega columna "origen_tts"
// 2. Migración de seed (v1 → v2) popula "origen_tts" desde el JSON
fun migrateSeedEstructural(fromVersion: Int, toVersion: Int) {
    val toList = loadSeed(toVersion, "ejercicios.json")
    toList.forEach { item ->
        dao.upsert(item.toEntity().copy(origenTts = item.origenTts ?: ""))
    }
}
```

## Preservar datos del usuario

Tres datos cruciales del usuario NO se sobrescriben al migrar seed:

1. **`activo` (soft delete):** el seed nunca cambia este campo directamente. Lo controla la lógica del usuario.
2. **`completado` / `visto`:** si el usuario marcó una lección como vista, eso persiste.
3. **`favorito` / `destacado`:** marca personal del usuario.

Estrategia:

```kotlin
fun applySeedRespectingUserData(item: EjercicioSeedItem, existing: EjercicioEntity?) {
    val userFields = existing?.let {
        EjercicioEntity(
            slug = item.slug,
            nombre = item.nombre,
            tipo = item.tipo,
            duracionMinutos = item.duracionMinutos,
            pasos = item.pasos,
            activo = it.activo,        // conserva estado del usuario
            favorito = it.favorito,    // conserva marca personal
            createdAt = it.createdAt,  // conserva fecha de creación
            updatedAt = now()
        )
    } ?: item.toEntity().copy(updatedAt = now())
    
    dao.upsert(userFields)
}
```

## Cuándo DESTRUCTIVO (con justificación)

Solo si:

1. El modelo de dominio cambió fundamentalmente (ej. se renombró "consumo" → "usoProblematico").
2. El usuario aceptó perder su historial (ej. un "reset total" explícito en Mi Perfil).
3. Es beta temprana donde los datos no son producción.

Documentar en `version.json`:

```json
{
  "version": 3,
  "destructive": true,
  "destructive_notes": "Reset total de bitácora. El usuario debe confirmar manualmente."
}
```

Y requerir acción del usuario:

```kotlin
fun applyDestructiveSeed(version: Int) {
    if (preferences.isDestructiveSeedConfirmed()) {
        db.clearAllTables()
        applySeed(version)
    } else {
        _effects.send(Effect.PromptDestructiveReset(version))
    }
}
```

## Versionado semántico del seed

| Bump | Significado |
|---|---|
| Patch (1.0 → 1.0.1) | Solo dentro de una versión. No se versiona en assets. |
| Minor (1.0 → 1.1) | Nuevos items, copy mejorado. No rompe nada. |
| Major (1.0 → 2.0) | Cambios incompatibles. Requiere migración de BD. |

## Convención de nombres

- `seed/v1/ejercicios.json`
- `seed/v2/ejercicios.json`
- Los archivos pueden renombrarse entre versiones si la convención cambia (ej. `ejercicios.json` → `regulacion.json`), pero debe documentarse en `_migration_notes`.

## Anti-patrones prohibidos

1. **Borrar físicamente items.** Soft delete siempre.
2. **Sobrescribir datos del usuario** (favoritos, marcas personales, progreso).
3. **Saltar versiones** (1 → 3 sin pasar por 2).
4. **No documentar migraciones.**
5. **Modificar `version.json` sin bumpear `_latest_seed_version`.**
6. **Asumir que el seed se aplica una sola vez.** Aplicar idempotente cada vez que el usuario abre la app (con cache).
7. **Esconder cambios destructivos.** Si lo son, declararlos y pedir confirmación.