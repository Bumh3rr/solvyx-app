package com.solvyx.backend.common.streak

import com.solvyx.backend.data.local.entity.JournalEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakCalculatorImpl @Inject constructor() : StreakCalculator {

    private val milestoneDays = listOf(3, 7, 10, 15, 30)
    private val zone = ZoneId.systemDefault()

    override fun compute(entries: List<JournalEntity>, today: LocalDate): StreakStats {
        val entryMap = entries.groupBy {
            Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
        }

        // Racha actual: hacia atrás desde hoy, se detiene en el primer día
        // sin registro o con consumed = true.
        var streak = 0
        var day = today
        while (true) {
            val dayEntries = entryMap[day]
            if (dayEntries == null || dayEntries.any { it.consumed }) break
            streak++
            day = day.minusDays(1)
        }

        // Mejor racha histórica: corrida consecutiva más larga sin consumed = true.
        var best = 0
        var current = 0
        val sortedDates = entryMap.keys.sorted()
        for (i in sortedDates.indices) {
            val d = sortedDates[i]
            val hasConsumption = entryMap[d]!!.any { it.consumed }
            if (!hasConsumption) {
                current = if (i > 0 && sortedDates[i - 1] == d.minusDays(1)) current + 1 else 1
                if (current > best) best = current
            } else {
                current = 0
            }
        }
        val bestStreak = maxOf(best, streak)

        // Próximo hito y progreso hacia él.
        val next = milestoneDays.firstOrNull { it > streak }
        val nextMilestone = next ?: milestoneDays.last()
        val progress = if (next != null) {
            val prev = milestoneDays.lastOrNull { it <= streak } ?: 0
            (streak - prev).toFloat() / (next - prev).coerceAtLeast(1)
        } else 1f

        return StreakStats(
            current = streak,
            best = bestStreak,
            nextMilestone = nextMilestone,
            progress = progress
        )
    }
}
