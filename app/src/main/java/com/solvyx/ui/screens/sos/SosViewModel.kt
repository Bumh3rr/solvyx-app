package com.solvyx.ui.screens.sos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.repository.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * Estados finales honestos: la pantalla solo puede afirmar "Alerta enviada" cuando el envío
 * realmente ocurrió.
 *
 * - [NO_CONTACTS]: no hay a quién avisar. Se ofrecen líneas de ayuda y Berto (siempre funcionan).
 * - [SEND_FAILED]: había contactos pero el SMS no salió (permiso denegado o error del sistema).
 *   Se ofrece llamar al contacto con `ACTION_DIAL`, que no requiere ningún permiso.
 */
enum class SosState { COUNTDOWN, SENT, NO_CONTACTS, SEND_FAILED }

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

    /** Contacto al que ofrecer llamar cuando el SMS no salió. Solo se llena en [SosState.SEND_FAILED]. */
    var fallbackContactName by mutableStateOf("")
        private set
    var fallbackContactPhone by mutableStateOf("")
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

            // El envío ocurre de inmediato, como siempre: la cuenta regresiva es feedback, no una
            // ventana para cancelarlo. El estado final depende de si salió de verdad.
            val enviado = sendSms(contactos.map { it.phone })
            if (!enviado) {
                // Falla rápido: hacer esperar 3s para enterarse de que nadie fue avisado es cruel.
                val principal = contactos.first()
                fallbackContactName = principal.name
                fallbackContactPhone = principal.phone
                sosState = SosState.SEND_FAILED
                initTts()
                return@launch
            }

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

    /**
     * `true` solo si el SMS realmente salió. Antes esto era fire-and-forget con un `runCatching`
     * sin `onFailure`: cualquier fallo se tragaba en silencio y la pantalla afirmaba igual que los
     * contactos habían sido notificados.
     *
     * `SEND_SMS` es un permiso *dangerous*: declararlo en el manifest no basta desde Android 6, hay
     * que tenerlo concedido en runtime (se pide en Mi Red de Apoyo, al configurar los contactos).
     * Sin él `sendTextMessage` lanza `SecurityException`, que es exactamente lo que se tragaba.
     */
    private suspend fun sendSms(phones: List<String>): Boolean = withContext(Dispatchers.IO) {
        if (phones.isEmpty()) return@withContext false
        if (!hasSmsPermission()) return@withContext false
        runCatching {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                appContext.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            } ?: return@runCatching false
            val msg = "Hola, estoy en crisis y necesito apoyo. " +
                "Este mensaje fue enviado automáticamente por Solvyx."
            phones.forEach { phone ->
                smsManager.sendTextMessage(phone, null, msg, null, null)
            }
            repository.registerEvent(phones)
            true
        }.getOrDefault(false)
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED

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
                    "Alerta enviada. Tus contactos han sido notificados."
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
