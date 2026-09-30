package com.solvyx.backend.common.goals

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalTitlesTest {

    @Test
    fun `days without use titles`() {
        assertEquals("5 días sin alcohol", GoalTitles.withoutUse(5, "alcohol"))
        assertEquals("1 día sin vapear", GoalTitles.withoutUse(1, "vape"))
        assertEquals("3 días sin cristal", GoalTitles.withoutUse(3, "cristal"))
        assertEquals("7 días sin fumar", GoalTitles.withoutUse(7, "cigarro"))
        assertEquals("5 días sin consumir", GoalTitles.withoutUse(5, null))
    }

    @Test
    fun `reduce frequency titles`() {
        assertEquals("Máximo 2 días de alcohol por semana", GoalTitles.reduceFrequency(2, "alcohol"))
        assertEquals("Máximo 1 día de tabaco por semana", GoalTitles.reduceFrequency(1, "cigarro"))
    }
}
