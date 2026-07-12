package com.solvyx.ui.components.drawer.model

import com.solvyx.R

/**
 * Items del **drawer** lateral de Solvyx.
 *
 * Organización:
 *
 * - **RUTINA**: Inicio · Plan · Registro diario · Mis Avances · Rutinas
 *
 * - **HERRAMIENTAS**: Hablar con Berto · Ejercicios · Guías de Primeros
 *   Auxilios (con submenú) · Psicoeducación · Journaling · Insights de
 *   Berto · Directorio Profesional · Descubrir Solvyx
 *
 * - **MI CUENTA**: Mi Perfil · Cerrar sesión
 *
 * Los items con sufijo `*` se renderizan en drawer pero **no** son
 * destinos top-level de NavGraph — viven dentro de MainScreen como
 * ramas del shell (igual que `Inicio`, `Plan`, etc.).
 *
 * Los items marcados con `isNew = true` muestran el badge "NUEVO" la
 * primera vez que el usuario los descubre. El badge se oculta al
 * primer tap.
 */
enum class NavigationItem(
    val title: String,
    val icon: Int,
    val isNew: Boolean = false
) {
    // ── Rutina ─────────────────────────────────────────
    Inicio(title = "Inicio",                  icon = R.drawable.ic_home),
    Plan(title = "Mi Plan",                   icon = R.drawable.ic_plan),
    RegistroEmocional(title = "Registro diario", icon = R.drawable.ic_trending_up),
    Avances(title = "Mis Avances",            icon = R.drawable.ic_trophy),
    Rutinas(title = "Rutinas",                icon = R.drawable.ic_calendar, isNew = true),

    // ── Herramientas ───────────────────────────────────
    Berto(title = "Hablar con Berto",         icon = R.drawable.ic_chat),
    Ejercicios(title = "Ejercicios",           icon = R.drawable.ic_wind, isNew = true),
    GuiasPrimerosAuxilios(
        title = "Guías de primeros auxilios", icon = R.drawable.ic_guide
    ),
    Psicoeducacion(title = "Psicoeducación",    icon = R.drawable.ic_brain, isNew = true),
    Journaling(title = "Journaling",            icon = R.drawable.ic_pencil, isNew = true),
    Insights(title = "Insights de Berto",       icon = R.drawable.ic_chart_bar, isNew = true),
    Directorio(title = "Directorio Profesional", icon = R.drawable.ic_building),
    Descubrir(title = "Descubrir Solvyx",        icon = R.drawable.ic_gem, isNew = true),

    // ── Mi Cuenta ──────────────────────────────────────
    MiPerfil(title = "Mi Perfil",            icon = R.drawable.ic_person),
    CerrarSesion(title = "Cerrar sesión",    icon = R.drawable.ic_logout),

    // ── Acceso interno (no aparece en el drawer) ───────
    RedApoyo(title = "Mi Red de apoyo",      icon = R.drawable.ic_people)
}

fun NavigationItem.isRutina(): Boolean =
    this in listOf(
        NavigationItem.Inicio, NavigationItem.Plan,
        NavigationItem.RegistroEmocional, NavigationItem.Avances,
        NavigationItem.Rutinas
    )

fun NavigationItem.isHerramientas(): Boolean =
    this in listOf(
        NavigationItem.Berto, NavigationItem.Ejercicios,
        NavigationItem.GuiasPrimerosAuxilios, NavigationItem.Psicoeducacion,
        NavigationItem.Journaling, NavigationItem.Insights,
        NavigationItem.Directorio, NavigationItem.Descubrir
    )

fun NavigationItem.isMiCuenta(): Boolean =
    this in listOf(NavigationItem.MiPerfil, NavigationItem.CerrarSesion)

/** Items que muestran el badge "NUEVO" en el drawer. */
val NavigationItem.isNewFeature: Boolean get() = this.isNew

/** Para compatibilidad con NavigationItemView. */
fun NavigationItem.isPrimary(): Boolean = isRutina()
fun NavigationItem.isSecondary(): Boolean = isHerramientas()
fun NavigationItem.isBottom(): Boolean = this == NavigationItem.CerrarSesion
