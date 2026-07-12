package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.GuiaExtendida
import com.solvyx.backend.repository.GuiasExtendidasRepository
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

sealed interface GuiasExtendidasUiState {
    data object Loading : GuiasExtendidasUiState
    data class Loaded(
        val guias: List<GuiaExtendida>,
        val categoriaFiltro: String?
    ) : GuiasExtendidasUiState
    data class Error(val message: String) : GuiasExtendidasUiState
}

sealed interface GuiasExtendidasEffect {
    data class NavigateToDetalle(val slug: String) : GuiasExtendidasEffect
}

@HiltViewModel
class GuiasExtendidasViewModel @Inject constructor(
    private val repository: GuiasExtendidasRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GuiasExtendidasUiState>(GuiasExtendidasUiState.Loading)
    val state: StateFlow<GuiasExtendidasUiState> = _state.asStateFlow()

    private val _filtro = MutableStateFlow<String?>(null)

    private val _effects = Channel<GuiasExtendidasEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val guias: StateFlow<List<GuiaExtendida>> = _filtro
        .flatMapLatest { cat ->
            if (cat == null) repository.observeGuias()
            else repository.observeByCategoria(cat)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            guias.collect { lista ->
                _state.value = GuiasExtendidasUiState.Loaded(
                    guias = lista,
                    categoriaFiltro = _filtro.value
                )
            }
        }
    }

    fun onFiltroChange(categoria: String?) {
        _filtro.value = categoria
        _state.value = GuiasExtendidasUiState.Loading
    }

    fun onGuiaClick(slug: String) {
        viewModelScope.launch { _effects.send(GuiasExtendidasEffect.NavigateToDetalle(slug)) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = GuiasExtendidasUiState.Loading
            runCatching { repository.refresh() }
                .onFailure { e ->
                    _state.value = GuiasExtendidasUiState.Error(
                        e.message ?: "No pudimos cargar las guías. Inténtalo de nuevo."
                    )
                }
        }
    }
}
