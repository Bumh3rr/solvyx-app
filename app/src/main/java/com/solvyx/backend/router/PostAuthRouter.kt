package com.solvyx.backend.router

import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.ContactoSosRepository
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
    private val contactoSosRepository: ContactoSosRepository
) {
    suspend fun resolver(bloquearSiAssistPendiente: Boolean = false): Destino {
        val user = authRepository.usuarioActual() ?: return Destino.AuthChoice
        if (user.isAnonymous) return Destino.HomeDirecto
        val assistCompletado = authRepository.assistCompletado(user.uid)
        if (!assistCompletado) {
            return if (bloquearSiAssistPendiente) Destino.AssistPendiente else Destino.HomeDirecto
        }
        val tieneContactos = contactoSosRepository.observar().first().isNotEmpty()
        return if (tieneContactos) Destino.HomeDirecto else Destino.RedApoyoSetupOmitible
    }
}
