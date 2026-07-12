package com.solvyx.backend.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.Rutina
import com.solvyx.backend.repository.RutinasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RutinaDetalleUiState {
    data object Loading : RutinaDetalleUiState
    data class Loaded(
        val rutina: Rutina,
        val pasosCompletadosHoy: Set<Int>
    ) : RutinaDetalleUiState
    data class Error(val message: String) : RutinaDetalleUiState
}

sealed interface RutinaDetalleEffect {
    data object NavigateToActivo : RutinaDetalleEffect
}

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class RutinaDetalleViewModel @Inject constructor(
    private val repository: RutinasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()

    private val _state = MutableStateFlow<RutinaDetalleUiState>(RutinaDetalleUiState.Loading)
    val state: StateFlow<RutinaDetalleUiState> = _state.asStateFlow()

    private val _effects = Channel<RutinaDetalleEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** Flujo reactivo del set de pasos completados HOY. */
    val pasosCompletadosHoy: StateFlow<Set<Int>> =
        repository.observeProgresoDelDia()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = RutinaDetalleUiState.Loading
            runCatching { repository.findRutinaBySlug(slug) }
                .onSuccess { r ->
                    _state.value = if (r != null) {
                        RutinaDetalleUiState.Loaded(r, pasosCompletadosHoy.value)
                    } else {
                        RutinaDetalleUiState.Error("No encontramos esta rutina.")
                    }
                }
                .onFailure { e ->
                    _state.value = RutinaDetalleUiState.Error(
                        e.message ?: "No pudimos cargar la rutina. Inténtalo de nuevo."
                    )
                }
        }

        // Reflejamos el progreso del día en el estado.
        viewModelScope.launch {
            pasosCompletadosHoy.collect { ids ->
                val current = _state.value
                if (current is RutinaDetalleUiState.Loaded) {
                    _state.value = current.copy(pasosCompletadosHoy = ids)
                }
            }
        }
    }

    fun onPasoCompletado(pasoId: Int) {
        viewModelScope.launch {
            runCatching { repository.marcarPasoCompletado(pasoId) }
        }
    }

    fun onIniciar() {
        viewModelScope.launch { _effects.send(RutinaDetalleEffect.NavigateToActivo) }
    }
}
