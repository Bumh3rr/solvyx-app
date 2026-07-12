package com.solvyx.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registro y configuración de los [android.app.NotificationChannel] que
 * Solvyx usa para comunicarse con el usuario en background.
 *
 * Canales:
 * - [CHANNEL_BITACORA]: invitación a registrar la jornada. Prioridad
 *   `DEFAULT` para que suene pero sin encabezar la lista de notificaciones.
 * - [CHANNEL_RUTINAS]: recordatorios de rutinas matutina/nocturna. Misma
 *   importancia que bitácora.
 * - [CHANNEL_INSIGHTS]: avisos del motor de insights (Berto notó algo).
 *   `DEFAULT` también; la severidad clínica del contenido la modula el
 *   copy (validado por `psicologo-solvyx`), no la importancia del canal.
 *
 * **Regla operativa**: `ensureChannels()` se llama una sola vez en
 * `SolvyxApp.onCreate()` (vía [com.solvyx.backend.scheduling.WorkScheduler]).
 * Es idempotente: si el canal ya existe, `createNotificationChannel`
 * no hace nada.
 *
 * **Android 8+ (API 26)**: los canales se crean SOLO si el SDK lo permite.
 * En APIs < 26, `NotificationCompat.Builder` ignora el channelId, así
 * que la app sigue funcionando.
 */
@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Crea los canales si no existen. Idempotente y barato.
     *
     * Llamar en `onCreate()` de la Application para garantizar que los
     * workers que se ejecuten en background tengan un canal válido donde
     * publicar.
     */
    fun ensureChannels() {
        // NotificationChannelCompat funciona en cualquier SDK; en < 26
        // no crea nada real pero tampoco rompe.
        val manager = NotificationManagerCompat.from(context)

        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_BITACORA, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(CHANNEL_BITACORA_DISPLAY)
                .setDescription("Invitación amable a registrar tu día.")
                .setShowBadge(true)
                .build()
        )

        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_RUTINAS, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(CHANNEL_RUTINAS_DISPLAY)
                .setDescription("Recordatorios amables de tus rutinas matutina y nocturna.")
                .setShowBadge(true)
                .build()
        )

        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_INSIGHTS, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(CHANNEL_INSIGHTS_DISPLAY)
                .setDescription("Cuando Berto nota algo relevante en tu proceso.")
                .setShowBadge(true)
                .build()
        )
    }

    /**
     * Nombre "humano" del canal bitácora. Lo usa `setName()` para que
     * aparezca en Ajustes > Apps > Solvyx > Notificaciones.
     */
    fun channelBitacoraDisplayName(): String = CHANNEL_BITACORA_DISPLAY

    /**
     * Helper para verificar si el sistema permite publicar (no es lo
     * mismo que tener un canal creado: el usuario puede haberlo silenciado
     * desde Ajustes). Lo usan los workers antes de postear.
     */
    fun canPost(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    companion object {
        // IDs de canal (estables; cambiarlo perdería la configuración del
        // usuario en Ajustes del sistema).
        const val CHANNEL_BITACORA = "solvyx_bitacora"
        const val CHANNEL_RUTINAS = "solvyx_rutinas"
        const val CHANNEL_INSIGHTS = "solvyx_insights"

        // Nombres visibles. Mantener concisos: aparecen en el centro de
        // notificaciones del sistema.
        private const val CHANNEL_BITACORA_DISPLAY = "Bitácora"
        private const val CHANNEL_RUTINAS_DISPLAY = "Rutinas"
        private const val CHANNEL_INSIGHTS_DISPLAY = "Insights"

        /**
         * Versión legacy para código que aún use `NotificationManager`
         * directo en lugar de `NotificationManagerCompat`. Hoy sin uso,
         * pero queda como constante de referencia.
         */
        @Suppress("unused")
        const val IMPORTANCE_DEFAULT_LEGACY: Int = NotificationManager.IMPORTANCE_DEFAULT
    }
}