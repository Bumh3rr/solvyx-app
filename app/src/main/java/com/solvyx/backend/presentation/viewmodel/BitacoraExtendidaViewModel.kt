package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.BitacoraEntry
import com.solvyx.backend.repository.BitacoraExtendidaException
import com.solvyx.backend.repository.BitacoraExtendidaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de UI del registro emocional **con campos extendidos**.
 *
 * Este VM es independiente del `RegistroViewModel` existente (que vive
 * en `ui/screens/bitacora/`); expone un [BitacoraRegistroUiState] con
 * los campos opcionales de sueño, comida, contexto social, etc. La
 * capa de UI puede decidir en el futuro cómo unificar ambos flujos;
 * mientras tanto, ambos coexisten.
 *
 * Decisiones de diseño:
 * - **No es obligatorio** completar los campos extendidos. Si todos
 *   quedan en `null`, el guardado tiene éxito: respeta la regla
 *   "el usuario decide qué compartir".
 * - Este VM guarda la entrada COMPLETA (básicos + extendidos) en un
 *   solo paso vía [BitacoraExtendidaRepository.guardar]. La UI puede
 *   usar este VM como reemplazo del `RegistroViewModel` actual o como
 *   complemento (insertando primero la fila base y luego llamando a
 *   [actualizarExtendidos]).
 */
data class BitacoraRegistroUiState(
    val fecha: Long = System.currentTimeMillis(),
    val estadoAnimo: String? = null,
    val consumio: Boolean? = null,
    val sustancia: String? = null,
    val nota: String? = null,
    // Campos extendidos (todos opcionales)
    val suenoHoras: Int? = null,
    val suenoCalidad: Int? = null,
    val comio: Boolean? = null,
    val calidadComida: Int? = null,
    val actividadFisica: String? = null,
    val contextoSocial: String? = null,
    val detonantePrincipal: String? = null,
    val nivelAnsiedad: Int? = null,
    val tuvoCraving: Boolean? = null,
    val ejercicioFisico: Boolean? = null,
    val notaPrivada: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val error: String? = null
)

sealed interface BitacoraExtendidaEffect {
    data class Saved(val id: Int) : BitacoraExtendidaEffect
    data class ShowMessage(val message: String) : BitacoraExtendidaEffect
}

@HiltViewModel
class BitacoraExtendidaViewModel @Inject constructor(
    private val repository: BitacoraExtendidaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BitacoraRegistroUiState())
    val state: StateFlow<BitacoraRegistroUiState> = _state.asStateFlow()

    private val _effects = Channel<BitacoraExtendidaEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Lista completa de entradas (reactiva). Se expone para pantallas
     * tipo "historial" o "esta semana" que quieran leer los campos
     * extendidos desde una sola fuente.
     */
    val entradas: StateFlow<List<BitacoraEntry>> =
        repository.observar()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ---------------------------------------------------------------
    // Setters de UI (uno por campo; mínimos y predecibles)
    // ---------------------------------------------------------------

    fun setFecha(fecha: Long) = _state.update { it.copy(fecha = fecha, error = null) }
    fun setEstadoAnimo(s: String?) = _state.update { it.copy(estadoAnimo = s) }
    fun setConsumio(b: Boolean?) = _state.update { it.copy(consumio = b) }
    fun setSustancia(s: String?) = _state.update { it.copy(sustancia = s) }
    fun setNota(s: String?) = _state.update { it.copy(nota = s?.take(MAX_NOTA)) }

    fun setSuenoHoras(v: Int?) = _state.update { it.copy(suenoHoras = v?.coerceIn(0, 24)) }
    fun setSuenoCalidad(v: Int?) = _state.update { it.copy(suenoCalidad = v?.coerceIn(1, 5)) }
    fun setComio(b: Boolean?) = _state.update { it.copy(comio = b) }
    fun setCalidadComida(v: Int?) = _state.update { it.copy(calidadComida = v?.coerceIn(1, 5)) }
    fun setActividadFisica(s: String?) = _state.update { it.copy(actividadFisica = s) }
    fun setContextoSocial(s: String?) = _state.update { it.copy(contextoSocial = s) }
    fun setDetonantePrincipal(s: String?) = _state.update { it.copy(detonantePrincipal = s) }
    fun setNivelAnsiedad(v: Int?) = _state.update { it.copy(nivelAnsiedad = v?.coerceIn(0, 10)) }
    fun setTuvoCraving(b: Boolean?) = _state.update { it.copy(tuvoCraving = b) }
    fun setEjercicioFisico(b: Boolean?) = _state.update { it.copy(ejercicioFisico = b) }
    fun setNotaPrivada(s: String?) = _state.update { it.copy(notaPrivada = s?.take(MAX_NOTA)) }

    // ---------------------------------------------------------------
    // Acciones de UI
    // ---------------------------------------------------------------

    /**
     * Persiste la entrada completa (básicos + extendidos).
     *
     * Validación: solo se exige lo mismo que el `RegistroViewModel`
     * actual (`estadoAnimo` + `consumio` + `sustancia` si consumió).
     * Los campos extendidos son 100% opcionales: si todos son `null`
     * se guarda igual.
     */
    fun onGuardar() {
        val s = _state.value
        if (s.guardando) return

        val basicosOk = s.estadoAnimo != null && s.consumio != null &&
            (s.consumio == false || s.sustancia != null)
        if (!basicosOk) {
            _state.update { it.copy(error = "Completa cómo te sientes y si consumiste hoy.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null) }

            val entry = s.toEntry()
            repository.guardar(entry)
                .onSuccess { id ->
                    _state.update { it.copy(guardando = false, guardado = true) }
                    _effects.send(BitacoraExtendidaEffect.Saved(id))
                }
                .onFailure { e ->
                    val msg = when (e) {
                        is BitacoraExtendidaException -> e.message
                        else -> "No pudimos guardar tu registro. Inténtalo de nuevo."
                    }
                    _state.update { it.copy(guardando = false, error = msg) }
                }
        }
    }

    /**
     * Aplica SOLO los campos extendidos del estado actual a una entrada
     * existente (creada previamente por el `RegistroViewModel` legacy).
     *
     * Útil para una transición suave: la UI puede crear la fila base
     * con el flujo actual y luego, opcionalmente, llamar a este método
     * si el usuario quiere completar los campos extendidos.
     */
    fun actualizarExtendidos(id: Int) {
        val s = _state.value
        if (s.guardando) return
        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null) }
            repository.actualizarCamposExtendidos(
                id = id,
                suenoHoras = s.suenoHoras,
                suenoCalidad = s.suenoCalidad,
                comio = s.comio,
                calidadComida = s.calidadComida,
                actividadFisica = s.actividadFisica,
                contextoSocial = s.contextoSocial,
                detonantePrincipal = s.detonantePrincipal,
                nivelAnsiedad = s.nivelAnsiedad,
                tuvoCraving = s.tuvoCraving,
                ejercicioFisico = s.ejercicioFisico,
                notaPrivada = s.notaPrivada
            ).onSuccess {
                _state.update { it.copy(guardando = false, guardado = true) }
                _effects.send(BitacoraExtendidaEffect.Saved(id))
            }.onFailure { e ->
                val msg = when (e) {
                    is BitacoraExtendidaException -> e.message
                    else -> "No pudimos guardar los datos extendidos. Inténtalo de nuevo."
                }
                _state.update { it.copy(guardando = false, error = msg) }
                _effects.send(BitacoraExtendidaEffect.ShowMessage(msg ?: ""))
            }
        }
    }

    fun resetForm() {
        _state.value = BitacoraRegistroUiState()
    }

    // ---------------------------------------------------------------
    // Mapper privado UiState → dominio
    // ---------------------------------------------------------------

    private fun BitacoraRegistroUiState.toEntry(): BitacoraEntry = BitacoraEntry(
        id = 0, // autogenerado
        fecha = fecha,
        estadoAnimo = estadoAnimo ?: "",
        consumio = consumio ?: false,
        sustancia = sustancia,
        nota = nota,
        suenoHoras = suenoHoras,
        suenoCalidad = suenoCalidad,
        comio = comio,
        calidadComida = calidadComida,
        actividadFisica = actividadFisica,
        contextoSocial = contextoSocial,
        detonantePrincipal = detonantePrincipal,
        nivelAnsiedad = nivelAnsiedad,
        tuvoCraving = tuvoCraving,
        ejercicioFisico = ejercicioFisico,
        notaPrivada = notaPrivada,
        updatedAt = System.currentTimeMillis()
    )

    private companion object {
        const val MAX_NOTA = 1000
    }
}
