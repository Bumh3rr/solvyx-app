---
description: Reglas determinísticas de correlación para insights offline de Solvyx. Cómo diseñar reglas testeables sin usar IA.
---

# Skill: Correlation Rules

Esta skill te entrega el marco para diseñar reglas de correlación determinísticas que generan insights offline en Solvyx. Aplícala al implementar cada nueva regla de `InsightsEngine`.

## Principios

1. **Cada regla es una función pura** sobre `List<BitacoraEntry>`. Sin estado, sin side effects.
2. **Testeable al 100%.** Cobertura ≥80% por regla.
3. **Determinística.** Mismos datos → mismo insight. Sin aleatoriedad.
4. **Insights son observaciones, no predicciones.** "Esta semana dormiste menos", no "Seguirás durmiendo poco".
5. **Insights no son diagnósticos.** "Tienes ansiedad" NO. "La emoción ansioso apareció 4 veces esta semana" SÍ.
6. **Tolerancia a datos incompletos.** La regla debe decidir cuándo NO emitir insight por falta de datos.

## Anatomía de una regla

```kotlin
interface InsightRule {
    /**
     * @param entries Bitácora completa. La regla filtra por ventana temporal si necesita.
     * @return Insight si la regla se cumple, null en caso contrario.
     */
    suspend fun evaluate(entries: List<BitacoraEntry>): Insight?
}
```

```kotlin
data class Insight(
    val id: String,                                    // slug estable
    val tipo: TipoInsight,                             // OBSERVACION, SUGERENCIA, RECONOCIMIENTO
    val severidad: Severidad,                          // BAJA, MEDIA, ALTA
    val ventanaTexto: String,                          // "esta semana", "los últimos 3 días"
    val datos: Map<String, Any> = emptyMap(),          // para que el copy layer arme el texto
    val accion: AccionInsight? = null                  // opcional: navegar, mostrar, etc.
)

enum class TipoInsight { OBSERVACION, SUGERENCIA, RECONOCIMIENTO }
enum class Severidad { BAJA, MEDIA, ALTA }
```

## Reglas predefinidas para Solvyx

### 1. Sueño bajo esta semana

```kotlin
class SuenoBajoEstaSemanaRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        val ultimos7 = ultimosNDias(entries, 7)
        val conSueno = ultimos7.mapNotNull { it.suenoHoras }
        
        if (conSueno.size < 3) return@withContext null
        
        val promedio = conSueno.average()
        if (promedio >= 6.0) return@withContext null
        
        return@withContext Insight(
            id = "sueno_bajo_esta_semana",
            tipo = TipoInsight.OBSERVACION,
            severidad = Severidad.MEDIA,
            ventanaTexto = "esta semana",
            datos = mapOf(
                "promedio" to promedio,
                "dias_con_datos" to conSueno.size
            )
        )
    }
}
```

### 2. Racha de registro

```kotlin
class RachaRegistroRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        if (entries.isEmpty()) return@withContext null
        
        val fechasOrdenadas = entries.map { inicioDelDia(it.fecha) }.distinct().sortedDescending()
        val racha = diasConsecutivos(fechasOrdenadas)
        
        if (racha < 5) return@withContext null
        
        return@withContext Insight(
            id = "racha_registro",
            tipo = TipoInsight.RECONOCIMIENTO,
            severidad = Severidad.BAJA,
            ventanaTexto = "consecutivos",
            datos = mapOf("dias" to racha)
        )
    }
}
```

### 3. Emoción recurrente

```kotlin
class EmocionRecurrenteRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        val ultimos7 = ultimosNDias(entries, 7)
        
        // Contar frecuencia de cada emoción
        val frecuencias = ultimos7.groupingBy { it.animo }.eachCount()
        
        // Buscar emoción que aparezca 3+ veces
        val recurrente = frecuencias.filter { it.value >= 3 }.maxByOrNull { it.value }
            ?: return@withContext null
        
        return@withContext Insight(
            id = "emocion_recurrente",
            tipo = TipoInsight.OBSERVACION,
            severidad = Severidad.MEDIA,
            ventanaTexto = "esta semana",
            datos = mapOf(
                "emocion" to recurrente.key,
                "frecuencia" to recurrente.value
            )
        )
    }
}
```

### 4. Cravings agrupados por día

```kotlin
class CravingsAgrupadosPorDiaRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        val ultimos30 = ultimosNDias(entries, 30)
        val conCraving = ultimos30.filter { it.craving == true }
        
        if (conCraving.size < 4) return@withContext null  // necesitamos suficientes datos
        
        // Agrupar por día de la semana
        val porDia = conCraving.groupBy { entry ->
            Calendar.getInstance().apply { timeInMillis = entry.fecha }
                .get(Calendar.DAY_OF_WEEK)
        }
        
        // Buscar si algún día tiene >= 60% de las ocurrencias
        val total = conCraving.size
        val diaDominante = porDia.maxByOrNull { it.value.size } ?: return@withContext null
        
        if (diaDominante.value.size.toDouble() / total < 0.6) return@withContext null
        
        val nombresDias = mapOf(
            Calendar.SUNDAY to "domingo",
            Calendar.MONDAY to "lunes",
            Calendar.TUESDAY to "martes",
            Calendar.WEDNESDAY to "miércoles",
            Calendar.THURSDAY to "jueves",
            Calendar.FRIDAY to "viernes",
            Calendar.SATURDAY to "sábado"
        )
        
        return@withContext Insight(
            id = "cravings_agrupados_dia",
            tipo = TipoInsight.OBSERVACION,
            severidad = Severidad.BAJA,
            ventanaTexto = "el último mes",
            datos = mapOf(
                "dia" to (nombresDias[diaDominante.key] ?: "desconocido"),
                "ocurrencias" to diaDominante.value.size
            )
        )
    }
}
```

### 5. Gap de registro

```kotlin
class GapRegistroRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        if (entries.isEmpty()) return@withContext null
        
        val ultimaEntrada = entries.maxBy { it.fecha }
        val diasDesdeUltima = diasEntre(ultimaEntrada.fecha)
        
        if (diasDesdeUltima < 5) return@withContext null  // no es gap significativo
        
        return@withContext Insight(
            id = "gap_registro",
            tipo = TipoInsight.OBSERVACION,
            severidad = Severidad.BAJA,
            ventanaTexto = "desde $diasDesdeUltima días",
            datos = mapOf("dias_sin_registrar" to diasDesdeUltima)
        )
    }
}
```

### 6. Logro pequeño (volver tras gap)

```kotlin
class LogroPequenoRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        if (entries.size < 2) return@withContext null
        
        val ordenadas = entries.sortedByDescending { it.fecha }
        val ultima = ordenadas[0]
        val anterior = ordenadas[1]
        
        val gapDias = diasEntre(anterior.fecha, ultima.fecha)
        
        if (gapDias < 5) return@withContext null
        
        return@withContext Insight(
            id = "logro_volver_tras_gap",
            tipo = TipoInsight.RECONOCIMIENTO,
            severidad = Severidad.BAJA,
            ventanaTexto = "tras $gapDias días",
            datos = mapOf("dias_de_ausencia" to gapDias)
        )
    }
}
```

### 7. Consumo reciente

```kotlin
class ConsumoRecienteRule : InsightRule {
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        val ultimas24h = ultimosNDias(entries, 1)
        val conConsumoReciente = ultimas24h.filter { it.consumo }
        
        if (conConsumoReciente.isEmpty()) return@withContext null
        
        return@withContext Insight(
            id = "consumo_reciente",
            tipo = TipoInsight.OBSERVACION,
            severidad = Severidad.MEDIA,
            ventanaTexto = "ayer",
            datos = mapOf(
                "sustancia" to (conConsumoReciente.first().sustancia ?: "no especificada"),
                "intensidad_animo_hoy" to entries.firstOrNull()?.animo
            )
        )
    }
}
```

## Anti-patrones prohibidos

1. **Reglas con estado mutable.** Si necesitas estado, cachéalo fuera de la regla.
2. **Reglas que dependen de la hora actual fuera de la ventana.** Toda comparación es relativa a `now()`.
3. **Insights con juicio moral.** "Deberías..." nunca. "Podrías considerar..." a veces (con cautela).
4. **Reglas que requieren >1000 entradas en memoria.** Mejor consulta SQL.
5. **Insights en momentos sensibles sin copy validado por `psicologo-solvyx`.** Especialmente consumo reciente.
6. **Reglas con `Thread.sleep` o delays.** El debouncing se hace fuera de la regla.
7. **Reglas que dependen de orden de evaluación.** Cada regla es independiente.

## Diseño de insight copy

El copy del insight lo diseña `backend-content-curator` (no la regla). La regla solo expone `datos` para que el copy layer arme el texto.

```kotlin
// La regla NO hace esto:
Insight(
    id = "...",
    mensaje = "Esta semana dormiste poco"  // MAL: copy en la regla
)

// La regla SÍ hace esto:
Insight(
    id = "...",
    ventanaTexto = "esta semana",
    datos = mapOf("promedio" to 5.2)  // BIEN: datos estructurados
)

// Y el copy layer hace:
fun Insight.toCopy(): String = when (id) {
    "sueno_bajo_esta_semana" -> "Esta semana promediaste ${"%.1f".format(datos["promedio"])}h de sueño."
    // ...
}
```

## Testing obligatorio por regla

```kotlin
class SuenoBajoEstaSemanaRuleTest {
    private val rule = SuenoBajoEstaSemanaRule()
    
    @Test
    fun `no emite insight con menos de 3 datos`() = runTest {
        val entries = listOf(
            entryConSueno(hace = 1, horas = 5),
            entryConSueno(hace = 2, horas = 4)
        )
        assertNull(rule.evaluate(entries))
    }
    
    @Test
    fun `emite insight cuando promedio es menor a 6h`() = runTest {
        val entries = listOf(
            entryConSueno(hace = 1, horas = 5),
            entryConSueno(hace = 3, horas = 5),
            entryConSueno(hace = 5, horas = 5)
        )
        val insight = rule.evaluate(entries)
        
        assertNotNull(insight)
        assertEquals("sueno_bajo_esta_semana", insight?.id)
        assertEquals(Severidad.MEDIA, insight?.severidad)
    }
    
    @Test
    fun `no emite insight cuando promedio es 6 o mas`() = runTest {
        val entries = listOf(
            entryConSueno(hace = 1, horas = 7),
            entryConSueno(hace = 3, horas = 6),
            entryConSueno(hace = 5, horas = 6)
        )
        assertNull(rule.evaluate(entries))
    }
    
    private fun entryConSueno(hace: Int, horas: Int) = BitacoraEntry(
        fecha = now() - TimeUnit.DAYS.toMillis(hace.toLong()),
        animo = "neutral",
        consumo = false,
        suenoHoras = horas
    )
}
```

## Cuándo agregar una nueva regla

1. Hay un patrón clínico observable que mejora con información.
2. La información es accionable o generadora de insight.
3. No es diagnóstica ni predictiva.
4. Se puede expresar con una regla determinística.
5. El copy se valida con `psicologo-solvyx`.

## Pipeline de evaluación

```kotlin
@Singleton
class InsightsEngine @Inject constructor(
    private val rules: Set<@JvmSuppressWildcards InsightRule>,
    private val lastInsightTime: LastInsightTimeRepository
) {
    
    suspend fun evaluate(entries: List<BitacoraEntry>): List<Insight> = withContext(Dispatchers.Default) {
        // 1. Verificar debouncing
        if (!shouldEvaluate(lastInsightTime.get())) {
            return@withContext emptyList()
        }
        
        // 2. Evaluar todas las reglas en paralelo
        val results = rules.map { rule ->
            async { runCatching { rule.evaluate(entries) }.getOrNull() }
        }.awaitAll()
        
        // 3. Filtrar nulos y ordenar por severidad
        results.filterNotNull()
            .sortedByDescending { it.severidad.ordinal }
    }
    
    private fun shouldEvaluate(last: Long): Boolean {
        if (last == 0L) return true
        val diff = now() - last
        return TimeUnit.MILLISECONDS.toHours(diff) >= 72  // 3 días
    }
}
```

## Métricas de calidad

- **Precisión:** ¿la regla captura los casos que debería?
- **Recall:** ¿no se le escapan casos obvios?
- **Falsos positivos:** ¿emite insights cuando no debería?
- **Costo cognitivo:** ¿el insight es útil o satura al usuario?
- **Frecuencia de emisión:** ¿se dispara demasiado seguido? Ajustar debouncing.