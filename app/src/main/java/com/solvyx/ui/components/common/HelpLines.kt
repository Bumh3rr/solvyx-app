package com.solvyx.ui.components.common

/** Free 24/7 support lines in Mexico. Fixed by product decision — never change these numbers. */
enum class HelpLine(val displayName: String, val dialNumber: String) {
    LINEA_DE_LA_VIDA("Línea de la Vida", "8009112000"),
    SAPTEL("SAPTEL", "5552598121");

    val displayNumber: String get() = formatMexicanPhone(dialNumber)
}

const val EMERGENCY_NUMBER = "911"
