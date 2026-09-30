package com.solvyx.ui.screens.journey.progress

import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.moodOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val SpanishMexico = Locale("es", "MX")
// Fixed three-letter months: the JDK gives "sept" for September, uneven next to the others.
private val MonthAbbreviations = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
private const val DAYS_IN_WEEK = 7L
private const val MIN_DAYS_FOR_PATTERN = 5
private const val MIN_ENTRIES_TO_COMPARE_MOOD = 2
private const val MIN_CLEAN_DAYS_FOR_CORRELATION = 3
private const val MIN_USE_DAYS_FOR_CORRELATION = 2
private const val MIN_REPEATS_FOR_WEEKDAY = 2
// On the 1–10 mood scale, less than this is "about the same".
private const val MEANINGFUL_MOOD_GAP = 1f

/** One column of "Mi semana". [entry] is `null` when nothing was written that day. */
data class WeekDay(val date: LocalDate, val entry: JournalEntry?, val isToday: Boolean, val isFuture: Boolean)

/** Everything "Mi semana" shows for one week. */
data class WeekView(
    val days: List<WeekDay>,
    val summary: WeekSummary,
    val label: String,
    val insights: List<String>,
    val canGoBack: Boolean,
    val canGoForward: Boolean
)

/** The week's numbers. [topMoodId] is `null` when nothing was written that week. */
data class WeekSummary(val written: Int, val clean: Int, val topMoodId: String?)

fun weekStartOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun weekDays(weekStart: LocalDate, entriesByDate: Map<LocalDate, JournalEntry>, today: LocalDate): List<WeekDay> =
    (0 until DAYS_IN_WEEK).map { offset ->
        val date = weekStart.plusDays(offset)
        WeekDay(date, entriesByDate[date]?.takeIf { it.isRegistered }, date == today, date.isAfter(today))
    }

fun weekSummary(days: List<WeekDay>): WeekSummary {
    val written = days.mapNotNull { it.entry }
    return WeekSummary(
        written = written.size,
        clean = written.count { it.consumed != true },
        topMoodId = written.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
    )
}

/** Week starts the arrows can reach: from the oldest entry's week up to this week, oldest first. */
fun browsableWeeks(entries: List<JournalEntry>, today: LocalDate): List<LocalDate> {
    val current = weekStartOf(today)
    val oldest = entries.filter { it.isRegistered }.minOfOrNull { it.date }?.let(::weekStartOf) ?: current
    return generateSequence(oldest) { it.plusWeeks(1) }.takeWhile { !it.isAfter(current) }.toList()
}

/** "21–27 sep" or "29 sep – 5 oct". */
fun weekLabel(weekStart: LocalDate): String {
    val end = weekStart.plusDays(DAYS_IN_WEEK - 1)
    val startMonth = MonthAbbreviations[weekStart.monthValue - 1]
    val endMonth = MonthAbbreviations[end.monthValue - 1]
    return if (weekStart.month == end.month) {
        "${weekStart.dayOfMonth}–${end.dayOfMonth} $endMonth"
    } else {
        "${weekStart.dayOfMonth} $startMonth – ${end.dayOfMonth} $endMonth"
    }
}

/**
 * What Berto notices, newest facts first. Each line is only said when the diary really shows it;
 * with too little data there is a single, honest "not yet" line.
 */
fun weeklyInsights(entries: List<JournalEntry>, weekStart: LocalDate): List<String> {
    val written = entries.filter { it.isRegistered }
    val thisWeek = written.inWeek(weekStart)
    val lastWeek = written.inWeek(weekStart.minusWeeks(1))
    val insights = listOfNotNull(
        writingComparison(thisWeek.size, lastWeek.size),
        moodComparison(thisWeek, lastWeek),
        cleanDaysMoodPattern(written),
        hardestWeekday(written)
    )
    return insights.ifEmpty { listOf(notEnoughYet(written)) }
}

private fun List<JournalEntry>.inWeek(weekStart: LocalDate): List<JournalEntry> {
    val end = weekStart.plusDays(DAYS_IN_WEEK)
    return filter { !it.date.isBefore(weekStart) && it.date.isBefore(end) }
}

private fun writingComparison(thisWeek: Int, lastWeek: Int): String? = when {
    thisWeek == 0 || lastWeek == 0 -> null
    thisWeek > lastWeek -> "Esta semana escribiste ${daysText(thisWeek)}, ${thisWeek - lastWeek} más que la anterior. Vas construyendo el hábito."
    thisWeek < lastWeek -> "Esta semana llevas ${daysText(thisWeek)}; la anterior fueron $lastWeek. Cada registro cuenta."
    else -> "Escribiste lo mismo que la semana pasada: ${daysText(thisWeek)}. Constancia pura."
}

private fun moodComparison(thisWeek: List<JournalEntry>, lastWeek: List<JournalEntry>): String? {
    if (thisWeek.size < MIN_ENTRIES_TO_COMPARE_MOOD || lastWeek.size < MIN_ENTRIES_TO_COMPARE_MOOD) return null
    val gap = thisWeek.averageMood() - lastWeek.averageMood()
    return when {
        gap >= MEANINGFUL_MOOD_GAP -> "Tu ánimo de esta semana va mejor que el de la semana pasada."
        gap <= -MEANINGFUL_MOOD_GAP -> "Esta semana ha pesado más que la anterior. Aquí sigo contigo."
        else -> "Tu ánimo se ha mantenido parecido al de la semana pasada."
    }
}

private fun cleanDaysMoodPattern(written: List<JournalEntry>): String? {
    val clean = written.filter { it.consumed != true }
    val withUse = written.filter { it.consumed == true }
    if (clean.size < MIN_CLEAN_DAYS_FOR_CORRELATION || withUse.size < MIN_USE_DAYS_FOR_CORRELATION) return null
    val gap = clean.averageMood() - withUse.averageMood()
    return if (gap >= MEANINGFUL_MOOD_GAP) {
        "Tus días sin consumo suelen ser días de mejor ánimo. Vale la pena recordarlo cuando lleguen las ganas."
    } else {
        null
    }
}

private fun hardestWeekday(written: List<JournalEntry>): String? {
    if (written.size < MIN_DAYS_FOR_PATTERN) return null
    val useDays = written.filter { it.consumed == true }
    val (weekday, count) = useDays.groupingBy { it.date.dayOfWeek }.eachCount().maxByOrNull { it.value } ?: return null
    if (count < MIN_REPEATS_FOR_WEEKDAY) return null
    val name = weekday.getDisplayName(TextStyle.FULL, SpanishMexico).let { if (it.endsWith("s")) it else "${it}s" }
    return "Los $name concentran tu mayor consumo registrado ($count de ${useDays.size}). Planear algo distinto ese día puede ayudarte."
}

private fun notEnoughYet(written: List<JournalEntry>): String = when {
    written.size >= MIN_DAYS_FOR_PATTERN && written.none { it.consumed == true } ->
        "En ${written.size} días registrados no reportaste consumo. Ese es un patrón que vale la pena sostener."
    else -> "Aún no tengo suficientes registros para ver patrones. Registra unos días más y aquí te cuento lo que encuentre."
}

private fun List<JournalEntry>.averageMood(): Float = map { moodOption(it.mood).value }.average().toFloat()

private fun daysText(count: Int): String = if (count == 1) "1 día" else "$count días"
