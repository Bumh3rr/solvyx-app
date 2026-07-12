package com.solvyx.backend.scheduling.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solvyx.BuildConfig
import com.solvyx.backend.data.local.preferences.NotificationPreferencesRepository
import com.solvyx.backend.insights.InsightsEngine
import com.solvyx.backend.scheduling.WorkScheduler
import com.solvyx.notifications.NotificationChannels
import com.solvyx.notifications.Notifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

/**
 * Worker que evalúa el motor de insights y notifica si hay uno nuevo.
 *
 * **Disparo**: cada 3 días (programado en [WorkScheduler]).
 *
 * **Por qué 3 días**: el [com.solvyx.backend.insights.repository.InsightsDebounceRepository]
 * ya impone un mínimo de 72h entre insights automáticos. Programar el
 * worker a 3 días alinea el costo de evaluación con la frecuencia de
 * emisión: nunca evaluamos más a menudo de lo que podemos publicar.
 *
 * **Comportamiento**:
 * 1. Respetar preferencia global de notificaciones.
 * 2. Respetar quiet hours.
 * 3. Evaluar el motor (`InsightsEngine.evaluateNow`).
 *    - Si devuelve lista vacía → no postea (motor ya hizo su debounce).
 * 4. Si hay insights, postea el de mayor severidad (el primero de la
 *    lista ordenada por peso desc).
 *
 * **Copy**: el texto del insight viene de `backend-content-curator`.
 * Aquí dejamos un placeholder hasta que se entregue la versión final.
 * El contrato es: el motor produce el `Insight` (id, tipo, datos); el
 * content-curator mapea esos datos a un texto validado por psicología.
 */
@HiltWorker
class InsightsCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val insightsEngine: InsightsEngine,
    private val notifPrefs: NotificationPreferencesRepository,
    private val notifier: Notifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "doWork() — iniciando evaluación de insights.")

        // 1. Preferencia global.
        if (!notifPrefs.areNotificationsEnabled()) return Result.success()

        // 2. Quiet hours.
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (notifPrefs.isInQuietHours(currentHour)) return Result.success()

        // 3. Evaluar motor.
        val insights = runCatching { insightsEngine.evaluateNow() }
            .onFailure { e ->
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Fallo al evaluar insights engine", e)
                }
            }
            .getOrDefault(emptyList())

        if (insights.isEmpty()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Motor no devolvió insights. Saliendo.")
            return Result.success()
        }

        // 4. Postear el más relevante.
        val top = insights.first()
        val copy = renderCopy(top.id, top.tipo.name)
        notifier.post(
            title = "Berto",
            body = copy,
            channelId = NotificationChannels.CHANNEL_INSIGHTS,
            deepLink = DEEP_LINK_INSIGHTS
        )

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Insight '${top.id}' notificado (severidad=${top.severidad.name}).")
        }
        return Result.success()
    }

    /**
     * Renderiza el copy del insight. Hoy es un placeholder genérico;
     * en una iteración posterior `backend-content-curator` proveerá un
     * mapeo `id → copy` validado por psicología, y este método leerá
     * de un `Map<String, String>` o de un recurso string.
     *
     * El copy se mantiene CONSTANTE mientras no se integre el catálogo
     * de contenido, para que las pruebas automatizadas no se rompan con
     * cambios cosméticos.
     */
    private fun renderCopy(insightId: String, tipo: String): String = COPY_GENERICO

    companion object {
        private const val TAG = "InsightsCheckWorker"
        private const val DEEP_LINK_INSIGHTS = "solvyx://insights"

        // Copy placeholder hasta que `backend-content-curator` entregue el
        // mapeo id → texto validado por `psicologo-solvyx`.
        private const val COPY_GENERICO = "Berto notó algo en tu proceso. Te lo cuento en la app."
    }
}