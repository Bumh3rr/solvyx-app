package com.solvyx.backend.common.streak

import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate

data class StreakStats(
    val current: Int,
    val best: Int,
    val nextMilestone: Int,
    val progress: Float
)

interface StreakCalculator {
    fun compute(entries: List<JournalEntry>, today: LocalDate): StreakStats
}
