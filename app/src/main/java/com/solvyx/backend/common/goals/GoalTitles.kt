package com.solvyx.backend.common.goals

/**
 * Títulos que genera la app para cada meta (el usuario no escribe texto libre).
 * Ej.: "5 días sin vapear", "Máximo 2 días de alcohol por semana".
 */
object GoalTitles {

    fun withoutUse(days: Int, substance: String?): String = "${days(days)} ${withoutPhrase(substance)}"

    fun reduceFrequency(weeklyLimit: Int, substance: String?): String =
        "Máximo ${days(weeklyLimit)} de ${substanceNoun(substance)} por semana"

    private fun days(n: Int): String = if (n == 1) "1 día" else "$n días"

    /** "sin alcohol", "sin vapear", "sin fumar"… o "sin consumir" (cualquier sustancia). */
    fun withoutPhrase(substance: String?): String = when (substance) {
        "alcohol" -> "sin alcohol"
        "vape" -> "sin vapear"
        "cristal" -> "sin cristal"
        "cigarro" -> "sin fumar"
        else -> "sin consumir"
    }

    private fun substanceNoun(substance: String?): String = when (substance) {
        "alcohol" -> "alcohol"
        "vape" -> "vape"
        "cristal" -> "cristal"
        "cigarro" -> "tabaco"
        else -> "consumo"
    }
}
