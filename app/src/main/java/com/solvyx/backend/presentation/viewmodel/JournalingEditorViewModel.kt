package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.JournalingEntry
import com.solvyx.backend.repository.JournalingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado del editor de entradas de journaling.
 *
 * `guardado: Boolean` se activa brevemente cuando la inserción tiene
 * éxito; la UI la usa para cerrar la pantalla o mostrar confirmación.
 */
data class JournalingEditorUiState(
    val promptId: Int? = null,
    val promptTexto: String? = null,
    val contenido: String = "",
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val error: String? = null
)

sealed interface JournalingEditorEffect {
    data object Cerrar : JournalingEditorEffect
    data class ShowMessage(val message: String) : JournalingEditorEffect
}

@HiltViewModel
class JournalingEditorViewModel @Inject constructor(
    private val repository: JournalingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        JournalingEditorUiState(
            promptId = savedStateHandle.get<Int?>("promptId"),
            promptTexto = savedStateHandle.get<String?>("promptTexto")
        )
    )
    val state: StateFlow<JournalingEditorUiState> = _state.asStateFlow()

    private val _effects = Channel<JournalingEditorEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onContenidoChange(texto: String) {
        // Cap blando para evitar entradas absurdamente largas; el límite
        // exacto puede ajustarse sin migrar la DB (es solo UI).
        _state.update { it.copy(contenido = texto.take(MAX_CONTENIDO), error = null) }
    }

    fun onGuardar() {
        val current = _state.value
        if (current.guardando) return
        if (current.contenido.isBlank()) {
            _state.update { it.copy(error = "Escribe algo antes de guardar.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null) }
            val entry = JournalingEntry(
                id = 0L,
                fecha = System.currentTimeMillis(),
                promptId = current.promptId,
                promptTexto = current.promptTexto,
                contenido = current.contenido,
                createdAt = System.currentTimeMillis()
            )
            repository.insertar(entry)
                .onSuccess {
                    _state.update { it.copy(guardando = false, guardado = true) }
                    _effects.send(JournalingEditorEffect.Cerrar)
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            guardando = false,
                            error = e.message ?: "No pudimos guardar tu entrada."
                        )
                    }
                }
        }
    }

    fun onCancelar() {
        viewModelScope.launch { _effects.send(JournalingEditorEffect.Cerrar) }
    }

    private companion object {
        /** Tope defensivo: 5000 caracteres (~1-2 páginas de Word). */
        const val MAX_CONTENIDO = 5000
    }
}
