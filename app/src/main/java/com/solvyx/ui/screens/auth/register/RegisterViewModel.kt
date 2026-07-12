package com.solvyx.ui.screens.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
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

    /** Mensaje de error de validación; null si no hay error. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** true durante el guardado. */
    var isLoading by mutableStateOf(false)
        private set

    fun onNicknameChange(value: String) { nickname = value; errorMessage = null }
    fun onEmailChange(value: String) { email = value; errorMessage = null }
    fun onBirthdateChange(value: String) {
        // Solo dígitos, max 8 chars (DDMMYYYY)
        val filtered = value.filter { it.isDigit() }.take(8)
        birthdate = filtered
        errorMessage = null
    }
    fun onPasswordChange(value: String) { password = value; errorMessage = null }
    fun onConfirmPasswordChange(value: String) { confirmPassword = value; errorMessage = null }
    fun onTermsChange(value: Boolean) { acceptedTerms = value; errorMessage = null }

    fun register(onSuccess: () -> Unit) {
        val validation = validar()
        if (validation != null) {
            errorMessage = validation
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                userRepository.guardar(
                    UserEntity(
                        apodo = nickname.trim(),
                        email = email.trim().lowercase(),
                        fechaRegistro = System.currentTimeMillis(),
                        fechaNacimiento = birthdate.trim()
                    )
                )
                errorMessage = null
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "No pudimos crear tu cuenta. Inténtalo de nuevo."
            } finally {
                isLoading = false
            }
        }
    }

    private fun validar(): String? {
        if (nickname.trim().length < 2) {
            return "Elige un apodo de al menos 2 caracteres."
        }
        if (!esEmailValido(email.trim())) {
            return "Revisa tu correo. Debe tener formato válido."
        }
        if (password.length < 8) {
            return "Tu contraseña debe tener al menos 8 caracteres."
        }
        if (password != confirmPassword) {
            return "Las contraseñas no coinciden."
        }
        val edad = calcularEdad(birthdate.trim())
        if (edad == null) {
            return "Tu fecha de nacimiento debe tener 8 dígitos (DD/MM/AAAA)."
        }
        if (edad !in 13..25) {
            return "Solvyx está pensado para jóvenes de 13 a 25 años."
        }
        if (!acceptedTerms) {
            return "Acepta los términos para continuar."
        }
        return null
    }

    private fun esEmailValido(email: String): Boolean {
        if (email.isBlank()) return false
        val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return regex.matches(email)
    }

    private fun calcularEdad(ddmmaaaa: String): Int? {
        if (ddmmaaaa.length != 8) return null
        val dd = ddmmaaaa.substring(0, 2).toIntOrNull() ?: return null
        val mm = ddmmaaaa.substring(2, 4).toIntOrNull() ?: return null
        val yyyy = ddmmaaaa.substring(4, 8).toIntOrNull() ?: return null
        if (dd !in 1..31 || mm !in 1..12 || yyyy !in 1900..Calendar.getInstance().get(Calendar.YEAR)) {
            return null
        }
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - yyyy
        if (hoy.get(Calendar.MONTH) + 1 < mm ||
            (hoy.get(Calendar.MONTH) + 1 == mm && hoy.get(Calendar.DAY_OF_MONTH) < dd)) {
            edad--
        }
        return if (edad < 0) null else edad
    }
}
