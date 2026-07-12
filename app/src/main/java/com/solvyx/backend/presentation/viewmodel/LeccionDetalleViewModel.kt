package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.dao.LeccionProgresoDao
import com.solvyx.backend.models.Leccion
import com.solvyx.backend.repository.LeccionesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LeccionDetalleUiState {
    data object Loading : LeccionDetalleUiState
    data class Loaded(
        val leccion: Leccion,
        val yaLeida: Boolean
    ) : LeccionDetalleUiState
    data class Error(val message: String) : LeccionDetalleUiState
}

/**
 * VM de la pantalla de detalle de una lección.
 *
 * Recibe `slug` y `sustancia` por `SavedStateHandle` (típicamente
 * vía `hiltViewModel()` + nav args). Permite marcar la lección como
 * leída al final del recorrido.
 */
@HiltViewModel
class LeccionDetalleViewModel @Inject constructor(
    private val repository: LeccionesRepository,
    private val progresoDao: LeccionProgresoDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()
    private val sustancia: String = savedStateHandle.get<String>("sustancia").orEmpty()

    private val _state = MutableStateFlow<LeccionDetalleUiState>(LeccionDetalleUiState.Loading)
    val state: StateFlow<LeccionDetalleUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = LeccionDetalleUiState.Loading
            runCatching {
                val lec = repository.findBySlug(slug)
                if (lec == null) null
                else lec to (progresoDao.findBySlug(slug)?.leida == true)
            }.onSuccess { result ->
                _state.value = if (result != null) {
                    val (lec, leida) = result
                    LeccionDetalleUiState.Loaded(lec, yaLeida = leida)
                } else {
                    LeccionDetalleUiState.Error("No encontramos esta lección.")
                }
            }.onFailure { e ->
                _state.value = LeccionDetalleUiState.Error(
                    e.message ?: "No pudimos cargar la lección. Inténtalo de nuevo."
                )
            }
        }
    }

    /**
     * Marca la lección como leída. La UI muestra la insignia
     * "leída" inmediatamente porque el estado se actualiza en memoria.
     */
    fun onMarcarComoLeida() {
        if (slug.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.marcarComoLeida(slug) }
                .onSuccess {
                    val current = _state.value
                    if (current is LeccionDetalleUiState.Loaded) {
                        _state.value = current.copy(yaLeida = true)
                    }
                }
        }
    }
}
