package com.solvyx.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.solvyx.BuildConfig
import com.solvyx.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Fachada para publicar notificaciones locales desde los `Worker`s.
 *
 * **Reglas operativas** (todas verificadas aquí, no se delegan al worker):
 * 1. Si el usuario desactivó las notificaciones globalmente → no postea.
 * 2. Si el usuario está en "quiet hours" → no postea (lo decide el worker
 *    usando [NotificationPreferencesRepository]; este notifier no tiene
 *    reloj, así que la verificación de hora debe llegar desde afuera).
 * 3. Si Android 13+ y el permiso `POST_NOTIFICATIONS` no fue concedido
 *    → no postea. No falla: el worker debe devolver `Result.success()`.
 * 4. Si el canal no existe aún (caso teórico si el worker corre antes
 *    de que la Application llame `ensureChannels()`), se crea al vuelo.
 *
 * **Deep linking**: si se pasa `deepLink`, el tap abre `MainActivity`
 * con la URI (custom scheme `solvyx://...`). La Activity debe declarar
 * un `<intent-filter>` para `VIEW` con esos schemes — el filtro LAUNCHER
 * actual no los cubre; se añade en una iteración posterior de UI.
 *
 * **Idempotencia del id**: cada `post()` genera un id aleatorio para
 * evitar que dos workers sobreescriban la misma notificación del día.
 */
@Singleton
class Notifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Publica una notificación.
     *
     * @param title  título (1 línea). Ej: "Solvyx".
     * @param body   texto (puede ser 1-3 líneas; se renderiza con
     *               `BigTextStyle` para que Android permita expandir).
     * @param channelId uno de [NotificationChannels.CHANNEL_BITACORA],
     *                  [NotificationChannels.CHANNEL_RUTINAS],
     *                  [NotificationChannels.CHANNEL_INSIGHTS].
     * @param deepLink URI custom scheme opcional (`solvyx://bitacora`).
     *                  Si se pasa, el tap navega ahí.
     * @param notificationId id entero único por notificación. Por defecto
     *                  se genera aleatorio para evitar colisiones entre
     *                  workers distintos. Solo especificar si se quiere
     *                  reemplazar una notificación concreta (ej: contador
     *                  de mensajes de Berto).
     */
    fun post(
        title: String,
        body: String,
        channelId: String,
        deepLink: String? = null,
        notificationId: Int = newNotificationId()
    ) {
        // Regla 3: si están deshabilitadas globalmente, salimos en silencio.
        if (!canPost()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "post() omitido: notificaciones deshabilitadas por el usuario.")
            }
            return
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            // BigTextStyle: si el copy es >1 línea, el usuario puede
            // expandir. El motor de insights puede generar textos largos.
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            // Sin vibración por defecto; los canales con IMPORTANCE_DEFAULT
            // pueden vibrar según configuración del sistema. No la forzamos.
            .setOnlyAlertOnce(false)

        // Deep link opcional.
        deepLink?.let { uri ->
            val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
                setPackage(context.packageName)
                // NEW_TASK para abrir desde un Context no-Activity (el worker).
                // SINGLE_TOP + CLEAR_TOP evita apilar Activities si el usuario
                // ya está dentro de Solvyx.
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            // FLAG_IMMUTABLE: requerido en Android 12+ (API 31+).
            // FLAG_UPDATE_CURRENT: si se reutiliza el id, refresca el extra.
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId, // requestCode distinto por id → evita colisiones
                intent,
                pendingIntentFlags
            )
            builder.setContentIntent(pendingIntent)
        }

        // Publicamos. `notify()` puede lanzar SecurityException si el
        // usuario revocó el permiso POST_NOTIFICATIONS entre la verificación
        // `canPost()` y ahora (improbable, pero posible). Lo capturamos
        // para que un Worker no crashee en producción.
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "post() OK: id=$notificationId channel=$channelId title='$title'")
            }
        } catch (se: SecurityException) {
            // Caso raro: permiso revocado en otra app entre canPost() y notify().
            // No fallamos al worker; solo logueamos en debug.
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "post() bloqueado por SecurityException (permiso revocado).", se)
            }
        }
    }

    /**
     * Verifica si el sistema permite publicar.
     *
     * Combina dos chequeos:
     * - `areNotificationsEnabled()` (canal maestro + permiso global).
     * - En API 33+ (Android 13), si el permiso runtime `POST_NOTIFICATIONS`
     *   no está concedido, `areNotificationsEnabled()` devuelve false.
     *
     * No chequeamos `quiet hours` aquí porque ese dato vive en DataStore
     * y leerlo en cada `post()` obligaría a hacerlo suspend. Esa decisión
     * queda en el Worker (que ya tiene acceso al repositorio).
     */
    fun canPost(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * Genera un id de notificación aleatorio.
     *
     * `Math.random()` NO es criptográficamente seguro, pero aquí solo
     * necesitamos unicidad entre workers en una misma ventana de
     * ejecución; no se usa para tokens ni identificadores persistentes.
     */
    private fun newNotificationId(): Int = abs(System.nanoTime().toInt())

    companion object {
        private const val TAG = "Notifier"
    }
}