package com.solvyx.ui.screens.profilesetup

import androidx.annotation.DrawableRes
import com.solvyx.R

/**
 * The 3 steps of the profile setup wizard, which runs once after creating an email account
 * (fresh registration or anonymous→email conversion). Not to be confused with `Routes.ONBOARDING`
 * (the introductory carousel shown before `AuthChoice`) — these are distinct concepts.
 * [label], [summary] and [icon] feed the shared step header and the welcome screen.
 */
enum class ProfileSetupStep(
    val label: String,
    val summary: String,
    @DrawableRes val icon: Int
) {
    SUBSTANCES("Sustancias", "Elige qué quieres cuidar", R.drawable.ic_activity),
    ASSIST("Diagnóstico", "Unas preguntas rápidas de la OMS", R.drawable.ic_clipboard),
    RED_APOYO("Red de apoyo", "Quién te acompaña si pides ayuda", R.drawable.ic_people)
}
