package com.solvyx.backend.data.model

import java.time.LocalDate

/** Tipos de meta (docs/app/Solvyx_Firebase.md). `tecnicas_regulacion` queda para después. */
enum class GoalType(val firestoreValue: String) {
    /** "N días sin [sustancia]": +1 por día registrado sin consumir esa sustancia (no tienen que ser seguidos). */
    SIN_CONSUMO("sin_consumo"),

    /** "Máximo N días de [sustancia] por semana, durante M semanas": +1 por semana cumplida. */
    REDUCIR_FRECUENCIA("reducir_frecuencia");

    companion object {
        fun fromFirestore(value: String?): GoalType? = entries.firstOrNull { it.firestoreValue == value }
    }
}

enum class GoalOrigin(val firestoreValue: String) {
    USUARIO("usuario"),
    SUGERIDA_BERTO("sugerida_berto");

    companion object {
        fun fromFirestore(value: String?): GoalOrigin = entries.firstOrNull { it.firestoreValue == value } ?: USUARIO
    }
}

/**
 * Meta de reducción de daños, en `users/{uid}/metas/{id}`.
 *
 * [target] se mide en días (SIN_CONSUMO) o en semanas (REDUCIR_FRECUENCIA). [weeklyLimit] solo
 * existe en REDUCIR_FRECUENCIA. [substance] null = cualquier sustancia (solo metas propias de
 * SIN_CONSUMO). El progreso nunca se reinicia: un día con consumo simplemente no suma.
 */
data class Goal(
    val id: String = "",
    val type: GoalType,
    val origin: GoalOrigin,
    val substance: String?,
    val title: String,
    val target: Int,
    val weeklyLimit: Int? = null,
    val progress: Int = 0,
    val startDate: LocalDate,
    val active: Boolean = true,
    val completed: Boolean = false,
    val completedAt: Long? = null
) {
    val unit: String get() = if (type == GoalType.SIN_CONSUMO) UNIT_DAYS else UNIT_WEEKS

    companion object {
        const val MAX_ACTIVE = 3
        const val UNIT_DAYS = "dias"
        const val UNIT_WEEKS = "semanas"
    }
}
