package com.solvyx.backend.router

import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.SosContactRepository
import com.solvyx.ui.screens.profilesetup.ProfileSetupStep
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class Destino {
    object AuthChoice : Destino()
    object HomeDirecto : Destino()
    data class ProfileSetup(val step: ProfileSetupStep) : Destino()
}

/**
 * Determines the first missing step of the profile configuration wizard, given that the 3 data
 * components it requires are already resolved. A pure function (no Firebase/Room) for testing
 * without mocks — the project has no mocking library, following the same pattern as
 * `needsQuestionReload` in `DiagnosticoViewModel.kt`. Called only when a session is confirmed
 * and the wizard is forced (see `PostAuthRouter.resolver()`).
 */
fun resolveOnboardingDestination(
    selectedSubstances: List<String>,
    assistCompletado: Boolean,
    tieneContactos: Boolean
): Destino {
    if (selectedSubstances.isEmpty()) return Destino.ProfileSetup(ProfileSetupStep.SUBSTANCES)
    if (!assistCompletado) return Destino.ProfileSetup(ProfileSetupStep.ASSIST)
    return if (tieneContactos) Destino.HomeDirecto
           else Destino.ProfileSetup(ProfileSetupStep.RED_APOYO)
}

@Singleton
class PostAuthRouter @Inject constructor(
    private val authRepository: AuthRepository,
    private val sosContactRepository: SosContactRepository
) {
    /**
     * @param forzarOnboarding false (Splash/Login): never enters the wizard, only checks for an
     * active session. true (Register/anonymous-to-email conversion): calculates the first missing
     * step of the 3-step wizard.
     */
    suspend fun resolver(forzarOnboarding: Boolean = false): Destino {
        val user = authRepository.currentUser ?: return Destino.AuthChoice
        if (!forzarOnboarding) return Destino.HomeDirecto

        val perfil = authRepository.getProfile()
        val assistCompletado = authRepository.isAssistCompleted(user.uid)
        val tieneContactos = sosContactRepository.observe().first().isNotEmpty()

        return resolveOnboardingDestination(
            selectedSubstances = perfil?.selectedSubstances ?: emptyList(),
            assistCompletado = assistCompletado,
            tieneContactos = tieneContactos
        )
    }
}
