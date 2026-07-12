---
description: Convenciones para construir notificaciones locales en Solvyx. NotificationChannel, permisos Android 13+, pending intents, copy validado.
---

# Skill: Notification Builder

Esta skill te entrega las convenciones para construir notificaciones locales en Solvyx. Aplícala cada vez que un Worker o pantalla necesite mostrar una notificación al usuario.

## Principios

1. **NotificationChannel por categoría.** No todas las notificaciones en el mismo canal.
2. **Importancia por contexto.** Rutinas e insights son `IMPORTANCE_DEFAULT`; crisis podría ser `IMPORTANCE_HIGH` (pero no `MAX`).
3. **Permiso Android 13+ explícito.** Pedir `POST_NOTIFICATIONS` con justificación.
4. **Copy validado por `psicologo-solvyx`.** El texto de notificaciones clínicas pasa por validación.
5. **Tap profundo a la pantalla correcta.** No abrir la app genérica.
6. **Acciones inline cuando aporten** (ej. "Marcar como hecho" en recordatorio de bitácora).
7. **Respeto a quiet hours y al estado de la app** (foreground vs background).

## NotificationChannel

### Creación

```kotlin
@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(
                id = CHANNEL_RUTINAS,
                name = "Rutinas y recordatorios",
                description = "Recordatorios amables de tus rutinas y bitácora.",
                importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
            createChannel(
                id = CHANNEL_INSIGHTS,
                name = "Insights de Berto",
                description = "Cuando Berto nota algo en tu proceso.",
                importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
            createChannel(
                id = CHANNEL_BITACORA,
                name = "Bitácora",
                description = "Invitación a registrar tu día.",
                importance = NotificationManagerCompat.IMPORTANCE_LOW
            )
        }
    }
    
    private fun createChannel(id: String, name: String, description: String, importance: Int) {
        val channel = NotificationChannelCompat.Builder(id, importance)
            .setName(name)
            .setDescription(description)
            .setShowBadge(true)
            .build()
        
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
    
    companion object {
        const val CHANNEL_RUTINAS = "solvyx_rutinas"
        const val CHANNEL_INSIGHTS = "solvyx_insights"
        const val CHANNEL_BITACORA = "solvyx_bitacora"
    }
}
```

### Cuándo crear canales

En `Application.onCreate()` o en la primera ejecución. Solo se crean una vez; el sistema los persiste.

```kotlin
@HiltAndroidApp
class SolvyxApp : Application(), Configuration.Provider {
    
    @Inject lateinit var channels: NotificationChannels
    
    override fun onCreate() {
        super.onCreate()
        channels.ensureChannels()
    }
}
```

## Notifier helper

```kotlin
@Singleton
class Notifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val throttler: NotificationThrottler
) {
    
    suspend fun post(
        title: String,
        body: String,
        channelId: String = NotificationChannels.CHANNEL_RUTINAS,
        deepLink: String? = null,
        actions: List<NotificationAction> = emptyList()
    ) {
        // 1. Verificar permiso
        if (!canPost()) return
        
        // 2. Verificar throttle
        if (!throttler.canNotify()) return
        
        // 3. Construir
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)  // ícono monocromo
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.teal_primary))
        
        // 4. Tap intent (deep link)
        deepLink?.let { link ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                setPackage(context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        // 5. Acciones
        actions.forEach { action ->
            builder.addAction(
                NotificationCompat.Action.Builder(
                    action.icon,
                    action.label,
                    action.pendingIntent
                ).build()
            )
        }
        
        // 6. Post
        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }
        
        // 7. Throttle
        throttler.recordNotification()
    }
    
    fun canPost(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

data class NotificationAction(
    val icon: Int,
    val label: String,
    val pendingIntent: PendingIntent
)
```

## Permiso POST_NOTIFICATIONS (Android 13+)

### Solicitar permiso

```kotlin
class NotificationPermissionHelper {
    
    fun shouldShowRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    
    fun requestPermission(activity: Activity, requestCode: Int = 100) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            requestCode
        )
    }
    
    fun isGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true  // antes de 13, no requiere permiso runtime
        }
    }
}
```

### Cuándo pedir el permiso

1. Cuando el usuario activa explícitamente recordatorios (no antes).
2. Con rationale claro: "Para recordarte amablemente el registro, necesitamos permiso de notificaciones. Puedes desactivarlo cuando quieras desde Mi Perfil."
3. No en el onboarding genérico.

## Deep links a pantallas específicas

```kotlin
fun buildDeepLinkIntent(context: Context, ruta: String): PendingIntent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("solvyx://$ruta")).apply {
        setPackage(context.packageName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    return PendingIntent.getActivity(
        context,
        ruta.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
```

Uso:

```kotlin
notifier.post(
    title = "Solvyx",
    body = "Aquí sigo cuando quieras. ¿Cómo te fue hoy?",
    channelId = NotificationChannels.CHANNEL_BITACORA,
    deepLink = "solvyx://bitacora"
)
```

## Acciones inline

```kotlin
val marcarHechoIntent = buildActionIntent(context, "marcar_hecho") {
    // Broadcast receiver o worker
}

val posponerIntent = buildActionIntent(context, "posponer_30min") {
    // Reagendar
}

notifier.post(
    title = "Buenos días",
    body = "Tu rutina matutina está lista.",
    actions = listOf(
        NotificationAction(
            icon = R.drawable.ic_check,
            label = "Marcar como hecha",
            pendingIntent = marcarHechoIntent
        ),
        NotificationAction(
            icon = R.drawable.ic_clock,
            label = "Posponer 30 min",
            pendingIntent = posponerIntent
        )
    )
)
```

## Estilo visual

### Color y brand

- **Small icon:** monocromo, silhouette blanco sobre transparente. NO íconos de colores.
- **Color:** `TealPrimary` del proyecto para `setColor()`.
- **Sin emojis en el small icon** (limitación de Android).

### BigTextStyle para mensajes largos

```kotlin
.setStyle(NotificationCompat.BigTextStyle()
    .setBigContentTitle(title)
    .bigText(body)
    .setSummaryText("Berto"))
```

## Copy de notificaciones (validado clínicamente)

### Tabla de ejemplos

| Canal | Contexto | Ejemplo |
|---|---|---|
| Bitácora | Recordatorio diario | "Aquí sigo cuando quieras. ¿Cómo te fue hoy?" |
| Rutinas | Matutina | "Buenos días. Tu rutina de hoy está lista." |
| Rutinas | Nocturna | "Es momento de cerrar el día. ¿Listx?" |
| Insights | Berto | "Berto notó algo en tu proceso. Te lo cuento." |
| Bitácora | Volver tras gap | "Hace {N} días que no registras. Aquí sigo." |

**Todos pasan por `psicologo-solvyx`.**

## Foreground vs Background

- Si la app está en foreground y se postea una notificación, el usuario la ve al pasar a background.
- En foreground, **muestra un Toast o Banner en la app** en lugar de una notificación del sistema.
- Helper:

```kotlin
fun shouldShowNotification(foreground: Boolean): Boolean {
    return !foreground
}
```

## Quiet hours

Las notificaciones no deben postearse durante las horas configuradas:

```kotlin
suspend fun post(
    title: String,
    body: String,
    // ...
    quietHoursStart: Int? = null,
    quietHoursEnd: Int? = null
) {
    val now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    val inQuietHours = if (quietHoursStart != null && quietHoursEnd != null) {
        if (quietHoursStart < quietHoursEnd) {
            now in quietHoursStart until quietHoursEnd
        } else {
            now >= quietHoursStart || now < quietHoursEnd
        }
    } else false
    
    if (inQuietHours) return
    
    // ... resto
}
```

## Testing

```kotlin
@Test
fun `canPost returns false when permission denied`() {
    val notifier = Notifier(context, throttler)
    // Asumir que el permiso está denegado
    assertFalse(notifier.canPost())
}
```

## Anti-patrones prohibidos

1. **NotificationChannel único para todo.** Categorías separadas.
2. **`IMPORTANCE_HIGH` o `IMPORTANCE_MAX` para tareas no críticas.** Ruido y battery drain.
3. **Small icon de colores.** Solo monocromo.
4. **Sin tap intent.** Notificación que no abre nada.
5. **Texto largo sin `BigTextStyle`.** Se trunca.
6. **Notificaciones en foreground.** Mostrar banner interno.
7. **Notificaciones sin respetar quiet hours.**
8. **Sin validación clínica del copy.** Especialmente en canales emocionales.
9. **Permiso de notificaciones pedido en onboarding sin contexto.** Confuso.
10. **`setUsesChronometer(true)` o `setWhen()` con timestamps antiguos.** Confuso para el usuario.