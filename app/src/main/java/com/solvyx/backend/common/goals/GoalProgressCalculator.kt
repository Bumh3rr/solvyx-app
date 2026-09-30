package com.solvyx.backend.common.goals

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate

/** Cómo va la semana en curso de una meta de reducir frecuencia (para mostrar "1 de 2 días"). */
data class WeekStatus(
    val useDays: Int,
    val registeredDays: Int,
    val daysLeft: Int
)

/** Un día de una meta de días sin consumo: [counted] si sumó (registrado y sin consumir). */
data class GoalDay(val date: LocalDate, val counted: Boolean)

enum class GoalWeekStatus { COUNTED, NOT_COUNTED, IN_PROGRESS }

/** Una semana (7 días desde el inicio) de una meta de reducir frecuencia. [number] empieza en 1. */
data class GoalWeek(
    val number: Int,
    val start: LocalDate,
    val end: LocalDate,
    val status: GoalWeekStatus,
    val useDays: Int,
    val registeredDays: Int
)

/**
 * Progreso de una meta calculado desde la bitácora. Funciones puras: sin Firebase ni Android.
 *
 * Reglas (acordadas con enfoque de reducción de daños):
 * - Solo cuentan los días en que el usuario **respondió si consumió** (`consumed != null`); el
 *   ánimo rápido de Inicio o un día sin registro son neutros: ni suman ni restan.
 * - Nada se reinicia: un día con consumo simplemente no suma.
 * - Una semana de reducir frecuencia son 7 días desde `startDate`. Cuenta cuando ya terminó,
 *   tiene al menos [MIN_REGISTERED_DAYS_PER_WEEK] días registrados y no pasó del límite.
 */
object GoalProgressCalculator {

    const val DAYS_PER_WEEK = 7
    const val MIN_REGISTERED_DAYS_PER_WEEK = 4

    /** Progreso actual, sin pasar del objetivo. */
    fun progress(goal: Goal, journal: List<JournalEntry>, today: LocalDate): Int {
        val raw = when (goal.type) {
            GoalType.SIN_CONSUMO -> daysWithoutUse(goal, journal, today)
            GoalType.REDUCIR_FRECUENCIA -> weeksWithinLimit(goal, journal, today)
        }
        return raw.coerceAtMost(goal.target)
    }

    /** Semana en curso de una meta de reducir frecuencia; null en otros tipos o antes de empezar. */
    fun currentWeek(goal: Goal, journal: List<JournalEntry>, today: LocalDate): WeekStatus? {
        if (goal.type != GoalType.REDUCIR_FRECUENCIA || today.isBefore(goal.startDate)) return null
        val weeksElapsed = (today.toEpochDay() - goal.startDate.toEpochDay()) / DAYS_PER_WEEK
        val weekStart = goal.startDate.plusDays(weeksElapsed * DAYS_PER_WEEK)
        val days = answeredDays(journal, weekStart, today)
        return WeekStatus(
            useDays = days.count { usedSubstance(it, goal.substance) },
            registeredDays = days.size,
            daysLeft = (weekStart.plusDays(DAYS_PER_WEEK - 1L).toEpochDay() - today.toEpochDay()).toInt()
        )
    }

    /** Días sin consumo: cada día desde el inicio hasta hoy y si sumó. Vacío en otros tipos. */
    fun dayBreakdown(goal: Goal, journal: List<JournalEntry>, today: LocalDate): List<GoalDay> {
        if (goal.type != GoalType.SIN_CONSUMO || today.isBefore(goal.startDate)) return emptyList()
        val counted = answeredDays(journal, goal.startDate, today)
            .filterNot { usedSubstance(it, goal.substance) }
            .map { it.first().date }
            .toSet()
        return generateSequence(goal.startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .map { GoalDay(it, it in counted) }
            .toList()
    }

    /**
     * Reducir frecuencia: las semanas que ya terminaron más la semana en curso. Vacío en otros
     * tipos o sin límite semanal.
     */
    fun weekBreakdown(goal: Goal, journal: List<JournalEntry>, today: LocalDate): List<GoalWeek> {
        val limit = goal.weeklyLimit
        if (goal.type != GoalType.REDUCIR_FRECUENCIA || limit == null || today.isBefore(goal.startDate)) return emptyList()
        val weeks = mutableListOf<GoalWeek>()
        var weekStart = goal.startDate
        while (!weekStart.isAfter(today)) {
            val weekEnd = weekStart.plusDays(DAYS_PER_WEEK - 1L)
            val days = answeredDays(journal, weekStart, minOf(weekEnd, today))
            val useDays = days.count { usedSubstance(it, goal.substance) }
            val status = when {
                // Solo cuentan las semanas que ya terminaron (su último día es anterior a hoy).
                !weekEnd.isBefore(today) -> GoalWeekStatus.IN_PROGRESS
                days.size >= MIN_REGISTERED_DAYS_PER_WEEK && useDays <= limit -> GoalWeekStatus.COUNTED
                else -> GoalWeekStatus.NOT_COUNTED
            }
            weeks += GoalWeek(weeks.size + 1, weekStart, weekEnd, status, useDays, days.size)
            weekStart = weekStart.plusDays(DAYS_PER_WEEK.toLong())
        }
        return weeks
    }

    private fun daysWithoutUse(goal: Goal, journal: List<JournalEntry>, today: LocalDate): Int =
        answeredDays(journal, goal.startDate, today).count { !usedSubstance(it, goal.substance) }

    private fun weeksWithinLimit(goal: Goal, journal: List<JournalEntry>, today: LocalDate): Int =
        weekBreakdown(goal, journal, today).count { it.status == GoalWeekStatus.COUNTED }

    /** Un día por fecha dentro de [from, to] en que el usuario respondió la pregunta de consumo. */
    private fun answeredDays(journal: List<JournalEntry>, from: LocalDate, to: LocalDate): List<List<JournalEntry>> =
        journal
            .filter { !it.date.isBefore(from) && !it.date.isAfter(to) && it.consumed != null }
            .groupBy { it.date }
            .values
            .toList()

    /** [substance] null = cualquier sustancia. */
    private fun usedSubstance(dayEntries: List<JournalEntry>, substance: String?): Boolean =
        dayEntries.any { it.consumed == true && (substance == null || it.substance == substance) }
}
