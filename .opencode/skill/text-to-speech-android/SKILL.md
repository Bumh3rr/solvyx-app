---
description: API TextToSpeech de Android para Solvyx. Inicialización, voces, lifecycle, errores, fallback, cleanup.
---

# Skill: Text-to-Speech Android

Esta skill te entrega el conocimiento de la API TextToSpeech de Android para Solvyx. Aplícala al implementar o mantener TTS en cualquier parte del proyecto.

## Principios

1. **Inicialización asíncrona.** `onInit` puede tardar varios segundos.
2. **Cleanup explícito** en `onCleared()` o `onDispose()`.
3. **Configurar voz antes del primer `speak()`.**
4. **Verificar disponibilidad** de idioma antes de usar.
5. **Fallback a voz por defecto** si no hay voz en español.
6. **Idempotencia:** llamar `speak()` múltiples veces con el mismo ID no causa duplicados.

## Inicialización

```kotlin
class TtsEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureVoice()
                isInitialized = true
                onReady()
            } else {
                Log.e("TtsEngine", "Initialization failed: $status")
            }
        }
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
    
    private fun configureVoice() {
        val voice = selectSpanishVoice()
        voice?.let { tts?.voice = it }
        
        tts?.setPitch(1.0f)
        tts?.setSpeechRate(0.85f)
    }
    
    private fun selectSpanishVoice(): Voice? {
        val voices = tts?.voices ?: return null
        
        // Prioridad 1: voz femenina es-MX
        voices.firstOrNull {
            it.locale.language == "es" &&
            it.locale.country == "MX" &&
            it.name.contains("female", ignoreCase = true)
        }?.let { return it }
        
        // Prioridad 2: voz es-MX cualquiera
        voices.firstOrNull {
            it.locale.language == "es" && it.locale.country == "MX"
        }?.let { return it }
        
        // Prioridad 3: voz es cualquiera
        voices.firstOrNull { it.locale.language == "es" }?.let { return it }
        
        // Prioridad 4: cualquier voz que no sea inglés (fallback)
        voices.firstOrNull { it.locale.language != "en" }
        
        return null
    }
}
```

## API principal

### `speak(text, queueMode, params, utteranceId)`

```kotlin
fun speak(
    text: String,
    queueMode: Int = TextToSpeech.QUEUE_FLUSH,
    utteranceId: String = UUID.randomUUID().toString()
) {
    if (!isInitialized || tts == null) return
    
    val clean = text.trim()
        .replace(Regex("\n+"), ". ")
        .replace(Regex(" +"), " ")
    
    tts?.speak(clean, queueMode, null, utteranceId)
}
```

**Modos de cola:**
- `QUEUE_FLUSH` (0): reemplaza la cola actual.
- `QUEUE_ADD` (1): agrega al final de la cola.
- `QUEUE_ADD_DONE` (2): agrega cuando termine el actual.

### `stop()`

Detiene la reproducción inmediatamente:

```kotlin
fun stop() {
    tts?.stop()
    tts?.speak("", TextToSpeech.QUEUE_FLUSH, null, "stop_marker")
}
```

### `isSpeaking`

```kotlin
val speaking = tts?.isSpeaking ?: false
```

## Configuración de voz

### Idioma

```kotlin
val result = tts?.setLanguage(Locale("es", "MX"))
when (result) {
    TextToSpeech.LANG_MISSING_DATA -> Log.w("TTS", "Falta data del idioma")
    TextToSpeech.LANG_NOT_SUPPORTED -> Log.w("TTS", "Idioma no soportado")
    else -> { /* OK */ }
}
```

### Pitch y rate

```kotlin
tts?.setPitch(1.0f)     // 1.0 = normal, 0.5 = grave, 2.0 = agudo
tts?.setSpeechRate(1.0f) // 1.0 = normal, 0.5 = lento, 2.0 = rápido
```

### Voice específica

```kotlin
val voice = tts?.voices?.firstOrNull { /* ... */ }
tts?.voice = voice
```

## Lifecycle

### En ViewModel

```kotlin
@HiltViewModel
class MiViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val ttsEngine = TtsEngine(context)
    
    init {
        ttsEngine.initialize { /* on ready */ }
    }
    
    override fun onCleared() {
        ttsEngine.shutdown()
        super.onCleared()
    }
}
```

### En Composable con DisposableEffect

```kotlin
@Composable
fun PantallaConTts(viewModel: PantallaViewModel = hiltViewModel()) {
    DisposableEffect(Unit) {
        viewModel.inicializarTts()
        
        onDispose {
            viewModel.shutdownTts()
        }
    }
    
    // ...
}
```

## Strings TTS en strings.xml

```xml
<string name="tts_ejercicio_intro">Bienvenido/a al ejercicio de respiración.</string>
<string name="tts_respiracion_inhala">Inhala por la nariz en 4 tiempos.</string>
<string name="tts_respiracion_sostener">Sostén 7 tiempos.</string>
<string name="tts_respiracion_exhalar">Exhala por la boca en 8 tiempos.</string>
<string name="tts_ejercicio_cierre">Excelente. Tu cuerpo se está calmando.</string>
```

```kotlin
fun speakEjercicioIntro(context: Context) {
    tts.speak(context.getString(R.string.tts_ejercicio_intro), QUEUE_FLUSH, "intro")
}
```

## Audio Focus

Solicitar audio focus antes de hablar:

```kotlin
class TtsEngine(private val context: Context) {
    private val audioManager = context.getSystemService<AudioManager>()
    
    private val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stop()
        }
    }
    
    fun requestFocus(): Boolean {
        val result = audioManager?.requestAudioFocus(
            focusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    
    fun abandonFocus() {
        audioManager?.abandonAudioFocus(focusListener)
    }
}
```

## Permisos y verificación

### Verificar que TTS está instalado

```kotlin
fun isTtsAvailable(context: Context): Boolean {
    val pm = context.packageManager
    return pm.resolveActivity(
        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
        PackageManager.MATCH_DEFAULT_ONLY
    ) != null
}
```

### Verificar idioma instalado

```kotlin
fun isSpanishAvailable(tts: TextToSpeech): Boolean {
    return tts.isLanguageAvailable(Locale("es", "MX")) >= TextToSpeech.LANG_AVAILABLE
}
```

## Errores comunes

| Error | Causa | Solución |
|---|---|---|
| `onInit` con `ERROR` | Engine TTS no instalado o falla | Mostrar mensaje al usuario, sugerir instalar Google TTS. |
| `LANG_NOT_SUPPORTED` | Idioma no instalado | Verificar `isLanguageAvailable` antes de configurar. |
| `LANG_MISSING_DATA` | Datos del idioma no descargados | Prompt al usuario para descargar. |
| Audio no se reproduce | Audio focus no granted | Solicitar focus antes de `speak`. |
| TTS sigue hablando tras salir de pantalla | Sin cleanup | Implementar `onDispose` o `onCleared`. |
| Voice null al configurar | No hay voces en español | Fallback a voz por defecto del sistema. |

## Anti-patrones prohibidos

1. **`TextToSpeech` instanciado en cada composable.** Singleton o VM.
2. **Sin cleanup.** Memory leak.
3. **`speak()` sin verificar `isInitialized`.** Crash.
4. **Pitch/rate extremos (>2.0 o <0.5).** Distorsiona la voz.
5. **Strings hardcoded.** Usar `strings.xml`.
6. **TTS en background.** Solo foreground.
7. **Bloquear UI durante TTS.** Es asíncrono.
8. **No verificar `isLanguageAvailable`.** Asumir que está disponible.