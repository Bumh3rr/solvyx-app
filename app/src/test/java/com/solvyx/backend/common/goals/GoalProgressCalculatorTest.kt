package com.solvyx.backend.common.goals

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalProgressCalculatorTest {

    private val start = LocalDate.of(2026, 9, 1)

    private fun withoutUse(target: Int, substance: String? = "vape") = Goal(
        type = GoalType.SIN_CONSUMO, origin = GoalOrigin.USUARIO, substance = substance,
        title = "t", target = target, startDate = start
    )

    private fun reduce(weeks: Int, limit: Int, substance: String = "alcohol") = Goal(
        type = GoalType.REDUCIR_FRECUENCIA, origin = GoalOrigin.USUARIO, substance = substance,
        title = "t", target = weeks, weeklyLimit = limit, startDate = start
    )

    private fun day(offset: Int, consumed: Boolean?, substance: String? = null, mood: String? = "bien") =
        JournalEntry(date = start.plusDays(offset.toLong()), mood = mood, consumed = consumed, substance = substance)

    // ── Días sin consumo ─────────────────────────────────────────────────────

    @Test
    fun `days without use count even if they are not consecutive`() {
        val journal = listOf(day(0, false), day(2, false), day(5, false))
        assertEquals(3, GoalProgressCalculator.progress(withoutUse(5), journal, start.plusDays(6)))
    }

    @Test
    fun `a day using the substance does not add but does not reset either`() {
        val journal = listOf(day(0, false), day(1, true, "vape"), day(2, false))
        assertEquals(2, GoalProgressCalculator.progress(withoutUse(5), journal, start.plusDays(3)))
    }

    @Test
    fun `using another substance still counts as a day without this one`() {
        val journal = listOf(day(0, true, "alcohol"))
        assertEquals(1, GoalProgressCalculator.progress(withoutUse(5), journal, start))
    }

    @Test
    fun `an any-substance goal does not count a day with any use`() {
        val journal = listOf(day(0, true, "alcohol"), day(1, false))
        assertEquals(1, GoalProgressCalculator.progress(withoutUse(5, substance = null), journal, start.plusDays(1)))
    }

    @Test
    fun `quick mood days without the consumption answer are neutral`() {
        val journal = listOf(day(0, consumed = null), day(1, false))
        assertEquals(1, GoalProgressCalculator.progress(withoutUse(5), journal, start.plusDays(1)))
    }

    @Test
    fun `days before the goal started or after today do not count`() {
        val journal = listOf(day(-1, false), day(0, false), day(3, false))
        assertEquals(1, GoalProgressCalculator.progress(withoutUse(5), journal, start.plusDays(2)))
    }

    @Test
    fun `progress never goes above the target and completes the goal`() {
        val journal = (0..6).map { day(it, false) }
        val goal = withoutUse(3)
        assertEquals(goal.target, GoalProgressCalculator.progress(goal, journal, start.plusDays(6)))
    }

    // ── Reducir frecuencia ───────────────────────────────────────────────────

    @Test
    fun `a finished week with enough records and within the limit counts`() {
        // Semana 1 (días 0-6): 5 registrados, 2 con alcohol, límite 2.
        val journal = listOf(
            day(0, true, "alcohol"), day(1, false), day(2, true, "alcohol"), day(4, false), day(6, false)
        )
        assertEquals(1, GoalProgressCalculator.progress(reduce(3, limit = 2), journal, start.plusDays(7)))
    }

    @Test
    fun `a week over the limit does not count`() {
        val journal = listOf(
            day(0, true, "alcohol"), day(1, true, "alcohol"), day(2, true, "alcohol"), day(3, false)
        )
        assertEquals(0, GoalProgressCalculator.progress(reduce(3, limit = 2), journal, start.plusDays(7)))
    }

    @Test
    fun `a week with fewer than 4 records is neutral`() {
        val journal = listOf(day(0, false), day(3, false), day(5, false))
        assertEquals(0, GoalProgressCalculator.progress(reduce(3, limit = 2), journal, start.plusDays(7)))
    }

    @Test
    fun `the week in progress is not counted yet`() {
        val journal = (0..5).map { day(it, false) }
        assertEquals(0, GoalProgressCalculator.progress(reduce(3, limit = 2), journal, start.plusDays(6)))
    }

    @Test
    fun `use of another substance does not count against the weekly limit`() {
        val journal = listOf(
            day(0, true, "vape"), day(1, true, "vape"), day(2, true, "vape"), day(3, false)
        )
        assertEquals(1, GoalProgressCalculator.progress(reduce(3, limit = 1), journal, start.plusDays(7)))
    }

    @Test
    fun `several weeks are counted independently`() {
        val week1Ok = (0..3).map { day(it, false) }
        val week2Over = (7..9).map { day(it, true, "alcohol") } + day(10, false)
        val week3Ok = (14..17).map { day(it, false) }
        val journal = week1Ok + week2Over + week3Ok
        assertEquals(2, GoalProgressCalculator.progress(reduce(3, limit = 2), journal, start.plusDays(21)))
    }

    @Test
    fun `current week reports use days, records and days left`() {
        val journal = listOf(day(7, true, "alcohol"), day(8, false))
        val status = GoalProgressCalculator.currentWeek(reduce(3, limit = 2), journal, start.plusDays(9))!!
        assertEquals(1, status.useDays)
        assertEquals(2, status.registeredDays)
        assertEquals(4, status.daysLeft)
    }

    @Test
    fun `current week is only for reduce-frequency goals`() {
        assertNull(GoalProgressCalculator.currentWeek(withoutUse(5), emptyList(), start))
        assertEquals(0, GoalProgressCalculator.progress(reduce(1, 2), emptyList(), start))
    }

    // ── Detalle día por día / semana por semana ──────────────────────────────

    @Test
    fun `day breakdown marks every day since the start and only counted ones are true`() {
        // Día 0 sin consumo, día 1 sin registro, día 2 con vape, día 3 solo ánimo rápido.
        val journal = listOf(day(0, false), day(2, true, "vape"), day(3, consumed = null))
        val days = GoalProgressCalculator.dayBreakdown(withoutUse(5), journal, start.plusDays(3))
        assertEquals((0..3).map { start.plusDays(it.toLong()) }, days.map { it.date })
        assertEquals(listOf(true, false, false, false), days.map { it.counted })
    }

    @Test
    fun `day breakdown is empty for reduce goals and before the start`() {
        assertTrue(GoalProgressCalculator.dayBreakdown(reduce(3, 2), emptyList(), start.plusDays(3)).isEmpty())
        assertTrue(GoalProgressCalculator.dayBreakdown(withoutUse(5), emptyList(), start.minusDays(1)).isEmpty())
    }

    @Test
    fun `week breakdown lists finished weeks and the one in progress`() {
        val week1Ok = (0..3).map { day(it, false) }
        val week2Over = (7..9).map { day(it, true, "alcohol") } + day(10, false)
        val week3Few = listOf(day(14, false))
        val journal = week1Ok + week2Over + week3Few + day(21, true, "alcohol")
        val weeks = GoalProgressCalculator.weekBreakdown(reduce(5, limit = 2), journal, start.plusDays(22))

        assertEquals(listOf(1, 2, 3, 4), weeks.map { it.number })
        assertEquals(
            listOf(
                GoalWeekStatus.COUNTED, GoalWeekStatus.NOT_COUNTED,
                GoalWeekStatus.NOT_COUNTED, GoalWeekStatus.IN_PROGRESS
            ),
            weeks.map { it.status }
        )
        assertEquals(start.plusDays(21), weeks.last().start)
        assertEquals(start.plusDays(27), weeks.last().end)
        assertEquals(1, weeks.last().useDays)
        assertEquals(1, weeks.last().registeredDays)
    }

    @Test
    fun `the last day of a week is still in progress`() {
        val journal = (0..6).map { day(it, false) }
        val weeks = GoalProgressCalculator.weekBreakdown(reduce(3, limit = 2), journal, start.plusDays(6))
        assertEquals(listOf(GoalWeekStatus.IN_PROGRESS), weeks.map { it.status })
    }
}
