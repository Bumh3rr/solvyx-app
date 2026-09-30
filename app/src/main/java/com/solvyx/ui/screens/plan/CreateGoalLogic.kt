package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import com.solvyx.R
import com.solvyx.backend.common.goals.GoalSuggestions
import com.solvyx.backend.common.goals.GoalTitles
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.ui.components.common.TrackedSubstances
import java.time.LocalDate

// Pure rules of "Crear mi propia meta": three steps with buttons only (type → substance → number),
// so the title is always generated and there is no free text.

enum class CreateGoalStep { TYPE, SUBSTANCE, AMOUNT }

/**
 * The goal being built. [anySubstance] marks "Cualquier sustancia" (only for days without use);
 * then [substance] stays null. Numbers are always inside their ranges.
 */
data class CreateGoalDraft(
    val step: CreateGoalStep = CreateGoalStep.TYPE,
    val type: GoalType? = null,
    val substance: String? = null,
    val anySubstance: Boolean = false,
    val days: Int = DEFAULT_DAYS,
    val weeklyLimit: Int = GoalSuggestions.MIN_WEEKLY_LIMIT,
    val weeks: Int = DEFAULT_WEEKS
) {
    val hasSubstance: Boolean get() = substance != null || anySubstance

    /** Generated title, or null until type and substance are chosen. */
    val title: String?
        get() = when {
            !hasSubstance -> null
            type == GoalType.SIN_CONSUMO -> GoalTitles.withoutUse(days, substance)
            type == GoalType.REDUCIR_FRECUENCIA -> GoalTitles.reduceFrequency(weeklyLimit, substance)
            else -> null
        }

    companion object {
        val DAYS_RANGE = 1..30
        const val DEFAULT_DAYS = 3
        val WEEKLY_LIMIT_RANGE = GoalSuggestions.MIN_WEEKLY_LIMIT..GoalSuggestions.MAX_WEEKLY_LIMIT
        val WEEKS_RANGE = 1..8
        const val DEFAULT_WEEKS = 3
    }
}

const val WithdrawalWarning =
    "Si bebes mucho, dejarlo de golpe puede sentirse muy mal físicamente. " +
        "Si notas temblores o sudor frío, busca atención médica."

/** Choosing a different type starts over: the substance and numbers depended on it. */
fun CreateGoalDraft.selectType(type: GoalType): CreateGoalDraft =
    if (type == this.type) copy(step = CreateGoalStep.SUBSTANCE)
    else CreateGoalDraft(step = CreateGoalStep.SUBSTANCE, type = type)

/**
 * [substance] null = "Cualquier sustancia". A new substance brings its own proposed weekly limit;
 * coming back to the same one keeps what the user already set.
 */
fun CreateGoalDraft.selectSubstance(substance: String?, defaultWeeklyLimit: Int): CreateGoalDraft {
    val same = hasSubstance && this.substance == substance && anySubstance == (substance == null)
    return copy(
        step = CreateGoalStep.AMOUNT,
        substance = substance,
        anySubstance = substance == null,
        weeklyLimit = if (same) weeklyLimit else defaultWeeklyLimit.coerceIn(CreateGoalDraft.WEEKLY_LIMIT_RANGE)
    )
}

/** One step back, or null to close the sheet from the first step. */
fun CreateGoalDraft.back(): CreateGoalDraft? = when (step) {
    CreateGoalStep.TYPE -> null
    CreateGoalStep.SUBSTANCE -> copy(step = CreateGoalStep.TYPE)
    CreateGoalStep.AMOUNT -> copy(step = CreateGoalStep.SUBSTANCE)
}

fun CreateGoalDraft.withDays(value: Int) = copy(days = value.coerceIn(CreateGoalDraft.DAYS_RANGE))

fun CreateGoalDraft.withWeeklyLimit(value: Int) = copy(weeklyLimit = value.coerceIn(CreateGoalDraft.WEEKLY_LIMIT_RANGE))

fun CreateGoalDraft.withWeeks(value: Int) = copy(weeks = value.coerceIn(CreateGoalDraft.WEEKS_RANGE))

/** The goal to save, or null while the draft is incomplete. */
fun CreateGoalDraft.toGoal(today: LocalDate): Goal? {
    val title = title ?: return null
    return when (type) {
        GoalType.SIN_CONSUMO -> Goal(
            type = type, origin = GoalOrigin.USUARIO, substance = substance,
            title = title, target = days, startDate = today
        )
        GoalType.REDUCIR_FRECUENCIA -> Goal(
            type = type, origin = GoalOrigin.USUARIO, substance = substance,
            title = title, target = weeks, weeklyLimit = weeklyLimit, startDate = today
        )
        null -> null
    }
}

/** A substance option in step 2. [id] null = "Cualquier sustancia". */
data class SubstanceChoice(
    val id: String?,
    val label: String,
    @DrawableRes val icon: Int,
    val enabled: Boolean
)

/**
 * All four substances with the profile ones first, plus "Cualquier sustancia" for days without
 * use (reduce goals always need a specific one). An option equal to an active goal is disabled.
 */
fun substanceChoices(type: GoalType, profileSubstances: List<String>, activeGoals: List<Goal>): List<SubstanceChoice> {
    fun taken(id: String?) = activeGoals.any { it.type == type && it.substance == id }
    val substances = TrackedSubstances
        .sortedBy { if (it.id in profileSubstances) 0 else 1 }
        .map { SubstanceChoice(it.id, it.label, it.icon, enabled = !taken(it.id)) }
    val any = if (type == GoalType.SIN_CONSUMO) {
        listOf(SubstanceChoice(null, "Cualquier sustancia", R.drawable.ic_shield, enabled = !taken(null)))
    } else {
        emptyList()
    }
    return substances + any
}

/** Berto confirms the goal before it is created. */
fun confirmationLine(draft: CreateGoalDraft): BertoLine? {
    val title = draft.title ?: return null
    val supporting = when (draft.type) {
        GoalType.SIN_CONSUMO ->
            "“$title”. Cada día que registras ${GoalTitles.withoutPhrase(draft.substance)} suma."
        GoalType.REDUCIR_FRECUENCIA ->
            "“$title”, durante ${unitCount(draft.weeks, GoalType.REDUCIR_FRECUENCIA)}. Cada semana cuenta por separado."
        null -> return null
    }
    return BertoLine("¡Buena meta!", supporting)
}
