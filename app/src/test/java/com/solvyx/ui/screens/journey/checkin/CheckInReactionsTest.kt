package com.solvyx.ui.screens.journey.checkin

import com.solvyx.ui.components.common.MoodOptions
import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.screens.journey.WizardStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInReactionsTest {

    @Test
    fun `greets before a mood is chosen`() {
        val reaction = reactionFor(WizardStep.MOOD, null, null, null, isTyping = false)
        assertEquals(BertoGesture.GREET, reaction.gesture)
    }

    @Test
    fun `every mood gets its own gesture and message`() {
        val reactions = MoodOptions.map { reactionFor(WizardStep.MOOD, it.id, null, null, isTyping = false) }
        assertEquals(MoodOptions.size, reactions.map { it.gesture }.toSet().size)
        assertEquals(MoodOptions.size, reactions.map { it.message }.toSet().size)
        assertTrue(reactions.none { it.gesture == BertoGesture.GREET })
    }

    @Test
    fun `berto reads while the user types`() {
        val reaction = reactionFor(WizardStep.NOTE, "bien", null, null, isTyping = true)
        assertEquals(BertoGesture.READING, reaction.gesture)
    }

    @Test
    fun `no consumption is celebrated and consumption is supported`() {
        assertEquals(BertoGesture.PROUD, reactionFor(WizardStep.USE, "bien", false, null, false).gesture)
        assertEquals(BertoGesture.SUPPORTIVE, reactionFor(WizardStep.USE, "bien", true, null, false).gesture)
    }

    @Test
    fun `consumption message is honest about the streak`() {
        val message = reactionFor(WizardStep.USE, "bien", true, null, false).message
        assertTrue(message.contains("racha"))
    }

    @Test
    fun `every tracked substance has a harm reduction tip`() {
        TrackedSubstances.forEach { option ->
            assertTrue(option.id, SubstanceTips.containsKey(option.id))
            val reaction = reactionFor(WizardStep.SUBSTANCE, "bien", true, option.id, false)
            assertEquals(SubstanceTips.getValue(option.id), reaction.message)
        }
    }

    @Test
    fun `result celebrates a clean day and comforts otherwise`() {
        assertEquals(BertoGesture.CELEBRATE, resultReaction(used = false).gesture)
        assertEquals(BertoGesture.COMFORT, resultReaction(used = true).gesture)
    }
}
