---
description: Estrategias de debouncing y throttling para insights offline de Solvyx. Cuándo mostrar, cuándo callar, respeto al usuario.
---

# Skill: Debouncing Strategies

Esta skill te entrega las estrategias para evitar que los insights offline de Solvyx saturen al usuario. Aplícala al configurar la frecuencia de insights, notificaciones y banners.

## Principios

1. **Callar más de lo que se habla.** El usuario debe sentir que la app respeta su atención.
2. **Insights son complemento, no notificación push.** No compiten con mensajes urgentes.
3. **Configurabilidad por el usuario.** Si el usuario quiere más, que pueda pedir más.
4. **Reset en hitos significativos.** Un primer insight tras instalación es válido; el segundo debe esperar.
5. **Logging del debouncing.** Para debugging y auditoría.

## Política de debouncing por defecto

| Categoría | Frecuencia mínima | Configurable |
|---|---|---|
| Insight automático | 72 horas (3 días) | Sí (24h si "más insights") |
| Notificación local | 24 horas | Sí |
| Banner en Home | Cuando hay nuevo insight | N/A |
| Recordatorio de bitácora | 24 horas | Sí |
| Rutina matutina/noturna | 1 vez al día cada una | Sí |

## Implementación de debouncing temporal

### Última vez mostrado

```kotlin
interface LastInsightTimeRepository {
    suspend fun get(): Long
    suspend fun set(timestamp: Long)
}

class LastInsightTimeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LastInsightTimeRepository {
    
    private val key = longPreferencesKey("last_insight_timestamp")
    
    override suspend fun get(): Long = dataStore.data.first()[key] ?: 0L
    
    override suspend fun set(timestamp: Long) {
        dataStore.edit { it[key] = timestamp }
    }
}
```

### Función de decisión

```kotlin
object InsightDebounce {
    
    fun shouldShow(
        lastShownTimestamp: Long,
        now: Long = System.currentTimeMillis(),
        minIntervalHours: Long = 72,
        userAcceptsMore: Boolean = false
    ): Boolean {
        // Primera vez: mostrar
        if (lastShownTimestamp == 0L) return true
        
        // Si el usuario quiere más, intervalo más corto
        val effectiveInterval = if (userAcceptsMore) 24L else minIntervalHours
        
        val elapsedHours = TimeUnit.MILLISECONDS.toHours(now - lastShownTimestamp)
        return elapsedHours >= effectiveInterval
    }
}
```

## Estados del usuario

### "Más insights" / "Menos insights"

```kotlin
data class UserInsightsPreferences(
    val enabled: Boolean = true,
    val acceptMore: Boolean = false,    // reduce intervalo
    val quietHoursStart: Int? = null,  // 22 (10pm)
    val quietHoursEnd: Int? = null,    // 8 (8am)
)
```

Si el usuario tiene "enabled = false", no se evalúan reglas.

## Quiet hours

```kotlin
fun shouldShowConsideringQuietHours(
    lastShown: Long,
    now: Long,
    userPrefs: UserInsightsPreferences
): Boolean {
    val calendar = Calendar.getInstance().apply { timeInMillis = now }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    
    val quietStart = userPrefs.quietHoursStart
    val quietEnd = userPrefs.quietHoursEnd
    
    if (quietStart != null && quietEnd != null) {
        val enQuietHours = if (quietStart < quietEnd) {
            currentHour in quietStart until quietEnd
        } else {
            currentHour >= quietStart || currentHour < quietEnd
        }
        
        if (enQuietHours) return false
    }
    
    return shouldShow(lastShown, now)
}
```

## Debouncing por tipo de insight

Algunos insights no deben mostrarse demasiado seguido:

```kotlin
class InsightsEngine @Inject constructor(/* ... */) {
    
    private val lastShownByType = mutableMapOf<String, Long>()
    
    suspend fun evaluate(entries: List<BitacoraEntry>, userPrefs: UserInsightsPreferences): List<Insight> {
        if (!userPrefs.enabled) return emptyList()
        
        val results = rules.map { rule ->
            async { runCatching { rule.evaluate(entries) }.getOrNull() }
        }.awaitAll()
        
        val now = now()
        val insightsFiltrados = results.filterNotNull().filter { insight ->
            shouldShowInsight(insight, userPrefs, now)
        }
        
        // Actualizar timestamps
        insightsFiltrados.forEach { insight ->
            lastInsightTimeRepo.set(now)
            lastShownByType[insight.id] = now
        }
        
        return insightsFiltrados.sortedByDescending { it.severidad.ordinal }
    }
    
    private fun shouldShowInsight(
        insight: Insight,
        userPrefs: UserInsightsPreferences,
        now: Long
    ): Boolean {
        // 1. Regla global
        if (!shouldShowConsideringQuietHours(
            lastInsightTimeRepo.get(),
            now,
            userPrefs
        )) return false
        
        // 2. Regla por tipo (mínimo 7 días entre mismo insight)
        val lastShownTipo = lastShownByType[insight.id] ?: 0L
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(now - lastShownTipo)
        if (elapsedDays < 7) return false
        
        return true
    }
}
```

## Throttling en VM (no emitir si ya hay uno mostrado)

```kotlin
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val engine: InsightsEngine,
    private val repository: InsightsRepository,
    private val userPrefsRepo: UserInsightsPreferencesRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<InsightsUiState>(InsightsUiState.Idle)
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()
    
    private val _currentInsight = MutableStateFlow<Insight?>(null)
    val currentInsight: StateFlow<Insight?> = _currentInsight.asStateFlow()
    
    fun evaluarAhora() {
        viewModelScope.launch {
            _state.value = InsightsUiState.Loading
            
            val entries = repository.getEntriesOnce()
            val prefs = userPrefsRepo.getOnce()
            
            val insights = engine.evaluate(entries, prefs)
            
            _state.value = if (insights.isEmpty()) {
                InsightsUiState.SinInsightsNuevos
            } else {
                _currentInsight.value = insights.first()
                InsightsUiState.InsightsDisponibles(insights)
            }
        }
    }
    
    fun onDismiss() {
        _currentInsight.value = null
    }
}
```

## Throttling de notificaciones locales

### Regla

- **Máximo 1 notificación local por día.**
- **Si hay insight nuevo Y notificación de bitácora Y recordatorio de rutina al mismo tiempo, prioriza:** Bitácora > Rutina > Insight.

```kotlin
class NotificationThrottler @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val lastNotificationKey = longPreferencesKey("last_notification_timestamp")
    private val notificationCountTodayKey = intPreferencesKey("notification_count_today")
    private val notificationDateKey = stringPreferencesKey("notification_date")
    
    suspend fun canNotify(now: Long = System.currentTimeMillis()): Boolean {
        val prefs = dataStore.data.first()
        val today = isoDate(now)
        val lastDate = prefs[notificationDateKey]
        val countToday = if (lastDate == today) prefs[notificationCountTodayKey] ?: 0 else 0
        
        return countToday < 3  // máximo 3 notificaciones por día
    }
    
    suspend fun recordNotification(now: Long = System.currentTimeMillis()) {
        dataStore.edit { prefs ->
            val today = isoDate(now)
            val lastDate = prefs[notificationDateKey]
            
            if (lastDate == today) {
                prefs[notificationCountTodayKey] = (prefs[notificationCountTodayKey] ?: 0) + 1
            } else {
                prefs[notificationDateKey] = today
                prefs[notificationCountTodayKey] = 1
            }
            
            prefs[lastNotificationKey] = now
        }
    }
    
    private fun isoDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        return formatter.format(Date(timestamp))
    }
}
```

## Cancelación de debouncing cuando el usuario interactúa

Si el usuario abre la sección de Insights manualmente, resetear el debouncing:

```kotlin
fun onUserOpenedInsights() {
    viewModelScope.launch {
        lastInsightTimeRepo.set(0L)  // reset
    }
}
```

Esto permite al usuario "forzar" insights si los necesita.

## Estrategia para evitar ansiedad por ausencia de insights

| Comportamiento | Acción |
|---|---|
| Usuario ve insights regularmente | Mantener cadencia. |
| Usuario ignora insights consistentemente | Reducir frecuencia automáticamente. |
| Usuario marca insight como "no útil" | Desactivar esa regla específica. |
| Usuario desactiva insights globalmente | Respetar inmediatamente. |

```kotlin
class InsightFeedbackRepository @Inject constructor(
    private val dao: InsightFeedbackDao
) {
    suspend fun markNotUseful(insightId: String, tipo: String) {
        dao.insert(InsightFeedbackEntity(
            insightId = insightId,
            tipo = tipo,
            timestamp = now(),
            feedback = "not_useful"
        ))
    }
    
    suspend fun shouldDisableRule(insightId: String, tipo: String): Boolean {
        val recent = dao.countRecentByFeedback(insightId, tipo, "not_useful", dias = 30)
        return recent >= 3  // si el usuario marca 3 veces como "no útil", desactivar
    }
}
```

## Monitoreo y logging

```kotlin
object DebounceMetrics {
    private val metrics = mutableMapOf<String, DebounceMetric>()
    
    data class DebounceMetric(
        var shown: Int = 0,
        var suppressedByGlobalDebounce: Int = 0,
        var suppressedByQuietHours: Int = 0,
        var suppressedByRuleType: Int = 0,
        var suppressedByUserFeedback: Int = 0
    )
    
    fun recordShown(insightId: String) {
        metrics.getOrPut(insightId) { DebounceMetric() }.shown++
    }
    
    fun recordSuppressed(insightId: String, reason: String) {
        val metric = metrics.getOrPut(insightId) { DebounceMetric() }
        when (reason) {
            "global_debounce" -> metric.suppressedByGlobalDebounce++
            "quiet_hours" -> metric.suppressedByQuietHours++
            "rule_type" -> metric.suppressedByRuleType++
            "user_feedback" -> metric.suppressedByUserFeedback++
        }
    }
    
    fun getMetrics(): Map<String, DebounceMetric> = metrics.toMap()
    
    fun reset() {
        metrics.clear()
    }
}
```

## Testing

```kotlin
class DebounceTest {
    
    @Test
    fun `primera vez siempre muestra`() {
        assertTrue(shouldShow(lastShown = 0L, now = 1000L))
    }
    
    @Test
    fun `intervalo minimo respetado`() {
        val now = 1000L * 60 * 60 * 24 * 4  // 4 días
        val lastShown = 0L
        
        // Con intervalo 72h, después de 4 días debe mostrar
        assertTrue(shouldShow(lastShown, now, minIntervalHours = 72))
    }
    
    @Test
    fun `intervalo minimo bloquea si no se cumplio`() {
        val now = 1000L * 60 * 60 * 24 * 2  // 2 días
        val lastShown = 0L
        
        // Con intervalo 72h, después de 2 días NO debe mostrar
        assertFalse(shouldShow(lastShown, now, minIntervalHours = 72))
    }
    
    @Test
    fun `userAcceptsMore reduce el intervalo`() {
        val now = 1000L * 60 * 60 * 30  // 30 horas
        val lastShown = 0L
        
        assertFalse(shouldShow(lastShown, now, minIntervalHours = 72, userAcceptsMore = false))
        assertTrue(shouldShow(lastShown, now, minIntervalHours = 72, userAcceptsMore = true))
    }
}
```

## Anti-patrones prohibidos

1. **Emitir insight en cada cambio de bitácora.** Saturación.
2. **Notificación local por cada insight.** Exceso.
3. **Debouncing hardcoded sin posibilidad de configuración.** Falta de respeto al usuario.
4. **Ignorar quiet hours.** Invadir horarios sensibles.
5. **Reset del debounce automático tras inactividad larga.** Mostrar de golpe muchos insights.
6. **No loguear métricas de debouncing.** Sin visibilidad.
7. **Comparar timestamps con `Date` en lugar de `Long`.** Errores de zona horaria.
8. **Usar `Handler.postDelayed` en lugar de WorkManager o coroutines.** Menos robusto.