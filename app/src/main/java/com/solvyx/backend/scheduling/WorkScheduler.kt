package com.solvyx.backend.scheduling

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.solvyx.BuildConfig
import com.solvyx.backend.scheduling.workers.BitacoraReminderWorker
import com.solvyx.backend.scheduling.workers.InsightsCheckWorker
import com.solvyx.backend.scheduling.workers.RutinaMatutinaWorker
import com.solvyx.backend.scheduling.workers.RutinaNocturnaWorker
import com.solvyx.notifications.NotificationChannels
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Punto único de programación de todos los `Worker`s recurrentes de
 * Solvyx.
 *
 * **Cuándo se invoca**: una vez en `SolvyxApp.onCreate()`.
 *
 * **Política de conflicto**: [ExistingPeriodicWorkPolicy.KEEP] para
 * todos. Esto significa que si la app se reinstala o se re-lanza, NO
 * reprogramamos: si ya existe un `WorkRequest` con ese `uniqueWorkName`,
 * lo dejamos como está. Solo cambiamos a `UPDATE` cuando la UI de Mi
 * Perfil modifique horarios (esa parte la hace la UI en una iteración
 * posterior; aquí preparamos el terreno exponiendo [rescheduleAll]).
 *
 * **Constraints globales**: `setRequiresBatteryNotLow(true)` por defecto.
 * No exigimos red ni carga: las tareas son 100% locales (bitácora,
 * rutinas, insights). Si la batería está baja, los workers simplemente
 * se difieren; WorkManager los ejecutará cuando se recupere.
 *
 * **Backoff exponencial**: 30s inicial, multiplica x2 en cada reintento.
 * WorkManager tiene un tope de ~5h, suficiente para reintentos suaves.
 *
 * **Periodicidad mínima**: 15 minutos (límite de WorkManager).
 * - Bitácora reminder: 1 día.
 * - Rutinas: 1 día (con `flexInterval` de 30 min para alinear con la
 *   hora objetivo).
 * - Insights: 3 días (porque el debouncing del motor ya está en 72h).
 */
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channels: NotificationChannels
) {

    init {
        // Garantiza que los canales existan ANTES de que cualquier worker
        // intente publicar. Es idempotente: si ya están creados, no hace nada.
        channels.ensureChannels()
    }

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    /**
     * Programa los 4 workers recurrentes. Llamar en `Application.onCreate()`.
     *
     * Si ya existen (`enqueueUniquePeriodicWork` con `KEEP`), no hace
     * nada. Esto es seguro de llamar múltiples veces.
     */
    fun scheduleAll() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "scheduleAll() — programando los 4 workers recurrentes.")
        }
        scheduleBitacoraReminder()
        scheduleRutinaMatutina()
        scheduleRutinaNocturna()
        scheduleInsightsCheck()
    }

    /**
     * Cancela los 4 workers. Usado por tests y por una futura opción
     * "Desactivar todo" en Mi Perfil.
     */
    fun cancelAll() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "cancelAll() — cancelando los 4 workers recurrentes.")
        }
        workManager.cancelUniqueWork(WORK_BITACORA_REMINDER)
        workManager.cancelUniqueWork(WORK_RUTINA_MATUTINA)
        workManager.cancelUniqueWork(WORK_RUTINA_NOCTURNA)
        workManager.cancelUniqueWork(WORK_INSIGHTS_CHECK)
    }

    // ---------------------------------------------------------------
    // Programación individual
    // ---------------------------------------------------------------

    /**
     * Worker de bitácora: corre cada 24h. Dentro del `doWork()` verifica
     * si han pasado >24h desde el último registro y postea invitación.
     */
    fun scheduleBitacoraReminder() {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<BitacoraReminderWorker>(
            REPEAT_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            // Flex interval: 1h de holgura para que Android no despierte
            // al dispositivo con precisión quirúrgica. Cuesta batería.
            .setInitialDelay(computeInitialDelayToNextHour(hour = 21), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_BITACORA_REMINDER,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Worker de rutina matutina: corre cada 24h, intentando aterrizar
     * alrededor de las 08:00 (default). Flex de 30 min para no ser
     * agresivo con el reloj del sistema.
     *
     * El `doWork()` chequea internamente si la hora actual está dentro
     * de la ventana objetivo ±30 min, y si el usuario ya completó la
     * rutina hoy (no postea dos veces).
     */
    fun scheduleRutinaMatutina() {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<RutinaMatutinaWorker>(
            REPEAT_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            .setInitialDelay(
                computeInitialDelayToNextHour(hour = DEFAULT_RUTINA_MATUTINA_HOUR),
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_RUTINA_MATUTINA,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Worker de rutina nocturna: cada 24h, intentando aterrizar alrededor
     * de las 22:00 (default). Misma lógica que matutina.
     */
    fun scheduleRutinaNocturna() {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<RutinaNocturnaWorker>(
            REPEAT_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            .setInitialDelay(
                computeInitialDelayToNextHour(hour = DEFAULT_RUTINA_NOCTURNA_HOUR),
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_RUTINA_NOCTURNA,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Worker de insights: cada 3 días (72h). El motor ya tiene su propio
     * debouncing interno ([com.solvyx.backend.insights.repository.InsightsDebounceRepository.DEFAULT_DEBOUNCE_HOURS]),
     * así que este intervalo es solo "frecuencia máxima".
     */
    fun scheduleInsightsCheck() {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<InsightsCheckWorker>(
            REPEAT_INSIGHTS_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            // Alineamos con la hora del recordatorio de bitácora: si Berto
            // tiene algo que decir, lo dice a la hora de cierre del día.
            .setInitialDelay(
                computeInitialDelayToNextHour(hour = DEFAULT_INSIGHTS_HOUR),
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_INSIGHTS_CHECK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // ---------------------------------------------------------------
    // Utilidades para la UI (Mi Perfil): reprogramar con nuevos horarios
    // ---------------------------------------------------------------

    /**
     * Reprograma los workers de rutinas con una hora objetivo distinta.
     *
     * Usamos [ExistingPeriodicWorkPolicy.UPDATE] para refrescar el
     * `setInitialDelay`. Las próximas ejecuciones se realinearán al
     * nuevo target hour.
     *
     * Lo invocará la pantalla de Mi Perfil cuando el usuario cambie
     * "Hora rutina matutina" o "Hora rutina nocturna".
     */
    fun rescheduleRutinas(matutinaHour: Int, nocturnaHour: Int) {
        rescheduleRutinaMatutina(matutinaHour)
        rescheduleRutinaNocturna(nocturnaHour)
    }

    fun rescheduleRutinaMatutina(hour: Int) {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<RutinaMatutinaWorker>(
            REPEAT_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            .setInitialDelay(computeInitialDelayToNextHour(hour.coerceIn(0, 23)), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_RUTINA_MATUTINA,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun rescheduleRutinaNocturna(hour: Int) {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<RutinaNocturnaWorker>(
            REPEAT_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            .setInitialDelay(computeInitialDelayToNextHour(hour.coerceIn(0, 23)), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_RUTINA_NOCTURNA,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun rescheduleBitacoraReminder(hour: Int) {
        val constraints = defaultConstraints()
        val request = PeriodicWorkRequestBuilder<BitacoraReminderWorker>(
            REPEAT_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_INITIAL_SECONDS, TimeUnit.SECONDS)
            .setInitialDelay(computeInitialDelayToNextHour(hour.coerceIn(0, 23)), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_BITACORA_REMINDER,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Reposiciona TODOS los workers. Útil si el usuario cambia la zona
     * horaria del dispositivo y queremos realinear.
     */
    fun rescheduleAll() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "rescheduleAll() — reemplazando los 4 workers con UPDATE.")
        }
        workManager.cancelUniqueWork(WORK_BITACORA_REMINDER)
        workManager.cancelUniqueWork(WORK_RUTINA_MATUTINA)
        workManager.cancelUniqueWork(WORK_RUTINA_NOCTURNA)
        workManager.cancelUniqueWork(WORK_INSIGHTS_CHECK)
        scheduleAll()
    }

    /**
     * Inspecciona el estado de un worker (útil para debugging desde
     * una pantalla "Estado del scheduler" o desde tests).
     */
    fun getWorkInfo(uniqueName: String) = workManager.getWorkInfosForUniqueWork(uniqueName)

    /**
     * Lista los estados de los 4 workers. Útil para una futura pantalla
     * de diagnóstico.
     */
    fun getAllWorkInfos() = listOf(
        WORK_BITACORA_REMINDER,
        WORK_RUTINA_MATUTINA,
        WORK_RUTINA_NOCTURNA,
        WORK_INSIGHTS_CHECK
    )

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Constraints por defecto: batería no baja. Sin red (todo local) y
     * sin carga obligatoria.
     */
    private fun defaultConstraints(): Constraints =
        Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            // Red y carga NO requeridas: tareas 100% locales (DAO + DataStore).
            .setRequiresCharging(false)
            .setRequiresStorageNotLow(true) // evita fallos si el dispositivo está casi sin espacio
            .build()

    /**
     * Calcula el delay en ms hasta la próxima ocurrencia de la hora dada.
     *
     * Si ahora son las 14:00 y `hour=8`, devuelve ms hasta mañana 08:00.
     * Si ahora son las 07:30 y `hour=8`, devuelve 30 minutos.
     *
     * Se usa para alinear el `setInitialDelay` del primer ciclo del worker;
     * los ciclos siguientes los calcula Android cada 24h (puede haber drift
     * de algunos minutos, lo compensamos en el `doWork()` con ventana de
     * tolerancia).
     */
    private fun computeInitialDelayToNextHour(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Si la hora objetivo ya pasó hoy, apuntamos a mañana.
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    companion object {
        private const val TAG = "WorkScheduler"

        // Unique work names (visibles en `adb shell dumpsys jobscheduler | grep solvyx`).
        const val WORK_BITACORA_REMINDER = "solvyx_bitacora_reminder"
        const val WORK_RUTINA_MATUTINA = "solvyx_rutina_matutina"
        const val WORK_RUTINA_NOCTURNA = "solvyx_rutina_nocturna"
        const val WORK_INSIGHTS_CHECK = "solvyx_insights_check"

        // Periodicidades. Mínimo de WorkManager: 15 min.
        private const val REPEAT_DAYS = 1L
        private const val REPEAT_INSIGHTS_DAYS = 3L // 72h = ventana de debounce

        // Backoff exponencial. 30s inicial.
        private const val BACKOFF_INITIAL_SECONDS = 30L

        // Defaults alineados con NotificationPreferencesRepository.
        private const val DEFAULT_RUTINA_MATUTINA_HOUR = 8
        private const val DEFAULT_RUTINA_NOCTURNA_HOUR = 22
        private const val DEFAULT_INSIGHTS_HOUR = 21 // mismo que recordatorio bitácora
    }
}

/**
 * Extensión interna para que la UI pueda verificar de un vistazo si un
 * worker está "ENQUEUED" (programado) vs "RUNNING" (ejecutándose) vs
 * "FAILED" (no se pudo ejecutar tras agotar reintentos).
 */
fun WorkInfo.State.isHealthy(): Boolean =
    this == WorkInfo.State.ENQUEUED ||
        this == WorkInfo.State.RUNNING ||
        this == WorkInfo.State.SUCCEEDED