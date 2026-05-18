package com.solvyx.ui.screens.sos

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.SmsManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.repository.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

enum class SosState { COUNTDOWN, SENT }

@HiltViewModel
class SosViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: SosRepository
) : ViewModel() {

    var sosState by mutableStateOf(SosState.COUNTDOWN)
        private set

    var countdown by mutableIntStateOf(3)
        private set

    var contactoNames by mutableStateOf(listOf<String>())
        private set

    private var cachedContactos = listOf<ContactoSosEntity>()
    private var countdownJob: Job? = null
    private var tts: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        viewModelScope.launch {
            repository.observarContactos().collect { contactos ->
                cachedContactos = contactos
                contactoNames = contactos.map { it.nombre }
            }
        }
    }

    // ── Countdown ─────────────────────────────────────────────────────────────

    fun startCountdown() {
        val phones = cachedContactos.map { it.telefono }.filter { it.isNotBlank() }
        countdownJob?.cancel()
        countdown = 3
        sendSmsBackground(phones)
        countdownJob = viewModelScope.launch {
            repeat(3) {
                delay(1000L)
                countdown--
            }
            sosState = SosState.SENT
            initTts()
        }
    }

    fun cancel() {
        countdownJob?.cancel()
        tts?.stop()
    }

    // ── SMS ───────────────────────────────────────────────────────────────────

    private fun sendSmsBackground(phones: List<String>) {
        if (phones.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    appContext.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                val msg = "Hola, estoy en crisis y necesito apoyo. " +
                    "Este mensaje fue enviado automáticamente por Solvyx."
                phones.forEach { phone ->
                    smsManager?.sendTextMessage(phone, null, msg, null, null)
                }
                repository.registrarEvento(phones)
            }
        }
    }

    // ── TTS ───────────────────────────────────────────────────────────────────

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech

            val esVoice = tts?.voices?.firstOrNull { v ->
                v.locale.language == "es" &&
                    (v.name.contains("female", ignoreCase = true) ||
                        v.name.contains("esd", ignoreCase = true))
            } ?: tts?.voices?.firstOrNull { v -> v.locale.language == "es" }
            esVoice?.let { tts?.voice = it } ?: run {
                tts?.language = Locale("es", "MX")
            }
            tts?.setPitch(0.85f)
            tts?.setSpeechRate(0.90f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {}
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) {}
            })
            mainHandler.post { speakInitialGuide() }
        }
    }

    fun speakInitialGuide() {
        viewModelScope.launch {
            speak("Alerta enviada. Tus contactos han sido notificados.")
            delay(3800L)
            speak("Intenta respirar con este círculo. Inhala durante cuatro segundos.")
        }
    }

    fun speakPhase(phaseName: String) {
        speak(phaseName)
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sos_tts")
    }

    fun stopTts() {
        tts?.stop()
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        countdownJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
