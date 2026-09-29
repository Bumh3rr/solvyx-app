package com.solvyx.ui.screens.sos

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.repository.SosRepository
import com.solvyx.backend.data.local.preferences.SosPreferencesRepository
import com.solvyx.backend.location.SosLocationProvider
import com.solvyx.backend.sms.SosSmsSender
import com.solvyx.backend.sms.sosMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Estados finales honestos: la pantalla solo puede afirmar "Alerta enviada" cuando el envío
 * realmente ocurrió.
 *
 * - [NO_CONTACTS]: no hay a quién avisar. Se ofrecen líneas de ayuda y Berto (siempre funcionan).
 * - [SEND_FAILED]: había contactos pero el SMS no salió a ninguno (sin permiso, sin señal/saldo o
 *   el radio no confirmó el envío).
 *   Se ofrece llamar al contacto con `ACTION_DIAL`, que no requiere ningún permiso.
 */
enum class SosState { COUNTDOWN, SENT, NO_CONTACTS, SEND_FAILED }

@HiltViewModel
class SosViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: SosRepository,
    private val smsSender: SosSmsSender,
    private val sosPreferences: SosPreferencesRepository,
    private val locationProvider: SosLocationProvider
) : ViewModel() {

    var sosState by mutableStateOf(SosState.COUNTDOWN)
        private set

    var countdown by mutableIntStateOf(3)
        private set

    var contactoNames by mutableStateOf(listOf<String>())
        private set

    /** Contacto al que ofrecer llamar cuando el SMS no salió. Solo se llena en [SosState.SEND_FAILED]. */
    var fallbackContactName by mutableStateOf("")
        private set
    var fallbackContactPhone by mutableStateOf("")
        private set

    /** Whether the SMS that went out carried the maps link (the user opted in and a fix arrived). */
    var locationShared by mutableStateOf(false)
        private set

    private var cachedContactos = listOf<SosContactEntity>()
    private var countdownJob: Job? = null
    private var tts: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        viewModelScope.launch {
            repository.observeContacts().collect { contactos ->
                cachedContactos = contactos
                contactoNames = contactos.map { it.name }
            }
        }
    }

    // ── Countdown ─────────────────────────────────────────────────────────────

    fun startCountdown() {
        countdownJob?.cancel()
        countdown = 3
        countdownJob = viewModelScope.launch {
            // Espera la primera emisión REAL de Room en vez de leer `cachedContactos`, que en un
            // arranque en frío (p. ej. entrando por el App Shortcut de crisis) todavía está vacío
            // aunque el usuario sí tenga contactos: leerlo aquí se saltaba el SMS en silencio.
            val contactos = try {
                repository.observeContacts().first().filter { it.phone.isNotBlank() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin este catch, un fallo leyendo Room dejaba sosState congelado en COUNTDOWN
                // para siempre (el countdown deja de avanzar pero la pantalla nunca resuelve).
                // Tratamos "no pude leer contactos" igual que "no hay contactos": no se afirma
                // ningún envío y se ofrecen las líneas de ayuda + Berto.
                emptyList()
            }

            if (contactos.isEmpty()) {
                // Nada que enviar: no se corre la cuenta regresiva ni se afirma que se avisó.
                sosState = SosState.NO_CONTACTS
                initTts()
                return@launch
            }

            // The send starts right away (after at most a few seconds looking for the location, only
            // if the user opted in) and the countdown runs alongside it as feedback. The final state
            // waits for the radio's confirmation, so "Alerta enviada" is only shown when true.
            val delivery = async(Dispatchers.IO) {
                val location = if (sosPreferences.shareLocation.first()) locationProvider.currentLocation() else null
                location to smsSender.send(contactos.map { it.phone }, sosMessage(location))
            }
            val countdownTicks = launch {
                repeat(3) {
                    delay(1000L)
                    countdown--
                }
            }
            val (location, delivered) = delivery.await()
            locationShared = location != null
            if (delivered.isEmpty()) {
                // Fail fast: making someone in crisis wait out the countdown to learn nobody was
                // alerted would be cruel.
                countdownTicks.cancel()
                val principal = contactos.first()
                fallbackContactName = principal.name
                fallbackContactPhone = principal.phone
                sosState = SosState.SEND_FAILED
                initTts()
                return@launch
            }
            countdownTicks.join()
            logEvent(delivered)
            sosState = SosState.SENT
            initTts()
        }
    }

    /** Audit trail only: a failure writing it must never keep the screen from resolving. */
    private suspend fun logEvent(deliveredPhones: List<String>) {
        try {
            repository.registerEvent(deliveredPhones)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The SMS already went out; losing the log row is acceptable.
        }
    }

    fun cancel() {
        countdownJob?.cancel()
        tts?.stop()
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
            // La guía hablada tiene que decir lo mismo que la pantalla: sin contactos no se
            // avisó a nadie, y afirmarlo en voz alta a alguien en crisis sería peor que callar.
            val apertura = when (sosState) {
                SosState.NO_CONTACTS ->
                    "No tienes contactos de apoyo configurados, así que no se envió ninguna alerta. " +
                        "Puedes llamar a la Línea de la Vida o hablar conmigo."
                SosState.SEND_FAILED ->
                    "No se pudo enviar el mensaje. Puedes llamar directamente a tu contacto, " +
                        "o a la Línea de la Vida."
                else ->
                    if (locationShared) "Alerta enviada. Tus contactos recibieron tu ubicación."
                    else "Alerta enviada. Tus contactos han sido notificados."
            }
            speak(apertura)
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
