package com.solvyx.ui.screens.profilesetup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.SosContactRepository
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shared state of the setup wizard: what the welcome and closing screens need to show. */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    sosContactRepository: SosContactRepository
) : ViewModel() {

    var nickname by mutableStateOf("")
        private set
    var selectedSubstances by mutableStateOf(setOf<String>())
        private set
    var contactCount by mutableIntStateOf(0)
        private set

    init {
        viewModelScope.launch {
            nickname = authRepository.getProfile()?.nickname?.trim().orEmpty()
        }
        viewModelScope.launch {
            userRepository.observe().first()?.substancesJson
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.let { stored -> if (stored.isNotEmpty()) selectedSubstances = stored.toSet() }
        }
        viewModelScope.launch {
            sosContactRepository.observe().collect { contacts ->
                contactCount = contacts.count { it.name.isNotBlank() }
            }
        }
    }

    /** "¡Hola, Emma!" — or just "¡Hola!" while (or if) the nickname isn't known. */
    val greeting: String get() = if (nickname.isBlank()) "¡Hola!" else "¡Hola, $nickname!"

    fun toggleSubstance(id: String) {
        selectedSubstances = if (id in selectedSubstances) selectedSubstances - id else selectedSubstances + id
    }

    /**
     * Saves to both Room and Firestore — the same pair of writes ProfileViewModel.toggleSubstance()
     * does, so Mi perfil sees the data immediately. Guarded: a network failure here shouldn't block
     * the user on the wizard's first step — they can fix substances later in Mi perfil.
     */
    fun saveSubstancesAndContinue(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val current = userRepository.observe().first() ?: UserEntity()
                userRepository.save(current.copy(substancesJson = selectedSubstances.joinToString(",")))
                authRepository.updateSubstances(selectedSubstances)
            } catch (e: Exception) {
                // Best-effort, same criteria as BUG-19/20.
            }
            onDone()
        }
    }
}
