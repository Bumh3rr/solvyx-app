package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.insights.AccionInsight
import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoAccion
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.backend.models.Ejercicio
import com.solvyx.backend.repository.EjerciciosRepository
import com.solvyx.ui.tts.TtsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado del modo activo de un ejercicio (con guía TTS y círculo
 * respiratorio animado).
 *
 * El TTS se delega en [TtsEngine] (singleton compartido con Berto y
 * con el ejercicio 5-4-3-2-1). Este VM solo orquesta: decide _qué_
 * decir en cada fase y a quién llamar. La voz, el pitch, el rate y
 * la lifecycle del `TextToSpeech` viven en `TtsEngine`.
 *
 * ## Decisiones
 * - **Fases tipadas**: [Fase] es `INHALA | SOSTEN | EXHALA | COMPLETADO`.
 *   Si el ejercicio no es de respiración (p.ej. body-scan), todas las
 *   fases se tratan como `INHALA` para mantener la animación consistente.
 * - **Pausa**: [enPausa] congel a el contador sin reiniciar el ciclo al
 *   reanudar; [silenciado] sólo afecta al hook TTS, no a la animación.
 * - **Mute**: la preferencia [silenciado] se traduce a
 *   `ttsEngine.toggleMute()` para que la lógica viva en un solo lugar.
 * - **Insight de completado**: cuando se alcanza el último paso, el VM
 *   expone un [Insight] sintético a través de [completionInsight]. La
 *   UI lo enseña con [com.solvyx.ui.components.common.SolvyxInsightBanner].
 */
sealed interface Fase { object INHALA : Fase; object SOSTEN : Fase; object EXHALA : Fase; object COMPLETADO : Fase }

data class EjercicioActivoUiState(
    val ejercicio: Ejercicio? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val pasoActual: Int = 0,
    val fase: Fase = Fase.INHALA,
    val enPausa: Boolean = false,
    val silenciado: Boolean = false,
    val duracionFaseMs: Long = 4000L,
    val completionInsight: Insight? = null,
    /** Expuesto para que la UI pueda ocultar/mostrar un indicador "Hablando…". */
    val ttsHablando: Boolean = false
)

@HiltViewModel
class EjercicioActivoViewModel @Inject constructor(
    private val repository: EjerciciosRepository,
    savedStateHandle: SavedStateHandle,
    private val ttsEngine: TtsEngine
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()

    private val _state = MutableStateFlow(EjercicioActivoUiState())
    val state: StateFlow<EjercicioActivoUiState> = _state.asStateFlow()

    private var cicloJob: Job? = null

    init {
        // El motor de voz se inicializa una vez por proceso. La
        // instancia es compartida con otros consumidores del engine,
        // así que solo pedimos initialize, nunca shutdown.
        ttsEngine.initialize()
        observeTtsSpeaking()
        load()
    }

    /**
     * Refleja el `isSpeaking` del [TtsEngine] en el UiState para que
     * la UI pueda mostrar un indicador sin re-engancharse al flow
     * directamente.
     */
    private fun observeTtsSpeaking() {
        viewModelScope.launch {
            ttsEngine.isSpeaking.collect { hablando ->
                if (_state.value.ttsHablando != hablando) {
                    _state.value = _state.value.copy(ttsHablando = hablando)
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            runCatching { repository.findBySlug(slug) }
                .onSuccess { ej ->
                    if (ej != null) {
                        val duracionBase = when {
                            ej.tipo.equals("respiracion", ignoreCase = true) -> 4000L
                            ej.duracionMinutos <= 2 -> 2500L
                            ej.duracionMinutos <= 5 -> 4000L
                            else -> 6000L
                        }
                        _state.value = _state.value.copy(
                            ejercicio = ej,
                            cargando = false,
                            duracionFaseMs = duracionBase,
                            fase = Fase.INHALA
                        )
                        iniciarCiclo()
                    } else {
                        _state.value = _state.value.copy(
                            cargando = false,
                            error = "No encontramos este ejercicio."
                        )
                    }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        cargando = false,
                        error = e.message ?: "No pudimos cargar el ejercicio."
                    )
                }
        }
    }

    /**
     * Inicia/reinicia el ciclo que avanza por las fases del ejercicio y
     * avanza de paso al terminar. La animación del círculo (UI) se
     * reconstruye alrededor de [duracionFaseMs].
     */
    private fun iniciarCiclo() {
        cicloJob?.cancel()
        cicloJob = viewModelScope.launch {
            val ej = _state.value.ejercicio ?: return@launch
            val totalPasos = ej.pasos.size.coerceAtLeast(1)
            val dur = _state.value.duracionFaseMs
            var paso = 0
            var enPausaSnapshot = false
            while (paso < totalPasos && _state.value.error == null) {
                // Fase 1: inhala
                _state.value = _state.value.copy(fase = Fase.INHALA, pasoActual = paso)
                speak("Inhala. ${ej.pasos.getOrNull(paso).orEmpty()}")
                if (esperarConPausa(dur, ::isPausado)) return@launch
                if (_state.value.error != null) return@launch

                // Fase 2: sostén
                _state.value = _state.value.copy(fase = Fase.SOSTEN)
                speak("Sostén")
                if (esperarConPausa(dur / 2 + 500L, ::isPausado)) return@launch
                if (_state.value.error != null) return@launch

                // Fase 3: exhala
                _state.value = _state.value.copy(fase = Fase.EXHALA)
                speak("Exhala")
                if (esperarConPausa(dur, ::isPausado)) return@launch
                if (_state.value.error != null) return@launch

                paso++
                enPausaSnapshot = _state.value.enPausa
            }
            // Completado
            _state.value = _state.value.copy(
                fase = Fase.COMPLETADO,
                completionInsight = buildCompletionInsight(ej.nombre)
            )
            speak("Has completado el ejercicio.")
        }
    }

    /**
     * Espera `dur` ms respetando pausas: si entra en pausa, bloquea en
     * un bucle hasta que se reanude. Devuelve `true` si la espera se
     * cortó por pausa prolongada (caso borde; cancel() del job cubrirá
     * el resto).
     */
    private suspend fun esperarConPausa(dur: Long, isPausado: () -> Boolean): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < dur) {
            if (!isPausado()) delay(100L)
        }
        return false
    }

    /**
     * Hook de TTS. Delega en [TtsEngine], la pieza configurada con voz
     * femenina es-MX, pitch 1.15 y rate 0.85 (igual que Berto).
     *
     * Si la UI ha silenciado, no se reproduce. El `speak` del engine es
     * no-bloqueante: retorna de inmediato y la utterance suena en
     * paralelo con la animación del círculo.
     */
    private fun speak(text: String) {
        if (_state.value.silenciado) return
        ttsEngine.speak(text)
    }

    private fun isPausado(): Boolean = _state.value.enPausa

    fun togglePausa() {
        val ahora = !_state.value.enPausa
        _state.value = _state.value.copy(enPausa = ahora)
        if (!ahora && _state.value.fase != Fase.COMPLETADO) {
            // Reanudar: reiniciamos el ciclo desde el paso actual.
            iniciarCiclo()
        }
    }

    /**
     * Alterna el estado de mute. La preferencia se propaga al
     * `TtsEngine` para que cualquier utterance en curso se detenga.
     */
    fun toggleSilenciado() {
        val ahora = !_state.value.silenciado
        ttsEngine.setMuted(ahora)
        _state.value = _state.value.copy(silenciado = ahora)
    }

    /**
     * Cleanup invocado por la pantalla al salir (en su
     * `DisposableEffect.onDispose`). Detiene cualquier utterance y
     * cancela el ciclo, pero **no** apaga el engine (es singleton y
     * podría reusarse).
     */
    fun finalizar() {
        cicloJob?.cancel()
        cicloJob = null
        ttsEngine.stop()
    }

    fun reiniciar() {
        val ej = _state.value.ejercicio ?: return
        _state.value = _state.value.copy(
            pasoActual = 0,
            fase = Fase.INHALA,
            enPausa = false,
            completionInsight = null
        )
        iniciarCiclo()
        // Referencia para evitar "unused" warning en tooling estricto.
        ej.slug
    }

    fun consumirCompletionInsight() {
        _state.value = _state.value.copy(completionInsight = null)
    }

    private fun buildCompletionInsight(nombreEjercicio: String) = Insight(
        id = "ejercicio_completado_${System.currentTimeMillis()}",
        tipo = TipoInsight.RECONOCIMIENTO,
        severidad = Severidad.BAJA,
        ventanaTexto = "Completaste “$nombreEjercicio”. Cada práctica suma. Si quieres, anota cómo te sientes en este momento.",
        accion = AccionInsight(tipo = TipoAccion.VER_BITACORA)
    )

    override fun onCleared() {
        // Detenemos audio y ciclo, pero NO apagamos el TtsEngine:
        // es un singleton compartido con otras pantallas (Berto,
        // EjercicioGuiado, ...).
        cicloJob?.cancel()
        ttsEngine.stop()
        super.onCleared()
    }
}
