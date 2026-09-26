package com.solvyx.ui.screens.directory.model

import androidx.annotation.DrawableRes
import com.solvyx.R

/** Kind of help an entry offers. Declaration order is the order sections appear in the list. */
enum class DirectoryCategory(
    val pluralLabel: String,
    val singularLabel: String,
    val sectionTitle: String,
    @DrawableRes val icon: Int,
    val callScript: String
) {
    HELPLINE(
        pluralLabel = "Líneas",
        singularLabel = "Línea de apoyo",
        sectionTitle = "Líneas de apoyo",
        icon = R.drawable.ic_phone,
        callScript = "Hola, estoy pasando por un momento difícil y quiero hablar con alguien."
    ),
    CENTER(
        pluralLabel = "Centros",
        singularLabel = "Centro de atención",
        sectionTitle = "Centros de atención",
        icon = R.drawable.ic_building,
        callScript = "Hola, quiero información para recibir atención. ¿Qué necesito para una primera cita?"
    ),
    PSYCHOLOGIST(
        pluralLabel = "Psicólogos",
        singularLabel = "Psicólogo",
        sectionTitle = "Psicólogos",
        icon = R.drawable.ic_user,
        callScript = "Hola, me gustaría agendar una primera consulta. ¿Qué días tiene disponibles y cuál es el costo?"
    )
}

/**
 * One real place or person in the professional directory. Hardcoded on purpose (business rule):
 * see `DirectoryData`. [cost] is null when the provider didn't publish a price.
 */
data class DirectoryEntry(
    val id: String,
    val name: String,
    val category: DirectoryCategory,
    val description: String,
    val phone: String,
    val address: String? = null,
    val schedule: String? = null,
    val specialty: String? = null,
    val appointment: String? = null,
    val cost: String? = null,
    val isFree: Boolean = false,
    val available24h: Boolean = false,
    val isVerified: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tags: List<String> = emptyList()
)
