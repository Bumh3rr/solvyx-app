package com.solvyx.ui.components.drawer.model

import com.solvyx.R

enum class NavigationItem(
    val title: String,
    val icon: Int
) {
    // ── Rutina ─────────────────────────────────────────
    Inicio(title = "Inicio",               icon = R.drawable.ic_home),
    Plan(title = "Mi Plan",                icon = R.drawable.ic_plan),
    Avances(title = "Mi camino",           icon = R.drawable.ic_footsteps),

    // ── Herramientas ───────────────────────────────────
    Berto(title = "Hablar con Berto",      icon = R.drawable.ic_chat),
    GuiasPrimerosAuxilios(title = "Guías de Primeros Auxilios", icon = R.drawable.ic_guide),
    Directorio(title = "Directorio Profesional", icon = R.drawable.ic_building),

    // ── Mi Cuenta ──────────────────────────────────────
    MiPerfil(title = "Mi Perfil",          icon = R.drawable.ic_person),

    // ── Acceso interno (no aparece en el drawer) ───────
    RedApoyo(title = "Mi Red de apoyo",    icon = R.drawable.ic_people)
}

fun NavigationItem.isRutina(): Boolean = this in listOf(NavigationItem.Inicio)

fun NavigationItem.isHerramientas(): Boolean =
    this in listOf(NavigationItem.Berto, NavigationItem.GuiasPrimerosAuxilios, NavigationItem.Directorio)

// Kept for compatibility with NavigationItemView
fun NavigationItem.isPrimary(): Boolean = isRutina()
fun NavigationItem.isSecondary(): Boolean = isHerramientas()
