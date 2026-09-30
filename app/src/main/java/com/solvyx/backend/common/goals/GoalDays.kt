package com.solvyx.backend.common.goals

import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate

/** What the check-in did for the goals: which ones advanced today and which ones were just completed. */
data class CheckInGoalOutcome(
    val advanced: List<Goal> = emptyList(),
    val completed: List<Goal> = emptyList()
)

/** What a diary day says about the goals: it added to one, and/or it completed some. */
data class DayGoalNote(
    val advanced: Boolean = false,
    val completedTitles: List<String> = emptyList()
)

/**
 * Goals seen day by day, for the check-in result and the diary. Funciones puras.
 *
 * Only days without use goals "advance" day by day; reduce goals add up when a week closes, so a
 * single day never shows as advancing them. Archived goals don't count anywhere.
 */
object GoalDays {

    /**
     * Days-without-use goals that [day] added to, with their progress already updated. [goals]
     * should be the active ones.
     */
    fun advancedOn(goals: List<Goal>, journal: List<JournalEntry>, day: LocalDate): List<Goal> =
        goals
            .filter { it.type == GoalType.SIN_CONSUMO }
            .filter { goal -> GoalProgressCalculator.dayBreakdown(goal, journal, day).lastOrNull()?.let { it.date == day && it.counted } == true }
            .map { it.copy(progress = GoalProgressCalculator.progress(it, journal, day)) }

    /**
     * Diary notes per day. A days-without-use goal advanced on its first [Goal.target] counted
     * days, and the last of them is the day it was completed. A reduce goal was completed on the
     * last registered day of its last week that counted.
     */
    fun dayNotes(goals: List<Goal>, journal: List<JournalEntry>, today: LocalDate): Map<LocalDate, DayGoalNote> {
        val advanced = mutableSetOf<LocalDate>()
        val completed = mutableMapOf<LocalDate, MutableList<String>>()
        goals.filter { it.active || it.completed }.forEach { goal ->
            when (goal.type) {
                GoalType.SIN_CONSUMO -> {
                    val counted = GoalProgressCalculator.dayBreakdown(goal, journal, today)
                        .filter { it.counted }
                        .map { it.date }
                        .take(goal.target)
                    advanced += counted
                    if (goal.completed && counted.size == goal.target) {
                        completed.getOrPut(counted.last()) { mutableListOf() } += goal.title
                    }
                }
                GoalType.REDUCIR_FRECUENCIA -> if (goal.completed) {
                    val lastWeek = GoalProgressCalculator.weekBreakdown(goal, journal, today)
                        .filter { it.status == GoalWeekStatus.COUNTED }
                        .take(goal.target)
                        .lastOrNull()
                    val day = lastWeek?.let { week ->
                        journal.filter { it.consumed != null && !it.date.isBefore(week.start) && !it.date.isAfter(week.end) }
                            .maxOfOrNull { it.date }
                    }
                    if (day != null) completed.getOrPut(day) { mutableListOf() } += goal.title
                }
            }
        }
        return (advanced + completed.keys).associateWith { date ->
            DayGoalNote(advanced = date in advanced, completedTitles = completed[date].orEmpty())
        }
    }
}
