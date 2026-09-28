package com.solvyx.ui.screens.journey.achievements

import com.solvyx.backend.data.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AchievementsLogicTest {

    private val milestones = listOf(3, 7, 10, 15, 30)
    private val today = LocalDate.of(2026, 9, 27)

    @Test
    fun `trail starts at 0 and reaches a node exactly on its milestone`() {
        assertEquals(0f, trailPosition(0, milestones), 0.001f)
        assertEquals(1f, trailPosition(3, milestones), 0.001f)
        assertEquals(2f, trailPosition(7, milestones), 0.001f)
    }

    @Test
    fun `between milestones Berto moves proportionally`() {
        assertEquals(1f / 3f, trailPosition(1, milestones), 0.001f)
        assertEquals(1.5f, trailPosition(5, milestones), 0.001f)
    }

    @Test
    fun `past the last milestone Berto stays at the summit`() {
        assertEquals(5f, trailPosition(45, milestones), 0.001f)
    }

    @Test
    fun `next milestone and trail message`() {
        assertEquals(3, nextMilestone(0, milestones))
        assertEquals(10, nextMilestone(7, milestones))
        assertNull(nextMilestone(30, milestones))
        assertTrue(trailMessage(2, milestones).contains("Mañana"))
        assertTrue(trailMessage(4, milestones).contains("Te faltan 3 días"))
        assertTrue(trailMessage(31, milestones).contains("cima"))
    }

    private fun entry(daysAgo: Long, mood: String? = "bien", note: String? = null, consumed: Boolean? = false, goal: Boolean = false) =
        JournalEntry(date = today.minusDays(daysAgo), mood = mood, note = note, consumed = consumed, metaLograda = goal)

    @Test
    fun `badges count only written days`() {
        val goalOnly = JournalEntry(date = today, metaLograda = true)
        val badges = diaryBadges(listOf(goalOnly)).associateBy { it.id }

        assertFalse(badges.getValue("primer_registro").unlocked)
        assertEquals(1, badges.getValue("tres_metas").current)
    }

    @Test
    fun `badges unlock at their targets and cap their progress`() {
        val journal = (0L until 8L).map { entry(it, note = "nota $it") } + entry(9, mood = "triste", consumed = true)
        val badges = diaryBadges(journal).associateBy { it.id }

        assertTrue(badges.getValue("primer_registro").unlocked)
        assertTrue(badges.getValue("siete_registros").unlocked)
        assertEquals(7, badges.getValue("siete_registros").current)
        assertTrue(badges.getValue("cinco_notas").unlocked)
        assertTrue(badges.getValue("honestidad").unlocked)
        assertEquals(2, badges.getValue("todos_los_animos").current)
        assertFalse(badges.getValue("todos_los_animos").unlocked)
    }

    @Test
    fun `badge progress is a 0 to 1 fraction`() {
        val badge = diaryBadges(listOf(entry(0, note = "hola"))).first { it.id == "cinco_notas" }
        assertEquals(0.2f, badge.progress, 0.001f)
    }
}
