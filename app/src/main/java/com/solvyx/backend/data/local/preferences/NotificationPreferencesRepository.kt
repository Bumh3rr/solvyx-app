package com.solvyx.backend.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de preferencias relacionadas con notificaciones y horarios.
 *
 * Almacena en el `DataStore<Preferences>` global de Solvyx (el mismo
 * `solvyx_prefs` declarado en [com.solvyx.SolvyxApp.solvyxDataStore]).
 *
 * **Por qué un repositorio dedicado y no "mezclar" con
 * [SeedPreferencesRepository]**:
 * - Separación de responsabilidades: uno es seed, otro es UX/notif.
 * - Permite que la UI de Mi Perfil inyecte SOLO este repositorio (no el
 *   de seed) sin acoplar conceptos.
 *
 * **Defaults sensatos** (alineados con `proyecto.md`):
 * - Notificaciones activadas por defecto.
 * - Bitácora reminder a las 21:00 (el típico "antes de dormir, ¿cómo te fue?").
 * - Rutina matutina a las 08:00.
 * - Rutina nocturna a las 22:00.
 * - Quiet hours desactivado (`null`, `null`). Si el usuario lo activa,
 *   valores típicos: 23 → 7.
 */
@Singleton
class NotificationPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ---------------------------------------------------------------
    // Keys
    // ---------------------------------------------------------------

    private val keyNotificationsEnabled = booleanPreferencesKey(KEY_NOTIFICATIONS_ENABLED)
    private val keyBitacoraReminderHour = intPreferencesKey(KEY_BITACORA_REMINDER_HOUR)
    private val keyRutinaMatutinaHora = intPreferencesKey(KEY_RUTINA_MATUTINA_HORA)
    private val keyRutinaNocturnaHora = intPreferencesKey(KEY_RUTINA_NOCTURNA_HORA)
    private val keyQuietStart = intPreferencesKey(KEY_QUIET_START)
    private val keyQuietEnd = intPreferencesKey(KEY_QUIET_END)

    // ---------------------------------------------------------------
    // Notificaciones: enabled global
    // ---------------------------------------------------------------

    /**
     * `true` si el usuario quiere recibir notificaciones de Solvyx.
     * `false` apaga TODOS los workers de notificación (los workers
     * chequean este flag al inicio de `doWork()`).
     */
    suspend fun areNotificationsEnabled(): Boolean =
        dataStore.data.map { it[keyNotificationsEnabled] ?: DEFAULT_NOTIFICATIONS_ENABLED }
            .first()

    fun observeNotificationsEnabled(): Flow<Boolean> =
        dataStore.data.map { it[keyNotificationsEnabled] ?: DEFAULT_NOTIFICATIONS_ENABLED }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[keyNotificationsEnabled] = enabled }
    }

    // ---------------------------------------------------------------
    // Bitácora reminder: hora del día (0-23) en zona horaria del user
    // ---------------------------------------------------------------

    /**
     * Hora sugerida (0-23) a la que Solvyx recuerda registrar el día.
     * El Worker programa el recordatorio con periodicidad diaria y
     * dispara dentro de la ventana de ±1h alrededor de esta hora.
     */
    suspend fun getBitacoraReminderHour(): Int =
        dataStore.data.map { it[keyBitacoraReminderHour] ?: DEFAULT_BITACORA_HOUR }.first()

    suspend fun setBitacoraReminderHour(hour: Int) {
        val safe = hour.coerceIn(0, 23)
        dataStore.edit { it[keyBitacoraReminderHour] = safe }
    }

    // ---------------------------------------------------------------
    // Rutinas
    // ---------------------------------------------------------------

    suspend fun getRutinaMatutinaHora(): Int =
        dataStore.data.map { it[keyRutinaMatutinaHora] ?: DEFAULT_RUTINA_MATUTINA_HORA }.first()

    suspend fun getRutinaNocturnaHora(): Int =
        dataStore.data.map { it[keyRutinaNocturnaHora] ?: DEFAULT_RUTINA_NOCTURNA_HORA }.first()

    suspend fun setRutinaMatutinaHora(hour: Int) {
        dataStore.edit { it[keyRutinaMatutinaHora] = hour.coerceIn(0, 23) }
    }

    suspend fun setRutinaNocturnaHora(hour: Int) {
        dataStore.edit { it[keyRutinaNocturnaHora] = hour.coerceIn(0, 23) }
    }

    // ---------------------------------------------------------------
    // Quiet hours: rango horario en el que NO se postean notificaciones
    // ---------------------------------------------------------------

    /**
     * Hora de inicio del "no molestar" (0-23), o `null` si está
     * desactivado.
     *
     * Si `start <= end`, la ventana es `[start, end)`.
     * Si `start > end`, la ventana cruza medianoche (ej: 23 → 7).
     */
    suspend fun getQuietHoursStart(): Int? =
        dataStore.data.map { it[keyQuietStart] }.first()

    suspend fun getQuietHoursEnd(): Int? =
        dataStore.data.map { it[keyQuietEnd] }.first()

    /**
     * Activa/desactiva "no molestar".
     *
     * Pasar `null` en ambos parámetros desactiva el modo.
     * Pasar horas válidas (0-23) lo activa.
     */
    suspend fun setQuietHours(start: Int?, end: Int?) {
        dataStore.edit { prefs ->
            if (start == null || end == null) {
                prefs.remove(keyQuietStart)
                prefs.remove(keyQuietEnd)
            } else {
                prefs[keyQuietStart] = start.coerceIn(0, 23)
                prefs[keyQuietEnd] = end.coerceIn(0, 23)
            }
        }
    }

    // ---------------------------------------------------------------
    // Lógica de quiet hours (la invocan los Workers, no la UI)
    // ---------------------------------------------------------------

    /**
     * Determina si la hora actual (0-23 local) cae dentro del rango
     * "no molestar" configurado por el usuario.
     *
     * **Casos**:
     * - Quiet hours desactivado (start o end == null) → `false`
     *   (siempre se puede postear).
     * - `start <= end` (mismo día, ej: 14-16 siesta) → ventana
     *   `[start, end)`.
     * - `start > end` (cruza medianoche, ej: 23-07) → ventana
     *   `[start, 24) ∪ [0, end)`.
     *
     * Se declara `suspend` para leer DataStore sin `runBlocking` (que
     * bloquearía el hilo del Worker).
     *
     * @param currentHour hora actual 0-23 en zona local del usuario.
     */
    suspend fun isInQuietHours(currentHour: Int): Boolean {
        val start = getQuietHoursStart() ?: return false
        val end = getQuietHoursEnd() ?: return false
        return when {
            start == end -> false // rango vacío → no silenciamos
            start < end -> currentHour in start until end
            else -> currentHour >= start || currentHour < end
        }
    }

    companion object {
        // Keys en DataStore. Prefijo `notif_` para distinguir de otras prefs.
        const val KEY_NOTIFICATIONS_ENABLED = "notif_enabled"
        const val KEY_BITACORA_REMINDER_HOUR = "notif_bitacora_hour"
        const val KEY_RUTINA_MATUTINA_HORA = "notif_rutina_matutina_hour"
        const val KEY_RUTINA_NOCTURNA_HORA = "notif_rutina_nocturna_hour"
        const val KEY_QUIET_START = "notif_quiet_start"
        const val KEY_QUIET_END = "notif_quiet_end"

        // Defaults sensatos (alineados con proyecto.md y opinión Solvyx).
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
        const val DEFAULT_BITACORA_HOUR = 21 // 9pm
        const val DEFAULT_RUTINA_MATUTINA_HORA = 8 // 8am
        const val DEFAULT_RUTINA_NOCTURNA_HORA = 22 // 10pm
    }
}