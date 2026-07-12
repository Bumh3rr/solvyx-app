package com.solvyx.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shapes del Design System Solvyx.
 *
 * Se entrega a `MaterialTheme` desde `SolvyxappTheme`. Los componentes
 * del core del sistema deben consumir estos tokens vía
 * `MaterialTheme.shapes.*` y NUNCA instanciar `RoundedCornerShape` directo
 * con valores mágicos.
 *
 * Convención de la app:
 * - `extraSmall` (4dp)  → chips pequeños, radios casi imperceptibles.
 * - `small`      (8dp)  → inputs secundarios, chips medianos.
 * - `medium`     (12dp) → cards estándar del sistema (SolvyxCard-like).
 * - `large`      (16dp) → cards de "feature", contenedores destacados.
 * - `extraLarge` (24dp) → superficies premium, onboarding, hero panels.
 *
 * Algunos componentes existentes (SolvyxButton, SolvyxTextField) usan
 * esquinas pildoradas fijas (28dp, 16dp) por decisión de marca previa a
 * este sistema. Esos casos son legacy intencional y NO se refactorizan
 * desde aquí.
 */
val SolvyxShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
