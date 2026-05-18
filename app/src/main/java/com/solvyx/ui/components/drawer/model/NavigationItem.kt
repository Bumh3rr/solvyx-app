package com.solvyx.ui.components.drawer.model

import com.solvyx.R

enum class NavigationItem(
    val title: String,
    val icon: Int
) {
    // ── Sección principal ─────────────────────────────
    Inicio(
        title = "Inicio",
        icon = R.drawable.ic_home
    ),
    Berto(
        title = "Hablar con Berto",
        icon = R.drawable.ic_chat
    ),
    PlanReduccion(
        title = "Plan de Reducción de Daños",
        icon = R.drawable.ic_plan
    ),
    RegistroEmocional(
        title = "Registro Emocional",
        icon = R.drawable.ic_trending_up
    ),
    GuiasPrimerosAuxilios(
        title = "Guías de Primeros Auxilios",
        icon = R.drawable.ic_guide
    ),

    // ── Sección secundaria ────────────────────────────
    RedApoyo(
        title = "Mi Red de apoyo",
        icon = R.drawable.ic_people
    ),
    Directorio(
        title = "Directorio Profesional",
        icon = R.drawable.ic_building
    ),
    MiPerfil(
        title = "Mi Perfil",
        icon = R.drawable.ic_person
    ),

    // ── Sección bottom ────────────────────────────────
    CerrarSesion(
        title = "Cerrar sesión",
        icon = R.drawable.ic_logout
    )
}

fun NavigationItem.isPrimary(): Boolean =
    this in listOf(
        NavigationItem.Inicio,
        NavigationItem.Berto,
        NavigationItem.PlanReduccion,
        NavigationItem.RegistroEmocional,
        NavigationItem.GuiasPrimerosAuxilios
    )

fun NavigationItem.isSecondary(): Boolean =
    this in listOf(
        NavigationItem.RedApoyo,
        NavigationItem.Directorio,
        NavigationItem.MiPerfil
    )

fun NavigationItem.isBottom(): Boolean =
    this == NavigationItem.CerrarSesion
