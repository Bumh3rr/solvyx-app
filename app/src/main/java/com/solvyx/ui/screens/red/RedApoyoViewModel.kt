package com.solvyx.ui.screens.red

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.repository.SosContactRepository
import com.solvyx.backend.validation.Validadores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RedApoyoViewModel @Inject constructor(
    private val repository: SosContactRepository
) : ViewModel() {

    var contactos by mutableStateOf(listOf(SosContactEntity()))
        private set

    var isSaving by mutableStateOf(false)
        private set

    var savedSuccessfully by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.observe().collect { stored ->
                if (stored.isNotEmpty()) contactos = stored
            }
        }
    }

    fun phoneValido(telefono: String): Boolean =
        Validadores.esTelefonoValido(telefono)

    fun canSave(): Boolean {
        val c0 = contactos.firstOrNull() ?: return false
        return Validadores.esNombreValido(c0.name) && phoneValido(c0.phone)
    }

    fun setContacto(index: Int, contacto: SosContactEntity) {
        contactos = contactos.toMutableList().also { it[index] = contacto }
    }

    fun addContacto() {
        if (contactos.size >= 3) return
        contactos = contactos + SosContactEntity()
    }

    fun removeContacto(index: Int) {
        if (index == 0) return
        contactos = contactos.filterIndexed { i, _ -> i != index }
    }

    fun guardar() {
        if (!canSave()) return
        viewModelScope.launch {
            isSaving = true
            try {
                repository.saveAll(contactos)
                savedSuccessfully = true
            } finally {
                // Sin el finally, una excepción en saveAll() dejaba isSaving en true para
                // siempre: el botón Guardar quedaba deshabilitado y el spinner no paraba.
                isSaving = false
            }
        }
    }

    fun resetSaved() {
        savedSuccessfully = false
    }
}
