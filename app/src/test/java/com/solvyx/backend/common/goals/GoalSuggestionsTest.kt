package com.solvyx.backend.common.goals

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.models.NivelRiesgo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalSuggestionsTest {

    private val today = LocalDate.of(2026, 9, 29)

    private fun lastWeek(daysAgo: Int, consumed: Boolean, substance: String? = null) =
        JournalEntry(date = today.minusDays(daysAgo.toLong()), mood = "bien", consumed = consumed, substance = substance)

    private fun active(type: GoalType, substance: String) = Goal(
        type = type, origin = GoalOrigin.USUARIO, substance = substance, title = "t", target = 3, startDate = today
    )

    private fun suggest(
        substances: List<String>,
        levels: Map<String, NivelRiesgo> = emptyMap(),
        journal: List<JournalEntry> = emptyList(),
        activeGoals: List<Goal> = emptyList()
    ) = GoalSuggestions.suggest(substances, levels, journal, activeGoals, today)

    @Test
    fun `low level suggests 3 days without and max 1 day per week for 2 weeks`() {
        val s = suggest(listOf("vape"), mapOf("vape" to NivelRiesgo.BAJO))
        assertEquals(listOf("3 días sin vapear", "Máximo 1 día de vape por semana"), s.map { it.title })
        assertEquals(2, s[1].target)
        assertEquals(1, s[1].weeklyLimit)
    }

    @Test
    fun `moderate level suggests 5 days without and max 2 days per week for 3 weeks`() {
        val s = suggest(listOf("cristal"), mapOf("cristal" to NivelRiesgo.MODERADO))
        assertEquals(5, s[0].target)
        assertEquals(GoalType.REDUCIR_FRECUENCIA, s[1].type)
        assertEquals(3, s[1].target)
        assertEquals(2, s[1].weeklyLimit)
    }

    @Test
    fun `no ASSIST suggests 3 days without and max 2 days per week for 2 weeks`() {
        val s = suggest(listOf("cigarro"))
        assertEquals(listOf("3 días sin fumar", "Máximo 2 días de tabaco por semana"), s.map { it.title })
        assertEquals(2, s[1].target)
    }

    @Test
    fun `high level with alcohol never suggests quitting at once`() {
        val s = suggest(listOf("alcohol"), mapOf("alcohol" to NivelRiesgo.ALTO))
        assertTrue(s.none { it.type == GoalType.SIN_CONSUMO })
        assertEquals(GoalType.REDUCIR_FRECUENCIA, s.single().type)
    }

    @Test
    fun `high level with other substances suggests reducing first`() {
        val s = suggest(listOf("cristal"), mapOf("cristal" to NivelRiesgo.ALTO))
        assertEquals(listOf(GoalType.REDUCIR_FRECUENCIA, GoalType.SIN_CONSUMO), s.map { it.type })
    }

    @Test
    fun `weekly limit comes from last week minus one when there are records`() {
        val journal = (1..4).map { lastWeek(it, true, "alcohol") } + lastWeek(5, false)
        val s = suggest(listOf("alcohol"), mapOf("alcohol" to NivelRiesgo.MODERADO), journal)
        assertEquals(3, s.first { it.type == GoalType.REDUCIR_FRECUENCIA }.weeklyLimit)
    }

    @Test
    fun `no reduce suggestion when last week had one use day or less`() {
        val journal = listOf(lastWeek(1, true, "vape"), lastWeek(2, false), lastWeek(3, false))
        val s = suggest(listOf("vape"), mapOf("vape" to NivelRiesgo.MODERADO), journal)
        assertTrue(s.none { it.type == GoalType.REDUCIR_FRECUENCIA })
    }

    @Test
    fun `highest risk substance comes first and there are at most 3 suggestions`() {
        val s = suggest(
            listOf("vape", "alcohol", "cristal"),
            mapOf("vape" to NivelRiesgo.BAJO, "alcohol" to NivelRiesgo.MODERADO, "cristal" to NivelRiesgo.ALTO)
        )
        assertEquals(GoalSuggestions.MAX_SUGGESTIONS, s.size)
        assertEquals("cristal", s.first().substance)
    }

    @Test
    fun `a suggestion equal to an active goal is skipped`() {
        val s = suggest(listOf("vape"), activeGoals = listOf(active(GoalType.SIN_CONSUMO, "vape")))
        assertTrue(s.none { it.type == GoalType.SIN_CONSUMO })
    }

    @Test
    fun `no suggestions with 3 active goals or without tracked substances`() {
        val three = listOf(
            active(GoalType.SIN_CONSUMO, "vape"), active(GoalType.SIN_CONSUMO, "alcohol"), active(GoalType.SIN_CONSUMO, "cristal")
        )
        assertTrue(suggest(listOf("cigarro"), activeGoals = three).isEmpty())
        assertTrue(suggest(emptyList()).isEmpty())
    }

    @Test
    fun `withdrawal warning only for days without alcohol or any substance at high alcohol risk`() {
        val high = mapOf("alcohol" to NivelRiesgo.ALTO)
        assertTrue(GoalSuggestions.needsWithdrawalWarning(GoalType.SIN_CONSUMO, "alcohol", high))
        assertTrue(GoalSuggestions.needsWithdrawalWarning(GoalType.SIN_CONSUMO, null, high))
        assertFalse(GoalSuggestions.needsWithdrawalWarning(GoalType.REDUCIR_FRECUENCIA, "alcohol", high))
        assertFalse(GoalSuggestions.needsWithdrawalWarning(GoalType.SIN_CONSUMO, "vape", high))
        assertFalse(GoalSuggestions.needsWithdrawalWarning(GoalType.SIN_CONSUMO, "alcohol", mapOf("alcohol" to NivelRiesgo.MODERADO)))
    }

    @Test
    fun `weekly limit from last week is null without records`() {
        assertNull(GoalSuggestions.weeklyLimitFromLastWeek("alcohol", emptyList(), today))
    }

    @Test
    fun `default weekly limit for a custom goal comes from last week minus one`() {
        val journal = listOf(
            lastWeek(1, true, "vape"), lastWeek(2, true, "vape"), lastWeek(3, true, "vape"), lastWeek(4, false)
        )
        assertEquals(2, GoalSuggestions.defaultWeeklyLimit("vape", NivelRiesgo.BAJO, journal, today))
    }

    @Test
    fun `default weekly limit is 1 when last week had one use day or less`() {
        val journal = listOf(lastWeek(1, true, "vape"), lastWeek(2, false))
        assertEquals(1, GoalSuggestions.defaultWeeklyLimit("vape", NivelRiesgo.ALTO, journal, today))
    }

    @Test
    fun `default weekly limit without records follows the ASSIST table`() {
        assertEquals(1, GoalSuggestions.defaultWeeklyLimit("vape", NivelRiesgo.BAJO, emptyList(), today))
        assertEquals(2, GoalSuggestions.defaultWeeklyLimit("vape", NivelRiesgo.MODERADO, emptyList(), today))
        assertEquals(2, GoalSuggestions.defaultWeeklyLimit("vape", NivelRiesgo.ALTO, emptyList(), today))
        assertEquals(2, GoalSuggestions.defaultWeeklyLimit("vape", null, emptyList(), today))
    }

    @Test
    fun `suggested goal keeps its data and is marked as suggested by Berto`() {
        val goal = suggest(listOf("vape")).first().toGoal(today)
        assertEquals(GoalOrigin.SUGERIDA_BERTO, goal.origin)
        assertEquals(today, goal.startDate)
        assertEquals("vape", goal.substance)
    }
}
