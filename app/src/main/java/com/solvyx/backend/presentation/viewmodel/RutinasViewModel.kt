package com.solvyx.backend.presentation.viewmodel

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

sealed interface RutinasUiState {
    data object Loading : RutinasUiState
    data class Loaded(val rutinas: List<Rutina>) : RutinasUiState
    data class Error(val message: String) : RutinasUiState
}

sealed interface RutinasEffect {
    data class NavigateToDetalle(val slug: String) : RutinasEffect
}

@HiltViewModel
class RutinasViewModel @Inject constructor(
    private val repository: RutinasRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RutinasUiState>(RutinasUiState.Loading)
    val state: StateFlow<RutinasUiState> = _state.asStateFlow()

    private val _effects = Channel<RutinasEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val rutinas: StateFlow<List<Rutina>> =
        repository.observeRutinas()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            rutinas.collect { lista ->
                _state.value = RutinasUiState.Loaded(lista)
            }
        }
    }

    fun onRutinaClick(slug: String) {
        viewModelScope.launch { _effects.send(RutinasEffect.NavigateToDetalle(slug)) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = RutinasUiState.Loading
            runCatching { repository.refresh() }
                .onFailure { e ->
                    _state.value = RutinasUiState.Error(
                        e.message ?: "No pudimos cargar las rutinas. Inténtalo de nuevo."
                    )
                }
        }
    }
}
