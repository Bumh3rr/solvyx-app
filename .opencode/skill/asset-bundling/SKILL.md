---
description: Cómo Solvyx empaqueta contenido en assets/, lee JSON, versiona seeds y actualiza contenido local sin conexión.
---

# Skill: Asset Bundling

Esta skill te entrega las convenciones para empaquetar contenido clínico/educativo en la carpeta `assets/` de Solvyx. Aplícala al crear seed data, recursos descargables o contenido estático que deba vivir dentro del APK.

## Principios

1. **Contenido versionado en subcarpetas.** `assets/seed/v1/`, `assets/seed/v2/`, etc.
2. **Un archivo JSON por dominio.** `ejercicios.json`, `guias.json`, `lecciones.json`. No monolitos.
3. **`_version`, `_created_at`, `_app_min_version`** en cada archivo como metadata.
4. **UTF-8 sin caracteres escapados innecesarios.** Saltos de línea como `\n` real.
5. **Carga en `Dispatchers.IO`** con `withContext` para evitar ANR.

## Estructura de carpetas

```
app/src/main/assets/
├── seed/
│   ├── version.json
│   ├── v1/
│   │   ├── ejercicios.json
│   │   ├── guias.json
│   │   ├── lecciones.json
│   │   ├── prompts_journaling.json
│   │   └── rutinas.json
│   └── v2/
│       ├── ejercicios.json  (con nuevos ejercicios)
│       ├── lecciones.json   (con 3 lecciones nuevas)
│       └── ...
├── audio/        (futuro: audios guiados si se decide incluir)
├── static/       (textos largos, FAQ, etc.)
└── config/
    └── defaults.json  (valores por defecto de preferencias)
```

## Formato de cada archivo

### version.json (global)

```json
{
  "_format": "solvyx-seed-index",
  "_schema_version": 1,
  "_latest_seed_version": 2,
  "_available_versions": [
    { "version": 1, "files": ["ejercicios.json", "guias.json", "lecciones.json", "prompts_journaling.json", "rutinas.json"] },
    { "version": 2, "files": ["ejercicios.json", "lecciones.json"], "app_min_version": "1.3.0" }
  ]
}
```

### Cada archivo de seed

```json
{
  "_format": "solvyx-seed-ejercicios",
  "_schema_version": 1,
  "_seed_version": 1,
  "_created_at": "2026-07-15T00:00:00Z",
  "_reviewed_by": "psicologo-solvyx v1",
  "items": [
    {
      "slug": "respiracion-4-7-8",
      "nombre": "Respiración 4-7-8",
      "tipo": "respiracion",
      "duracionMinutos": 3,
      "copyBienvenida": "Vamos a hacer una respiración juntos. Inhala, sostén, exhala.",
      "copyCierre": "Lo lograste. Tu cuerpo está más tranquilo.",
      "pasos": [
        "Inhala por la nariz contando 4 tiempos.",
        "Sostén la respiración 7 tiempos.",
        "Exhala por la boca 8 tiempos.",
        "Repite 4 ciclos."
      ],
      "tonoVoz": "calido",
      "ttsText": {
        "intro": "Bienvenido/a a la respiración 4-7-8.",
        "paso1": "Inhala por la nariz en 4 tiempos.",
        "paso2": "Sostén 7 tiempos.",
        "paso3": "Exhala por la boca en 8 tiempos.",
        "cierre": "Excelente. Tu cuerpo se está calmando."
      }
    }
  ]
}
```

### Data classes para deserializar

```kotlin
@Serializable
data class EjerciciosSeedFile(
    @SerialName("_format") val format: String,
    @SerialName("_schema_version") val schemaVersion: Int,
    @SerialName("_seed_version") val seedVersion: Int,
    @SerialName("_created_at") val createdAt: String,
    @SerialName("_reviewed_by") val reviewedBy: String,
    val items: List<EjercicioSeedItem>
)

@Serializable
data class EjercicioSeedItem(
    val slug: String,
    val nombre: String,
    val tipo: String,
    val duracionMinutos: Int,
    val copyBienvenida: String,
    val copyCierre: String,
    val pasos: List<String>,
    val tonoVoz: String,
    val ttsText: TtsText
)

@Serializable
data class TtsText(
    val intro: String,
    val paso1: String,
    val paso2: String,
    val paso3: String,
    val cierre: String
)
```

## Lector de assets

```kotlin
@Singleton
class AssetsLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun readSeedFile(version: Int, fileName: String): String = withContext(Dispatchers.IO) {
        context.assets.open("seed/v$version/$fileName")
            .bufferedReader()
            .use { it.readText() }
    }
    
    suspend fun listSeedFiles(version: Int): List<String> = withContext(Dispatchers.IO) {
        context.assets.list("seed/v$version")?.toList().orEmpty()
    }
    
    suspend fun readVersionIndex(): String = withContext(Dispatchers.IO) {
        context.assets.open("seed/version.json")
            .bufferedReader()
            .use { it.readText() }
    }
}
```

## Seeder

```kotlin
@Singleton
class EjerciciosSeeder @Inject constructor(
    private val assetsLoader: AssetsLoader,
    private val dao: EjercicioDao,
    private val preferences: UserPreferencesRepository
) {
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        val currentVersion = preferences.getEjerciciosSeedVersion()
        val targetVersion = getCurrentAssetsVersion()
        
        if (currentVersion < targetVersion) {
            applySeed(targetVersion)
            preferences.setEjerciciosSeedVersion(targetVersion)
        }
    }
    
    private suspend fun applySeed(version: Int) {
        val json = assetsLoader.readSeedFile(version, "ejercicios.json")
        val seedFile = SolvyxJson.instance.decodeFromString<EjerciciosSeedFile>(json)
        
        val entities = seedFile.items.map { it.toEntity() }
        dao.upsertAll(entities)
    }
    
    private fun getCurrentAssetsVersion(): Int = 2  // o leer de version.json
}
```

## Versionado

### Cuándo crear v(N+1)

- Nuevo campo en entidades (migración Room también).
- Nueva lección/ejercicio/guía agregada.
- Cambios mayores de copy validados clínicamente.

### Cuándo NO crear nueva versión

- Bugfix de typo en un copy → modificar in-place en la versión actual.
- Cambios menores de spacing/orden → in-place.

### Política de migraciones de seed

| Caso | Acción |
|---|---|
| Solo nuevos items | `INSERT ... ON CONFLICT REPLACE` con los nuevos. |
| Item modificado | UPDATE por slug. Conserva `updatedAt`. |
| Item eliminado | Soft delete (`activo = 0`). No se borra físicamente. |
| Cambios estructurales | Crear v(N+1) y dejar v(N) para downgrade. |

## Tamaño y límites

1. **APK size:** vigilar que `assets/` no supere ~2-3 MB total. Texto es barato; multimedia no.
2. **Archivos JSON:** legibles para diff en code review (sin minificar).
3. **Sin duplicación:** si un texto se repite en varios lados, extraer a `strings.xml` o a una constante.
4. **Strings de usuario** (visibles) van en `strings.xml` para futura localización. Solo el contenido clínico estático puede vivir en assets.

## Cuándo NO usar assets

- Datos específicos del usuario → Room.
- Preferencias → DataStore.
- Configuración dinámica → JSON descargado en runtime.
- Textos traducibles → `strings.xml`.

## Anti-patrones prohibidos

1. **JSON minificado en assets** (ilegible para code review).
2. **Archivos sin metadata de versión.**
3. **Carga de assets en `Dispatchers.Main`** con archivos grandes.
4. **Hardcodear rutas de assets** en múltiples lugares. Centralizar en `AssetsLoader`.
5. **Mezclar datos de usuario con seed estático** en la misma carpeta.
6. **Olvidar `withContext(Dispatchers.IO)`** al leer archivos.