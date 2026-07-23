// app/src/test/java/com/solvyx/ui/screens/journey/JourneyLogicTest.kt
package com.solvyx.ui.screens.journey

import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JourneyLogicTest {

    private val today = LocalDate.of(2026, 7, 20)

    @Test fun totalSteps_esTres_siNoConsumio() {
        assertEquals(3, totalWizardSteps(used = false))
    }

    @Test fun totalSteps_esCuatro_siConsumioOSinResponder() {
        assertEquals(4, totalWizardSteps(used = true))
        assertEquals(4, totalWizardSteps(used = null))
    }

    @Test fun isLastStep_consumoNo_terminaEnPasoConsumo() {
        assertTrue(isLastWizardStep(WizardStep.USE.ordinal, used = false))
        assertFalse(isLastWizardStep(WizardStep.NOTE.ordinal, used = false))
    }

    @Test fun isLastStep_consumoSi_terminaEnPasoSustancia() {
        assertFalse(isLastWizardStep(WizardStep.USE.ordinal, used = true))
        assertTrue(isLastWizardStep(WizardStep.SUBSTANCE.ordinal, used = true))
    }

    @Test fun canAdvance_animo_requiereEstadoAnimo() {
        assertFalse(canAdvanceWizard(WizardStep.MOOD.ordinal, null, null, null))
        assertTrue(canAdvanceWizard(WizardStep.MOOD.ordinal, "bien", null, null))
    }

    @Test fun canAdvance_nota_siempreTrue() {
        assertTrue(canAdvanceWizard(WizardStep.NOTE.ordinal, "bien", null, null))
    }

    @Test fun canAdvance_consumo_requiereRespuesta() {
        assertFalse(canAdvanceWizard(WizardStep.USE.ordinal, "bien", null, null))
        assertTrue(canAdvanceWizard(WizardStep.USE.ordinal, "bien", false, null))
        assertTrue(canAdvanceWizard(WizardStep.USE.ordinal, "bien", true, null))
    }

    @Test fun canAdvance_sustancia_requiereSustanciaSiConsumio() {
        assertFalse(canAdvanceWizard(WizardStep.SUBSTANCE.ordinal, "bien", true, null))
        assertTrue(canAdvanceWizard(WizardStep.SUBSTANCE.ordinal, "bien", true, "alcohol"))
    }

    @Test fun esRegistroCompleto_true_conMoodYConsumed() {
        val entries = listOf(JournalEntry(date = today, mood = "bien", consumed = false))
        assertTrue(isDayComplete(entries, today))
    }

    @Test fun esRegistroCompleto_false_animoRapido_consumedNull() {
        val entries = listOf(JournalEntry(date = today, mood = "bien", consumed = null))
        assertFalse(isDayComplete(entries, today))
    }

    @Test fun esRegistroCompleto_false_sinEntradaDeHoy() {
        val entries = listOf(JournalEntry(date = today.minusDays(1), mood = "bien", consumed = false))
        assertFalse(isDayComplete(entries, today))
    }

    @Test fun tabConstants_progresoPrimeroLogrosSegundo() {
        assertEquals(0, TAB_PROGRESS)
        assertEquals(1, TAB_ACHIEVEMENTS)
    }

    @Test fun achievementsState_emptyList_isEmpty() {
        assertTrue(achievementsStateFrom(emptyList()) is AchievementsUiState.Empty)
    }

    @Test fun achievementsState_nonEmpty_isContentWithUnlockedCount() {
        val list = listOf(
            UiAchievement("racha_a", 0, "a", "", unlocked = true, progress = 1f),
            UiAchievement("racha_b", 0, "b", "", unlocked = false, progress = 0f),
            UiAchievement("racha_c", 0, "c", "", unlocked = true, progress = 1f)
        )
        val state = achievementsStateFrom(list)
        assertTrue(state is AchievementsUiState.Content)
        assertEquals(2, (state as AchievementsUiState.Content).unlockedCount)
    }

    @Test fun dateForChartIndex_devuelveFechaONull() {
        val days = listOf(today.minusDays(1), today)
        assertEquals(today, dateForChartIndex(1, days))
        assertNull(dateForChartIndex(5, days))
        assertNull(dateForChartIndex(-1, days))
    }

    @Test fun progressToward_lockedNoStreak_isZero() {
        assertEquals(0f, progressToward(currentStreak = 0, threshold = 10, unlocked = false), 0.001f)
    }

    @Test fun progressToward_lockedHalfway_isHalf() {
        assertEquals(0.5f, progressToward(currentStreak = 5, threshold = 10, unlocked = false), 0.001f)
    }

    @Test fun progressToward_lockedPastThreshold_clampsToOne() {
        // No debería pasar en la práctica (autoUnlock ya lo habría desbloqueado), pero nunca
        // reportar más de 100%.
        assertEquals(1f, progressToward(currentStreak = 15, threshold = 10, unlocked = false), 0.001f)
    }

    @Test fun progressToward_unlocked_isAlwaysOneRegardlessOfStreak() {
        assertEquals(1f, progressToward(currentStreak = 0, threshold = 10, unlocked = true), 0.001f)
    }
}
