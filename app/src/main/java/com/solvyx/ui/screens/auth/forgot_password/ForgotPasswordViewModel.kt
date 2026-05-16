package com.solvyx.ui.screens.auth.forgot_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {

    var email by mutableStateOf("")
        private set

    fun onEmailChange(value: String) { email = value }

    fun sendRecoveryEmail(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: implement with Retrofit
            onSuccess()
        }
    }
}
