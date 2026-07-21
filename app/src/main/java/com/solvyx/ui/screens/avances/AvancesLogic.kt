// app/src/main/java/com/solvyx/ui/screens/avances/AvancesLogic.kt
package com.solvyx.ui.screens.avances

import com.solvyx.backend.data.model.JournalEntry
import java.time.LocalDate

const val TAB_HOY = 0
const val TAB_PROGRESO = 1
const val TAB_LOGROS = 2

/** Pasos del wizard del Tab Hoy, en orden. El paso SUSTANCIA solo aplica si hubo consumo. */
enum class WizardStep { ANIMO, NOTA, CONSUMO, SUSTANCIA }

/** 3 pasos si el usuario respondió que NO consumió; 4 en otro caso (Sí o aún sin responder). */
fun totalWizardSteps(consumo: Boolean?): Int = if (consumo == false) 3 else 4

fun isLastWizardStep(stepIndex: Int, consumo: Boolean?): Boolean =
    stepIndex == totalWizardSteps(consumo) - 1

/** Si el paso actual permite avanzar/guardar. Ánimo obligatorio; consumo obligatorio; si consumió, sustancia obligatoria. */
fun canAdvanceWizard(
    stepIndex: Int,
    estadoAnimo: String?,
    consumo: Boolean?,
    sustancia: String?
): Boolean = when (stepIndex) {
    WizardStep.ANIMO.ordinal    -> estadoAnimo != null
    WizardStep.NOTA.ordinal     -> true
    WizardStep.CONSUMO.ordinal  -> consumo != null
    WizardStep.SUSTANCIA.ordinal -> consumo != true || sustancia != null
    else -> false
}

/** "Registrado hoy" para tab default y tarjeta-resumen: doc de hoy con mood Y consumed no nulos. */
fun esRegistroCompleto(entries: List<JournalEntry>, today: LocalDate): Boolean =
    entries.any { it.date == today && it.mood != null && it.consumed != null }

fun defaultTabIndex(registroCompletoHoy: Boolean): Int =
    if (registroCompletoHoy) TAB_PROGRESO else TAB_HOY

fun dateForChartIndex(index: Int, days: List<LocalDate>): LocalDate? = days.getOrNull(index)
