package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.JournalingEntry
import com.solvyx.backend.models.PromptJournaling
import com.solvyx.backend.repository.JournalingRepository
import com.solvyx.backend.repository.PromptJournalingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JournalingUiState {
    data object Loading : JournalingUiState
    data class Loaded(
        val promptsPorCategoria: Map<String, List<PromptJournaling>>,
        val entries: List<JournalingEntry>
    ) : JournalingUiState
    data class Error(val message: String) : JournalingUiState
}

sealed interface JournalingEffect {
    data class NavigateToEditor(val promptId: Int?, val promptTexto: String?) : JournalingEffect
    data class ShowMessage(val message: String) : JournalingEffect
}

@HiltViewModel
class JournalingViewModel @Inject constructor(
    private val promptsRepo: PromptJournalingRepository,
    private val entriesRepo: JournalingRepository
) : ViewModel() {

    private val _state = MutableStateFlow<JournalingUiState>(JournalingUiState.Loading)
    val state: StateFlow<JournalingUiState> = _state.asStateFlow()

    private val _effects = Channel<JournalingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Combinación de los dos flows: el banco de prompts (catálogo) y
     * las entradas escritas (privadas del usuario). La UI recibe ambos
     * en un solo `StateFlow`.
     */
    val data: StateFlow<JournalingUiState> = combine(
        promptsRepo.observePrompts(),
        entriesRepo.observeEntries()
    ) { prompts, entries ->
        val porCategoria = prompts
            .groupBy { it.categoria }
            .mapValues { (_, list) -> list.sortedBy { it.orden } }
            .toSortedMap()
        JournalingUiState.Loaded(porCategoria, entries) as JournalingUiState
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        JournalingUiState.Loading
    )

    init {
        viewModelScope.launch {
            data.collect { _state.value = it }
        }
    }

    fun onPromptClick(promptId: Int?, promptTexto: String?) {
        viewModelScope.launch {
            _effects.send(JournalingEffect.NavigateToEditor(promptId, promptTexto))
        }
    }

    fun onEliminarEntry(entry: JournalingEntry) {
        viewModelScope.launch {
            entriesRepo.eliminar(entry)
                .onFailure { e ->
                    _effects.send(
                        JournalingEffect.ShowMessage(
                            e.message ?: "No pudimos borrar tu entrada."
                        )
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { promptsRepo.refresh() }
                .onFailure { e ->
                    _state.value = JournalingUiState.Error(
                        e.message ?: "No pudimos cargar los prompts."
                    )
                }
        }
    }
}
