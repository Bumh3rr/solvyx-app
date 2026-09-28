package com.solvyx.ui.screens.journey.diary

import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DiaryLogicTest {

    private val today = LocalDate.of(2026, 9, 27)

    private fun entry(daysAgo: Long, mood: String? = "bien", consumed: Boolean? = false) =
        JournalEntry(date = today.minusDays(daysAgo), mood = mood, consumed = consumed)

    @Test
    fun `diary keeps only full check-ins, newest first`() {
        val goalOnly = JournalEntry(date = today.minusDays(1), metaLograda = true)
        val result = diaryEntries(listOf(entry(3), goalOnly, entry(0), entry(5)))

        assertEquals(listOf(today, today.minusDays(3), today.minusDays(5)), result.map { it.date })
    }

    @Test
    fun `summary counts clean days and the most frequent mood`() {
        val entries = listOf(entry(0, "bien"), entry(1, "bien", consumed = true), entry(2, "triste"))

        val summary = summarize(entries, bestStreak = 4)

        assertEquals(DiarySummary(registeredDays = 3, cleanDays = 2, bestStreak = 4, topMoodId = "bien"), summary)
    }

    @Test
    fun `a day without an answer about use counts as clean`() {
        assertEquals(1, summarize(listOf(entry(0, consumed = null)), bestStreak = 0).cleanDays)
    }

    @Test
    fun `filters combine use and mood`() {
        val entries = listOf(entry(0, "bien"), entry(1, "bien", consumed = true), entry(2, "triste"))

        assertEquals(2, filterEntries(entries, UseFilter.CLEAN, moodId = null).size)
        assertEquals(1, filterEntries(entries, UseFilter.WITH_USE, moodId = null).size)
        assertEquals(1, filterEntries(entries, UseFilter.CLEAN, moodId = "bien").size)
        assertEquals(3, filterEntries(entries, UseFilter.ALL, moodId = null).size)
    }

    @Test
    fun `months are grouped newest first`() {
        val entries = diaryEntries(listOf(entry(0), entry(40), entry(2)))

        val months = groupByMonth(entries).map { it.month }

        assertEquals(listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 8)), months)
    }

    @Test
    fun `calendar pads the first week so day 1 falls on its weekday`() {
        // September 2026 starts on a Tuesday: one blank (Monday) before day 1.
        val days = calendarDays(YearMonth.of(2026, 9), emptyMap())

        assertNull(days[0])
        assertEquals(LocalDate.of(2026, 9, 1), days[1]?.date)
        assertEquals(1 + 30, days.size)
    }

    @Test
    fun `calendar attaches the entry of each logged day`() {
        val logged = entry(0)
        val days = calendarDays(YearMonth.of(2026, 9), mapOf(logged.date to logged))

        assertEquals(logged, days.last { it?.date == today }?.entry)
    }

    @Test
    fun `browsable months go from the oldest entry to the current month`() {
        val months = browsableMonths(listOf(entry(0), entry(70)), YearMonth.of(2026, 9))

        assertEquals(listOf(YearMonth.of(2026, 7), YearMonth.of(2026, 8), YearMonth.of(2026, 9)), months)
    }

    @Test
    fun `without entries only the current month can be browsed`() {
        assertEquals(listOf(YearMonth.of(2026, 9)), browsableMonths(emptyList(), YearMonth.of(2026, 9)))
    }

    @Test
    fun `relative day labels`() {
        assertEquals("Hoy", relativeDay(today, today))
        assertEquals("Ayer", relativeDay(today.minusDays(1), today))
        assertEquals("Hace 5 días", relativeDay(today.minusDays(5), today))
        assertEquals("Hace 2 meses", relativeDay(today.minusDays(65), today))
    }

    @Test
    fun `Berto recap only claims what the diary shows`() {
        assertTrue(bertoRecap(summarize(emptyList(), 0)).startsWith("Cuando registres"))
        assertTrue(bertoRecap(summarize(listOf(entry(0), entry(1)), 2)).contains("todos son días limpios"))
        assertTrue(
            bertoRecap(summarize(listOf(entry(0, "triste", consumed = true)), 0))
                .contains("Tu ánimo más frecuente es triste")
        )
    }
}
