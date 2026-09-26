package com.solvyx.backend.common.streak

import com.solvyx.backend.data.model.Achievement
import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakCalculatorImpl @Inject constructor() : StreakCalculator {

    private val milestoneDays = Achievement.MILESTONE_DAYS

    override fun compute(entries: List<JournalEntry>, today: LocalDate): StreakStats {
        // Con doc-por-día hay a lo sumo una entrada por fecha; groupBy queda en grupos de 1.
        val entryMap = entries.groupBy { it.date }

        // Racha actual: hacia atrás desde hoy, se detiene en el primer día sin registro o con
        // consumed = true. `consumed` es nullable; un día con consumed=null (p. ej. ánimo rápido)
        // cuenta como día limpio y no rompe la racha.
        var streak = 0
        var day = today
        while (true) {
            val dayEntries = entryMap[day]
            if (dayEntries == null || dayEntries.any { it.consumed == true }) break
            streak++
            day = day.minusDays(1)
        }

        var best = 0
        var current = 0
        val sortedDates = entryMap.keys.sorted()
        for (i in sortedDates.indices) {
            val d = sortedDates[i]
            val hasConsumption = entryMap[d]!!.any { it.consumed == true }
            if (!hasConsumption) {
                current = if (i > 0 && sortedDates[i - 1] == d.minusDays(1)) current + 1 else 1
                if (current > best) best = current
            } else {
                current = 0
            }
        }
        val bestStreak = maxOf(best, streak)

        val milestone = milestoneProgress(streak, milestoneDays)
        return StreakStats(streak, bestStreak, milestone.next, milestone.progress)
    }
}
