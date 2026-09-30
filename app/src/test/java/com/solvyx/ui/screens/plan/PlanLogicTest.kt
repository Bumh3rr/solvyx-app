package com.solvyx.ui.screens.plan

import com.solvyx.backend.common.goals.GoalWeek
import com.solvyx.backend.common.goals.GoalWeekStatus
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
import java.time.ZoneOffset

class PlanLogicTest {

    private val start = LocalDate.of(2026, 9, 1)

    private fun withoutUse(target: Int, progress: Int = 0) = Goal(
        id = "a", type = GoalType.SIN_CONSUMO, origin = GoalOrigin.SUGERIDA_BERTO, substance = "alcohol",
        title = "$target días sin alcohol", target = target, progress = progress, startDate = start
    )

    private fun reduce(weeks: Int, limit: Int) = Goal(
        id = "b", type = GoalType.REDUCIR_FRECUENCIA, origin = GoalOrigin.USUARIO, substance = "alcohol",
        title = "Máximo $limit días de alcohol por semana", target = weeks, weeklyLimit = limit, startDate = start
    )

    private fun day(offset: Int, consumed: Boolean?, substance: String? = null) =
        JournalEntry(date = start.plusDays(offset.toLong()), mood = "bien", consumed = consumed, substance = substance)

    private fun card(goal: Goal, progress: Int) = GoalCardUi(goal, progress, "")

    // ── Mensaje de Berto ─────────────────────────────────────────────────────

    @Test
    fun `anonymous users are invited to create an account`() {
        val line = bertoLine(requiresAccount = true, activeGoals = emptyList(), hasSuggestions = true, answeredToday = false, event = null)
        assertEquals("Con una cuenta guardo tus metas y tu progreso.", line.supporting)
    }

    @Test
    fun `a just completed goal wins over everything`() {
        val line = bertoLine(
            requiresAccount = false,
            activeGoals = listOf(card(withoutUse(3), 2)),
            hasSuggestions = true,
            answeredToday = false,
            event = PlanEvent.GoalCompleted("3 días sin alcohol")
        )
        assertEquals("¡Cumpliste tu meta!", line.message)
        assertEquals("“3 días sin alcohol” ya es tuya.", line.supporting)
    }

    @Test
    fun `adding a goal is confirmed`() {
        val line = bertoLine(false, listOf(card(withoutUse(3), 0)), true, false, PlanEvent.GoalAdded)
        assertEquals("¡Hecho! Empezamos hoy.", line.message)
    }

    @Test
    fun `one step away comes before the check-in reminder`() {
        val line = bertoLine(false, listOf(card(withoutUse(3), 2)), true, answeredToday = false, event = null)
        assertEquals("¡Ya casi!", line.message)
        assertEquals("Te falta 1 día para “3 días sin alcohol”.", line.supporting)
        val weeks = bertoLine(false, listOf(card(reduce(3, 1), 2)), true, answeredToday = true, event = null)
        assertEquals("Te falta 1 semana para “Máximo 1 días de alcohol por semana”.", weeks.supporting)
    }

    @Test
    fun `a one day goal is not almost done from the start`() {
        assertFalse(card(withoutUse(1), 0).oneStepAway)
    }

    @Test
    fun `without goals Berto invites to start`() {
        val withIdeas = bertoLine(false, emptyList(), hasSuggestions = true, answeredToday = false, event = null)
        assertEquals("¿Empezamos con una meta pequeña?", withIdeas.message)
        assertEquals("Te dejé algunas ideas abajo.", withIdeas.supporting)
        val noIdeas = bertoLine(false, emptyList(), hasSuggestions = false, answeredToday = false, event = null)
        assertEquals("Cuando quieras, armamos una juntos.", noIdeas.supporting)
    }

    @Test
    fun `with goals and no check-in today Berto asks about the day and links the check-in`() {
        val line = bertoLine(false, listOf(card(withoutUse(5), 1)), true, answeredToday = false, event = null)
        assertEquals("¿Cómo va tu día?", line.message)
        assertTrue(line.opensCheckIn)
        assertFalse(bertoLine(false, listOf(card(withoutUse(5), 1)), true, answeredToday = true, event = null).opensCheckIn)
    }

    @Test
    fun `check-in result line for the goals that advanced`() {
        assertNull(advancedLine(emptyList()))
        val one = advancedLine(listOf(withoutUse(3, progress = 2)))!!
        assertEquals("Avanzaste en tu meta", one.message)
        assertEquals("3 días sin alcohol · 2 de 3", one.supporting)
        val two = advancedLine(listOf(withoutUse(3, progress = 2), withoutUse(5, progress = 1)))!!
        assertEquals("Avanzaste en 2 metas", two.message)
        assertEquals("3 días sin alcohol · 5 días sin alcohol", two.supporting)
    }

    @Test
    fun `three active goals and the default message`() {
        val three = List(3) { card(withoutUse(5), 1) }
        assertEquals("Tienes 3 metas en marcha", bertoLine(false, three, false, true, null).message)
        assertEquals("Vas a tu ritmo", bertoLine(false, three.take(1), false, true, null).message)
    }

    @Test
    fun `answered today needs the consumption answer, not only the quick mood`() {
        assertFalse(answeredToday(listOf(day(0, consumed = null)), start))
        assertTrue(answeredToday(listOf(day(0, consumed = false)), start))
    }

    // ── Tarjetas ─────────────────────────────────────────────────────────────

    @Test
    fun `days without use card shows live progress`() {
        val card = goalCard(withoutUse(3), listOf(day(0, false), day(1, false)), start.plusDays(1))
        assertEquals(2, card.progress)
        assertEquals("2 de 3 días", card.progressLabel)
        assertNull(card.weekLine)
        assertEquals(2f / 3f, card.fraction, 0.001f)
    }

    @Test
    fun `reduce card shows this week and the registering nudge`() {
        val card = goalCard(reduce(2, limit = 1), listOf(day(0, false)), start.plusDays(2))
        assertEquals("0 de 2 semanas", card.progressLabel)
        assertEquals("Esta semana: 0 de 1 día · quedan 4 días", card.weekLine)
        assertEquals("Registra tu día para que esta semana cuente", card.weekHint)
    }

    @Test
    fun `reduce card over the limit looks ahead to next week`() {
        val journal = listOf(day(0, true, "alcohol"), day(1, true, "alcohol"))
        val card = goalCard(reduce(2, limit = 1), journal, start.plusDays(1))
        assertEquals("Esta semana: 2 días · la próxima semana empieza de nuevo", card.weekLine)
        assertNull(card.weekHint)
    }

    @Test
    fun `reduce card without enough records left looks ahead to next week`() {
        // Último día de la semana con solo 1 registro: ya no puede llegar a 4.
        val card = goalCard(reduce(2, limit = 1), listOf(day(0, false)), start.plusDays(6))
        assertEquals("Esta semana: 0 de 1 día · la próxima semana empieza de nuevo", card.weekLine)
        assertNull(card.weekHint)
    }

    @Test
    fun `reduce card with enough records has no nudge`() {
        val journal = (0..3).map { day(it, false) }
        val card = goalCard(reduce(2, limit = 1), journal, start.plusDays(6))
        assertEquals("Esta semana: 0 de 1 día · hoy termina", card.weekLine)
        assertNull(card.weekHint)
    }

    // ── Guía "Ganas muy fuertes" ─────────────────────────────────────────────

    @Test
    fun `craving guide shows only active days-without-use goals, most advanced first`() {
        val journal = listOf(day(0, false), day(1, false))
        val almost = withoutUse(3).copy(id = "almost", title = "3 días sin alcohol")
        val far = withoutUse(10).copy(id = "far", title = "10 días sin alcohol")
        val done = withoutUse(3).copy(id = "done", completed = true, active = false)
        val archived = withoutUse(3).copy(id = "archived", active = false)
        val cards = cravingGoals(listOf(far, reduce(2, 1), almost, done, archived), journal, start.plusDays(1))
        assertEquals(listOf("almost", "far"), cards.map { it.goal.id })
        assertEquals("2 de 3 días", cards.first().progressLabel)
    }

    @Test
    fun `craving guide shows nothing without days-without-use goals`() {
        assertTrue(cravingGoals(listOf(reduce(2, 1)), emptyList(), start).isEmpty())
    }

    // ── Historial y detalle ──────────────────────────────────────────────────

    @Test
    fun `completed goals are newest first and archived ones are left out`() {
        val old = withoutUse(3).copy(id = "old", completed = true, active = false, completedAt = 1_000L)
        val new = withoutUse(3).copy(id = "new", completed = true, active = false, completedAt = 2_000L)
        val archived = withoutUse(3).copy(id = "archived", active = false)
        assertEquals(listOf("new", "old"), completedGoals(listOf(old, archived, new)).map { it.id })
    }

    @Test
    fun `detail labels`() {
        val completedAt = LocalDate.of(2026, 10, 3).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val done = withoutUse(3).copy(completed = true, completedAt = completedAt)
        assertEquals("Cumplida el 3 de octubre", completedLabel(done, ZoneOffset.UTC))
        assertEquals("Desde el 1 de septiembre", startLabel(withoutUse(3)))
        assertEquals("Sugerida por Berto", originLabel(withoutUse(3)))
        assertEquals("Tu meta", originLabel(reduce(2, 1)))
        assertEquals("Suma cada día que registras sin alcohol. No tienen que ser seguidos.", ruleLabel(withoutUse(3)))
    }

    @Test
    fun `week rows`() {
        val week = GoalWeek(1, start, start.plusDays(6), GoalWeekStatus.IN_PROGRESS, useDays = 1, registeredDays = 2)
        assertEquals("1 sep – 7 sep", weekRangeLabel(week))
        assertEquals("En curso · 1 de 2 días", weekStatusLabel(week, 2))
        assertEquals("Sumó ✓", weekStatusLabel(week.copy(status = GoalWeekStatus.COUNTED), 2))
        assertEquals("No sumó", weekStatusLabel(week.copy(status = GoalWeekStatus.NOT_COUNTED), 2))
    }
}
