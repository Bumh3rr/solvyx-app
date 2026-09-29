package com.solvyx.ui.screens.journey.progress

import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeekLogicTest {

    // Sunday 27 September 2026; its week starts on Monday the 21st.
    private val today = LocalDate.of(2026, 9, 27)
    private val thisWeek = LocalDate.of(2026, 9, 21)

    private fun entry(date: LocalDate, mood: String? = "bien", consumed: Boolean? = false) =
        JournalEntry(date = date, mood = mood, consumed = consumed)

    @Test
    fun `week starts on Monday`() {
        assertEquals(thisWeek, weekStartOf(today))
        assertEquals(thisWeek, weekStartOf(thisWeek))
    }

    @Test
    fun `week days mark today, the future and unwritten days`() {
        val saturday = entry(today.minusDays(1))
        val goalOnly = JournalEntry(date = today, metaLograda = true)
        val days = weekDays(thisWeek, mapOf(saturday.date to saturday, goalOnly.date to goalOnly), today)

        assertEquals(7, days.size)
        assertEquals(saturday, days[5].entry)
        assertNull("a goal-only doc is not a written day", days[6].entry)
        assertTrue(days[6].isToday)
        val nextWeek = weekDays(thisWeek.plusWeeks(1), emptyMap(), today)
        assertTrue(nextWeek.all { it.isFuture })
    }

    @Test
    fun `summary counts written and clean days and the top mood`() {
        val a = entry(thisWeek, "bien")
        val b = entry(thisWeek.plusDays(1), "bien", consumed = true)
        val summary = weekSummary(weekDays(thisWeek, mapOf(a.date to a, b.date to b), today))

        assertEquals(WeekSummary(written = 2, clean = 1, topMoodId = "bien"), summary)
    }

    @Test
    fun `browsable weeks go from the oldest entry's week to this week`() {
        val weeks = browsableWeeks(listOf(entry(today), entry(today.minusDays(15))), today)

        assertEquals(listOf(thisWeek.minusWeeks(2), thisWeek.minusWeeks(1), thisWeek), weeks)
        assertEquals(listOf(thisWeek), browsableWeeks(emptyList(), today))
    }

    @Test
    fun `week labels`() {
        assertEquals("21–27 sep", weekLabel(thisWeek))
        assertEquals("28 sep – 4 oct", weekLabel(thisWeek.plusWeeks(1)))
    }

    @Test
    fun `with little data Berto says so, once`() {
        val insights = weeklyInsights(listOf(entry(today)), thisWeek)

        assertEquals(1, insights.size)
        assertTrue(insights.single().startsWith("Aún no tengo suficientes registros"))
    }

    @Test
    fun `compares writing and mood with the previous week`() {
        val entries = listOf(
            entry(thisWeek, "euforico"), entry(thisWeek.plusDays(1), "bien"), entry(thisWeek.plusDays(2), "bien"),
            entry(thisWeek.minusDays(1), "triste"), entry(thisWeek.minusDays(2), "ansioso")
        )

        val insights = weeklyInsights(entries, thisWeek)

        assertTrue(insights.any { it.contains("3 días, 1 más que la anterior") })
        assertTrue(insights.any { it.contains("va mejor que el de la semana pasada") })
    }

    @Test
    fun `notices when clean days come with a better mood`() {
        val entries = listOf(
            entry(today.minusDays(1), "bien"), entry(today.minusDays(2), "bien"), entry(today.minusDays(3), "euforico"),
            entry(today.minusDays(10), "triste", consumed = true), entry(today.minusDays(11), "ansioso", consumed = true)
        )

        assertTrue(weeklyInsights(entries, thisWeek).any { it.startsWith("Tus días limpios suelen ser") })
    }

    @Test
    fun `does not claim the clean-day pattern when moods are alike`() {
        val entries = listOf(
            entry(today.minusDays(1)), entry(today.minusDays(2)), entry(today.minusDays(3)),
            entry(today.minusDays(10), consumed = true), entry(today.minusDays(11), consumed = true)
        )

        assertFalse(weeklyInsights(entries, thisWeek).any { it.startsWith("Tus días limpios suelen ser") })
    }

    @Test
    fun `names the weekday with the most use when it repeats`() {
        // Two Saturdays with use, plus enough days to claim a pattern.
        val entries = listOf(
            entry(LocalDate.of(2026, 9, 26), consumed = true), entry(LocalDate.of(2026, 9, 19), consumed = true),
            entry(LocalDate.of(2026, 9, 25)), entry(LocalDate.of(2026, 9, 24)), entry(LocalDate.of(2026, 9, 23))
        )

        assertTrue(weeklyInsights(entries, thisWeek).any { it.startsWith("Los sábados concentran") })
    }
}
