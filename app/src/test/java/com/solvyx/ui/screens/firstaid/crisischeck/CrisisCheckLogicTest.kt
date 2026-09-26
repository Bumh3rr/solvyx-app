package com.solvyx.ui.screens.firstaid.crisischeck

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrisisCheckLogicTest {

    private val calmSignals = CrisisSignalGroups.flatMap { it.signals }.filterNot { it.isRedFlag }
    private val redFlag = CrisisSignalGroups.flatMap { it.signals }.first { it.isRedFlag }

    @Test
    fun `no signals selected asks the user to pick some`() {
        assertEquals(CrisisCheckResult.NOTHING_SELECTED, assessCrisisCheck(emptyList()))
    }

    @Test
    fun `a few signals suggest self care`() {
        val selected = calmSignals.take(POSSIBLE_CRISIS_THRESHOLD - 1)
        assertEquals(CrisisCheckResult.SELF_CARE, assessCrisisCheck(selected))
    }

    @Test
    fun `reaching the threshold suggests a possible crisis`() {
        val selected = calmSignals.take(POSSIBLE_CRISIS_THRESHOLD)
        assertEquals(CrisisCheckResult.POSSIBLE_CRISIS, assessCrisisCheck(selected))
    }

    @Test
    fun `a single red flag is always urgent`() {
        assertEquals(CrisisCheckResult.URGENT, assessCrisisCheck(listOf(redFlag)))
    }

    @Test
    fun `a red flag outranks the signal count`() {
        val selected = calmSignals.take(POSSIBLE_CRISIS_THRESHOLD) + redFlag
        assertEquals(CrisisCheckResult.URGENT, assessCrisisCheck(selected))
    }

    @Test
    fun `self harm thoughts are flagged as red`() {
        assertTrue(redFlag.label.contains("hacerte daño"))
    }
}
