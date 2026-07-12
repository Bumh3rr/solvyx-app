package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.Ejercicio
import com.solvyx.backend.repository.EjerciciosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de listado de ejercicios.
 *
 * Modelado como `sealed interface` para que la UI ramifique con
 * `when` exhaustivo. El estado es **inmutable** desde fuera: la VM
 * expone sólo `StateFlow` de lectura.
 */
sealed interface EjerciciosUiState {
    data object Loading : EjerciciosUiState
    data class Loaded(
        val ejercicios: List<Ejercicio>,
        val filtroActivo: String?
    ) : EjerciciosUiState
    data class Error(val message: String) : EjerciciosUiState
}

/**
 * Eventos one-shot que la UI debe consumir (navegación, snackbars).
 * Se exponen con `Channel<Channel.BUFFERED>` para que no se pierdan
 * ante recomposiciones.
 */
sealed interface EjerciciosEffect {
    data class NavigateToDetalle(val slug: String) : EjerciciosEffect
    data class ShowMessage(val message: String) : EjerciciosEffect
}

@HiltViewModel
class EjerciciosViewModel @Inject constructor(
    private val repository: EjerciciosRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EjerciciosUiState>(EjerciciosUiState.Loading)
    val state: StateFlow<EjerciciosUiState> = _state.asStateFlow()

    private val _filtro = MutableStateFlow<String?>(null)

    private val _effects = Channel<EjerciciosEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Flujo derivado: cuando cambia el filtro, la fuente de datos cambia.
     * `stateIn` lo convierte en un `StateFlow` con vida ligada a este VM.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val ejercicios: StateFlow<List<Ejercicio>> = _filtro
        .flatMapLatest { filtro ->
            if (filtro == null) repository.observeEjercicios()
            else repository.observeByTipo(filtro)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Reflejamos el flujo de ejercicios en el UiState.
        viewModelScope.launch {
            ejercicios.collect { lista ->
                val current = _state.value
                if (current is EjerciciosUiState.Loaded) {
                    _state.value = current.copy(ejercicios = lista)
                } else if (current !is EjerciciosUiState.Error) {
                    _state.value = EjerciciosUiState.Loaded(
                        ejercicios = lista,
                        filtroActivo = _filtro.value
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Acciones de la UI
    // ---------------------------------------------------------------

    fun onFiltroChange(tipo: String?) {
        _filtro.value = tipo
        val current = _state.value
        if (current is EjerciciosUiState.Loaded) {
            _state.value = current.copy(filtroActivo = tipo)
        } else {
            _state.value = EjerciciosUiState.Loading
        }
    }

    fun onEjercicioClick(slug: String) {
        viewModelScope.launch {
            _effects.send(EjerciciosEffect.NavigateToDetalle(slug))
        }
    }

    /**
     * Recarga el seed desde assets. Útil para un eventual botón
     * "Restablecer contenido" en ajustes.
     */
    fun refresh() {
        viewModelScope.launch {
            _state.value = EjerciciosUiState.Loading
            runCatching { repository.refresh() }
                .onFailure { e ->
                    _state.value = EjerciciosUiState.Error(
                        e.message ?: "No pudimos cargar los ejercicios. Inténtalo de nuevo."
                    )
                    _effects.send(EjerciciosEffect.ShowMessage("No pudimos actualizar el contenido."))
                }
        }
    }
}
