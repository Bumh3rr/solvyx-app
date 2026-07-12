package com.solvyx.backend.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.solvyx.solvyxDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferencias de la carga inicial (seed) del contenido offline.
 *
 * Persiste en el DataStore `solvyx_prefs` (declarado en
 * [com.solvyx.SolvyxApp.solvyxDataStore]) un **mapa de versiones por archivo**:
 * `Map<String, Int>` donde la clave es el nombre del archivo (ej.
 * `"prompts_journaling.json"`) y el valor es la última versión de su seed
 * que fue cargada con éxito.
 *
 * # Por qué por-archivo y no global
 *
 * Inicialmente el seeder usaba un único entero global (`_seed_version`).
 * Eso provocaba que si un archivo (ej. `prompts_journaling.json`) tenía
 * `_seed_version: 1` y el global ya estaba en `2` por haber cargado
 * otro archivo (ej. `guias_extendidas.json`), el archivo v1 NUNCA se
 * cargaba: el seeder concluía que todo estaba al día.
 *
 * Con el versionado por archivo, el seeder recarga SOLO los archivos
 * cuya versión interna supera la persistida para ese archivo.
 *
 * # Migración desde el formato legacy
 *
 * Si en el DataStore existe el campo legacy `KEY_SEED_VERSION_GLOBAL`
 * con un entero (por ejemplo `2`), se interpreta como: "todos los
 * archivos de la v1 ya fueron cargados". Al primer `getFileVersion()`
 * para un archivo que NO esté en el mapa, se devuelve `1` (versión
 * inicial) si la versión global es ≥ 1, o `0` si nunca hubo carga.
 *
 * Esto asegura que al actualizar desde una versión anterior, los
 * archivos v1 que aún no se cargaron se carguen una vez.
 */
@Singleton
class SeedPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyGlobalVersion = stringPreferencesKey(KEY_SEED_VERSION_GLOBAL)
    private val keyFileVersions = stringPreferencesKey(KEY_FILE_VERSIONS)
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Int>>() {}.type

    /** Lee el estado completo de versiones de seed del DataStore. */
    private suspend fun readState(): SeedVersionState {
        val prefs = context.solvyxDataStore.data.first()
        val globalRaw = prefs[keyGlobalVersion]
        val fileMapJson = prefs[keyFileVersions]

        val global = globalRaw?.toIntOrNull() ?: 0
        val fileMap: Map<String, Int> = fileMapJson
            ?.let { runCatching { gson.fromJson<Map<String, Int>>(it, mapType) }.getOrNull() }
            ?: emptyMap()
        return SeedVersionState(global = global, fileVersions = fileMap)
    }

    /**
     * Versión persistida para un archivo específico.
     *
     * Si el archivo NO está en el mapa y existe una versión global legacy
     * ≥ 1, devuelve 1 (asumimos que los archivos v1 ya se intentaron cargar;
     * si el archivo en cuestión es nuevo, no estaba en v1 y debe
     * recargarse cuando bumpeemos su versión).
     *
     * Si no hay versión global ni del archivo, devuelve 0 (nunca se cargó).
     */
    suspend fun getFileVersion(filename: String): Int {
        val state = readState()
        return state.fileVersions[filename] ?: if (state.global >= 1) 1 else 0
    }

    /** Persiste la versión de un archivo específico. */
    suspend fun setFileVersion(filename: String, version: Int) {
        context.solvyxDataStore.edit { prefs ->
            val current = prefs[keyFileVersions]
                ?.let { runCatching { gson.fromJson<Map<String, Int>>(it, mapType) }.getOrNull() }
                ?.toMutableMap()
                ?: mutableMapOf()
            current[filename] = version
            prefs[keyFileVersions] = gson.toJson(current)
        }
    }

    /**
     * Versión global (legacy) del seed. Se mantiene por compatibilidad y
     * para la migración inicial. Las nuevas operaciones deberían usar
     * [getFileVersion] y [setFileVersion].
     */
    suspend fun getSeedVersion(): Int = readState().global

    /** Persiste la versión global (legacy). Usar [setFileVersion] en código nuevo. */
    suspend fun setSeedVersion(version: Int) {
        context.solvyxDataStore.edit { prefs ->
            prefs[keyGlobalVersion] = version.toString()
        }
    }

    /** Flujo reactivo del estado de versiones de seed (para observadores). */
    fun observeFileVersions(): Flow<Map<String, Int>> =
        context.solvyxDataStore.data.map { prefs ->
            prefs[keyFileVersions]
                ?.let { runCatching { gson.fromJson<Map<String, Int>>(it, mapType) }.getOrNull() }
                ?: emptyMap()
        }

    /**
     * Resetea todo el estado de versiones. Fuerza al seeder a recargar
     * todos los archivos en el próximo `ensureLoaded()`. Útil para tests
     * y para una futura opción "Restablecer contenido offline" en ajustes.
     */
    suspend fun resetAll() {
        context.solvyxDataStore.edit { prefs ->
            prefs.remove(keyGlobalVersion)
            prefs.remove(keyFileVersions)
        }
    }

    /** Estructura inmutable del estado de versiones persistido. */
    data class SeedVersionState(
        val global: Int,
        val fileVersions: Map<String, Int>
    )

    companion object {
        const val KEY_SEED_VERSION_GLOBAL = "_seed_version_global"
        const val KEY_FILE_VERSIONS = "_seed_file_versions"
        // Alias legacy, conservado para no romper consumidores existentes.
        const val KEY_SEED_VERSION = "_seed_version"
    }
}
