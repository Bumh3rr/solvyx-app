package com.solvyx.backend.router

import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.SosContactRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class Destino {
    object AuthChoice : Destino()
    object HomeDirecto : Destino()
    object AssistPendiente : Destino()
    object RedApoyoSetupOmitible : Destino()
}

@Singleton
class PostAuthRouter @Inject constructor(
    private val authRepository: AuthRepository,
    private val sosContactRepository: SosContactRepository
) {
    suspend fun resolver(bloquearSiAssistPendiente: Boolean = false): Destino {
        val user = authRepository.currentUser ?: return Destino.AuthChoice
        val assistCompletado = authRepository.isAssistCompleted(user.uid)
        if (!assistCompletado) {
            return if (bloquearSiAssistPendiente) Destino.AssistPendiente else Destino.HomeDirecto
        }
        val tieneContactos = sosContactRepository.observe().first().isNotEmpty()
        return if (tieneContactos) Destino.HomeDirecto else Destino.RedApoyoSetupOmitible
    }
}
