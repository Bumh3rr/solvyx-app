package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.Ejercicio
import com.solvyx.backend.repository.EjerciciosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EjercicioDetalleUiState {
    data object Loading : EjercicioDetalleUiState
    data class Loaded(val ejercicio: Ejercicio) : EjercicioDetalleUiState
    data class Error(val message: String) : EjercicioDetalleUiState
}

sealed interface EjercicioDetalleEffect {
    data object NavigateToActivo : EjercicioDetalleEffect
}

/**
 * VM de la pantalla de detalle de un ejercicio.
 *
 * Recibe el `slug` por `SavedStateHandle` (típicamente vía
 * `hiltViewModel()` + nav arg). La clave esperada es "slug".
 */
@HiltViewModel
class EjercicioDetalleViewModel @Inject constructor(
    private val repository: EjerciciosRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()

    private val _state = MutableStateFlow<EjercicioDetalleUiState>(EjercicioDetalleUiState.Loading)
    val state: StateFlow<EjercicioDetalleUiState> = _state.asStateFlow()

    private val _effects = Channel<EjercicioDetalleEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = EjercicioDetalleUiState.Loading
            runCatching { repository.findBySlug(slug) }
                .onSuccess { ej ->
                    _state.value = if (ej != null) {
                        EjercicioDetalleUiState.Loaded(ej)
                    } else {
                        EjercicioDetalleUiState.Error("No encontramos este ejercicio.")
                    }
                }
                .onFailure { e ->
                    _state.value = EjercicioDetalleUiState.Error(
                        e.message ?: "No pudimos cargar el ejercicio. Inténtalo de nuevo."
                    )
                }
        }
    }

    fun onIniciar() {
        viewModelScope.launch {
            _effects.send(EjercicioDetalleEffect.NavigateToActivo)
        }
    }
}
