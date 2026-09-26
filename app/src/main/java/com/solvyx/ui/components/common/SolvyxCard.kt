package com.solvyx.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

/**
 * Tarjeta base de Solvyx: la superficie clara con borde sutil que comparten las tarjetas de la app.
 *
 * Centraliza el look en un solo lugar (esquinas 20dp, fondo `surfaceDim` por defecto, elevación 1dp
 * y borde `outline` al 25%) para que todas las vistas se vean iguales y un cambio de estilo se haga
 * una vez. [containerColor] existe solo para los casos donde el fondo mismo comunica un estado real
 * (p. ej. logro bloqueado/desbloqueado) — la forma y el borde nunca varían. Si llega con alpha se
 * aplana sobre `surface`, porque un Card elevado translúcido deja ver su sombra por dentro.
 *
 * Si se pasa [onClick] la tarjeta es interactiva —con el ripple recortado a su forma, sin el
 * `.clip()` extra que rompía la sombra en las esquinas—; si no, es puramente contenedora.
 */
@Composable
fun SolvyxCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    // Always opaque: an elevated Card with a translucent container shows its own shadow through
    // the fill (grey frame + a lighter inner block). Tints are flattened over the screen surface.
    val colors = CardDefaults.cardColors(
        containerColor = containerColor?.compositeOver(MaterialTheme.colorScheme.surface)
            ?: MaterialTheme.colorScheme.surfaceDim
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    }
}
