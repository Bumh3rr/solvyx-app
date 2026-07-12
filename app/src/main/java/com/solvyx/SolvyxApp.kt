package com.solvyx

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.solvyx.backend.assets.AssetsSeeder
import com.solvyx.backend.scheduling.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * DataStore global de Solvyx. Usado por [SeedPreferencesRepository],
 * [com.solvyx.backend.data.local.preferences.NotificationPreferencesRepository]
 * y el repositorio de debouncing de insights.
 */
val Context.solvyxDataStore by preferencesDataStore(name = "solvyx_prefs")

/**
 * Application de Solvyx.
 *
 * Hace tres cosas críticas para el scheduler:
 * 1. Habilita Hilt (`@HiltAndroidApp`).
 * 2. Provee una [Configuration] de WorkManager que usa
 *    [HiltWorkerFactory] para que los `@HiltWorker` puedan recibir
 *    sus dependencias inyectadas.
 * 3. Programa los workers recurrentes al arrancar la app.
 *
 * **Importante**: para que `Configuration.Provider` funcione, el
 * inicializador por defecto de `androidx.startup` para WorkManager
 * debe estar deshabilitado en el manifest (ver
 * `app/src/main/AndroidManifest.xml`). Si no, WorkManager se
 * inicializa con la config por defecto antes de que Hilt esté listo
 * y los `@HiltWorker` crashean al primer `doWork()`.
 */
@HiltAndroidApp
class SolvyxApp : Application(), Configuration.Provider {

    /**
     * Factory de workers que sabe construir instancias con dependencias
     * inyectadas vía `@AssistedInject`. Se inyecta después del
     * `super.onCreate()` (orden garantizado por Hilt).
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Scheduler único. Inyectado por Hilt; su `init {}` crea los canales
     * de notificación antes de que cualquier worker intente publicar.
     */
    @Inject
    lateinit var workScheduler: WorkScheduler

    /**
     * Seeder del contenido offline (guías, ejercicios, lecciones, etc.).
     * Se ejecuta al arrancar la app para que la primera vez que el usuario
     * abre cualquier pantalla de contenido (Guías, Ejercicios, etc.)
     * ya tenga datos que mostrar, en lugar del empty state con "Reintentar".
     */
    @Inject
    lateinit var assetsSeeder: AssetsSeeder

    /**
     * Scope para trabajos de inicialización en background. Vive lo que vive
     * la aplicación.
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Programamos los 4 workers recurrentes. Es idempotente: si ya
        // están programados (KEEP), no hace nada.
        workScheduler.scheduleAll()

        // Cargamos el seed de contenido offline (guías, ejercicios, lecciones,
        // rutinas, prompts de journaling). Es idempotente: si la versión ya
        // está al día, no hace nada. Si falla, se reintentará la próxima vez
        // que el usuario pulse "Reintentar" en un empty state.
        appScope.launch {
            assetsSeeder.ensureLoaded()
        }
    }

    /**
     * Configuración de WorkManager.
     *
     * **Por qué lazy**: `Configuration.Builder().build()` se invoca
     * cada vez que WorkManager consulta el provider. Lo hacemos lazy
     * vía `get()` para no construir un Configuration nuevo en cada
     * llamada si no es necesario (aunque en la práctica es barato).
     *
     * **Logging**: `setMinimumLoggingLevel` solo en debug. En release,
     * silenciamos para no contaminar logcat.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}