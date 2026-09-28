package com.solvyx.ui.screens.journey.diary

import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.moodOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Quick filter over the diary. Moods are filtered separately, so both can combine. */
enum class UseFilter(val label: String) {
    ALL("Todos"),
    CLEAN("Días limpios"),
    WITH_USE("Con consumo")
}

/** Numbers for the diary hero. [topMoodId] is the most frequent mood, `null` without entries. */
data class DiarySummary(
    val registeredDays: Int,
    val cleanDays: Int,
    val bestStreak: Int,
    val topMoodId: String?
)

data class MonthGroup(val month: YearMonth, val entries: List<JournalEntry>)

/** One day of the mood calendar; [entry] is `null` for days without a check-in. */
data class CalendarDay(val date: LocalDate, val entry: JournalEntry?)

/** Only full check-ins (with mood), newest first. A doc with only "meta lograda" is not a diary day. */
fun diaryEntries(all: List<JournalEntry>): List<JournalEntry> =
    all.filter { it.isRegistered }.sortedByDescending { it.date }

fun summarize(entries: List<JournalEntry>, bestStreak: Int): DiarySummary = DiarySummary(
    registeredDays = entries.size,
    cleanDays = entries.count { it.consumed != true },
    bestStreak = bestStreak,
    topMoodId = entries.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
)

fun filterEntries(entries: List<JournalEntry>, useFilter: UseFilter, moodId: String?): List<JournalEntry> =
    entries.filter { entry ->
        val matchesUse = when (useFilter) {
            UseFilter.ALL -> true
            UseFilter.CLEAN -> entry.consumed != true
            UseFilter.WITH_USE -> entry.consumed == true
        }
        matchesUse && (moodId == null || entry.mood == moodId)
    }

/** Keeps the order of [entries] (newest first), so months come out newest first too. */
fun groupByMonth(entries: List<JournalEntry>): List<MonthGroup> =
    entries.groupBy { YearMonth.from(it.date) }.map { (month, days) -> MonthGroup(month, days) }

/**
 * The days of [month] laid out Monday-first, as the calendar draws them: leading `null`s pad the
 * first week so day 1 falls under its weekday.
 */
fun calendarDays(month: YearMonth, entriesByDate: Map<LocalDate, JournalEntry>): List<CalendarDay?> {
    val leadingBlanks = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
    val days = (1..month.lengthOfMonth()).map { day ->
        val date = month.atDay(day)
        CalendarDay(date, entriesByDate[date])
    }
    return List(leadingBlanks) { null } + days
}

/** Months the calendar can browse: from the oldest entry's month up to [current]. */
fun browsableMonths(entries: List<JournalEntry>, current: YearMonth): List<YearMonth> {
    val oldest = entries.minOfOrNull { it.date }?.let(YearMonth::from) ?: current
    return generateSequence(oldest) { it.plusMonths(1) }.takeWhile { it <= current }.toList()
}

/** "Hoy", "Ayer", "Hace 5 días"… for the story header. */
fun relativeDay(date: LocalDate, today: LocalDate): String =
    when (val days = ChronoUnit.DAYS.between(date, today)) {
        0L -> "Hoy"
        1L -> "Ayer"
        in 2..30 -> "Hace $days días"
        else -> "Hace ${days / 30} ${if (days / 30 == 1L) "mes" else "meses"}"
    }

/** What Berto says about the whole diary. Only states what the entries really show. */
fun bertoRecap(summary: DiarySummary): String {
    val days = summary.registeredDays
    val dayWord = if (days == 1) "día" else "días"
    val mood = summary.topMoodId?.let { moodOption(it).label.lowercase() }
    return when {
        days == 0 -> "Cuando registres tu primer día, aquí lo vamos a recordar juntos."
        summary.cleanDays == days ->
            "Llevas $days $dayWord en tu diario y todos son días limpios. Qué bonito camino."
        mood != null ->
            "Llevas $days $dayWord en tu diario. Tu ánimo más frecuente es $mood. Toca un día para recordarlo."
        else -> "Llevas $days $dayWord en tu diario. Toca un día para recordarlo."
    }
}
