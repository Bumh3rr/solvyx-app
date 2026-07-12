package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.Leccion
import com.solvyx.backend.repository.LeccionesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Las lecciones se agrupan por sustancia en la pantalla principal.
 * La UI recibe un `Map<sustancia, List<Leccion>>` ya ordenado.
 */
sealed interface LeccionesUiState {
    data object Loading : LeccionesUiState
    data class Loaded(
        val leccionesPorSustancia: Map<String, List<Leccion>>
    ) : LeccionesUiState
    data class Error(val message: String) : LeccionesUiState
}

sealed interface LeccionesEffect {
    data class NavigateToDetalle(val sustancia: String, val slug: String) : LeccionesEffect
}

@HiltViewModel
class LeccionesViewModel @Inject constructor(
    private val repository: LeccionesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LeccionesUiState>(LeccionesUiState.Loading)
    val state: StateFlow<LeccionesUiState> = _state.asStateFlow()

    private val _effects = Channel<LeccionesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Flujo de lecciones agrupadas por sustancia, reactivo.
     * Se mantiene en `StateFlow` para que la UI lo consuma directamente.
     */
    val leccionesAgrupadas: StateFlow<Map<String, List<Leccion>>> =
        repository.observeLecciones()
            .map { lista ->
                lista
                    .groupBy { it.sustancia }
                    .mapValues { (_, lecciones) ->
                        // Dentro de cada sustancia, orden por (tema, orden).
                        lecciones.sortedWith(compareBy({ it.tema }, { it.orden }))
                    }
                    .toSortedMap()
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    init {
        viewModelScope.launch {
            leccionesAgrupadas.collect { mapa ->
                _state.value = LeccionesUiState.Loaded(mapa)
            }
        }
    }

    fun onLeccionClick(sustancia: String, slug: String) {
        viewModelScope.launch {
            _effects.send(LeccionesEffect.NavigateToDetalle(sustancia, slug))
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LeccionesUiState.Loading
            runCatching { repository.refresh() }
                .onFailure { e ->
                    _state.value = LeccionesUiState.Error(
                        e.message ?: "No pudimos cargar las lecciones. Inténtalo de nuevo."
                    )
                }
        }
    }
}
