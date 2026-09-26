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

/** Business rule: at most 3 SOS contacts; the first one is required. */
const val MAX_CONTACTS = 3

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

    /**
     * The first contact is required; optional ones may be left completely empty (they're dropped
     * on save), but a half-filled one blocks saving — SOS would otherwise text an invalid number.
     */
    fun canSave(): Boolean {
        val primary = contactos.firstOrNull() ?: return false
        return isComplete(primary) && contactos.drop(1).all { isEmpty(it) || isComplete(it) }
    }

    private fun isComplete(contact: SosContactEntity): Boolean =
        Validadores.esNombreValido(contact.name) && Validadores.esTelefonoValido(contact.phone)

    private fun isEmpty(contact: SosContactEntity): Boolean =
        contact.name.isBlank() && contact.phone.isBlank()

    fun setContacto(index: Int, contacto: SosContactEntity) {
        contactos = contactos.toMutableList().also { it[index] = contacto }
    }

    fun addContacto() {
        if (contactos.size >= MAX_CONTACTS) return
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
                repository.saveAll(contactos.filterIndexed { index, contact -> index == 0 || !isEmpty(contact) })
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
