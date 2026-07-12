---
description: Análisis de ventanas temporales para los insights offline de Solvyx. Cómo calcular estadísticas sobre días/semanas/meses desde la bitácora local.
---

# Skill: Time Window Analysis

Esta skill te entrega los patrones para calcular estadísticas y patrones sobre ventanas temporales en los insights offline de Solvyx. Aplícala al implementar reglas de correlación que requieren agregar datos por día, semana o mes.

## Principios

1. **Trabaja siempre con timestamps en milisegundos** (`Long`). La capa de presentación los formatea.
2. **Ventanas relativas al "ahora"**, no absolutas. Si defines "últimos 7 días", es desde `ahora()` hacia atrás.
3. **Tolera gaps en el registro.** El usuario puede no registrar todos los días. No contar gaps como "0".
4. **Documenta el tamaño de ventana** en cada insight. El usuario debe saber qué período cubriste.
5. **Performance O(n) o menor.** Si necesitas agregaciones pesadas, hazlo en Room (vía DAO).

## Utilidades base

### `now()`

```kotlin
fun now(): Long = System.currentTimeMillis()
```

### Inicio de día

```kotlin
fun inicioDelDia(timestamp: Long = now()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
```

### Diferencia en días

```kotlin
fun diasEntre(desde: Long, hasta: Long = now()): Int {
    val diff = hasta - desde
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}
```

### Generar días en una ventana

```kotlin
fun diasEnVentana(dias: Int, hasta: Long = now()): List<Long> {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = hasta
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    return (0 until dias).map { offset ->
        calendar.timeInMillis.also {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
    }.reversed()
}
```

## Agrupar entradas por día

```kotlin
fun agruparPorDia(entries: List<BitacoraEntry>): Map<Long, List<BitacoraEntry>> {
    return entries.groupBy { inicioDelDia(it.fecha) }
}
```

## Filtros por ventana temporal

### Últimos N días

```kotlin
fun ultimosNDias(entries: List<BitacoraEntry>, dias: Int, desde: Long = now()): List<BitacoraEntry> {
    val limite = desde - TimeUnit.DAYS.toMillis(dias.toLong())
    return entries.filter { it.fecha >= limite }
}
```

### Mismo día de la semana

```kotlin
fun mismosDiasDeSemana(entries: List<BitacoraEntry>, dia: Int): List<BitacoraEntry> {
    // dia: Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ...
    return entries.filter { entry ->
        val cal = Calendar.getInstance().apply { timeInMillis = entry.fecha }
        cal.get(Calendar.DAY_OF_WEEK) == dia
    }
}
```

### Misma hora del día (±2h)

```kotlin
fun enVentanaHoraria(entries: List<BitacoraEntry>, hora: Int, tolerancia: Int = 2): List<BitacoraEntry> {
    return entries.filter { entry ->
        val cal = Calendar.getInstance().apply { timeInMillis = entry.fecha }
        val horaDelEntry = cal.get(Calendar.HOUR_OF_DAY)
        abs(horaDelEntry - hora) <= tolerancia
    }
}
```

## Estadísticas básicas

### Promedio

```kotlin
fun promedio(valores: List<Int>): Double? {
    if (valores.isEmpty()) return null
    return valores.average()
}

fun promedio(valores: List<Long>): Double? {
    if (valores.isEmpty()) return null
    return valores.average()
}
```

### Moda (valor más frecuente)

```kotlin
fun moda(valores: List<String>): String? {
    if (valores.isEmpty()) return null
    return valores.groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}
```

### Conteo de frecuencia

```kotlin
fun frecuencias(valores: List<String>): Map<String, Int> {
    return valores.groupingBy { it }.eachCount()
}

fun diasConsecutivos(fechas: List<Long>): Int {
    if (fechas.isEmpty()) return 0
    
    val fechasOrdenadas = fechas.sortedDescending().map { inicioDelDia(it) }
    var consecutivas = 1
    val unDia = TimeUnit.DAYS.toMillis(1)
    
    for (i in 0 until fechasOrdenadas.size - 1) {
        if (fechasOrdenadas[i] - fechasOrdenadas[i + 1] == unDia) {
            consecutivas++
        } else {
            break
        }
    }
    
    return consecutivas
}
```

### Detección de gaps

```kotlin
fun gapMasGrande(fechas: List<Long>): Pair<Long, Int>? {
    // Retorna (fecha_inicio_gap, dias)
    if (fechas.size < 2) return null
    
    val ordenadas = fechas.sortedDescending().map { inicioDelDia(it) }
    val unDia = TimeUnit.DAYS.toMillis(1)
    var maxGap = 0L
    var fechaInicioMaxGap = ordenadas.first()
    
    for (i in 0 until ordenadas.size - 1) {
        val gap = ordenadas[i] - ordenadas[i + 1]
        if (gap > maxGap) {
            maxGap = gap
            fechaInicioMaxGap = ordenadas[i]
        }
    }
    
    val gapEnDias = TimeUnit.MILLISECONDS.toDays(maxGap).toInt() - 1
    return if (gapEnDias > 0) Pair(fechaInicioMaxGap, gapEnDias) else null
}
```

## Ventanas temporales predefinidas para Solvyx

| Ventana | Uso |
|---|---|
| Últimas 24 horas | Recordatorios de bitácora, detectar día sin registro. |
| Últimos 3 días | Insights de craving reciente. |
| Últimos 7 días (semana actual) | Insights semanales. Estándar para "esta semana". |
| Últimos 14 días | Comparar semana actual vs anterior. |
| Últimos 30 días | Insights mensuales. Detección de patrones a largo plazo. |

## Implementación de una regla con ventana

Ejemplo: "sueño bajo esta semana".

```kotlin
class SuenoBajoEstaSemanaRule : InsightRule {
    
    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? = withContext(Dispatchers.Default) {
        // 1. Filtrar últimos 7 días
        val ultimos7 = ultimosNDias(entries, 7)
        
        // 2. Filtrar los que tienen suenoHoras registrado
        val conSueno = ultimos7.filter { it.suenoHoras != null }
        
        // 3. Necesitamos al menos 3 datos para considerar la tendencia
        if (conSueno.size < 3) return@withContext null
        
        // 4. Calcular promedio
        val promedio = conSueno.mapNotNull { it.suenoHoras }.average()
        
        // 5. Comparar con la semana anterior
        val anteriores = entries.filter { 
            val dias = diasEntre(it.fecha)
            dias in 8..14
        }.mapNotNull { it.suenoHoras }
        
        if (anteriores.size < 3) return@withContext null
        
        val promedioAnterior = anteriores.average()
        
        // 6. Si el promedio actual es < 6h Y es menor que el anterior, generar insight
        if (promedio < 6.0 && promedio < promedioAnterior - 0.5) {
            Insight(
                id = "sueno_bajo_esta_semana",
                severidad = Severidad.MEDIA,
                ventanaTexto = "esta semana vs la anterior",
                datos = mapOf(
                    "promedio_actual" to promedio,
                    "promedio_anterior" to promedioAnterior,
                    "dias_con_datos" to conSueno.size
                )
            )
        } else null
    }
}
```

## Testing

```kotlin
@Test
fun `diasConsecutivos cuenta correctamente`() {
    val hoy = inicioDelDia(now())
    val ayer = hoy - TimeUnit.DAYS.toMillis(1)
    val anteayer = hoy - TimeUnit.DAYS.toMillis(2)
    val hace5Dias = hoy - TimeUnit.DAYS.toMillis(5)
    
    val fechas = listOf(hoy, ayer, anteayer, hace5Dias)
    assertEquals(3, diasConsecutivos(fechas))
}

@Test
fun `gapMasGrande detecta el gap correcto`() {
    val hoy = inicioDelDia(now())
    val ayer = hoy - TimeUnit.DAYS.toMillis(1)
    val hace4Dias = hoy - TimeUnit.DAYS.toMillis(4)
    
    val fechas = listOf(hoy, ayer, hace4Dias)
    val (inicioGap, dias) = gapMasGrande(fechas)!!
    
    assertEquals(ayer, inicioGap)
    assertEquals(2, dias)  // gap entre ayer y hace4Dias
}
```

## Performance

- **Agrupar por día** es O(n) y rápido para <1000 entradas.
- **Iterar múltiples ventanas** sin agrupar es O(n×k). Mejor agrupar una vez y filtrar.
- **Persistencia:** si necesitas análisis recurrentes, hazlos en Room con SQL (GROUP BY) en lugar de cargar todo a memoria.
- **Caching:** si una insight rule es costosa, cachea el resultado por N horas.

## Edge cases

| Caso | Manejo |
|---|---|
| Sin entradas | No generar insight. |
| Una sola entrada | No generar insight que requiera comparación. |
| Todas las entradas del mismo día | Contar como 1 día de datos. |
| Entradas con timestamps futuros (reloj mal configurado) | Filtrar `fecha <= now()`. |
| Entradas de hace años | Excluir si están fuera de ventana. |

## Anti-patrones prohibidos

1. **`java.util.Date` o `java.sql.Timestamp`.** Usa `Long` (millis) hasta la capa de UI.
2. **Iterar por días con `for` y `Date` manualmente.** Usa `Calendar` o `java.time` (con `coreLibraryDesugaring` si minSdk < 26).
3. **Asumir que cada día tiene una entrada.** Tolera gaps.
4. **Comparar con timestamps absolutos.** Usa ventanas relativas a `now()`.
5. **Recalcular todo en cada llamada.** Cachea o computa incrementalmente.