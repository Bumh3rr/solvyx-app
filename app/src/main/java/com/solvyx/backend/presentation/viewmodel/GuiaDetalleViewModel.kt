package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.GuiaExtendida
import com.solvyx.backend.repository.GuiasExtendidasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GuiaDetalleUiState {
    data object Loading : GuiaDetalleUiState
    data class Loaded(val guia: GuiaExtendida) : GuiaDetalleUiState
    data class Error(val message: String) : GuiaDetalleUiState
}

@HiltViewModel
class GuiaDetalleViewModel @Inject constructor(
    private val repository: GuiasExtendidasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()

    private val _state = MutableStateFlow<GuiaDetalleUiState>(GuiaDetalleUiState.Loading)
    val state: StateFlow<GuiaDetalleUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = GuiaDetalleUiState.Loading
            runCatching { repository.findBySlug(slug) }
                .onSuccess { g ->
                    _state.value = if (g != null) {
                        GuiaDetalleUiState.Loaded(g)
                    } else {
                        GuiaDetalleUiState.Error("No encontramos esta guía.")
                    }
                }
                .onFailure { e ->
                    _state.value = GuiaDetalleUiState.Error(
                        e.message ?: "No pudimos cargar la guía. Inténtalo de nuevo."
                    )
                }
        }
    }
}
