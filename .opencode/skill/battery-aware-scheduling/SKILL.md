---
description: Scheduling consciente de batería para Solvyx. Respeto a Doze mode, battery optimization, y life-critical scheduling justificado.
---

# Skill: Battery-Aware Scheduling

Esta skill te entrega las convenciones para que el scheduling de Solvyx respete la batería del dispositivo y el modo Doze de Android. Aplícala al configurar periodic work, one-time work con delay, y al decidir cuándo usar AlarmManager.

## Principios

1. **Respeto absoluto a Doze mode y App Standby.**
2. **`setExactAndAllowWhileIdle` solo en casos de vida o muerte** (ej. recordatorio de toma de medicamento). Para Solvyx: solo para crisis.
3. **WorkManager es el default.** AlarmManager solo para los casos críticos.
4. **Constraints por defecto:** batería no baja.
5. **Resiliencia ante battery optimization agresiva** de OEMs (Xiaomi, Huawei, Samsung).
6. **Pedir al usuario que desactive battery optimization** solo cuando sea crítico para la feature, con justificación clara.

## Doze mode y App Standby en Android

### Doze mode (desde API 23)

- Cuando el dispositivo está inactivo (pantalla apagada, sin movimiento, sin carga), entra en Doze.
- En Doze, las apps no pueden ejecutar código. Solo se desbloquean en "maintenance windows".
- Las ventanas de mantenimiento se hacen más espaciadas con el tiempo (cada 30 min, 2h, 4h, etc.).

### App Standby

- Apps no usadas recientemente entran en standby.
- Restricciones similares a Doze pero menos severas.

### Lo que se desbloquea en Doze

- WorkManager con `setRequiresBatteryNotLow(false)` o sin constraint.
- AlarmManager con `setAndAllowWhileIdle`.
- Jobs con `setOverrideDeadline` (no usado normalmente).
- WorkManager con `setExpedited` (a partir de API 31).

## Constraints recomendadas por defecto

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)  // tarea local
    .setRequiresBatteryNotLow(true)
    .setRequiresCharging(false)
    .setRequiresDeviceIdle(false)  // no requerir idle por defecto
    .setRequiresStorageNotLow(true)
    .build()
```

## Cuándo usar `setExactAndAllowWhileIdle`

**Solo en escenarios de vida o muerte.** WorkManager usa inexact scheduling por defecto para preservar batería.

Casos válidos para Solvyx:
- **Detección de crisis que requiera respuesta inmediata** (ej. recordatorio de tomar medicamento crítico). **No aplica a Solvyx actualmente.**
- **Alarmas médicas críticas.** No es el caso de Solvyx.

Casos NO válidos:
- "Recordatorio de bitácora" (puede esperar 1-3h).
- "Recordatorio de rutina matutina" (puede esperar 30 min).
- "Insight de Berto" (puede esperar 24-72h).
- "Notificación de bienvenida al abrir la app" (no debería ser scheduled work).

## Cuándo usar AlarmManager

**Evitar AlarmManager** en la medida de lo posible. WorkManager + constraints cubre el 99% de los casos.

Si tienes que usarlo (rara vez):

```kotlin
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService<AlarmManager>()!!
    
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleExact(hour: Int, minute: Int, requestCode: Int) {
        val triggerTime = computeTriggerTime(hour, minute)
        
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback a inexact
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                // Solicitar permiso al usuario
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
```

## OEMs agresivos con battery optimization

Algunos fabricantes (Xiaomi MIUI, Huawei EMUI, Samsung One UI) matan agresivamente las apps en background. WorkManager puede no ejecutarse.

### Solución: invitar al usuario a desactivar optimization

**Cuándo pedirlo:**
- Solo en features donde el no-ejecución rompe la funcionalidad (ej. recordatorio de bitácora).
- **Nunca** en features secundarias.

**Cómo pedirlo:**

```kotlin
class BatteryOptimizationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService<PowerManager>()!!
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    
    fun requestIgnoreBatteryOptimizations(activity: Activity) {
        if (!isIgnoringBatteryOptimizations()) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        }
    }
    
    fun openBatterySettings(activity: Activity) {
        // Para OEMs con settings custom
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        activity.startActivity(intent)
    }
}
```

**Cuándo mostrar el diálogo al usuario:**

1. Solo la primera vez que el usuario configura un recordatorio.
2. Solo si `isIgnoringBatteryOptimizations() == false`.
3. Con justificación clara: "Para que Solvyx pueda recordarte el registro a tiempo, necesitamos que sigamos ejecutando en segundo plano. Android a veces detiene las apps para ahorrar batería. ¿Quieres desactivar esa optimización para Solvyx?"

## Mejores prácticas para WorkManager

### 1. Flex time

Permite que el sistema ejecute el trabajo dentro de una ventana flexible:

```kotlin
PeriodicWorkRequestBuilder<BitacoraReminderWorker>(
    repeatInterval = 1, TimeUnit.DAYS,
    flexTimeInterval = 6, TimeUnit.HOURS  // entre 18h y 24h después del último run
)
```

### 2. Initial delay para evitar arranque inmediato

```kotlin
val request = PeriodicWorkRequestBuilder<RutinaMatutinaWorker>(/* ... */)
    .setInitialDelay(8, TimeUnit.HOURS)  // primer run en 8h
    .build()
```

### 3. Backoff con exponential

```kotlin
.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
```

### 4. Constraints que sí importan

```kotlin
// Para descarga: requiere red
.setRequiredNetworkType(NetworkType.CONNECTED)

// Para backup nocturno: requiere carga
.setRequiresCharging(true)

// Para recordatorios: solo batería no baja
.setRequiresBatteryNotLow(true)
```

### 5. Expedited work (API 31+)

Para trabajo que debe ejecutarse inmediatamente:

```kotlin
val request = OneTimeWorkRequestBuilder<CriticalWorker>()
    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    .build()
```

**Limitaciones:** 10 expedited jobs por app en modo foreground, hasta 30 segundos.

## Edge cases

### Caso: usuario fuerza la detención de la app

Si el usuario hace "Force Stop" desde Settings, **todos los WorkRequests se cancelan**. No hay workaround. La única opción es informar al usuario.

### Caso: cambio de zona horaria

Si el usuario viaja, los horarios de las rutinas pueden quedar desfasados. WorkManager usa `System.currentTimeMillis()` que es UTC. La presentación debe convertir a local.

```kotlin
private fun computeTriggerTime(hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
```

### Caso: cambio de hora (Daylight Saving)

WorkManager ajusta automáticamente. Calendar.getInstance() maneja DST correctamente en la mayoría de los casos.

## Monitoreo de ejecución

```kotlin
class WorkExecutionTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun observeState(workName: String): Flow<WorkInfo.State> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(workName)
            .map { infos -> infos.firstOrNull()?.state ?: WorkInfo.State.CANCELLED }
    }
}
```

En Debug, puedes ver el estado en:

```
adb shell dumpsys jobscheduler | grep solvyx
```

## Política de Solvyx

1. **Por defecto:** WorkManager con constraints normales (batería no baja, sin red requerida).
2. **Rutinas matutina/nocturna:** WorkManager + `flexTimeInterval` de 30-60 minutos.
3. **Insights:** WorkManager cada 3 días, debounced por el usuario.
4. **Bitácora reminder:** WorkManager diario, configurable.
5. **AlarmManager:** solo si en el futuro se agrega "alarma crítica de crisis" (ej. usuario programó una). Por ahora NO se usa.
6. **Battery optimization prompt:** solo en el primer setup de recordatorio, con justificación.

## Anti-patrones prohibidos

1. **`setExactAndAllowWhileIdle` para tareas no críticas.** Agota batería.
2. **Pedir al usuario que desactive battery optimization** sin justificación.
3. **WorkManager sin constraints.** Permitirá ejecución con batería baja.
4. **AlarmManager para tareas recurrentes.** Usar WorkManager.
5. **Asumir que WorkManager se ejecuta a la hora exacta.** Siempre hay jitter.
6. **`adb shell cmd jobscheduler run` en producción.** Solo para debugging.
7. **Confiar en Doze no ocurra.** Diseña como si Doze estuviera siempre activo.
8. **Olvidar `flexTimeInterval`** en periodic work. El sistema no tiene holgura.