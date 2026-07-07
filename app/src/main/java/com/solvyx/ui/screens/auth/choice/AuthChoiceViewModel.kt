package com.solvyx.ui.screens.auth.choice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.router.Destino
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthChoiceUiState(
    val mostrarSheet: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val destino: Destino? = null
)

@HiltViewModel
class AuthChoiceViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthChoiceUiState())
    val uiState: StateFlow<AuthChoiceUiState> = _uiState.asStateFlow()

    fun abrirSheet() { _uiState.update { it.copy(mostrarSheet = true) } }
    fun cerrarSheet() { _uiState.update { it.copy(mostrarSheet = false, error = null) } }

    fun entrarComoAnonimo() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.entrarComoAnonimo()
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, destino = Destino.HomeDirecto) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
