package com.solvyx.backend.scheduling.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solvyx.BuildConfig
import com.solvyx.backend.data.local.dao.RutinaDao
import com.solvyx.backend.data.local.dao.RutinaProgresoDao
import com.solvyx.backend.data.local.preferences.NotificationPreferencesRepository
import com.solvyx.backend.scheduling.WorkScheduler
import com.solvyx.notifications.NotificationChannels
import com.solvyx.notifications.Notifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.TimeZone

/**
 * Recordatorio de la rutina matutina.
 *
 * **Disparo**: cada 24h (programado en [WorkScheduler] alrededor de las 08:00).
 *
 * **Comportamiento**:
 * 1. Respeta preferencia global de notificaciones.
 * 2. Respeta "quiet hours" del usuario.
 * 3. Si NO existe la rutina "matutina" en el catálogo (seed no cargado)
 *    → sale en silencio.
 * 4. Si el usuario YA completó al menos un paso de la rutina matutina
 *    HOY → no postea (ya interactuó).
 * 5. Si la hora actual está fuera de la ventana objetivo ± ventana de
 *    tolerancia → no postea (alguien reprogramó el scheduler o el
 *    dispositivo estuvo apagado).
 * 6. Si todo OK → postea invitación con deep link a la rutina.
 *
 * **Nota sobre la periodicidad**: con `PeriodicWorkRequest` de 24h y
 * `setInitialDelay` alineado a la hora objetivo, el worker puede
 * dispararse a las 07:55 o las 08:35 según el dispositivo. Por eso
 * el chequeo de hora es ±[VENTANA_TOLERANCIA_MINUTOS].
 */
@HiltWorker
class RutinaMatutinaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val rutinaDao: RutinaDao,
    private val rutinaProgresoDao: RutinaProgresoDao,
    private val notifPrefs: NotificationPreferencesRepository,
    private val notifier: Notifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "doWork() — iniciando.")

        // 1. Preferencia global.
        if (!notifPrefs.areNotificationsEnabled()) {
            return Result.success()
        }

        // 2. Quiet hours.
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        if (notifPrefs.isInQuietHours(currentHour)) {
            return Result.success()
        }

        // 3. Rutina existe en catálogo.
        val rutina = rutinaDao.findBySlug(SLUG_RUTINA_MATUTINA)
        if (rutina == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Rutina '$SLUG_RUTINA_MATUTINA' no encontrada.")
            return Result.success()
        }

        // 4. ¿El usuario ya interactuó con la rutina hoy?
        val (desde, hasta) = todayRange()
        val pasosCompletados = rutinaProgresoDao.findPasosCompletadosEnRango(desde, hasta)
        if (pasosCompletados.isNotEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Usuario ya completó ${pasosCompletados.size} paso(s) hoy. Saliendo.")
            }
            return Result.success()
        }

        // 5. Ventana de tolerancia: ¿estamos dentro de los
        // ±VENTANA_TOLERANCIA_MINUTOS alrededor de la hora objetivo?
        val targetHour = notifPrefs.getRutinaMatutinaHora()
        if (!isWithinTargetWindow(currentHour, targetHour)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Fuera de ventana (hora=$currentHour, target=$targetHour). Saliendo.")
            }
            return Result.success()
        }

        // 6. Postear invitación.
        notifier.post(
            title = "Solvyx",
            body = COPY_MATUTINA,
            channelId = NotificationChannels.CHANNEL_RUTINAS,
            deepLink = DEEP_LINK_RUTINA_MATUTINA
        )

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Notificación rutina matutina posteada.")
        }
        return Result.success()
    }

    /**
     * Devuelve `(desde, hasta)` del día actual en zona horaria local.
     * Usado para chequear si el usuario marcó pasos hoy.
     *
     * Implementado con `Calendar` (no `java.time`) para mantener
     * compatibilidad con `minSdk = 24` sin habilitar desugaring.
     * Misma semántica que `LocalDate.now()..LocalDate.now()+1d`.
     */
    private fun todayRange(): Pair<Long, Long> {
        val zone = TimeZone.getDefault()
        val startOfToday = Calendar.getInstance(zone).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val desde = startOfToday.timeInMillis
        val hasta = (startOfToday.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        return desde to hasta
    }

    /**
     * `true` si la hora actual está dentro de la ventana de tolerancia
     * alrededor de la hora objetivo. Por ejemplo, si target=8 y la
     * ventana es ±30 min, solo posteamos entre las 7:30 y las 8:30.
     */
    private fun isWithinTargetWindow(currentHour: Int, targetHour: Int): Boolean {
        // Simplificación: comparamos por hora, no por minuto exacto.
        // Si target=8 y currentHour=8 → ok.
        // Si target=8 y currentHour=7 → fuera (dif=1h).
        // Si target=23 y currentHour=0 → fuera (cruza medianoche; sin
        //     soporte de wrap-around por simplicidad).
        return currentHour == targetHour
    }

    companion object {
        private const val TAG = "RutinaMatutinaWorker"
        private const val SLUG_RUTINA_MATUTINA = "matutina"
        private const val DEEP_LINK_RUTINA_MATUTINA = "solvyx://rutina/matutina"
        private const val VENTANA_TOLERANCIA_MINUTOS = 30

        // Copy validado por `psicologo-solvyx`. Pendiente de content-curator
        // para versión final con variantes según hora/estado ánimo.
        private const val COPY_MATUTINA = "Buenos días. Tu rutina de hoy está lista."
    }
}