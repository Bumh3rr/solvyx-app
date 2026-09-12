package com.solvyx.backend.router

import com.solvyx.ui.screens.profilesetup.ProfileSetupStep
import org.junit.Assert.assertEquals
import org.junit.Test

class PostAuthRouterTest {

    @Test
    fun `sends to SUBSTANCES when there are no tracked substances yet`() {
        val destino = resolveOnboardingDestination(
            selectedSubstances = emptyList(),
            assistCompletado = false,
            tieneContactos = false
        )
        assertEquals(Destino.ProfileSetup(ProfileSetupStep.SUBSTANCES), destino)
    }

    @Test
    fun `sends to ASSIST when substances are set but assist is not completed`() {
        val destino = resolveOnboardingDestination(
            selectedSubstances = listOf("alcohol"),
            assistCompletado = false,
            tieneContactos = false
        )
        assertEquals(Destino.ProfileSetup(ProfileSetupStep.ASSIST), destino)
    }

    @Test
    fun `sends to RED_APOYO when substances and assist are done but there are no SOS contacts`() {
        val destino = resolveOnboardingDestination(
            selectedSubstances = listOf("alcohol"),
            assistCompletado = true,
            tieneContactos = false
        )
        assertEquals(Destino.ProfileSetup(ProfileSetupStep.RED_APOYO), destino)
    }

    @Test
    fun `sends to Home when substances, assist and SOS contacts are all done`() {
        val destino = resolveOnboardingDestination(
            selectedSubstances = listOf("alcohol"),
            assistCompletado = true,
            tieneContactos = true
        )
        assertEquals(Destino.HomeDirecto, destino)
    }
}
