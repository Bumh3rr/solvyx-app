package com.solvyx.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.components.drawer.NewBadge

/**
 * Categoría semántica de una [SolvyxAccessCard]. Define el color de
 * fondo y el color del ícono para que el usuario identifique visualmente
 * el tipo de recurso al que está accediendo.
 */
enum class AccessCategory(val tintColor: Color) {
    Calm(Color(0xFF0F766E)),        // TealPrimary - Ejercicios, Rutinas
    Learn(Color(0xFF1E40AF)),       // Blue - Psicoeducación, Insights
    Express(Color(0xFFD97706)),     // Amber - Journaling
    Support(Color(0xFFE24B4A)),     // Red - Guías (de primeros auxilios)
    Discover(Color(0xFF7C3AED))     // Purple - Descubrir
}

/**
 * Card de acceso rápido usada en Home y "Descubrir".
 *
 * Renderiza un ícono grande + título + descripción de 1 línea.
 * El fondo es `MaterialTheme.colorScheme.surface` con un toque de
 * color de la categoría.
 *
 * @param title Título principal (≤ 16 caracteres recomendado).
 * @param description Descripción de 1 línea (≤ 40 caracteres).
 * @param iconRes drawable resource del ícono.
 * @param category Categoría semántica (afecta el color del ícono).
 * @param isNew Si true, muestra badge "NUEVO".
 * @param onClick Callback al tap.
 */
@Composable
fun SolvyxAccessCard(
    title: String,
    description: String,
    iconRes: Int,
    category: AccessCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNew: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = category.tintColor.copy(alpha = 0.18f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(14.dp)
            .semantics {
                contentDescription = if (isNew) {
                    "$title. Nuevo. $description"
                } else {
                    "$title. $description"
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(category.tintColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = category.tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (isNew) {
                    Spacer(Modifier.weight(1f))
                    NewBadge()
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
