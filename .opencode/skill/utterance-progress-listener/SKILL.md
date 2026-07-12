---
description: UtteranceProgressListener para TTS de Solvyx. onStart, onDone, onError, utteranceId, callbacks tipados.
---

# Skill: Utterance Progress Listener

Esta skill te entrega los patrones para usar `UtteranceProgressListener` en TTS de Solvyx. Aplícala cuando necesites reaccionar al inicio, fin o error de una utterance específica.

## Principios

1. **`utteranceId` único por llamada.** Permite correlacionar callbacks con `speak()`.
2. **`onStart`, `onDone`, `onError`** se llaman en el thread del listener. Usar `Handler` para UI.
3. **Manejar `onError` siempre.** No asumir éxito.
4. **Idempotencia:** un `onDone` por `speak()` (no más).
5. **Cleanup del listener** al destruir el engine.

## API básica

```kotlin
class TtsEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureVoice()
                setListener()
                onReady()
            }
        }
    }
    
    private fun setListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post { 
                    isSpeaking = true
                    callbacks[utteranceId]?.onStart?.invoke()
                }
            }
            
            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    isSpeaking = false
                    callbacks[utteranceId]?.onDone?.invoke()
                    callbacks.remove(utteranceId)
                }
            }
            
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    isSpeaking = false
                    callbacks[utteranceId]?.onError?.invoke("TTS error")
                    callbacks.remove(utteranceId)
                }
            }
            
            // onError con más detalle (API 21+)
            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                mainHandler.post {
                    isSpeaking = false
                    callbacks[utteranceId]?.onError?.invoke("TTS error code: $errorCode")
                    callbacks.remove(utteranceId)
                }
            }
        })
    }
    
    private data class UtteranceCallbacks(
        val onStart: (() -> Unit)? = null,
        val onDone: (() -> Unit)? = null,
        val onError: ((String) -> Unit)? = null
    )
    
    private val callbacks = mutableMapOf<String, UtteranceCallbacks>()
    
    fun speak(
        text: String,
        utteranceId: String = UUID.randomUUID().toString(),
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        callbacks[utteranceId] = UtteranceCallbacks(onStart, onDone, onError)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
}
```

## Uso típico

### Esperar a que termine de hablar

```kotlin
suspend fun TtsEngine.speakAndAwait(
    text: String,
    utteranceId: String = UUID.randomUUID().toString()
) = suspendCancellableCoroutine<Unit> { cont ->
    speak(
        text = text,
        utteranceId = utteranceId,
        onDone = { if (cont.isActive) cont.resume(Unit) },
        onError = { if (cont.isActive) cont.resume(Unit) }
    )
    
    cont.invokeOnCancellation {
        stop()
    }
}
```

Uso:

```kotlin
viewModelScope.launch {
    ttsEngine.speakAndAwait("Inhala por la nariz en 4 tiempos")
    delay(4000)  // esperar la fase
    ttsEngine.speakAndAwait("Sostén 7 tiempos")
    delay(7000)
    ttsEngine.speakAndAwait("Exhala por la boca en 8 tiempos")
}
```

### Reaccionar a múltiples utterances encadenadas

```kotlin
class EjercicioViewModel @Inject constructor(/* ... */) : ViewModel() {
    
    fun iniciarEjercicio() {
        viewModelScope.launch {
            val utterance1 = "intro_${UUID.randomUUID()}"
            ttsEngine.speak(
                text = "Vamos a hacer 4 ciclos de respiración 4-7-8.",
                utteranceId = utterance1,
                onDone = {
                    // Al terminar intro, empezar el ciclo
                    iniciarCiclo()
                }
            )
        }
    }
    
    private fun iniciarCiclo() {
        viewModelScope.launch {
            repeat(4) {
                ttsEngine.speakAndAwait("Inhala en 4 tiempos")
                delay(4000)
                ttsEngine.speakAndAwait("Sostén 7 tiempos")
                delay(7000)
                ttsEngine.speakAndAwait("Exhala en 8 tiempos")
                delay(8000)
            }
        }
    }
}
```

## Callbacks tipados con Flow

Para integración reactiva con Compose:

```kotlin
class TtsEngine(/* ... */) {
    
    private val _events = MutableSharedFlow<TtsEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<TtsEvent> = _events.asSharedFlow()
    
    sealed interface TtsEvent {
        data class Started(val utteranceId: String) : TtsEvent
        data class Finished(val utteranceId: String) : TtsEvent
        data class Error(val utteranceId: String, val code: Int) : TtsEvent
    }
    
    private fun setListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.let {
                    mainHandler.post { _events.tryEmit(TtsEvent.Started(it)) }
                }
            }
            
            override fun onDone(utteranceId: String?) {
                utteranceId?.let {
                    mainHandler.post { _events.tryEmit(TtsEvent.Finished(it)) }
                }
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                utteranceId?.let {
                    mainHandler.post { _events.tryEmit(TtsEvent.Error(it, errorCode)) }
                }
            }
        })
    }
}
```

En el VM:

```kotlin
init {
    viewModelScope.launch {
        ttsEngine.events.collect { event ->
            when (event) {
                is TtsEvent.Started -> _uiState.update { it.copy(isSpeaking = true) }
                is TtsEvent.Finished -> _uiState.update { it.copy(isSpeaking = false) }
                is TtsEvent.Error -> _uiState.update { 
                    it.copy(isSpeaking = false, errorTts = "Error de audio: ${event.code}")
                }
            }
        }
    }
}
```

## Errores y códigos

```kotlin
private val errorMessages = mapOf(
    TextToSpeech.ERROR_SUCCESS to "Sin error",
    TextToSpeech.ERROR_NETWORK to "Error de red",
    TextToSpeech.ERROR_NETWORK_TIMEOUT to "Timeout de red",
    TextToSpeech.ERROR_INVALID_PARAMETER to "Parámetro inválido",
    TextToSpeech.ERROR_SERVICE to "Error del servicio TTS",
    TextToSpeech.ERROR_OUTPUT to "Error de salida de audio",
    TextToSpeech.ERROR_SYNTHESIS to "Error de síntesis",
    TextToSpeech.ERROR_CANCELLED to "Cancelado",
    TextToSpeech.ERROR_INSTALLATION_INCOMPLETE to "Instalación incompleta",
    TextToSpeech.ERROR_NOT_YET_INSTALLED to "TTS no instalado",
    TextToSpeech.ERROR_LANGUAGE_NOT_SUPPORTED to "Idioma no soportado",
    TextToSpeech.ERROR_VOICE_DATA_NOT_INSTALLED to "Datos de voz no instalados",
    TextToSpeech.ERROR_TOO_MANY_REQUESTS to "Demasiadas solicitudes",
    TextToSpeech.ERROR_INTERRUPTED to "Interrumpido"
)

fun errorMessage(code: Int): String = errorMessages[code] ?: "Error desconocido ($code)"
```

## Cancelar utterance

```kotlin
fun cancelUtterance(utteranceId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        tts?.stopUtterance(utteranceId)
    } else {
        // No hay API para utteranceId específico antes de 33
        // Workaround: stop() y crear nueva utterance para reanudar
        tts?.stop()
    }
}
```

## Cancelar todas las utterances

```kotlin
fun stopAll() {
    tts?.stop()
    // Limpiar callbacks pendientes
    callbacks.clear()
    _events.tryEmit(TtsEvent.Finished("cancelled_all"))
}
```

## Errores comunes

| Error | Solución |
|---|---|
| `onStart`/`onDone` no se llaman | Verificar que `utteranceId` no es null y se pasa correctamente. |
| Callbacks en thread incorrecto | Usar `Handler(Looper.getMainLooper()).post { ... }`. |
| Memory leak por callbacks | Limpiar callbacks en `onDone` o `onError`. |
| Callback no se ejecuta tras `stop()` | Es esperado. `onDone` no se llama si se cancela. |
| `setOnUtteranceProgressListener` deprecated en API 21+ | Usar la versión con `errorCode`. |

## Threading

`UtteranceProgressListener` se llama en un thread del TTS engine, **no en main**. Para UI:

```kotlin
private val mainHandler = Handler(Looper.getMainLooper())

override fun onStart(utteranceId: String?) {
    mainHandler.post {
        // código de UI aquí
    }
}
```

Para coroutines:

```kotlin
private val ttsScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

override fun onStart(utteranceId: String?) {
    ttsScope.launch {
        // código de UI aquí
    }
}
```

## Anti-patrones prohibidos

1. **Modificar UI directo** desde el listener (thread incorrecto).
2. **No limpiar `callbacks`** después de `onDone` o `onError`.
3. **Asumir que `onDone` siempre se llama.** Puede no llamarse si se cancela.
4. **Pasar `utteranceId` null.** Pierdes correlación.
5. **Listener global sin cleanup.** Memory leak.
6. **Múltiples listeners** sin remover el anterior.
7. **`setOnUtteranceProgressListener`** API deprecated en versiones modernas sin usar la sobrecarga correcta.