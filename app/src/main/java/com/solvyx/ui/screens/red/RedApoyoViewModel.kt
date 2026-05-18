package com.solvyx.ui.screens.red

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactoSOS(
    val nombre: String = "",
    val telefono: String = ""
)

@HiltViewModel
class RedApoyoViewModel @Inject constructor() : ViewModel() {

    var contactos by mutableStateOf(listOf(ContactoSOS()))
        private set

    var isSaving by mutableStateOf(false)
        private set

    var savedSuccessfully by mutableStateOf(false)
        private set

    fun phoneValido(telefono: String): Boolean =
        telefono.filter { it.isDigit() }.length >= 7

    fun canSave(): Boolean {
        val c0 = contactos.firstOrNull() ?: return false
        return c0.nombre.trim().length >= 2 && phoneValido(c0.telefono)
    }

    fun setContacto(index: Int, contacto: ContactoSOS) {
        contactos = contactos.toMutableList().also { it[index] = contacto }
    }

    fun addContacto() {
        if (contactos.size >= 3) return
        contactos = contactos + ContactoSOS()
    }

    fun removeContacto(index: Int) {
        if (index == 0) return
        contactos = contactos.filterIndexed { i, _ -> i != index }
    }

    fun guardar() {
        if (!canSave()) return
        viewModelScope.launch {
            isSaving = true
            delay(1100L)
            isSaving = false
            savedSuccessfully = true
        }
    }

    fun resetSaved() {
        savedSuccessfully = false
    }
}
