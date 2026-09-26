package com.solvyx.ui.screens.profile.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.solvyx.R
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.RiskLow
import com.solvyx.ui.theme.RiskLowContainer
import com.solvyx.ui.theme.RiskModerate
import com.solvyx.ui.theme.WarnAmber

/**
 * ASSIST risk band. [storedValue] is what `LastAssistEntity.level` holds; [zoneCenter] is where
 * the gauge marker rests (0..1) — the center of the band, since the exact score thresholds differ
 * per substance and the gauge only claims the band, not a precise position.
 */
enum class AssistRiskLevel(
    val storedValue: String,
    val label: String,
    val accent: Color,
    val container: Color,
    val zoneCenter: Float
) {
    LOW("BAJO", "Bajo", RiskLow, RiskLowContainer, 1f / 6f),
    MODERATE("MODERADO", "Moderado", RiskModerate, WarnAmber, 3f / 6f),
    HIGH("ALTO", "Alto", CrisisRed, CrisisRedLight, 5f / 6f);

    companion object {
        fun fromStored(value: String): AssistRiskLevel? = entries.firstOrNull { it.storedValue == value }
    }
}

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
