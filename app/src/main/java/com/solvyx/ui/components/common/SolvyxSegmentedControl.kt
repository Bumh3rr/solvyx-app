package com.solvyx.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Colores del [SolvyxSegmentedControl] según la superficie donde vive. La app tiene dos:
 * el panel claro (default) y la cabecera teal.
 */
data class SolvyxSegmentedColors(
    val container: Color,
    val border: Color,
    val selected: Color,
    val selectedLabel: Color,
    val unselectedLabel: Color
)

object SolvyxSegmentedDefaults {

    /** Sobre el panel claro/crema — el look original de "Semana | Mes" en Mis Avances. */
    @Composable
    fun onSurface() = SolvyxSegmentedColors(
        container = MaterialTheme.colorScheme.surfaceDim,
        border = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        selected = MaterialTheme.colorScheme.primary,
        selectedLabel = Color.White,
        unselectedLabel = MaterialTheme.colorScheme.onSurfaceVariant
    )

    /** Sobre la cabecera teal — pill blanco, mismo lenguaje que los chips de los heroes. */
    @Composable
    fun onPrimary() = SolvyxSegmentedColors(
        container = Color.White.copy(alpha = 0.15f),
        border = Color.White.copy(alpha = 0.25f),
        selected = Color.White,
        selectedLabel = MaterialTheme.colorScheme.primary,
        unselectedLabel = Color.White.copy(alpha = 0.85f)
    )
}

/**
 * Selector de secciones excluyentes con el look ya establecido en el sistema de diseño.
 *
 * Extraído del selector inline "Semana | Mes" de Mis Avances para reutilizarlo en
 * Mi Registro ("Registrar hoy | Historial") sin duplicar el patrón.
 */
@Composable
fun SolvyxSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: SolvyxSegmentedColors = SolvyxSegmentedDefaults.onSurface()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.container)
            .border(
                width = 0.5.dp,
                color = colors.border,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colors.selected else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (selected) colors.selectedLabel else colors.unselectedLabel
                )
            }
        }
    }
}
