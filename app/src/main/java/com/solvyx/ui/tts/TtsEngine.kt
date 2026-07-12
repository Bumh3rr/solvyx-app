package com.solvyx.ui.tts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper reutilizable de Text-to-Speech para Solvyx.
 *
 * Centraliza la inicialización, configuración de voz, pitch y rate
 * de la misma forma en que `ChatViewModel` lo hace para Berto:
 *
 *  - Voz femenina es-MX (heurística: `female` o `esd` en el nombre).
 *    Fallback a cualquier voz en español.
 *  - `pitch = 1.15f`, `speechRate = 0.85f`.
 *
 * Esta clase fue extraída de un patrón in-line usado en
 * `ChatViewModel.kt` y `EjercicioGuiadoViewModel.kt` para que los
 * nuevos ejercicios no dupliquen esa lógica. Es un `@Singleton`
 * compartido por toda la app: **vive lo que viva el proceso** y se
 * reusa entre ejercicios y pantallas.
 *
 * ## Reglas de uso (operativas)
 *
 *  1. **Inicialización lazy.** El `TextToSpeech` se construye la
 *     primera vez que se llama [initialize]. Llamarlo más de una vez
 *     es no-op. Los ViewModels lo invocan desde su `init {}`.
 *  2. **Reutilización.** NO crear nuevas instancias de `TextToSpeech`
 *     por ejercicio: reusar este singleton.
 *  3. **Cleanup de pantalla.** La pantalla, en `onDispose`, debe
 *     invocar [stop] para silenciar la utterance actual. NO invocar
 *     [shutdown] en un `DisposableEffect` porque esto podría romper
 *     otro consumidor del engine.
 *  4. **Cleanup de proceso.** Solo `SolvyxApp` (cuando lo amerite)
 *     debe invocar [shutdown] durante el teardown.
 *  5. **Mute persistente.** Si la app quiere recordar el estado
 *     muteado, debe hacerlo fuera (DataStore) y aplicar la
 *     preferencia al iniciar con [toggleMute] según convenga.
 *  6. **Foreground only.** El TTS no debe pedirse en background.
 *
 * ## Hilt
 *
 * Como es `@Singleton @Inject constructor`, Hilt lo provee
 * automáticamente sin un módulo explícito en
 * `SingletonComponent`. Está disponible para cualquier
 * `@HiltViewModel` con sólo declarar la dependencia.
 *
 * @see com.solvyx.ui.screens.chatbot.ChatViewModel implementación previa in-line.
 * @see com.solvyx.ui.screens.guias.screens.panico.EjercicioGuiadoViewModel otra implementación previa.
 */
@Singleton
class TtsEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Estado público ───────────────────────────────────────────────────

    private val _isSpeaking = MutableStateFlow(false)
    /** true entre `onStart` y `onDone`/`onError` de la utterance actual. */
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    /** true tras un callback de inicialización exitoso. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    // ── Estado interno ──────────────────────────────────────────────────

    private var tts: TextToSpeech? = null
    private var isMuted: Boolean = false

    /** Callbacks por utterance. Se limpian en `onDone`/`onError`. */
    private val pendingUtterances =
        mutableMapOf<String, Triple<(() -> Unit)?, (() -> Unit)?, ((String) -> Unit)?>>()

    /** Handler del main thread para postear callbacks del TTS listener
     *  (que llegan en hilos de background). */
    private val mainHandler = Handler(Looper.getMainLooper())

    // ── API pública ─────────────────────────────────────────────────────

    /**
     * Inicializa el motor de TTS. Es idempotente.
     *
     * Llamar desde el `init {}` del ViewModel. La construcción del
     * `TextToSpeech` debe hacerse en el hilo principal, pero el
     * callback `OnInitListener` puede llegar en background, así que
     * las mutaciones de estado se postean al main looper.
     */
    @Synchronized
    fun initialize() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                // TTS engine no disponible (idioma no instalado, error
                // del sistema, etc.). El consumidor debería consultar
                // [isReady] para detectar el fallo y continuar sin voz.
                mainHandler.post { _isReady.value = false }
                return@TextToSpeech
            }
            configureVoice()
            mainHandler.post { _isReady.value = true }
        }
    }

    /**
     * Reproduce [text] usando la voz configurada.
     *
     * Si el motor no está listo o está muteado, **no reproduce** pero
     * no bloquea el flujo del llamador (devuelve inmediatamente).
     *
     * @param text        texto a hablar (puede tener saltos de línea;
     *                    se normalizan a punto-y-espacio).
     * @param utteranceId id que identifica esta utterance (para
     *                    correlacionar `onDone`/`onError`). Si no se
     *                    pasa, se autogenera.
     * @param onStart     callback cuando el motor empieza a hablar.
     * @param onDone      callback cuando termina exitosamente.
     * @param onError     callback cuando falla (recibe utteranceId).
     */
    fun speak(
        text: String,
        utteranceId: String = UUID.randomUUID().toString(),
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 1. Si está silenciado o no listo, no reproducimos
        if (isMuted) return
        val engine = tts
        if (engine == null || !_isReady.value) {
            // Respetamos el contrato: si no hay motor, simulamos onStart
            // solo si el llamador lo pidió, pero no bloqueamos.
            onStart?.invoke()
            onDone?.invoke()
            return
        }

        // 2. Normalización de texto: el TTS lee mejor sin espacios
        //    dobles ni saltos múltiples.
        val clean = text.trim()
            .replace(Regex("\n+"), ". ")
            .replace(Regex(" +"), " ")

        // 3. Registrar callbacks para cuando termine
        synchronized(pendingUtterances) {
            pendingUtterances[utteranceId] = Triple(onStart, onDone, onError)
        }

        // 4. Llamada real a TTS. `QUEUE_FLUSH` interrumpe lo que esté
        //    sonando, que es el comportamiento esperado cuando el
        //    ejercicio avanza de fase.
        engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        // 5. Disparar onStart inmediatamente — el listener
        //    `onStart(utteranceId)` también lo disparará, pero esto
        //    da una respuesta más inmediata al consumidor.
        onStart?.invoke()
    }

    /**
     * Detiene cualquier utterance en curso. Idempotente.
     *
     * No libera el engine: para eso es [shutdown]. Esta función debe
     * llamarse en `onDispose` de la pantalla para no dejar audio
     * sonando al salir.
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
            // algunos OEMs lanzan si TTS no está inicializado
        }
        _isSpeaking.value = false
    }

    /**
     * Alterna el estado de mute. Al mutear, también detiene cualquier
     * utterance en curso.
     */
    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) stop()
        return isMuted
    }

    /** @return `true` si el motor está actualmente silenciado. */
    fun isMuted(): Boolean = isMuted

    /**
     * Setea el estado de mute sin pasar por el toggle. Útil cuando se
     * restaura la preferencia desde DataStore al iniciar.
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (isMuted) stop()
    }

    /**
     * Apaga el engine por completo. **Solo debe llamarse en el
     * teardown del proceso** (p.ej. nunca desde un `DisposableEffect`
     * de una pantalla individual).
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
            // ignore
        }
        tts = null
        synchronized(pendingUtterances) { pendingUtterances.clear() }
        _isReady.value = false
        _isSpeaking.value = false
    }

    // ── Privados ────────────────────────────────────────────────────────

    /**
     * Configura voz, pitch y rate. Llamado desde el callback de
     * inicialización.
     *
     * Política de voz: igual que Berto, intentamos primero una voz
     * femenina en español (`female` o `esd` en el nombre) y caemos a
     * cualquier voz en español. Si no hay ninguna, dejamos la que el
     * sistema haya elegido por defecto.
     */
    private fun configureVoice() {
        val engine = tts ?: return

        val candidates = engine.voices.orEmpty()
        val spanishVoice = candidates.firstOrNull { v ->
            v.locale.language == ES_LANGUAGE &&
                (v.name.contains("female", ignoreCase = true) ||
                    v.name.contains("esd", ignoreCase = true))
        } ?: candidates.firstOrNull { v ->
            v.locale.language == ES_LANGUAGE
        }
        spanishVoice?.let { engine.voice = it }

        // Misma cadencia que Berto / EjercicioGuiadoViewModel.
        engine.setPitch(PITCH)
        engine.setSpeechRate(RATE)

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                mainHandler.post { _isSpeaking.value = true }
            }

            override fun onDone(id: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    if (id != null) {
                        val (_, onDone, _) = synchronized(pendingUtterances) {
                            pendingUtterances.remove(id) ?: Triple(null, null, null)
                        }
                        onDone?.invoke()
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    if (id != null) {
                        val (_, _, onError) = synchronized(pendingUtterances) {
                            pendingUtterances.remove(id) ?: Triple(null, null, null)
                        }
                        onError?.invoke(id)
                    }
                }
            }

            // API moderna (28+): onError(utteranceId, errorCode).
            override fun onError(utteranceId: String?, errorCode: Int) {
                @Suppress("DEPRECATION")
                super.onError(utteranceId, errorCode)
                mainHandler.post {
                    _isSpeaking.value = false
                    if (utteranceId != null) {
                        val (_, _, onError) = synchronized(pendingUtterances) {
                            pendingUtterances.remove(utteranceId) ?: Triple(null, null, null)
                        }
                        onError?.invoke(utteranceId)
                    }
                }
            }
        })
    }

    companion object {
        /** Igual que Berto: voz un poco más aguda. */
        private const val PITCH = 1.15f

        /** Igual que Berto: ritmo pausado y cálido. */
        private const val RATE = 0.85f

        /** BCP-47 `language` short code para español. Evita `Locale("es")` deprecado. */
        private const val ES_LANGUAGE = "es"
    }
}
