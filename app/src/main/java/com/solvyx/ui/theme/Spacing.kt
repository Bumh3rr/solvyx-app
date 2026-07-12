package com.solvyx.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado del Design System Solvyx.
 *
 * Reglas de uso:
 * - En **componentes del core** (carpeta `ui/components/common/`) NUNCA se usa
 *   `padding(16.dp)` directo. Se usa `padding(SolvyxSpacing.lg)`.
 * - En **pantallas** los tokens se siguen recomendando, pero se permite
 *   ajustar valores puntuales cuando la composición lo requiera (siempre
 *   justificado en el header del archivo).
 * - Los nombres siguen la escala `xs / sm / md / lg / xl / xxl`. Es
 *   deliberadamente corta: si necesitas algo entre dos valores, elige
 *   el siguiente y reescala el layout, no inventes un `space.xsPlus`.
 *
 * Escala:
 * - xs  (4dp)  → entre ícono y texto pequeños, gaps mínimos.
 * - sm  (8dp)  → padding interno de chips, separación ícono-texto.
 * - md  (12dp) → separación entre items en una lista.
 * - lg  (16dp) → padding estándar de cards y de pantalla (mínimo).
 * - xl  (24dp) → padding de pantalla cómodo, separación entre cards.
 * - xxl (32dp) → separación entre secciones, padding de hero.
 */
object SolvyxSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}
