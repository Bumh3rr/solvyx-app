package com.solvyx.ui.components.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.models.GuiaExtendida
import com.solvyx.backend.models.ContenidoGuia
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Tarjeta para una [GuiaExtendida] de primeros auxilios psicológicos.
 *
 * Decisiones:
 * - El color del chip de categoría sale de la paleta de "contenedores"
 *   de Material 3 (`errorContainer` para crisis, `tertiaryContainer`
 *   para consumo, `secondaryContainer` para craving, `surfaceVariant`
 *   para el resto). Esto evita inventar colores ad-hoc por categoría
 *   y mantiene coherencia con el dark mode.
 * - El ícono se deriva de la categoría (no del `iconAsset` del modelo)
 *   porque el ícono es **semántico** (qué tipo de ayuda), no
 *   decorativo. Si la categoría es desconocida, cae a un ícono genérico.
 * - La card entera es tappable vía `Card(onClick=...)`.
 */
@Composable
fun SolvyxGuiaCard(
    guia: GuiaExtendida,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (chipBg, chipFg) = categoriaColors(guia.categoria)
    @DrawableRes
    val iconRes = categoriaIcon(guia.categoria)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SolvyxSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de categoría
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(SolvyxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                // Chip de categoría
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(chipBg)
                        .padding(horizontal = SolvyxSpacing.sm, vertical = SolvyxSpacing.xs)
                ) {
                    Text(
                        text = guia.categoria.replace("_", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = chipFg
                    )
                }
                Spacer(Modifier.height(SolvyxSpacing.sm))
                // Título
                Text(
                    text = guia.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                // Descripción corta
                Text(
                    text = guia.descripcionCorta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun categoriaColors(categoria: String): Pair<Color, Color> = when (categoria.lowercase()) {
    "crisis" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    "consumo", "post_consumo", "consumo_no_planeado" ->
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    "craving" ->
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    else ->
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
}

@DrawableRes
private fun categoriaIcon(categoria: String): Int = when (categoria.lowercase()) {
    "crisis" -> R.drawable.ic_heart_pulse
    "consumo" -> R.drawable.ic_droplet
    "post_consumo", "consumo_no_planeado" -> R.drawable.ic_activity
    "craving" -> R.drawable.ic_brain
    "noches" -> R.drawable.ic_alert_circle
    "familia" -> R.drawable.ic_people
    "violencia" -> R.drawable.ic_alert_triangle
    else -> R.drawable.ic_info_circle
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val sampleGuiaCrisis = GuiaExtendida(
    id = 1,
    slug = "crisis-panico-agudo",
    titulo = "Estoy teniendo una crisis de pánico ahora",
    categoria = "crisis",
    descripcionCorta = "Pasos inmediatos para bajar el nivel de activación cuando sientes que el cuerpo se desborda.",
    contenido = ContenidoGuia(
        introduccion = "",
        pasos = emptyList(),
        senalesAlerta = emptyList(),
        cuandoLlamar911 = emptyList(),
        lineasAyuda = emptyList()
    ),
    iconAsset = null,
    orden = 1,
    activo = true
)

private val sampleGuiaCraving = GuiaExtendida(
    id = 4,
    slug = "craving-intenso",
    titulo = "Tengo un craving intenso ahora",
    categoria = "craving",
    descripcionCorta = "Estrategias en el momento para resistir la urgencia sin ponerte en riesgo.",
    contenido = ContenidoGuia(
        introduccion = "",
        pasos = emptyList(),
        senalesAlerta = emptyList(),
        cuandoLlamar911 = emptyList(),
        lineasAyuda = emptyList()
    ),
    iconAsset = null,
    orden = 1,
    activo = true
)

@Preview(name = "SolvyxGuiaCard / crisis", showBackground = true)
@Composable
private fun SolvyxGuiaCardCrisisPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxGuiaCard(guia = sampleGuiaCrisis, onClick = {})
        }
    }
}

@Preview(name = "SolvyxGuiaCard / craving", showBackground = true)
@Composable
private fun SolvyxGuiaCardCravingPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxGuiaCard(guia = sampleGuiaCraving, onClick = {})
        }
    }
}
