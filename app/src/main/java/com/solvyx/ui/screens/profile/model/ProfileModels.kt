package com.solvyx.ui.screens.profile.model

import androidx.annotation.DrawableRes
import com.solvyx.R
import com.solvyx.ui.components.common.AssistRiskLevel

/** Latest ASSIST result, already formatted for display. */
data class AssistSummary(
    val level: AssistRiskLevel,
    val score: Int,
    val substanceLabel: String,
    val date: String
)

/** One setup step that makes the app able to help in a crisis. */
enum class SafetyStep(
    @DrawableRes val icon: Int,
    val pendingTitle: String,
    val doneTitle: String,
    val hint: String
) {
    SUPPORT_CONTACTS(
        R.drawable.ic_people,
        pendingTitle = "Agrega un contacto de confianza",
        doneTitle = "Tienes contactos de confianza",
        hint = "Sin contactos, el botón SOS no avisa a nadie."
    ),
    ASSESSMENT(
        R.drawable.ic_clipboard,
        pendingTitle = "Haz tu diagnóstico ASSIST",
        doneTitle = "Diagnóstico hecho",
        hint = "Ayuda a Berto a darte mejores recomendaciones."
    ),
    SUBSTANCES(
        R.drawable.ic_activity,
        pendingTitle = "Elige qué sustancias seguir",
        doneTitle = "Sustancias elegidas",
        hint = "Así tu plan y tu registro se adaptan a ti."
    ),
    ACCOUNT(
        R.drawable.ic_add_user,
        pendingTitle = "Crea tu cuenta",
        doneTitle = "Cuenta creada",
        hint = "Si cierras sesión como invitado, pierdes tu progreso."
    )
}

data class SafetyItem(val step: SafetyStep, val done: Boolean)
