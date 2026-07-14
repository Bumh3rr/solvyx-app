package com.solvyx.ui.screens.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.router.Destino
import com.solvyx.backend.router.PostAuthRouter
import com.solvyx.backend.validation.Validadores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val destino: Destino? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postAuthRouter: PostAuthRouter
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) { email = value }
    fun onPasswordChange(value: String) { password = value }

    fun login() {
        if (_uiState.value.isLoading) return
        if (!Validadores.esEmailValido(email)) {
            _uiState.update { it.copy(error = "Ingresa un correo válido.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signIn(email.trim(), password)
                .onSuccess {
                    val destino = postAuthRouter.resolver()
                    _uiState.update { it.copy(isLoading = false, destino = destino) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
