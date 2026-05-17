package com.solvyx.ui.screens.guias.screens.panico

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EjercicioGuiadoViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private val TTS_SCRIPTS = mapOf(
            -1 to R.string.tts_intro,
            0  to R.string.tts_step_ver,
            1  to R.string.tts_step_tocar,
            2  to R.string.tts_step_escuchar,
            3  to R.string.tts_step_oler,
            4  to R.string.tts_step_saborear,
            5  to R.string.tts_completion
        )
        private const val SPEAK_DELAY_MS = 250L
    }

    private var tts: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speakJob: Job? = null
    private var pendingResId: Int? = null

    var isTtsReady by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    var step by mutableStateOf(-1)
        private set
    var tapped by mutableStateOf(setOf<Int>())
        private set

    init {
        // Se inicializa en cuanto el ViewModel existe — antes de que el usuario abra el ejercicio
        initTts()
    }

    // ── TTS ──────────────────────────────────────────────────────────────────

    private fun initTts() {
        // TextToSpeech debe construirse en el hilo principal; el ViewModel init cumple eso
        tts = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            val femaleVoice = tts?.voices?.firstOrNull { v ->
                v.locale.language == "es" &&
                (v.name.contains("female", ignoreCase = true) ||
                 v.name.contains("esd", ignoreCase = true))
            } ?: tts?.voices?.firstOrNull { v -> v.locale.language == "es" }

            femaleVoice?.let { tts?.voice = it }
            tts?.setPitch(1.15f)
            tts?.setSpeechRate(0.85f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                // Los callbacks del listener llegan en hilo de fondo — se despachan al Main
                override fun onStart(id: String?) { mainHandler.post { isSpeaking = true } }
                override fun onDone(id: String?) { mainHandler.post { isSpeaking = false } }
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) { mainHandler.post { isSpeaking = false } }
            })
            mainHandler.post {
                isTtsReady = true
                // Si hubo un intento de hablar mientras TTS inicializaba, lo ejecutamos ahora
                pendingResId?.let { resId ->
                    pendingResId = null
                    speakText(appContext.getString(resId))
                }
            }
        }
    }

    private fun speakText(text: String) {
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ex_tts")
    }

    private fun scheduleSpeak(targetStep: Int) {
        speakJob?.cancel()
        speakJob = viewModelScope.launch {
            delay(SPEAK_DELAY_MS)
            val resId = TTS_SCRIPTS[targetStep] ?: return@launch
            if (isTtsReady) {
                speakText(appContext.getString(resId))
            } else {
                // TTS aún cargando — guardamos para hablar en cuanto esté listo
                pendingResId = resId
            }
        }
    }

    fun stopSpeaking() {
        speakJob?.cancel()
        tts?.stop()
        isSpeaking = false
    }

    // ── Estado del ejercicio ─────────────────────────────────────────────────

    fun startExercise() {
        step = -1
        tapped = setOf()
        scheduleSpeak(-1)
    }

    fun nextStep() {
        val next = step + 1
        tapped = setOf()
        step = next
        scheduleSpeak(next)
    }

    fun tapBubble(index: Int) {
        tapped = tapped + index
    }

    // ── Ciclo de vida ────────────────────────────────────────────────────────
    override fun onCleared() {
        speakJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
