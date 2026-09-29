package com.solvyx.backend.common.streak

import com.solvyx.backend.data.model.Achievement
import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Regla de negocio (docs/app/Solvyx.md): solo un día con consumo rompe la racha. Los días sin
 * registro son **neutros**: no suman ni la rompen. Así la racha no cae a 0 porque el usuario aún no
 * hace el check-in de hoy o se saltó un día.
 */
@Singleton
class StreakCalculatorImpl @Inject constructor() : StreakCalculator {

    private val milestoneDays = Achievement.MILESTONE_DAYS

    override fun compute(entries: List<JournalEntry>, today: LocalDate): StreakStats {
        // Días que cuentan (limpios o con consumo), en orden; los neutros quedan fuera.
        val countedDays = entries
            .filter { !it.date.isAfter(today) }
            .groupBy { it.date }
            .mapValues { (_, dayEntries) -> dayStatus(dayEntries) }
            .filterValues { it != DayStatus.NEUTRAL }
            .toSortedMap()
            .values

        // Racha actual: hacia atrás desde hoy, hasta el primer día con consumo.
        var current = 0
        for (status in countedDays.reversed()) {
            if (status == DayStatus.CONSUMED) break
            current++
        }

        // Mejor racha: la serie más larga de días limpios sin un consumo en medio.
        var best = 0
        var run = 0
        for (status in countedDays) {
            run = if (status == DayStatus.CLEAN) run + 1 else 0
            best = maxOf(best, run)
        }

        val milestone = milestoneProgress(current, milestoneDays)
        return StreakStats(current, maxOf(best, current), milestone.next, milestone.progress)
    }

    private enum class DayStatus { CLEAN, CONSUMED, NEUTRAL }

    /**
     * `consumed = null` (p. ej. el ánimo rápido de Inicio) cuenta como día limpio si hay ánimo.
     * Un doc sin ánimo ni consumo (solo "meta lograda") no es un registro: es neutro.
     */
    private fun dayStatus(dayEntries: List<JournalEntry>): DayStatus = when {
        dayEntries.any { it.consumed == true } -> DayStatus.CONSUMED
        dayEntries.any { it.isRegistered } -> DayStatus.CLEAN
        else -> DayStatus.NEUTRAL
    }
}
