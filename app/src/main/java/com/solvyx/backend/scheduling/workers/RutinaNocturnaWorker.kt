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
 * Recordatorio de la rutina nocturna.
 *
 * Espejo de [RutinaMatutinaWorker] pero con la slug "nocturna" y un
 * default de hora 22:00. Ver documentación detallada en el worker
 * matutino: la lógica de quiet hours, ventana de tolerancia y
 * dedupe por progreso son idénticas.
 */
@HiltWorker
class RutinaNocturnaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val rutinaDao: RutinaDao,
    private val rutinaProgresoDao: RutinaProgresoDao,
    private val notifPrefs: NotificationPreferencesRepository,
    private val notifier: Notifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "doWork() — iniciando.")

        if (!notifPrefs.areNotificationsEnabled()) return Result.success()

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        if (notifPrefs.isInQuietHours(currentHour)) return Result.success()

        val rutina = rutinaDao.findBySlug(SLUG_RUTINA_NOCTURNA)
        if (rutina == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Rutina '$SLUG_RUTINA_NOCTURNA' no encontrada.")
            return Result.success()
        }

        val (desde, hasta) = todayRange()
        val pasosCompletados = rutinaProgresoDao.findPasosCompletadosEnRango(desde, hasta)
        if (pasosCompletados.isNotEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Usuario ya completó ${pasosCompletados.size} paso(s) hoy. Saliendo.")
            }
            return Result.success()
        }

        val targetHour = notifPrefs.getRutinaNocturnaHora()
        if (currentHour != targetHour) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Fuera de ventana (hora=$currentHour, target=$targetHour). Saliendo.")
            }
            return Result.success()
        }

        notifier.post(
            title = "Solvyx",
            body = COPY_NOCTURNA,
            channelId = NotificationChannels.CHANNEL_RUTINAS,
            deepLink = DEEP_LINK_RUTINA_NOCTURNA
        )

        if (BuildConfig.DEBUG) Log.d(TAG, "Notificación rutina nocturna posteada.")
        return Result.success()
    }

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

    companion object {
        private const val TAG = "RutinaNocturnaWorker"
        private const val SLUG_RUTINA_NOCTURNA = "nocturna"
        private const val DEEP_LINK_RUTINA_NOCTURNA = "solvyx://rutina/nocturna"

        // Copy validado por `psicologo-solvyx`. Pendiente content-curator.
        private const val COPY_NOCTURNA = "Es momento de cerrar el día. ¿Listx?"
    }
}