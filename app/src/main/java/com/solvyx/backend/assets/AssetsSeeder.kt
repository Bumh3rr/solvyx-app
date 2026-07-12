package com.solvyx.backend.assets

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.solvyx.backend.data.local.dao.EjercicioDao
import com.solvyx.backend.data.local.dao.GuiaExtendidaDao
import com.solvyx.backend.data.local.dao.LeccionDao
import com.solvyx.backend.data.local.dao.PromptJournalingDao
import com.solvyx.backend.data.local.dao.RutinaDao
import com.solvyx.backend.data.local.entity.EjercicioEntity
import com.solvyx.backend.data.local.entity.GuiaExtendidaEntity
import com.solvyx.backend.data.local.entity.LeccionEntity
import com.solvyx.backend.data.local.entity.PromptJournalingEntity
import com.solvyx.backend.data.local.entity.RutinaEntity
import com.solvyx.backend.data.local.entity.RutinaPasoEntity
import com.solvyx.backend.data.local.preferences.SeedPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cargador del contenido offline (seed) desde los assets de la APK.
 *
 * Origen de datos:
 * - `assets/seed/version.json` — índice con la última versión disponible.
 * - `assets/seed/vN/...` — archivos de la versión N (cualquier archivo
 *   `.json` dentro). En la v1 del seed
 *   solo existe la carpeta `v1/`; cuando haya un `v2/` con parches, el
 *   seeder lo soportará leyendo ambos.
 *
 ## Política de versionado
 *
 * La versión persistida en [SeedPreferencesRepository] refleja "hasta qué
 * versión del seed tenemos cargada en SQLite". El flujo es:
 *
 * ```
 * persistedVersion = prefs.getSeedVersion()        // 0 si nunca se cargó
 * latestVersion    = readVersionJson()._latest_seed_version
 * if (persistedVersion < latestVersion) {
 *     loadSeedFromAssets()                          // re-carga los archivos
 *     prefs.setSeedVersion(latestVersion)
 * }
 * ```
 *
 * En la primera ejecución `persistedVersion == 0` y se carga el seed. En
 * siguientes arranques, si el JSON no cambió, no se hace trabajo.
 *
 ## Decisiones de implementación
 *
 * - El parsing se hace en [Dispatchers.Default] (CPU bound) y la escritura
 *   en Room delega al `Dispatchers.IO` configurado por Room.
 * - Se usa Gson (ya en el proyecto) en lugar de `kotlinx.serialization`
 *   para no añadir dependencias.
 * - El JSON de cada item es opaco para Room (la entity guarda strings
 *   JSON-encoded), pero aquí se extraen los campos "planos" para construir
 *   la entity. Los campos anidados (`pasos`, `ttsText`, `contenido`)
 *   se **vuelven a serializar** al formato esperado por la entity.
 * - Se usa `upsert`/`upsertAll` en cada DAO, así que el seeder es
 *   idempotente: correrlo dos veces no duplica filas.
 * - Si el seed cambia (nuevos items o correcciones), la entity Room
 *   hace REPLACE por PK, así que solo se actualizan las filas existentes.
 *   Esto es lo que permite "corregir contenido sin re-instalar la app".
 */
@Singleton
class AssetsSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SeedPreferencesRepository,
    private val ejercicioDao: EjercicioDao,
    private val guiaExtendidaDao: GuiaExtendidaDao,
    private val leccionDao: LeccionDao,
    private val rutinaDao: RutinaDao,
    private val promptJournalingDao: PromptJournalingDao
) {
    private val gson = Gson()

    /**
     * Carga el seed archivo por archivo. Para cada archivo del seed v1
     * (la única carpeta presente por ahora), lee su `_seed_version`
     * interno, lo compara con la versión persistida de ese archivo
     * específico, y si es mayor, lo carga. Al terminar, actualiza la
     * versión persistida de cada archivo cargado.
     *
     * Esto resuelve el bug de la v1 donde el versionado global podía
     * hacer que un archivo v1 nunca se cargara si la versión global
     * ya estaba en v2 por haber cargado otros archivos.
     *
     * Devuelve [SeedResult.Loaded] cuando se cargó al menos un archivo
     * y [SeedResult.AlreadyUpToDate] cuando no hizo falta. Cualquier
     * excepción se mapea a [SeedResult.Failed] con un mensaje
     * user-friendly en español.
     *
     * Es seguro llamarlo varias veces: si todos los archivos están al día,
     * sale rápido.
     */
    suspend fun ensureLoaded(): SeedResult = withContext(Dispatchers.Default) {
        runCatching {
            var anyLoaded = false
            val loaded = mutableListOf<String>()

            for ((filename, loader) in seedFiles) {
                val internalVersion = readSeedVersion("$SEED_DIR/$V1/$filename")
                val persistedVersion = prefs.getFileVersion(filename)

                if (internalVersion > persistedVersion) {
                    loader()
                    prefs.setFileVersion(filename, internalVersion)
                    anyLoaded = true
                    loaded += filename
                }
            }

            if (anyLoaded) {
                SeedResult.Loaded(loaded = loaded)
            } else {
                SeedResult.AlreadyUpToDate(persisted = prefs.getSeedVersion())
            }
        }.getOrElse { e ->
            SeedResult.Failed(
                userMessage = "No pudimos cargar el contenido offline. Inténtalo de nuevo.",
                cause = e
            )
        }
    }

    /**
     * Fuerza la recarga de TODO el seed, ignorando las versiones
     * persistidas. Pensado para:
     * - Botón de "Restablecer contenido" en ajustes.
     * - Tests que parten de un estado conocido.
     *
     * NO se llama desde el flujo normal; usar [ensureLoaded] en su lugar.
     */
    suspend fun forceReload(): SeedResult = withContext(Dispatchers.Default) {
        runCatching {
            val loaded = mutableListOf<String>()
            for ((filename, loader) in seedFiles) {
                loader()
                val internalVersion = readSeedVersion("$SEED_DIR/$V1/$filename")
                prefs.setFileVersion(filename, internalVersion)
                loaded += filename
            }
            SeedResult.Loaded(loaded = loaded)
        }.getOrElse { e ->
            SeedResult.Failed(
                userMessage = "No pudimos recargar el contenido offline.",
                cause = e
            )
        }
    }

    // ---------------------------------------------------------------
    // Lectura del índice de versiones y de archivos individuales
    // ---------------------------------------------------------------

    /**
     * Lee la versión declarada dentro de un archivo de seed individual
     * (su campo `_seed_version`). Devuelve 1 por defecto si no está
     * presente.
     */
    private fun readSeedVersion(path: String): Int {
        return runCatching {
            val obj = readJsonObject(path)
            obj.get("_seed_version")?.asInt ?: 1
        }.getOrDefault(1)
    }

    private fun readVersionJson(): VersionInfo {
        val json = context.assets.open("$SEED_DIR/version.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val obj = gson.fromJson(json, JsonObject::class.java)
            ?: error("version.json no es un objeto JSON válido")
        val latest = obj.get("_latest_seed_version")?.asInt
            ?: error("version.json缺少 _latest_seed_version")
        return VersionInfo(latestSeedVersion = latest)
    }

    /**
     * Mapa `nombre_archivo -> función_de_carga` que define qué archivos
     * componen el seed v1. Agregar un archivo nuevo al seed es agregar
     * una entrada aquí.
     */
    private val seedFiles: Map<String, suspend () -> Unit> = mapOf(
        "ejercicios.json" to ::loadEjerciciosInternal,
        "guias_extendidas.json" to ::loadGuiasExtendidasInternal,
        "lecciones.json" to ::loadLeccionesInternal,
        "rutinas.json" to ::loadRutinasInternal,
        "rutina_pasos.json" to ::loadRutinasInternal,  // se carga junto con rutinas
        "prompts_journaling.json" to ::loadPromptsJournalingInternal
    )

    // ---------------------------------------------------------------
    // Carga de la v1 (única carpeta presente en el seed actual)
    // ---------------------------------------------------------------
    //
    // Las funciones `loadXxxInternal` (no `loadXxx`) son referenciadas
    // por el mapa `seedFiles` vía referencias a métodos (::loadXxxInternal).
    // El sufijo `Internal` evita la colisión con posibles wrappers públicos
    // futuros y deja claro que son helpers privados.

    private suspend fun loadEjerciciosInternal() {
        val items = readItems<EjercicioSeedItem>("$SEED_DIR/$V1/ejercicios.json")
        val now = System.currentTimeMillis()
        val entities = items.map { it.toEntity(gson, now) }
        ejercicioDao.upsertAll(entities)
    }

    private suspend fun loadGuiasExtendidasInternal() {
        val items = readItems<GuiaExtendidaSeedItem>("$SEED_DIR/$V1/guias_extendidas.json")
        val now = System.currentTimeMillis()
        val entities = items.map { it.toEntity(now) }
        guiaExtendidaDao.upsertAll(entities)
    }

    private suspend fun loadLeccionesInternal() {
        val items = readItems<LeccionSeedItem>("$SEED_DIR/$V1/lecciones.json")
        val now = System.currentTimeMillis()
        val entities = items.map { it.toEntity(now) }
        leccionDao.upsertAll(entities)
    }

    private suspend fun loadRutinasInternal() {
        val rutinasJson = readJsonObject("$SEED_DIR/$V1/rutinas.json")
        val pasosJson = readJsonObject("$SEED_DIR/$V1/rutina_pasos.json")

        val rutinaType = object : TypeToken<List<RutinaSeedItem>>() {}.type
        val pasoType = object : TypeToken<List<RutinaPasoSeedItem>>() {}.type

        val rutinas: List<RutinaSeedItem> = gson.fromJson(rutinasJson.get("items"), rutinaType)
        val pasos: List<RutinaPasoSeedItem> = gson.fromJson(pasosJson.get("items"), pasoType)

        val now = System.currentTimeMillis()
        // Los pasos vienen agrupados por `rutinaSlug`. Cargamos cada rutina
        // con su set de pasos en una sola transacción.
        for (r in rutinas) {
            val pasosDeRutina = pasos.filter { it.rutinaSlug == r.slug }
            val rutinaEntity = RutinaEntity(
                slug = r.slug,
                nombre = r.nombre,
                descripcion = r.descripcion,
                horaSugerida = r.horaSugerida,
                iconAsset = r.iconAsset,
                activo = r.activo,
                createdAt = now,
                updatedAt = now
            )
            val pasoEntities = pasosDeRutina.map { p ->
                RutinaPasoEntity(
                    rutinaId = 0, // se re-mapea en la transacción del DAO
                    orden = p.orden,
                    titulo = p.titulo,
                    descripcion = p.descripcion,
                    duracionSegundos = p.duracionSegundos,
                    iconAsset = p.iconAsset,
                    createdAt = now
                )
            }
            rutinaDao.upsertRutinaConPasos(rutinaEntity, pasoEntities)
        }
    }

    private suspend fun loadPromptsJournalingInternal() {
        val items = readItems<PromptJournalingSeedItem>("$SEED_DIR/$V1/prompts_journaling.json")
        val now = System.currentTimeMillis()
        val entities = items.map { it.toEntity(now) }
        promptJournalingDao.upsertAll(entities)
    }

    // ---------------------------------------------------------------
    // Helpers de lectura y parsing
    // ---------------------------------------------------------------

    private inline fun <reified T> readItems(path: String): List<T> {
        val obj = readJsonObject(path)
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(obj.get("items"), type)
    }

    private fun readJsonObject(path: String): JsonObject {
        val json = context.assets.open(path)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        return gson.fromJson(json, JsonObject::class.java)
            ?: error("$path no es un objeto JSON válido")
    }

    // ---------------------------------------------------------------
    // Estructuras internas para el seed
    // ---------------------------------------------------------------

    private data class VersionInfo(val latestSeedVersion: Int)

    private data class EjercicioSeedItem(
        val slug: String,
        val nombre: String,
        val tipo: String,
        val duracionMinutos: Int,
        val descripcionCorta: String,
        val pasos: List<String>,
        val ttsText: Map<String, String>,
        val iconAsset: String?,
        val orden: Int
    ) {
        fun toEntity(gson: Gson, now: Long): EjercicioEntity = EjercicioEntity(
            slug = slug,
            nombre = nombre,
            tipo = tipo,
            duracionMinutos = duracionMinutos,
            descripcionCorta = descripcionCorta,
            pasos = gson.toJson(pasos),
            ttsText = gson.toJson(ttsText),
            iconAsset = iconAsset,
            orden = orden,
            activo = true,
            createdAt = now,
            updatedAt = now
        )
    }

    private data class GuiaExtendidaSeedItem(
        val slug: String,
        val titulo: String,
        val categoria: String,
        val descripcionCorta: String,
        val contenido: JsonObject,
        val iconAsset: String?,
        val orden: Int
    ) {
        fun toEntity(now: Long): GuiaExtendidaEntity = GuiaExtendidaEntity(
            slug = slug,
            titulo = titulo,
            categoria = categoria,
            descripcionCorta = descripcionCorta,
            contenido = contenido.toString(),
            iconAsset = iconAsset,
            orden = orden,
            activo = true,
            createdAt = now,
            updatedAt = now
        )
    }

    private data class LeccionSeedItem(
        val slug: String,
        val sustancia: String,
        val tema: String,
        val titulo: String,
        val contenido: JsonObject,
        val duracionLecturaMinutos: Int,
        val orden: Int
    ) {
        fun toEntity(now: Long): LeccionEntity = LeccionEntity(
            slug = slug,
            sustancia = sustancia,
            tema = tema,
            titulo = titulo,
            contenido = contenido.toString(),
            duracionLecturaMinutos = duracionLecturaMinutos,
            orden = orden,
            activo = true,
            createdAt = now,
            updatedAt = now
        )
    }

    private data class RutinaSeedItem(
        val slug: String,
        val nombre: String,
        val descripcion: String,
        val horaSugerida: Int,
        val iconAsset: String?,
        val activo: Boolean
    )

    private data class RutinaPasoSeedItem(
        val rutinaSlug: String,
        val orden: Int,
        val titulo: String,
        val descripcion: String,
        val duracionSegundos: Int,
        val iconAsset: String?
    )

    private data class PromptJournalingSeedItem(
        val slug: String,
        val categoria: String,
        val texto: String,
        val orden: Int
    ) {
        fun toEntity(now: Long): PromptJournalingEntity = PromptJournalingEntity(
            categoria = categoria,
            texto = texto,
            orden = orden,
            activo = true,
            createdAt = now
        )
        // Nota: el `slug` del seed NO se persiste. Se reconstruye en el
        // mapper de dominio como `${categoria}-${orden.toString().padStart(3,'0')}`.
    }

    companion object {
        const val SEED_DIR = "seed"
        const val V1 = "v1"
    }
}

/**
 * Resultado de una operación del seeder.
 *
 * Se modela como sealed interface para que la UI o el caller (típicamente
 * el WorkManager scheduler) pueda decidir qué hacer en cada caso.
 */
sealed interface SeedResult {
    /** Se cargaron archivos. [loaded] lista los nombres de archivo. */
    data class Loaded(val loaded: List<String>) : SeedResult

    /** Ya estaba al día, no se hizo trabajo. [persisted] es la versión global legacy. */
    data class AlreadyUpToDate(val persisted: Int) : SeedResult

    /** Algo falló. El caller debe mostrar [userMessage] al usuario. */
    data class Failed(val userMessage: String, val cause: Throwable) : SeedResult
}
