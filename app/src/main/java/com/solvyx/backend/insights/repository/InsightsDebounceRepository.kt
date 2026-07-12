package com.solvyx.backend.insights.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contrato del repositorio de debouncing. Se extrae como interfaz
 * para que los tests del motor puedan inyectar fakes sin depender
 * de la implementación con DataStore (que requiere Android).
 */
interface InsightsDebounceRepository {
    suspend fun getLastShownTimestamp(): Long
    suspend fun setLastShownTimestamp(timestamp: Long)
    fun observe(): Flow<Long>

    companion object {
        const val KEY_LAST_INSIGHT_TIMESTAMP = "last_insight_shown_timestamp"
        const val DEFAULT_DEBOUNCE_HOURS = 72L
        const val ACCEPT_MORE_DEBOUNCE_HOURS = 24L
    }
}

/**
 * Implementación con [DataStore] del repositorio de debouncing.
 *
 * **Regla de negocio**: máximo 1 insight automático cada
 * [InsightsDebounceRepository.DEFAULT_DEBOUNCE_HOURS]h. El usuario puede
 * reducir el debounce a [InsightsDebounceRepository.ACCEPT_MORE_DEBOUNCE_HOURS]h
 * activando "más insights" en Mi Perfil (esa preferencia vive en otro
 * DataStore y se pasa como flag a `InsightsEngine.evaluateNow`).
 *
 * **Persistencia**: timestamp epoch millis del último insight mostrado,
 * guardado en el DataStore de preferencias `solvyx_prefs`.
 *
 * **Privacy**: este repo no loguea timestamps exactos fuera del dispositivo.
 */
@Singleton
class InsightsDebounceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : InsightsDebounceRepository {
    private val key = longPreferencesKey(InsightsDebounceRepository.KEY_LAST_INSIGHT_TIMESTAMP)

    override suspend fun getLastShownTimestamp(): Long = dataStore.data.first()[key] ?: 0L

    override suspend fun setLastShownTimestamp(timestamp: Long) {
        dataStore.edit { it[key] = timestamp }
    }

    override fun observe(): Flow<Long> = dataStore.data.map { it[key] ?: 0L }
}