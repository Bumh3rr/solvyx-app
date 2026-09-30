package com.solvyx.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

/** "Conoce tu sustancia": the substances in the order to show them, the profile ones first. */
@HiltViewModel
class InfoSustanciaViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    /** From Room, so it works offline. Catalog order until the profile is read. */
    val substances: StateFlow<List<SubstanceInfo>> = userRepository.observe()
        .map { user ->
            val profile = user?.substancesJson?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
            orderedSubstanceInfos(profile)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SubstanceInfos)
}
