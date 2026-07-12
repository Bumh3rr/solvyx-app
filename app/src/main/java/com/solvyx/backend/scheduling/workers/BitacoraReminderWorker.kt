package com.solvyx.backend.scheduling.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solvyx.BuildConfig
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.preferences.NotificationPreferencesRepository
import com.solvyx.backend.scheduling.WorkScheduler
import com.solvyx.notifications.NotificationChannels
import com.solvyx.notifications.Notifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

/**
 * Recordatorio de bitácora.
 *
 * **Disparo**: cada 24h (programado en [WorkScheduler]).
 *
 * **Comportamiento**:
 * 1. Si el usuario desactivó notificaciones globales → sale en silencio
 *    (`Result.success()`; no es un error).
 * 2. Si la hora actual cae dentro de "quiet hours" → no postea.
 * 3. Si NO existe ninguna entrada de bitácora (usuario nuevo) → no
 *    postea (lo abrumaría en su primer día).
 * 4. Si la última entrada tiene < 24h → no postea (ya registró hoy).
 * 5. Si han pasado > 24h → postea invitación con deep link a bitácora.
 *
 * **Idempotencia**: este worker es seguro de correr dos veces. El
 * `Notifier` genera ids aleatorios, así que dos invocaciones crean
 * dos notificaciones distintas (no deseable en producción, pero
 * WorkManager NO debería invocarlo dos veces salvo bug).
 */
@HiltWorker
class BitacoraReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val bitacoraDao: BitacoraDao,
    private val notifPrefs: NotificationPreferencesRepository,
    private val notifier: Notifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "doWork() — iniciando.")

        // Regla 1: respetar la preferencia global.
        if (!notifPrefs.areNotificationsEnabled()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Notificaciones desactivadas. Saliendo.")
            return Result.success()
        }

        // Regla 2: no molestar.
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (notifPrefs.isInQuietHours(currentHour)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Quiet hours activos (hora=$currentHour). Saliendo.")
            return Result.success()
        }

        // Regla 3: usuario sin entradas previas.
        val ultima = bitacoraDao.ultima()
        if (ultima == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Sin entradas previas. Saliendo sin notificar.")
            return Result.success()
        }

        // Regla 4: si registró hoy, no insistimos.
        val horasDesde = (System.currentTimeMillis() - ultima.fecha) / MILLIS_PER_HOUR
        if (horasDesde < HORAS_UMBRAL) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Última entrada hace $horasDesde h (<$HORAS_UMBRAL h). Saliendo.")
            }
            return Result.success()
        }

        // Regla 5: invitar amablemente.
        notifier.post(
            title = "Solvyx",
            body = COPY_INVITACION,
            channelId = NotificationChannels.CHANNEL_BITACORA,
            deepLink = DEEP_LINK_BITACORA
        )

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Notificación posteada (última entrada hace $horasDesde h).")
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "BitacoraReminderWorker"
        private const val HORAS_UMBRAL = 24L
        private const val MILLIS_PER_HOUR = 60L * 60L * 1000L

        // Copy validado por `psicologo-solvyx` (placeholder hasta que el
        // content-curator entregue la versión final). Es corto, sin culpa,
        // en segunda persona.
        private const val COPY_INVITACION = "Aquí sigo cuando quieras. ¿Cómo te fue hoy?"
        private const val DEEP_LINK_BITACORA = "solvyx://bitacora"
    }
}