package com.solvyx.ui.screens.plan

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CreateGoalLogicTest {

    private val today = LocalDate.of(2026, 9, 29)

    private fun active(type: GoalType, substance: String?) = Goal(
        type = type, origin = GoalOrigin.USUARIO, substance = substance, title = "t", target = 3, startDate = today
    )

    // ── Pasos ────────────────────────────────────────────────────────────────

    @Test
    fun `the three steps build a days-without goal`() {
        val draft = CreateGoalDraft()
            .selectType(GoalType.SIN_CONSUMO)
            .selectSubstance("vape", defaultWeeklyLimit = 2)
            .withDays(5)
        assertEquals(CreateGoalStep.AMOUNT, draft.step)
        assertEquals("5 días sin vapear", draft.title)
        val goal = draft.toGoal(today)!!
        assertEquals(GoalType.SIN_CONSUMO, goal.type)
        assertEquals(GoalOrigin.USUARIO, goal.origin)
        assertEquals("vape", goal.substance)
        assertEquals(5, goal.target)
        assertNull(goal.weeklyLimit)
        assertEquals(today, goal.startDate)
    }

    @Test
    fun `any substance keeps the substance empty`() {
        val draft = CreateGoalDraft().selectType(GoalType.SIN_CONSUMO).selectSubstance(null, 2)
        assertTrue(draft.anySubstance)
        assertEquals("3 días sin consumir", draft.title)
        assertNull(draft.toGoal(today)!!.substance)
    }

    @Test
    fun `a reduce goal starts from the proposed limit and 3 weeks`() {
        val draft = CreateGoalDraft().selectType(GoalType.REDUCIR_FRECUENCIA).selectSubstance("alcohol", 2)
        assertEquals(2, draft.weeklyLimit)
        assertEquals(3, draft.weeks)
        assertEquals("Máximo 2 días de alcohol por semana", draft.title)
        val goal = draft.withWeeks(4).toGoal(today)!!
        assertEquals(4, goal.target)
        assertEquals(2, goal.weeklyLimit)
    }

    @Test
    fun `numbers stay inside their ranges`() {
        val draft = CreateGoalDraft().selectType(GoalType.REDUCIR_FRECUENCIA).selectSubstance("vape", 9)
        assertEquals(6, draft.weeklyLimit)
        assertEquals(1, draft.withWeeklyLimit(0).weeklyLimit)
        assertEquals(8, draft.withWeeks(20).weeks)
        assertEquals(1, draft.withWeeks(0).weeks)
        assertEquals(30, draft.withDays(31).days)
        assertEquals(1, draft.withDays(0).days)
    }

    @Test
    fun `going back keeps the choices and returning to the same substance keeps the numbers`() {
        val atAmount = CreateGoalDraft().selectType(GoalType.REDUCIR_FRECUENCIA).selectSubstance("vape", 2).withWeeklyLimit(4)
        val atSubstance = atAmount.back()!!
        assertEquals(CreateGoalStep.SUBSTANCE, atSubstance.step)
        assertEquals(4, atSubstance.selectSubstance("vape", 2).weeklyLimit)
        assertEquals(1, atSubstance.selectSubstance("cristal", 1).weeklyLimit)
        assertEquals(CreateGoalStep.TYPE, atSubstance.back()!!.step)
        assertNull(CreateGoalDraft().back())
    }

    @Test
    fun `changing the type starts over`() {
        val draft = CreateGoalDraft().selectType(GoalType.SIN_CONSUMO).selectSubstance("vape", 2).withDays(9)
            .back()!!.back()!!
            .selectType(GoalType.REDUCIR_FRECUENCIA)
        assertFalse(draft.hasSubstance)
        assertEquals(CreateGoalDraft.DEFAULT_DAYS, draft.days)
        assertNull(draft.title)
        assertNull(draft.toGoal(today))
    }

    // ── Sustancias ───────────────────────────────────────────────────────────

    @Test
    fun `all four substances with the profile ones first and any substance only for days without`() {
        val days = substanceChoices(GoalType.SIN_CONSUMO, listOf("vape", "cigarro"), emptyList())
        assertEquals(listOf("vape", "cigarro", "alcohol", "cristal", null), days.map { it.id })
        assertEquals("Tabaco", days[1].label)
        assertEquals("Cualquier sustancia", days.last().label)
        val reduce = substanceChoices(GoalType.REDUCIR_FRECUENCIA, emptyList(), emptyList())
        assertEquals(listOf("alcohol", "cristal", "vape", "cigarro"), reduce.map { it.id })
    }

    @Test
    fun `an option equal to an active goal is disabled`() {
        val goals = listOf(active(GoalType.SIN_CONSUMO, "vape"), active(GoalType.SIN_CONSUMO, null))
        val days = substanceChoices(GoalType.SIN_CONSUMO, emptyList(), goals)
        assertFalse(days.first { it.id == "vape" }.enabled)
        assertFalse(days.first { it.id == null }.enabled)
        assertTrue(days.first { it.id == "alcohol" }.enabled)
        // Otro tipo con la misma sustancia sí se puede.
        assertTrue(substanceChoices(GoalType.REDUCIR_FRECUENCIA, emptyList(), goals).first { it.id == "vape" }.enabled)
    }

    // ── Confirmación ─────────────────────────────────────────────────────────

    @Test
    fun `Berto confirms the goal`() {
        val days = CreateGoalDraft().selectType(GoalType.SIN_CONSUMO).selectSubstance("vape", 2).withDays(5)
        assertEquals(BertoLine("¡Buena meta!", "“5 días sin vapear”. Cada día que registras sin vapear suma."), confirmationLine(days))
        val reduce = CreateGoalDraft().selectType(GoalType.REDUCIR_FRECUENCIA).selectSubstance("vape", 2)
        assertEquals(
            "“Máximo 2 días de vape por semana”, durante 3 semanas. Cada semana cuenta por separado.",
            confirmationLine(reduce)!!.supporting
        )
        assertNull(confirmationLine(CreateGoalDraft().selectType(GoalType.SIN_CONSUMO)))
    }
}
