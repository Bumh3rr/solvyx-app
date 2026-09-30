package com.solvyx.ui.screens.chatbot

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val UTTERANCE_PREFIX = "berto_tts_"
private const val VOICE_PITCH = 1.15f
private const val VOICE_RATE = 0.85f
private val LineBreaks = Regex("\n+")
private val RepeatedSpaces = Regex(" +")
private val SentenceEnd = Regex("(?<=[.!?])\\s+")

/**
 * Splits [text] (already stripped of markdown) into what the engine speaks one after another: a
 * chunk per paragraph, and a paragraph longer than [maxLength] cut at sentence ends (or hard cut
 * as a last resort), since the engine rejects anything over its maximum input length.
 */
internal fun speechChunks(text: String, maxLength: Int): List<String> =
    text.split(LineBreaks)
        .map { it.replace(RepeatedSpaces, " ").trim() }
        .filter { it.isNotEmpty() }
        .flatMap { paragraph -> fitToLength(paragraph, maxLength) }

private fun fitToLength(paragraph: String, maxLength: Int): List<String> {
    if (paragraph.length <= maxLength) return listOf(paragraph)
    val chunks = mutableListOf<String>()
    var current = StringBuilder()
    paragraph.split(SentenceEnd).forEach { sentence ->
        if (current.isNotEmpty() && current.length + 1 + sentence.length > maxLength) {
            chunks += current.toString()
            current = StringBuilder()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(sentence)
    }
    if (current.isNotEmpty()) chunks += current.toString()
    return chunks.flatMap { it.chunked(maxLength) }
}

/**
 * Berto's spoken voice. Several bubbles in a row (a tree node's detail, its question, the closing
 * line) are queued and said whole, in order: each one used to flush the previous, so the first
 * paragraph was cut halfway and the voice jumped to the last one. [speak] with `interrupt` (or
 * [stop], e.g. when the user acts) drops what's pending. Text said before the engine finishes
 * starting is kept and spoken once it is ready. Markdown is stripped so it doesn't read "asterisco".
 */
class BertoVoice(context: Context) {

    var isMuted by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set

    private var isReady = false
    private val pendingTexts = mutableListOf<String>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var utteranceCount = 0
    /** Only the end of the last queued chunk means Berto stopped talking. */
    @Volatile private var lastUtteranceId: String? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) configure()
        }
    }

    /** Queues [text] after whatever Berto is saying; [interrupt] cuts that off first. */
    fun speak(text: String, interrupt: Boolean = false) {
        if (isMuted) return
        if (!isReady) {
            if (interrupt) pendingTexts.clear()
            pendingTexts += text
            return
        }
        if (interrupt) stop()
        enqueue(text)
    }

    fun stop() {
        pendingTexts.clear()
        lastUtteranceId = null
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
            override fun onDone(id: String?) = onFinished(id)
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) = onFinished(id)
        })
        mainHandler.post {
            isReady = true
            val queued = pendingTexts.toList()
            pendingTexts.clear()
            queued.forEach { speak(it) }
        }
    }

    private fun onFinished(id: String?) {
        if (id == lastUtteranceId) mainHandler.post { isSpeaking = false }
    }

    private fun enqueue(text: String) {
        val engine = tts ?: return
        speechChunks(textoParaVoz(text), TextToSpeech.getMaxSpeechInputLength()).forEach { chunk ->
            val id = UTTERANCE_PREFIX + utteranceCount++
            lastUtteranceId = id
            engine.speak(chunk, TextToSpeech.QUEUE_ADD, null, id)
        }
    }
}
