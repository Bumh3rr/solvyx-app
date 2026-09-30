package com.solvyx.backend.common.goals

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.models.NivelRiesgo
import java.time.LocalDate

/** Meta que Berto propone; se vuelve [Goal] cuando el usuario la acepta. */
data class GoalSuggestion(
    val type: GoalType,
    val substance: String,
    val target: Int,
    val weeklyLimit: Int?,
    val title: String
) {
    fun toGoal(today: LocalDate): Goal = Goal(
        type = type,
        origin = GoalOrigin.SUGERIDA_BERTO,
        substance = substance,
        title = title,
        target = target,
        weeklyLimit = weeklyLimit,
        startDate = today
    )
}

/**
 * Sugerencias de metas según el nivel ASSIST de cada sustancia y lo registrado la semana pasada.
 * Funciones puras.
 *
 * | Nivel      | Sugerencias                                                          |
 * | ---------- | -------------------------------------------------------------------- |
 * | Bajo       | 3 días sin · máximo 1 día/semana por 2 semanas                        |
 * | Moderado   | 5 días sin · máximo 2 días/semana por 3 semanas                       |
 * | Alto       | reducir 1 día vs. la semana pasada por 3 semanas · 3 días sin (nunca alcohol) |
 * | Sin ASSIST | 3 días sin · máximo 2 días/semana por 2 semanas                       |
 *
 * El límite semanal sale de la semana pasada (días de consumo − 1) cuando hay registros; si ya
 * consumió 1 día o menos, no se sugiere reducir frecuencia (no hay a dónde bajar).
 *
 * Seguridad: con alcohol en nivel alto **nunca** se sugiere dejarlo de golpe (riesgo de
 * abstinencia grave); solo reducir, y la app invita a buscar apoyo profesional.
 */
object GoalSuggestions {

    const val MAX_SUGGESTIONS = 3
    const val ALCOHOL = "alcohol"
    const val MIN_WEEKLY_LIMIT = 1
    const val MAX_WEEKLY_LIMIT = 6

    fun suggest(
        substances: List<String>,
        riskLevels: Map<String, NivelRiesgo>,
        journal: List<JournalEntry>,
        activeGoals: List<Goal>,
        today: LocalDate
    ): List<GoalSuggestion> {
        if (activeGoals.size >= Goal.MAX_ACTIVE) return emptyList()
        return substances.distinct()
            // Primero la sustancia de mayor riesgo; sortedBy es estable y respeta el orden del perfil.
            .sortedBy { riskOrder(riskLevels[it]) }
            .flatMap { forSubstance(it, riskLevels[it], journal, today) }
            .filterNot { suggestion ->
                activeGoals.any { it.type == suggestion.type && it.substance == suggestion.substance }
            }
            .take(MAX_SUGGESTIONS)
    }

    /**
     * Límite semanal sugerido según la semana pasada (días de consumo − 1), o null si no hay
     * registros esa semana o si consumió 1 día o menos.
     */
    fun weeklyLimitFromLastWeek(substance: String, journal: List<JournalEntry>, today: LocalDate): Int? {
        val from = today.minusDays(GoalProgressCalculator.DAYS_PER_WEEK.toLong())
        val to = today.minusDays(1)
        val answered = journal
            .filter { !it.date.isBefore(from) && !it.date.isAfter(to) && it.consumed != null }
            .groupBy { it.date }
            .values
        if (answered.isEmpty()) return null
        val useDays = answered.count { day -> day.any { it.consumed == true && it.substance == substance } }
        return if (useDays <= 1) null else (useDays - 1).coerceAtMost(MAX_WEEKLY_LIMIT)
    }

    /**
     * Límite semanal que se propone al crear una meta propia de reducir frecuencia: lo de la semana
     * pasada menos 1 (mínimo 1). Sin registros esa semana, el de la tabla ASSIST (bajo 1, si no 2).
     */
    fun defaultWeeklyLimit(substance: String, level: NivelRiesgo?, journal: List<JournalEntry>, today: LocalDate): Int {
        if (!hasAnsweredDays(journal, today)) return tableWeeklyLimit(level)
        return weeklyLimitFromLastWeek(substance, journal, today) ?: MIN_WEEKLY_LIMIT
    }

    /** "Días sin consumo" de alcohol (o de cualquier sustancia) con alcohol en nivel alto. */
    fun needsWithdrawalWarning(type: GoalType, substance: String?, riskLevels: Map<String, NivelRiesgo>): Boolean =
        type == GoalType.SIN_CONSUMO &&
            (substance == ALCOHOL || substance == null) &&
            riskLevels[ALCOHOL] == NivelRiesgo.ALTO

    /** Con algún nivel alto, Plan acompaña las sugerencias con "Hablar con un profesional". */
    fun shouldSuggestProfessional(riskLevels: Map<String, NivelRiesgo>): Boolean =
        riskLevels.values.any { it == NivelRiesgo.ALTO }

    private fun forSubstance(
        substance: String,
        level: NivelRiesgo?,
        journal: List<JournalEntry>,
        today: LocalDate
    ): List<GoalSuggestion> {
        val lastWeekLimit = weeklyLimitFromLastWeek(substance, journal, today)
        val hadRecordsLastWeek = hasAnsweredDays(journal, today)

        val withoutUseDays: Int?
        val weeks: Int
        val tableLimit = tableWeeklyLimit(level)
        when (level) {
            NivelRiesgo.BAJO -> { withoutUseDays = 3; weeks = 2 }
            NivelRiesgo.MODERADO -> { withoutUseDays = 5; weeks = 3 }
            NivelRiesgo.ALTO -> { withoutUseDays = if (substance == ALCOHOL) null else 3; weeks = 3 }
            null -> { withoutUseDays = 3; weeks = 2 }
        }
        // Con registros de la semana pasada manda lo real; si consumió ≤1 día no se sugiere reducir.
        val weeklyLimit = if (hadRecordsLastWeek) lastWeekLimit else tableLimit

        val withoutUse = withoutUseDays?.let {
            GoalSuggestion(GoalType.SIN_CONSUMO, substance, it, null, GoalTitles.withoutUse(it, substance))
        }
        val reduce = weeklyLimit?.let {
            GoalSuggestion(GoalType.REDUCIR_FRECUENCIA, substance, weeks, it, GoalTitles.reduceFrequency(it, substance))
        }
        // En nivel alto, reducir va primero: es el paso más realista.
        val ordered = if (level == NivelRiesgo.ALTO) listOf(reduce, withoutUse) else listOf(withoutUse, reduce)
        return ordered.filterNotNull()
    }

    private fun hasAnsweredDays(journal: List<JournalEntry>, today: LocalDate): Boolean {
        val from = today.minusDays(GoalProgressCalculator.DAYS_PER_WEEK.toLong())
        val to = today.minusDays(1)
        return journal.any { !it.date.isBefore(from) && !it.date.isAfter(to) && it.consumed != null }
    }

    /** Límite semanal de la tabla: bajo 1; moderado, alto o sin ASSIST 2. */
    private fun tableWeeklyLimit(level: NivelRiesgo?): Int = if (level == NivelRiesgo.BAJO) 1 else 2

    private fun riskOrder(level: NivelRiesgo?): Int = when (level) {
        NivelRiesgo.ALTO -> 0
        NivelRiesgo.MODERADO -> 1
        null -> 2
        NivelRiesgo.BAJO -> 3
    }
}
