package com.solvyx.backend.common.goals

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalDaysTest {

    private val start = LocalDate.of(2026, 9, 1)

    private fun withoutUse(target: Int, substance: String? = "vape", title: String = "t") = Goal(
        id = title, type = GoalType.SIN_CONSUMO, origin = GoalOrigin.USUARIO, substance = substance,
        title = title, target = target, startDate = start
    )

    private fun reduce(weeks: Int, limit: Int, title: String = "r") = Goal(
        id = title, type = GoalType.REDUCIR_FRECUENCIA, origin = GoalOrigin.USUARIO, substance = "alcohol",
        title = title, target = weeks, weeklyLimit = limit, startDate = start
    )

    private fun day(offset: Int, consumed: Boolean?, substance: String? = null) =
        JournalEntry(date = start.plusDays(offset.toLong()), mood = "bien", consumed = consumed, substance = substance)

    // ── Registro diario ──────────────────────────────────────────────────────

    @Test
    fun `a day without use advances the goal and brings its new progress`() {
        val journal = listOf(day(0, false), day(1, false))
        val advanced = GoalDays.advancedOn(listOf(withoutUse(3)), journal, start.plusDays(1))
        assertEquals(1, advanced.size)
        assertEquals(2, advanced.first().progress)
    }

    @Test
    fun `using another substance still advances, using that one does not`() {
        val goals = listOf(withoutUse(3, "vape", "vape"), withoutUse(3, "alcohol", "alcohol"))
        val advanced = GoalDays.advancedOn(goals, listOf(day(0, true, "alcohol")), start)
        assertEquals(listOf("vape"), advanced.map { it.title })
    }

    @Test
    fun `reduce goals and days without the consumption answer never advance on a check-in`() {
        assertTrue(GoalDays.advancedOn(listOf(reduce(2, 1)), listOf(day(0, false)), start).isEmpty())
        assertTrue(GoalDays.advancedOn(listOf(withoutUse(3)), listOf(day(0, consumed = null)), start).isEmpty())
    }

    // ── Diario ───────────────────────────────────────────────────────────────

    @Test
    fun `diary marks the counted days and the day a goal was completed`() {
        val goal = withoutUse(2, title = "2 días sin vapear").copy(completed = true, active = false)
        // Día 1 con vape (no suma); el día 3 ya es después de cumplirla.
        val journal = listOf(day(0, false), day(1, true, "vape"), day(2, false), day(3, false))
        val notes = GoalDays.dayNotes(listOf(goal), journal, start.plusDays(3))
        assertEquals(setOf(start, start.plusDays(2)), notes.keys)
        assertTrue(notes.getValue(start).advanced)
        assertTrue(notes.getValue(start).completedTitles.isEmpty())
        assertEquals(listOf("2 días sin vapear"), notes.getValue(start.plusDays(2)).completedTitles)
    }

    @Test
    fun `archived goals leave no notes`() {
        val archived = withoutUse(3).copy(active = false)
        assertTrue(GoalDays.dayNotes(listOf(archived), listOf(day(0, false)), start).isEmpty())
    }

    @Test
    fun `a completed reduce goal is noted on the last registered day of its last counted week`() {
        val goal = reduce(1, limit = 1, title = "Máximo 1 día").copy(completed = true, active = false)
        val journal = listOf(day(0, false), day(1, false), day(3, true, "alcohol"), day(4, false))
        val notes = GoalDays.dayNotes(listOf(goal), journal, start.plusDays(8))
        assertEquals(listOf("Máximo 1 día"), notes.getValue(start.plusDays(4)).completedTitles)
        assertFalse(notes.getValue(start.plusDays(4)).advanced)
        assertNull(notes[start])
    }
}
