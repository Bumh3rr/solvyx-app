package com.solvyx.ui.screens.home.stage

import com.solvyx.ui.components.berto.BertoReactTrigger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BertoStageStateTest {

    private fun TestScope.stage() = BertoStageState(backgroundScope) { testScheduler.currentTime }

    @Test
    fun `a mood tapped during the greeting waits for it instead of getting lost`() = runTest {
        val stage = stage()
        stage.start(longGreeting = true)
        stage.reactTo("bien")

        advanceTimeBy(LONG_GREETING_MS - 100)
        assertNull(stage.cue)

        advanceTimeBy(200)
        runCurrent()
        assertEquals(BertoReactTrigger.GOOD.riveName, stage.cue?.trigger)
        assertEquals(StageLine.Reaction("bien"), stage.line)
    }

    @Test
    fun `Berto only reads once nothing else is pending`() = runTest {
        val stage = stage()
        stage.start(longGreeting = false)
        stage.reactTo("neutral")

        advanceTimeBy(SHORT_GREETING_MS + 100)
        assertFalse(stage.isReading)

        advanceTimeBy(REACTION_MS + 400)
        runCurrent()
        assertTrue(stage.isReading)
        assertEquals(1L, stage.readingKey)
    }

    @Test
    fun `sad gets a reaction and then an idea to talk`() = runTest {
        val stage = stage()
        stage.reactTo("triste")
        runCurrent()
        assertEquals(BertoReactTrigger.SAD.riveName, stage.cue?.trigger)
        assertNull(stage.suggestion)

        advanceTimeBy(REACTION_MS + 1)
        runCurrent()
        assertEquals("triggerSuggesting", stage.cue?.trigger)
        assertEquals(SuggestionAction.TALK, stage.suggestion?.action)
    }

    @Test
    fun `a tap on Berto is ignored while he is busy`() = runTest {
        val stage = stage()
        stage.start(longGreeting = true)
        stage.poke()
        advanceTimeBy(LONG_GREETING_MS + 1_000)
        assertNull(stage.cue)
    }

    @Test
    fun `a tap on Berto when free makes him react`() = runTest {
        val stage = stage()
        stage.poke()
        runCurrent()
        assertEquals(BertoReactTrigger.GOOD.riveName, stage.cue?.trigger)
        assertTrue(stage.line is StageLine.Poke)
    }

    @Test
    fun `another idea while Berto explains only swaps the card`() = runTest {
        val stage = stage()
        stage.askForIdea("ansioso")
        runCurrent()
        val firstCue = stage.cue
        assertEquals(SuggestionAction.BREATHE, stage.suggestion?.action)

        stage.nextIdea()
        runCurrent()
        assertEquals(SuggestionAction.GROUNDING, stage.suggestion?.action)
        assertEquals(firstCue, stage.cue)
    }

    @Test
    fun `accepting a self care idea closes the card and Berto thanks once free`() = runTest {
        val stage = stage()
        stage.askForIdea(null)
        runCurrent()
        assertNotNull(stage.suggestion)

        stage.acceptSelfCare()
        assertNull(stage.suggestion)
        advanceTimeBy(SUGGESTING_MS + 1)
        runCurrent()
        assertEquals(StageLine.SuggestionTaken, stage.line)
        assertEquals(BertoReactTrigger.GOOD.riveName, stage.cue?.trigger)
    }
}
