package com.solvyx.ui.screens.profile

import com.solvyx.backend.common.streak.milestoneProgress
import com.solvyx.ui.screens.profile.model.SafetyStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileLogicTest {

    @Test
    fun `registered user never sees the account step`() {
        val items = safetyItems(contactCount = 0, hasAssessment = false, hasSubstances = false, isAnonymous = false)
        assertFalse(items.any { it.step == SafetyStep.ACCOUNT })
    }

    @Test
    fun `guest gets the account step as pending`() {
        val items = safetyItems(contactCount = 1, hasAssessment = true, hasSubstances = true, isAnonymous = true)
        val account = items.single { it.step == SafetyStep.ACCOUNT }
        assertFalse(account.done)
    }

    @Test
    fun `support contacts come first`() {
        val items = safetyItems(contactCount = 0, hasAssessment = true, hasSubstances = true, isAnonymous = false)
        assertEquals(SafetyStep.SUPPORT_CONTACTS, items.first().step)
    }

    @Test
    fun `completion counts done steps`() {
        val items = safetyItems(contactCount = 2, hasAssessment = true, hasSubstances = false, isAnonymous = false)
        assertEquals(2f / 3f, items.completion(), 0.0001f)
    }

    @Test
    fun `everything set up is fully complete`() {
        val items = safetyItems(contactCount = 1, hasAssessment = true, hasSubstances = true, isAnonymous = false)
        assertEquals(1f, items.completion(), 0.0001f)
        assertTrue(items.all { it.done })
    }

    @Test
    fun `milestone progress between milestones`() {
        val progress = milestoneProgress(streak = 5, milestones = listOf(3, 7, 10))
        assertEquals(7, progress.next)
        assertEquals(0.5f, progress.progress, 0.0001f)
    }

    @Test
    fun `milestone progress starts at zero with no streak`() {
        val progress = milestoneProgress(streak = 0, milestones = listOf(3, 7, 10))
        assertEquals(3, progress.next)
        assertEquals(0f, progress.progress, 0.0001f)
    }

    @Test
    fun `milestone progress is full past the last milestone`() {
        val progress = milestoneProgress(streak = 40, milestones = listOf(3, 7, 10))
        assertEquals(10, progress.next)
        assertEquals(1f, progress.progress, 0.0001f)
    }
}
