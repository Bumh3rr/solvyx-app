package com.solvyx.ui.screens.plan

import com.solvyx.backend.common.goals.GoalProgressCalculator
import com.solvyx.backend.common.goals.GoalTitles
import com.solvyx.backend.common.goals.GoalWeek
import com.solvyx.backend.common.goals.GoalWeekStatus
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Pure text and state rules of "Mi plan", kept apart from Compose so they are unit tested.
// Language rules: "tú", gender-neutral, no failure words; a day or week that doesn't add is
// just "no sumó".

/** What Berto says at the top of "Mi plan". [opensCheckIn] adds the "Registrar mi día →" link. */
data class BertoLine(val message: String, val supporting: String, val opensCheckIn: Boolean = false)

val DefaultBertoLine = BertoLine("Aquí armamos tu plan", "Paso a paso y a tu ritmo.")

/** The last thing that happened during this visit to Plan; it wins over the other messages. */
sealed interface PlanEvent {
    data class GoalCompleted(val title: String) : PlanEvent
    /** A suggestion was accepted or a custom goal was created. */
    data object GoalAdded : PlanEvent
}

/** An active goal ready to draw: live progress from the journal plus its texts. */
data class GoalCardUi(
    val goal: Goal,
    val progress: Int,
    val progressLabel: String,
    val weekLine: String? = null,
    val weekHint: String? = null
) {
    val fraction: Float get() = if (goal.target <= 0) 1f else (progress.toFloat() / goal.target).coerceIn(0f, 1f)

    /** One day or week left. A 1-day goal is "almost" from the start, so it doesn't qualify. */
    val oneStepAway: Boolean get() = goal.target > 1 && progress == goal.target - 1
}

/** True when today's check-in already answered the consumption question (it counts for goals). */
fun answeredToday(journal: List<JournalEntry>, today: LocalDate): Boolean =
    journal.any { it.date == today && it.consumed != null }

/** Priority: what just happened, then one step away, then today's check-in, then the general state. */
fun bertoLine(
    requiresAccount: Boolean,
    activeGoals: List<GoalCardUi>,
    hasSuggestions: Boolean,
    answeredToday: Boolean,
    event: PlanEvent?
): BertoLine {
    if (requiresAccount) return BertoLine("Aquí armamos tu plan", "Con una cuenta guardo tus metas y tu progreso.")
    when (event) {
        is PlanEvent.GoalCompleted -> return BertoLine("¡Cumpliste tu meta!", "“${event.title}” ya es tuya.")
        PlanEvent.GoalAdded -> return BertoLine("¡Hecho! Empezamos hoy.", "Cada día que registras suma.")
        null -> Unit
    }
    val almost = activeGoals.firstOrNull { it.oneStepAway }
    return when {
        almost != null ->
            BertoLine("¡Ya casi!", "Te falta ${unitCount(1, almost.goal.type)} para “${almost.goal.title}”.")
        activeGoals.isEmpty() -> BertoLine(
            "¿Empezamos con una meta pequeña?",
            if (hasSuggestions) "Te dejé algunas ideas abajo." else "Cuando quieras, armamos una juntos."
        )
        !answeredToday -> BertoLine("¿Cómo va tu día?", "Regístralo para que cuente en tus metas.", opensCheckIn = true)
        activeGoals.size >= Goal.MAX_ACTIVE ->
            BertoLine("Tienes ${Goal.MAX_ACTIVE} metas en marcha", "Cuando cumplas una, podemos sumar otra.")
        else -> BertoLine("Vas a tu ritmo", "Cada día que registras cuenta.")
    }
}

/**
 * The card of an active goal. Reduce-frequency cards add how the current week is going and,
 * while the week can still count, a nudge to keep registering.
 */
fun goalCard(goal: Goal, journal: List<JournalEntry>, today: LocalDate): GoalCardUi {
    val progress = GoalProgressCalculator.progress(goal, journal, today)
    val label = "$progress de ${unitCount(goal.target, goal.type)}"
    val limit = goal.weeklyLimit
    val week = GoalProgressCalculator.currentWeek(goal, journal, today)
    if (limit == null || week == null) return GoalCardUi(goal, progress, label)

    // Records this week can still reach: the ones made, the days left and today if not answered yet.
    val reachableRecords = week.registeredDays + week.daysLeft + if (answeredToday(journal, today)) 0 else 1
    val overLimit = week.useDays > limit
    val canStillCount = !overLimit && reachableRecords >= GoalProgressCalculator.MIN_REGISTERED_DAYS_PER_WEEK
    val weekLine = when {
        overLimit -> "Esta semana: ${days(week.useDays)} · la próxima semana empieza de nuevo"
        !canStillCount -> "Esta semana: ${week.useDays} de ${days(limit)} · la próxima semana empieza de nuevo"
        else -> "Esta semana: ${week.useDays} de ${days(limit)} · ${daysLeft(week.daysLeft)}"
    }
    val hint = if (canStillCount && week.registeredDays < GoalProgressCalculator.MIN_REGISTERED_DAYS_PER_WEEK) {
        "Registra tu día para que esta semana cuente"
    } else {
        null
    }
    return GoalCardUi(goal, progress, label, weekLine, hint)
}

/**
 * The check-in result line for the goals that day added to: "Avanzaste en tu meta" with the goal
 * and its progress, or "Avanzaste en N metas". Null when it added to none: nothing negative is said.
 */
fun advancedLine(advanced: List<Goal>): BertoLine? = when (advanced.size) {
    0 -> null
    1 -> advanced.first().let { BertoLine("Avanzaste en tu meta", "${it.title} · ${it.progress} de ${it.target}") }
    else -> BertoLine("Avanzaste en ${advanced.size} metas", advanced.joinToString(" · ") { it.title })
}

/**
 * Goals shown in "Ganas muy fuertes": only the active days-without-use ones (a weekly limit
 * with room left could read as permission right when the cravings hit), the most advanced first.
 */
fun cravingGoals(goals: List<Goal>, journal: List<JournalEntry>, today: LocalDate): List<GoalCardUi> =
    goals
        .filter { it.active && !it.completed && it.type == GoalType.SIN_CONSUMO }
        .map { goalCard(it, journal, today) }
        .sortedByDescending { it.fraction }
        .take(Goal.MAX_ACTIVE)

/** Completed goals, newest first. */
fun completedGoals(goals: List<Goal>): List<Goal> =
    goals.filter { it.completed }.sortedByDescending { it.completedAt ?: 0L }

fun completedLabel(goal: Goal, zone: ZoneId = ZoneId.systemDefault()): String {
    val millis = goal.completedAt ?: return "Cumplida"
    return "Cumplida el ${longDate(Instant.ofEpochMilli(millis).atZone(zone).toLocalDate())}"
}

fun originLabel(goal: Goal): String = when (goal.origin) {
    GoalOrigin.SUGERIDA_BERTO -> "Sugerida por Berto"
    GoalOrigin.USUARIO -> "Tu meta"
}

fun startLabel(goal: Goal): String = "Desde el ${longDate(goal.startDate)}"

/** How a goal adds up, shown in its detail. */
fun ruleLabel(goal: Goal): String = when (goal.type) {
    GoalType.SIN_CONSUMO ->
        "Suma cada día que registras ${GoalTitles.withoutPhrase(goal.substance)}. " +
            "No tienen que ser seguidos."
    GoalType.REDUCIR_FRECUENCIA ->
        "Una semana suma cuando registras al menos ${GoalProgressCalculator.MIN_REGISTERED_DAYS_PER_WEEK} días " +
            "y te mantienes dentro de tu límite."
}

fun weekRangeLabel(week: GoalWeek): String = "${shortDate(week.start)} – ${shortDate(week.end)}"

fun weekStatusLabel(week: GoalWeek, weeklyLimit: Int): String = when (week.status) {
    GoalWeekStatus.COUNTED -> "Sumó ✓"
    GoalWeekStatus.NOT_COUNTED -> "No sumó"
    GoalWeekStatus.IN_PROGRESS -> "En curso · ${week.useDays} de ${days(weeklyLimit)}"
}

/** "1 día", "3 días", "1 semana", "2 semanas" by goal type. */
fun unitCount(n: Int, type: GoalType): String = when (type) {
    GoalType.SIN_CONSUMO -> days(n)
    GoalType.REDUCIR_FRECUENCIA -> if (n == 1) "1 semana" else "$n semanas"
}

private fun days(n: Int): String = if (n == 1) "1 día" else "$n días"

private fun daysLeft(n: Int): String = when (n) {
    0 -> "hoy termina"
    1 -> "queda 1 día"
    else -> "quedan $n días"
}

// Fixed Spanish month names: the JVM and Android disagree on abbreviations ("sep" / "sept.").
private val MonthNames = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)
private val MonthShortNames = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

private fun longDate(date: LocalDate): String = "${date.dayOfMonth} de ${MonthNames[date.monthValue - 1]}"

private fun shortDate(date: LocalDate): String = "${date.dayOfMonth} ${MonthShortNames[date.monthValue - 1]}"
