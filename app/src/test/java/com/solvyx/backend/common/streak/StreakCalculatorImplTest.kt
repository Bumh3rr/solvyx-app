package com.solvyx.backend.common.streak

import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorImplTest {

    private val zone = ZoneId.systemDefault()
    private val calculator = StreakCalculatorImpl()

    private fun entryFor(daysAgo: Int, today: LocalDate, consumed: Boolean, mood: String = "neutral") =
        JournalEntry(
            date = today.minusDays(daysAgo.toLong()),
            mood = mood,
            consumed = consumed
        )

    @Test
    fun `no entries means zero current streak`() {
        val today = LocalDate.of(2026, 7, 14)
        val stats = calculator.compute(emptyList(), today)
        assertEquals(0, stats.current)
    }

    @Test
    fun `consecutive days without consumption count toward current streak`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = listOf(
            entryFor(daysAgo = 0, today = today, consumed = false),
            entryFor(daysAgo = 1, today = today, consumed = false),
            entryFor(daysAgo = 2, today = today, consumed = false)
        )
        val stats = calculator.compute(entries, today)
        assertEquals(3, stats.current)
    }

    @Test
    fun `a consumed entry today breaks the current streak at zero`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = listOf(
            entryFor(daysAgo = 0, today = today, consumed = true),
            entryFor(daysAgo = 1, today = today, consumed = false)
        )
        val stats = calculator.compute(entries, today)
        assertEquals(0, stats.current)
    }

    @Test
    fun `a day with no entry is neutral and does not break the streak`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = listOf(
            entryFor(daysAgo = 0, today = today, consumed = false),
            entryFor(daysAgo = 2, today = today, consumed = false)
            // daysAgo = 1 no tiene entrada — día neutro
        )
        val stats = calculator.compute(entries, today)
        assertEquals(1, stats.current)
    }

    @Test
    fun `best streak can exceed the current streak when a past run was longer`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = listOf(
            entryFor(daysAgo = 0, today = today, consumed = false),
            entryFor(daysAgo = 3, today = today, consumed = false),
            entryFor(daysAgo = 4, today = today, consumed = false),
            entryFor(daysAgo = 5, today = today, consumed = false),
            entryFor(daysAgo = 6, today = today, consumed = false),
            entryFor(daysAgo = 7, today = today, consumed = false)
        )
        val stats = calculator.compute(entries, today)
        assertEquals(1, stats.current)
        assertEquals(5, stats.best)
    }

    @Test
    fun `next milestone is the smallest configured day count above current streak`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = (0..4).map { entryFor(daysAgo = it, today = today, consumed = false) }
        val stats = calculator.compute(entries, today)
        assertEquals(5, stats.current)
        assertEquals(7, stats.nextMilestone)
    }

    @Test
    fun `progress toward next milestone is fraction between previous and next milestone`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = (0..4).map { entryFor(daysAgo = it, today = today, consumed = false) }
        val stats = calculator.compute(entries, today)
        assertEquals(0.5f, stats.progress, 0.0001f)
    }

    @Test
    fun `progress is complete once streak passes the last milestone`() {
        val today = LocalDate.of(2026, 7, 14)
        val entries = (0..30).map { entryFor(daysAgo = it, today = today, consumed = false) }
        val stats = calculator.compute(entries, today)
        assertEquals(31, stats.current)
        assertEquals(30, stats.nextMilestone)
        assertEquals(1f, stats.progress, 0.0001f)
    }
}
