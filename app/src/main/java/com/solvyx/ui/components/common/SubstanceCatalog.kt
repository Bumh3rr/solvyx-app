package com.solvyx.ui.components.common

import androidx.annotation.DrawableRes
import com.solvyx.R

/** A substance the app can track. [id] is the value stored in Room/Firestore. */
data class SubstanceOption(val id: String, val label: String, @DrawableRes val icon: Int)

/**
 * The only valid substances (business rule): alcohol · cristal · vape · cigarro — shown to the user
 * as "Tabaco". Single source for every picker in the app; never add "cannabis" or a "tabaco" id.
 */
val TrackedSubstances: List<SubstanceOption> = listOf(
    SubstanceOption("alcohol", "Alcohol", R.drawable.ic_bottle),
    SubstanceOption("cristal", "Cristal", R.drawable.ic_gem),
    SubstanceOption("vape", "Vape", R.drawable.ic_vape),
    SubstanceOption("cigarro", "Tabaco", R.drawable.ic_cigarette)
)

fun substanceLabel(id: String): String = TrackedSubstances.firstOrNull { it.id == id }?.label ?: id
