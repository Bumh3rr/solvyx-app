package com.solvyx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Esquema de color modo claro ───────────────────────
private val LightColorScheme = lightColorScheme(
    // Primario → teal principal, botones, top bars
    primary            = TealPrimary,
    onPrimary          = White,
    primaryContainer   = TealLightest,
    onPrimaryContainer = TealDark,

    // Secundario → teal medio, elementos de apoyo
    secondary            = TealMedium,
    onSecondary          = White,
    secondaryContainer   = TealLight,
    onSecondaryContainer = TealDark,

    // Terciario → crisis / SOS (solo para botón SOS y pantalla de crisis)
    tertiary            = CrisisRed,
    onTertiary          = White,
    tertiaryContainer   = CrisisRedLight,
    onTertiaryContainer = CrisisRedDark,

    // Error → mismo rojo de crisis
    error            = CrisisRed,
    onError          = White,
    errorContainer   = CrisisRedLight,
    onErrorContainer = CrisisRedDark,

    // Fondo y superficie
    background  = BackgroundApp,
    onBackground = TealDark,
    surface      = BackgroundApp,
    surfaceDim = White,
    onSurface    = TealDark,

    // Surface variant → usado en campos de input, chips no activos
    surfaceVariant   = TealLightest,
    onSurfaceVariant = TealMedium,

    // Outline → bordes de cards, inputs
    outline        = TealLight,
    outlineVariant = TealLightest,
)

// ── Esquema de color modo oscuro (MVP: similar al claro) ─
private val DarkColorScheme = darkColorScheme(
    primary            = TealMedium,
    onPrimary          = TealDark,
    primaryContainer   = TealDark,
    onPrimaryContainer = TealLightest,

    secondary            = TealLight,
    onSecondary          = TealDark,
    secondaryContainer   = TealDark,
    onSecondaryContainer = TealLight,

    tertiary            = CrisisRed,
    onTertiary          = White,
    tertiaryContainer   = CrisisRedDark,
    onTertiaryContainer = CrisisRedLight,

    error            = CrisisRed,
    onError          = White,
    errorContainer   = CrisisRedDark,
    onErrorContainer = CrisisRedLight,

    background   = TealDark,
    onBackground = TealLightest,
    surface      = Color(0xFF0A3D2E),  // surface oscuro
    surfaceDim      = Color(0xFF0A3D2E),  // surface oscuro
    onSurface    = TealLightest,

    surfaceVariant   = Color(0xFF0F5440),
    onSurfaceVariant = TealLight,

    outline        = TealMedium,
    outlineVariant = TealDark,
)

@Composable
fun SolvyxappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SolvyxTypography,
        content = content
    )
}