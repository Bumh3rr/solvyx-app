package com.solvyx.ui.screens.red

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.repository.ContactoSosRepository
import com.solvyx.backend.validation.Validadores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RedApoyoViewModel @Inject constructor(
    private val repository: ContactoSosRepository
) : ViewModel() {

    var contactos by mutableStateOf(listOf(ContactoSosEntity()))
        private set

    var isSaving by mutableStateOf(false)
        private set

    var savedSuccessfully by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.observar().collect { stored ->
                if (stored.isNotEmpty()) contactos = stored
            }
        }
    }

    fun phoneValido(telefono: String): Boolean =
        Validadores.esTelefonoValido(telefono)

    fun canSave(): Boolean {
        val c0 = contactos.firstOrNull() ?: return false
        return Validadores.esNombreValido(c0.nombre) && phoneValido(c0.telefono)
    }

    fun setContacto(index: Int, contacto: ContactoSosEntity) {
        contactos = contactos.toMutableList().also { it[index] = contacto }
    }

    fun addContacto() {
        if (contactos.size >= 3) return
        contactos = contactos + ContactoSosEntity()
    }

    fun removeContacto(index: Int) {
        if (index == 0) return
        contactos = contactos.filterIndexed { i, _ -> i != index }
    }

    fun guardar() {
        if (!canSave()) return
        viewModelScope.launch {
            isSaving = true
            repository.guardarTodos(contactos)
            isSaving = false
            savedSuccessfully = true
        }
    }

    fun resetSaved() {
        savedSuccessfully = false
    }
}
