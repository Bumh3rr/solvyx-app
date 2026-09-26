package com.solvyx.ui.screens.profilesetup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var selectedSubstances by mutableStateOf(setOf<String>())
        private set

    fun toggleSubstance(id: String) {
        selectedSubstances = if (selectedSubstances.contains(id))
            selectedSubstances - id
        else
            selectedSubstances + id
    }

    /**
     * Saves to both Room and Firestore — the same pair of writes ProfileViewModel.toggleSubstance()
     * already does, so Mi Perfil sees the data immediately. Unlike that method (no try/catch, a
     * pre-existing gap out of scope for this work), this write IS guarded: a network failure here
     * shouldn't block the user on the wizard's first step — they can fix substances later in Mi
     * Perfil.
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
