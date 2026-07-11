package com.solvyx.ui.screens.auth.register

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

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val destino: Destino? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postAuthRouter: PostAuthRouter
) : ViewModel() {

    var nickname by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var birthdate by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var acceptedTerms by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNicknameChange(value: String) { nickname = value }
    fun onEmailChange(value: String) { email = value }
    fun onBirthdateChange(value: String) { birthdate = value }
    fun onPasswordChange(value: String) { password = value }
    fun onConfirmPasswordChange(value: String) { confirmPassword = value }
    fun onTermsChange(value: Boolean) { acceptedTerms = value }

    private fun validar(): String? {
        if (!Validadores.esNombreValido(nickname)) return "Ingresa un apodo de al menos 2 caracteres."
        if (!Validadores.esEmailValido(email)) return "Ingresa un correo válido."
        if (birthdate.trim().isBlank()) return "Ingresa tu fecha de nacimiento."
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres."
        if (password != confirmPassword) return "Las contraseñas no coinciden."
        if (!acceptedTerms) return "Debes aceptar los Términos de uso y la Política de privacidad."
        return null
    }

    fun register() {
        if (_uiState.value.isLoading) return
        val errorValidacion = validar()
        if (errorValidacion != null) {
            _uiState.update { it.copy(error = errorValidacion) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val esConversion = authRepository.usuarioActual()?.isAnonymous == true
            val resultado = if (esConversion) {
                authRepository.convertirAnonimoAEmail(
                    apodo = nickname.trim(),
                    email = email.trim(),
                    password = password,
                    fechaNacimiento = birthdate.trim()
                )
            } else {
                authRepository.registrarConEmail(
                    apodo = nickname.trim(),
                    email = email.trim(),
                    password = password,
                    fechaNacimiento = birthdate.trim()
                )
            }
            resultado
                .onSuccess {
                    val destino = if (esConversion) {
                        postAuthRouter.resolver(bloquearSiAssistPendiente = true)
                    } else {
                        Destino.AssistPendiente
                    }
                    _uiState.update { it.copy(isLoading = false, destino = destino) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
