---
description: Convenciones de WorkManager para Solvyx. PeriodicWorkRequest, OneTimeWorkRequest, CoroutineWorker, Hilt integration, chaining.
---

# Skill: WorkManager Android

Esta skill te entrega las convenciones para usar WorkManager en Solvyx. Aplícala al implementar tareas recurrentes (recordatorios, sincronizaciones, verificaciones) que deban sobrevivir al ciclo de vida de la app.

## Principios

1. **WorkManager para tareas que deben ejecutarse aunque la app esté cerrada.**
2. **PeriodicWorkRequest mínimo 15 minutos** (límite del sistema).
3. **Tareas idempotentes.** Pueden correr múltiples veces sin daño.
4. **Hilt integration** con `@HiltWorker` + `HiltWorkerFactory`.
5. **Configurar `HiltWorkerFactory` en Application** sino los Workers no se inyectan.
6. **Constraints por defecto:** batería no baja, sin requisito de red para tareas locales.

## Setup

### Gradle

```kotlin
dependencies {
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
}
```

### Application

```kotlin
@HiltAndroidApp
class SolvyxApp : Application(), Configuration.Provider {
    
    @Inject lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.WARN)
            .build()
}
```

### AndroidManifest

```xml
<!-- Desactivar el WorkManager default initializer -->
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    tools:node="remove" />

<!-- O alternativamente, dejar que Configuration.Provider funcione automáticamente -->
```

Con `Configuration.Provider` en Application, no es necesario remover el provider.

## Plantilla de Worker

```kotlin
@HiltWorker
class BitacoraReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bitacoraRepository: BitacoraRepository,
    private val notifier: Notifier,
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // 1. Verificar si hay permiso de notificaciones
        if (!notifier.canPost()) return Result.success()  // no fallar si no hay permiso
        
        // 2. Obtener última entrada
        val lastEntry = bitacoraRepository.getLastEntry() ?: return Result.success()
        
        // 3. Si han pasado más de 24h, notificar
        val horasDesde = TimeUnit.MILLISECONDS.toHours(now() - lastEntry.fecha)
        if (horasDesde < 24) return Result.success()
        
        // 4. Postear notificación
        notifier.post(
            title = "Solvyx",
            body = "Aquí sigo cuando quieras. ¿Cómo te fue hoy?"
        )
        
        return Result.success()
    }
    
    companion object {
        const val WORK_NAME = "bitacora_reminder"
    }
}
```

## Configuración de WorkRequest

### PeriodicWorkRequest

```kotlin
fun scheduleBitacoraReminder(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()
    
    val request = PeriodicWorkRequestBuilder<BitacoraReminderWorker>(
        repeatInterval = 24,
        repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 6,
        flexTimeIntervalUnit = TimeUnit.HOURS
    )
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        BitacoraReminderWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,  // o UPDATE si quieres reemplazar
        request
    )
}
```

**Parámetros de `flexTimeInterval`:** tiempo flexible dentro del período. Ej. cada 24h ± 6h. Útil para no saturar el sistema a la misma hora exacta.

### OneTimeWorkRequest

```kotlin
fun scheduleOneTimeCheck(context: Context) {
    val request = OneTimeWorkRequestBuilder<InsightsCheckWorker>()
        .setInitialDelay(1, TimeUnit.HOURS)
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueue(request)
}
```

## Tipos de Workers que Solvyx necesita

### Worker para Rutinas

```kotlin
@HiltWorker
class RutinaMatutinaWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userPrefs: UserPreferencesRepository,
    private val notifier: Notifier,
    private val rutinaRepo: RutinaRepository,
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        if (!notifier.canPost()) return Result.success()
        
        // 1. Verificar quiet hours
        if (userPrefs.isQuietHours()) return Result.success()
        
        // 2. Verificar que ya se hizo hoy
        if (rutinaRepo.yaCompletadaHoy("matutina")) return Result.success()
        
        // 3. Postear recordatorio
        notifier.post(
            title = "Buenos días",
            body = "Tu rutina de hoy está lista cuando quieras."
        )
        
        return Result.success()
    }
    
    companion object {
        const val WORK_NAME = "rutina_matutina"
    }
}
```

### Worker para verificar Insights

```kotlin
@HiltWorker
class InsightsCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bitacoraRepo: BitacoraRepository,
    private val insightsEngine: InsightsEngine,
    private val userPrefs: UserInsightsPreferencesRepository,
    private val notifier: Notifier,
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = runCatching {
        val entries = bitacoraRepo.getAllEntries()
        val prefs = userPrefs.getOnce()
        
        val insights = insightsEngine.evaluate(entries, prefs)
        
        insights.firstOrNull()?.let { insight ->
            notifier.post(
                title = "Berto",
                body = insight.copyToText()  // método del copy layer
            )
        }
        
        Result.success()
    }.getOrElse { e ->
        if (e is transientError) Result.retry() else Result.failure()
    }
}
```

## Encolar trabajo

### WorkManager wrapper

```kotlin
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationThrottler: NotificationThrottler
) {
    
    fun scheduleBitacoraReminder() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val request = PeriodicWorkRequestBuilder<BitacoraReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 6,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BitacoraReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
    
    fun scheduleRutinaMatutina(hora: Int) {
        val delay = computeDelayUntil(hora, 0)
        
        val request = PeriodicWorkRequestBuilder<RutinaMatutinaWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RutinaMatutinaWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
    
    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWork()
    }
    
    fun cancelWork(name: String) {
        WorkManager.getInstance(context).cancelUniqueWork(name)
    }
    
    private fun computeDelayUntil(hora: Int, minuto: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return calendar.timeInMillis - System.currentTimeMillis()
    }
}
```

## Chaining (encadenar Workers)

```kotlin
fun setupSeedPipeline() {
    val downloadRequest = OneTimeWorkRequestBuilder<DownloadSeedWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    
    val parseRequest = OneTimeWorkRequestBuilder<ParseSeedWorker>()
        .build()
    
    val applyRequest = OneTimeWorkRequestBuilder<ApplySeedWorker>()
        .build()
    
    WorkManager.getInstance(context)
        .beginUniqueWork("seed_pipeline", ExistingWorkPolicy.KEEP, downloadRequest)
        .then(parseRequest)
        .then(applyRequest)
        .enqueue()
}
```

## Observabilidad

### WorkInfo flow

```kotlin
fun observeBitacoraReminder(): Flow<WorkInfo> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(BitacoraReminderWorker.WORK_NAME)
        .map { it.firstOrNull() }
        .filterNotNull()
}
```

## Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class BitacoraReminderWorkerTest {
    
    @get:Rule
    val workManagerTestRule = WorkManagerTestInitHelper(
        context = ApplicationProvider.getApplicationContext(),
        config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
    )
    
    @Test
    fun worker_returns_success_when_no_entry() = runTest {
        val worker = TestListenableWorkerBuilder<BitacoraReminderWorker>(context)
            .build()
        
        val result = worker.startWork().get()
        
        assertEquals(ListenableWorker.Result.success(), result)
    }
}
```

## Observar y depurar

```bash
# Listar trabajos en cola
adb shell dumpsys jobscheduler | grep solvyx

# Forzar ejecución inmediata
adb shell cmd jobscheduler run -f com.solvyx <jobId>

# Ver trabajo específico de WorkManager
adb shell dumpsys jobscheduler | grep -A 20 solvyx

# Logs
adb logcat -s WM-WorkerWrapper:V
```

## Constraints disponibles

```kotlin
Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)  // CONNECTED, METERED, NOT_REQUIRED, UNMETERED
    .setRequiresBatteryNotLow(true)
    .setRequiresCharging(false)
    .setRequiresDeviceIdle(false)        // solo cuando el dispositivo está idle
    .setRequiresStorageNotLow(true)
    .setTriggerContentMaxDelay(0, TimeUnit.MILLISECONDS)  // para content URIs
    .setTriggerContentUpdateDelay(0, TimeUnit.MILLISECONDS)
    .build()
```

## Backoff y retry

```kotlin
val request = PeriodicWorkRequestBuilder<XWorker>(/* ... */)
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,  // o LINEAR
        backoffDelay = 30,
        timeUnit = TimeUnit.SECONDS
    )
    .build()
```

## Anti-patrones prohibidos

1. **PeriodicWorkRequest con intervalo <15 min.** No soportado por el sistema.
2. **Workers que no son idempotentes.** Pueden correr dos veces sin querer.
3. **Bloquear el hilo principal en `doWork()`.** Usar `CoroutineWorker` con `Dispatchers.IO`.
4. **Lanzar excepciones sin manejar.** `Result.retry()` o `Result.failure()`.
5. **Olvidar configurar `HiltWorkerFactory`** en Application.
6. **Hardcodear nombres de workers.** Usar `companion object`.
7. **`enqueueUniquePeriodicWork` sin `ExistingPeriodicWorkPolicy`.** Comportamiento indefinido.
8. **Tareas que requieren `setExact`** sin justificación de vida o muerte. Doze mode puede saltarlas.
9. **Olvidar `WorkerFactory`** para tests.