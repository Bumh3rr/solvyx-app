package com.solvyx.ui.screens.chatbot

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val UTTERANCE_ID = "berto_tts"
private const val VOICE_PITCH = 1.15f
private const val VOICE_RATE = 0.85f
private val LineBreaks = Regex("\n+")
private val RepeatedSpaces = Regex(" +")

/**
 * Berto's spoken voice. Text said before the engine finishes starting is kept and spoken once it
 * is ready. Markdown is stripped first so the engine does not read "asterisco".
 */
class BertoVoice(context: Context) {

    var isMuted by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set

    private var isReady = false
    private var pendingText: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) configure()
        }
    }

    fun speak(text: String) {
        if (isMuted) return
        if (isReady) doSpeak(text) else pendingText = text
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) stop()
    }

    fun shutdown() {
        // Best-effort: if the user backs out fast (before the async TextToSpeech engine
        // finishes connecting, or mid-utterance), shutdown() can race the engine's own
        // connection setup. On some OEM TTS engines that race throws from a background
        // binder thread, which crashes the whole process instead of just this cleanup —
        // never worth that for releasing a resource that's about to be garbage collected.
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
        }
        tts = null
    }

    private fun configure() {
        val engine = tts ?: return
        // Misma voz que EjercicioGuiadoViewModel: femenina española (female / esd)
        val voice = engine.voices?.firstOrNull { v ->
            v.locale.language == "es" &&
                (v.name.contains("female", ignoreCase = true) || v.name.contains("esd", ignoreCase = true))
        } ?: engine.voices?.firstOrNull { it.locale.language == "es" }
        voice?.let { engine.voice = it }
        engine.setPitch(VOICE_PITCH)
        engine.setSpeechRate(VOICE_RATE)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) { mainHandler.post { isSpeaking = true } }
            override fun onDone(id: String?) { mainHandler.post { isSpeaking = false } }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) { mainHandler.post { isSpeaking = false } }
        })
        mainHandler.post {
            isReady = true
            pendingText?.let { text ->
                pendingText = null
                speak(text)
            }
        }
    }

    private fun doSpeak(text: String) {
        val clean = textoParaVoz(text).trim()
            .replace(LineBreaks, ". ")
            .replace(RepeatedSpaces, " ")
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }
}
