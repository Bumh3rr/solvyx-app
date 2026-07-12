---
description: Configuración de pitch y speech rate para TTS de Solvyx. Valores por defecto, ajustes por contexto, sincronización con animaciones.
---

# Skill: TTS Pacing

Esta skill te entrega las convenciones para configurar pitch, rate y pacing del TTS en Solvyx. Aplícala al implementar ejercicios guiados, meditaciones, o cualquier texto hablado.

## Principios

1. **Pitch y rate por defecto del proyecto:** 1.15 pitch, 0.85 rate.
2. **Variar pitch/rate por contexto.** Ejercicios de respiración son más lentos; notificaciones son más rápidas.
3. **Ajustar pitch/rate al inicio del ejercicio,** no durante.
4. **Sincronizar TTS con animaciones de UI** para coherencia.
5. **Ofrecer al usuario control** de velocidad (lenta / normal / rápida).

## Pitch y rate base

### Definición

| Parámetro | Default Solvyx | Rango | Significado |
|---|---|---|---|
| `pitch` | 1.15f | 0.5 - 2.0 | Tono de voz. 1.0 = normal. |
| `speechRate` | 0.85f | 0.1 - 2.0 | Velocidad. 1.0 = normal. |

### Configuración

```kotlin
tts?.setPitch(1.15f)
tts?.setSpeechRate(0.85f)
```

Estos valores se usan en Berto y deben mantenerse consistentes.

## Configuraciones por contexto

### Ejercicios de respiración

Más lento y grave para inducir calma.

| Parámetro | Valor |
|---|---|
| `pitch` | 1.0f |
| `speechRate` | 0.75f |

```kotlin
fun configureParaRespiracion() {
    tts?.setPitch(1.0f)
    tts?.setSpeechRate(0.75f)
}
```

### Body scan

Velocidad moderada, más grave para intimidad.

| Parámetro | Valor |
|---|---|
| `pitch` | 1.1f |
| `speechRate` | 0.8f |

### Meditación / lugar seguro

Lento e íntimo.

| Parámetro | Valor |
|---|---|
| `pitch` | 1.05f |
| `speechRate` | 0.7f |

### Activación conductual

Más rápido, animado.

| Parámetro | Valor |
|---|---|
| `pitch` | 1.15f |
| `speechRate` | 0.9f |

### Notificaciones (cortas)

Velocidad normal.

| Parámetro | Valor |
|---|---|
| `pitch` | 1.15f |
| `speechRate` | 0.95f |

### Tabla resumen

| Contexto | Pitch | Rate |
|---|---|---|
| Berto (default) | 1.15 | 0.85 |
| Respiración | 1.0 | 0.75 |
| Body scan | 1.1 | 0.8 |
| Meditación | 1.05 | 0.7 |
| Activación conductual | 1.15 | 0.9 |
| Notificación | 1.15 | 0.95 |

## Preferencia del usuario

Ofrecer control de velocidad:

```kotlin
enum class TtsSpeed(val rate: Float) {
    LENTA(0.7f),
    NORMAL(0.85f),
    RAPIDA(1.0f)
}

class TtsEngine {
    fun setSpeed(speed: TtsSpeed) {
        tts?.setSpeechRate(speed.rate)
    }
}
```

En Mi Perfil:

```kotlin
@Composable
fun TtsSpeedSelector(
    actual: TtsSpeed,
    onChange: (TtsSpeed) -> Unit
) {
    Column {
        Text("Velocidad de la voz", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TtsSpeed.entries.forEach { speed ->
                FilterChip(
                    selected = speed == actual,
                    onClick = { onChange(speed) },
                    label = { Text(speed.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
    }
}
```

## Sincronización TTS con animaciones

### Problema

El TTS tarda un tiempo variable en hablar. La animación debe coincidir con el TTS, no con un timer fijo.

### Solución: usar `onStart`/`onDone`

```kotlin
class EjercicioRespiracionViewModel @Inject constructor(/* ... */) : ViewModel() {
    
    private val _fase = MutableStateFlow<Fase>(Fase.PREPARACION)
    val fase: StateFlow<Fase> = _fase.asStateFlow()
    
    fun iniciarCiclo() {
        viewModelScope.launch {
            // Inhala
            _fase.value = Fase.INHALAR
            ttsEngine.speakAndAwait("Inhala en 4 tiempos")
            // La animación de inhalar ya habrá terminado con este await
            
            // Sostener
            _fase.value = Fase.SOSTENER
            ttsEngine.speakAndAwait("Sostén 7 tiempos")
            
            // Exhalar
            _fase.value = Fase.EXHALAR
            ttsEngine.speakAndAwait("Exhala en 8 tiempos")
        }
    }
}
```

### Animación que coincide con TTS

```kotlin
@Composable
fun BreathingCircle(fase: Fase, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = when (fase) {
            Fase.INHALAR -> 1.0f
            Fase.SOSTENER -> 1.0f
            Fase.EXHALAR -> 0.5f
            else -> 0.7f
        },
        animationSpec = when (fase) {
            Fase.INHALAR -> tween(durationMillis = 4000, easing = LinearEasing)
            Fase.EXHALAR -> tween(durationMillis = 8000, easing = LinearEasing)
            else -> tween(durationMillis = 100)
        },
        label = "breathing_scale"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}
```

La animación arranca cuando `fase` cambia (porque `animateFloatAsState` se dispara). El TTS habla durante la animación. Cuando el TTS termina, cambiamos a la siguiente fase.

### Alternativa: timer fijo (menos preciso)

Si necesitas un timer exacto sin esperar al TTS:

```kotlin
suspend fun ejecutarFase(fase: Fase, copy: String, duracionMs: Long) {
    _fase.value = fase
    ttsEngine.speak(copy)
    delay(duracionMs)
}
```

Esto es menos natural (puede desincronizarse) pero más predecible.

## Sincronización con progreso (ProgressIndicator)

```kotlin
@Composable
fun EjercicioConProgreso(
    duracionTotalSeg: Int,
    onFinalizar: () -> Unit
) {
    var progreso by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (progreso < 1f) {
            val elapsed = System.currentTimeMillis() - startTime
            progreso = (elapsed / (duracionTotalSeg * 1000f)).coerceIn(0f, 1f)
            delay(100)
        }
        onFinalizar()
    }
    
    LinearProgressIndicator(
        progress = { progreso },
        modifier = Modifier.fillMaxWidth()
    )
}
```

## Casos edge

### TTS termina antes que la animación

Si el TTS termina antes que la animaciónتهاء (texto muy corto, rate rápido):

- **Aceptar:** la animación continúa hasta su duración natural.
- **Siguiente fase:** esperar a que termine la animación antes de pasar.

```kotlin
suspend fun ejecutarFase(fase: Fase, copy: String, duracionMs: Long) {
    _fase.value = fase
    ttsEngine.speak(copy)
    // Esperar lo que TTS o la animación tarde, lo que sea mayor
    val ttsJob = async { ttsEngine.speakAndAwait(copy) }
    val animJob = async { delay(duracionMs) }
    awaitAll(ttsJob, animJob)
}
```

### TTS termina después que la animación

Si el TTS dura más que la animación planeada (rate lento):

- **Recortar:** pasar a la siguiente fase cuando termine la animación, no esperar TTS.
- **O ajustar rate** según duración de la animación.

```kotlin
fun speak(text: String, targetDurationMs: Long) {
    val chars = text.length
    val charsPerSec = (chars / (targetDurationMs / 1000f)).coerceAtLeast(8f)  // mínimo 8 chars/sec
    
    // Calcular rate inversamente proporcional
    val rate = (1.0f / charsPerSec * 15f).coerceIn(0.5f, 1.5f)
    
    tts?.setSpeechRate(rate)
    tts?.speak(text, QUEUE_FLUSH, null, null)
}
```

Esta es una simplificación. Para cálculos más precisos, usa la duración estimada del TTS engine (no siempre exacta).

## Anti-patrones prohibidos

1. **Valores extremos de pitch (>2.0 o <0.5).** Distorsiona.
2. **Cambiar pitch/rate durante una utterance.** Aplicar antes de `speak()`.
3. **Timer fijo + TTS sin sincronización.** Desincronización evidente.
4. **Sin alternativa a TTS.** Ofrecer botón "Silenciar".
5. **Pitch/rate fijo para todos los contextos.** Ajustar por uso.
6. **Olvidar reset al volver a Berto.** Restaurar defaults.
7. **TTS con pitch/rate que se contradice con la animación.** Coherencia.