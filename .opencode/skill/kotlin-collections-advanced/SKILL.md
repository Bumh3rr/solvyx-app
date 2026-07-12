---
description: Colecciones avanzadas de Kotlin para análisis de bitácora en Solvyx. groupBy, partition, chunked, windowed, secuencias para performance.
---

# Skill: Kotlin Collections Advanced

Esta skill te entrega patrones avanzados de colecciones de Kotlin para procesar la bitácora de Solvyx de forma eficiente. Aplícala al implementar reglas de insights, transformaciones de datos, o cualquier análisis de listas grandes.

## Principios

1. **Usa `List` inmutable por defecto.** `MutableList` solo si necesitas mutación local.
2. **`Set` para unicidad, `Map` para lookup.** Elige bien el tipo.
3. **Secuencias (`Sequence`) para pipelines largos** sobre datos grandes (>10k elementos).
4. **`associateBy` y `groupBy` son O(n).** Aprovechar para evitar loops manuales.
5. **No conviertas a List si vas a volver a convertir.** Piensa el pipeline completo.

## groupBy y particiones

### groupBy básico

```kotlin
val porDia: Map<Long, List<BitacoraEntry>> = entries.groupBy { inicioDelDia(it.fecha) }
val porAnimo: Map<String, List<BitacoraEntry>> = entries.groupBy { it.animo }
```

### groupBy con conteo

```kotlin
val conteo: Map<String, Int> = entries.groupingBy { it.animo }.eachCount()
```

### groupBy con operación personalizada

```kotlin
val promedioPorAnimo: Map<String, Double> = entries
    .groupBy { it.animo }
    .mapValues { (_, entries) -> entries.mapNotNull { it.suenoHoras }.average() }
```

### partition (separar en dos)

```kotlin
val (conConsumo, sinConsumo) = entries.partition { it.consumo }
```

### chunked (agrupar en bloques)

```kotlin
val enBloquesDe7 = entries.chunked(7)
// Útil para "promedio por semana"
```

### windowed (ventanas deslizantes)

```kotlin
val ventanasDe7 = entries.windowed(size = 7, step = 1, partialWindows = false)
// Cada ventana tiene 7 elementos consecutivos
```

## Secuencias para performance

### Cuándo usar Sequence

- Pipeline con múltiples operaciones (`filter → map → groupBy → count`).
- Dataset grande (>10k elementos).
- Cuando solo necesitas evaluar hasta encontrar algo.

### Pipeline con Sequence

```kotlin
val insights = entries.asSequence()
    .filter { it.suenoHoras != null }
    .filter { it.fecha >= inicioHace7Dias }
    .mapNotNull { it.suenoHoras }
    .average()
```

Secuencias son **lazy**: solo procesan lo necesario. Si encadenas 5 operaciones, cada elemento pasa por las 5 sin materializar listas intermedias.

### Comparación con List

```kotlin
// Con List: materializa cada paso
val resultado = entries
    .filter { it.suenoHoras != null }   // crea lista intermedia
    .filter { it.fecha >= limite }       // crea otra lista intermedia
    .mapNotNull { it.suenoHoras }        // crea otra lista
    .average()

// Con Sequence: lazy
val resultado = entries.asSequence()
    .filter { it.suenoHoras != null }
    .filter { it.fecha >= limite }
    .mapNotNull { it.suenoHoras }
    .average()
```

### Romper el flujo de Sequence

`first()` corta la evaluación apenas encuentra el primer match:

```kotlin
val primeroEnCrisis = entries.asSequence()
    .filter { it.animo == "crisis" }
    .filter { diasEntre(it.fecha) <= 7 }
    .firstOrNull()
```

`take(n)` toma los primeros N elementos.

## associate y map

### associateBy (lookup)

```kotlin
val porSlug: Map<String, Ejercicio> = ejercicios.associateBy { it.slug }
val ejercicio = porSlug["respiracion-4-7-8"]
```

### associateWith

```kotlin
val contador = palabras.associateWith { it.length }
```

### map vs mapNotNull

```kotlin
val ids: List<Long> = entries.map { it.id }                  // puede incluir nulos si tipo nullable
val ids: List<Long> = entries.mapNotNull { it.suenoHoras }  // filtra nulos automáticamente
```

### mapIndexed

```kotlin
val conIndice = entries.mapIndexed { i, entry -> "$i: ${entry.animo}" }
```

## Filtros encadenados vs composite

### Encadenado (legible pero a veces ineficiente)

```kotlin
entries
    .filter { it.consumo }
    .filter { it.sustancia == "alcohol" }
    .filter { diasEntre(it.fecha) <= 7 }
```

### Composite (más eficiente con `all` o predicado)

```kotlin
entries.filter { entry ->
    entry.consumo && 
    entry.sustancia == "alcohol" && 
    diasEntre(entry.fecha) <= 7
}
```

Si el predicado es corto y todas las condiciones son O(1), el composite es ligeramente más rápido.

## Reducciones

### sumOf, count, average, min, max

```kotlin
val totalSueno = entries.sumOf { it.suenoHoras ?: 0 }
val diasConSueno = entries.count { it.suenoHoras != null }
val promedioAnsiedad = entries.mapNotNull { it.nivelAnsiedad }.average()
```

### maxByOrNull, minByOrNull

```kotlin
val ultimaEntrada = entries.maxByOrNull { it.fecha }
val masTemprana = entries.minByOrNull { it.fecha }
```

### reduce y fold

```kotlin
val totalAcumulado = entries.fold(0.0) { acc, entry ->
    acc + (entry.suenoHoras ?: 0.0)
}
```

## Ordenamiento

### sortedBy / sortedByDescending

```kotlin
val recientes = entries.sortedByDescending { it.fecha }
```

### sortedWith con comparador custom

```kotlin
val porImportancia = entries.sortedWith(compareBy(
    { it.severidad },    // primero por severidad
    { -it.fecha }        // luego por fecha descendente
))
```

### distinct y distinctBy

```kotlin
val diasUnicos = entries.map { inicioDelDia(it.fecha) }.distinct()
val sustSinDup = entries.distinctBy { it.sustancia }
```

## Unión, intersección, diferencia

```kotlin
val consumosAlcohol = entries.filter { it.sustancia == "alcohol" }
val consumosCristal = entries.filter { it.sustancia == "cristal" }

val ambos = consumosAlcohol intersect consumosCristal       // ambos
val soloAlcohol = consumosAlcohol subtract consumosCristal // solo alcohol
val todo = consumosAlcohol union consumosCristal           // ambos combinados
```

## flatMap y flatten

```kotlin
data class BitacoraConSustancias(val entry: BitacoraEntry, val sustancias: List<String>)

val todasSustancias = listaDeBitacoras
    .flatMap { it.sustancias }   // List<List<String>> → List<String>
```

## Trucos útiles

### Conteo de elementos por valor

```kotlin
val frecuencias: Map<String, Int> = entries
    .map { it.animo }
    .groupingBy { it }
    .eachCount()
```

### Top N más frecuentes

```kotlin
val top3Animos = entries
    .groupingBy { it.animo }
    .eachCount()
    .entries
    .sortedByDescending { it.value }
    .take(3)
```

### Suma por categoría

```kotlin
val horasPorDia: Map<Long, Int> = entries
    .groupBy { inicioDelDia(it.fecha) }
    .mapValues { (_, list) -> list.sumOf { it.suenoHoras ?: 0 } }
```

### Cuántos días distintos se registraron

```kotlin
val diasDistintos = entries.map { inicioDelDia(it.fecha) }.distinct().size
```

## Pair, Triple y destructuring

```kotlin
val (consumos, noConsumos) = entries.partition { it.consumo }

val (fecha, animo, nota) = Triple(entry.fecha, entry.animo, entry.nota)
```

## Performance: List vs Sequence vs Array

| Caso | List | Sequence | Array |
|---|---|---|---|
| Pipeline corto (<3 ops) | OK | OK | OK |
| Pipeline largo (>3 ops) | Lento | **Rápido** | OK |
| Lookups por índice | O(1) | O(n) | **O(1)** |
| Dataset grande | Lento | **OK** | OK |
| Mutación | Sí | No | Sí |
| Inmutabilidad forzada | Sí | Sí | No |

Reglas generales:
- **List** es el default. Simple, claro, suficiente.
- **Sequence** cuando el dataset es >10k y el pipeline es >3 ops.
- **Array** solo si necesitas mutación de tamaño fijo o performance crítica.
- **Map** cuando necesitas lookup por clave.

## Edge cases y validaciones

```kotlin
// Lista vacía
emptyList<BitacoraEntry>().average() // NaN
emptyList<BitacoraEntry>().maxByOrNull { it.fecha } // null

// Validar antes de usar
val promedio = entries.mapNotNull { it.suenoHoras }.takeIf { it.isNotEmpty() }?.average()
```

## Anti-patrones prohibidos

1. **`for` loops cuando hay operador de Kotlin que lo cubre.** Preferir `filter`, `map`, etc.
2. **Materializar listas intermedias** en pipelines largos sin usar `Sequence`.
3. **`!!` después de `firstOrNull` o `maxByOrNull`.** Usar `?:` o `getOrNull`.
4. **`distinct()` sin pensar si ya es único.** A veces no hace falta.
5. **`groupBy` con clave calculada costosa** que se puede cachear.
6. **`MutableList` expuesta.** Prefiere inmutabilidad.
7. **Sort manual con loops.** Usa `sortedBy`, `sortedWith`.
8. **Comparar colecciones con `==` que recorre todos los elementos** si no es necesario.
9. **Uso indiscriminado de `parallelStream`/operadores paralelos** — Coroutines `Flow` ya es mejor opción en Android.