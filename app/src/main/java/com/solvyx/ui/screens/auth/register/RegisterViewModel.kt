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

    fun onNicknameChange(value: String) { nickname = value }
    fun onEmailChange(value: String) { email = value }
    fun onBirthdateChange(value: String) { birthdate = value }
    fun onPasswordChange(value: String) { password = value }
    fun onConfirmPasswordChange(value: String) { confirmPassword = value }
    fun onTermsChange(value: Boolean) { acceptedTerms = value }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            userRepository.guardar(
                UserEntity(
                    apodo = nickname.trim(),
                    email = email.trim(),
                    fechaRegistro = System.currentTimeMillis(),
                    fechaNacimiento = birthdate.trim()
                )
            )
            onSuccess()
        }
    }
}
