package com.solvyx.ui.components.drawer.model

import com.solvyx.R

enum class NavigationItem(
    val title: String,
    val icon: Int
) {
    // ── Rutina ─────────────────────────────────────────
    Inicio(title = "Inicio",               icon = R.drawable.ic_home),
    Plan(title = "Mi Plan",                icon = R.drawable.ic_plan),
    RegistroEmocional(title = "Registro diario",     icon = R.drawable.ic_trending_up),
    Avances(title = "Mis Avances",         icon = R.drawable.ic_trophy),

    // ── Herramientas ───────────────────────────────────
    Berto(title = "Hablar con Berto",      icon = R.drawable.ic_chat),
    GuiasPrimerosAuxilios(title = "Guías de Primeros Auxilios", icon = R.drawable.ic_guide),
    Directorio(title = "Directorio Profesional", icon = R.drawable.ic_building),

    // ── Mi Cuenta ──────────────────────────────────────
    MiPerfil(title = "Mi Perfil",          icon = R.drawable.ic_person),
    CerrarSesion(title = "Cerrar sesión",  icon = R.drawable.ic_logout),

    // ── Acceso interno (no aparece en el drawer) ───────
    RedApoyo(title = "Mi Red de apoyo",    icon = R.drawable.ic_people)
}

fun NavigationItem.isRutina(): Boolean =
    this in listOf(NavigationItem.Inicio, NavigationItem.Plan,
                   NavigationItem.RegistroEmocional, NavigationItem.Avances)

fun NavigationItem.isHerramientas(): Boolean =
    this in listOf(NavigationItem.Berto, NavigationItem.GuiasPrimerosAuxilios,
                   NavigationItem.Directorio)

fun NavigationItem.isMiCuenta(): Boolean =
    this in listOf(NavigationItem.MiPerfil, NavigationItem.CerrarSesion)

// Kept for compatibility with NavigationItemView
fun NavigationItem.isPrimary(): Boolean = isRutina()
fun NavigationItem.isSecondary(): Boolean = isHerramientas()
fun NavigationItem.isBottom(): Boolean = this == NavigationItem.CerrarSesion
