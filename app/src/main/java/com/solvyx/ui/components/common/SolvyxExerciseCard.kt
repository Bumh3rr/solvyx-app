package com.solvyx.ui.components.common

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.models.Ejercicio
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Tarjeta para listar un [Ejercicio] de regulación emocional.
 *
 * Variantes:
 * - [SolvyxExerciseCardVariant.Default] → card elevada con `primaryContainer`
 *   como fondo. Pensada para grids de ejercicios o listas principales.
 * - [SolvyxExerciseCardVariant.Compact] → fila horizontal densa para
 *   "siguiente ejercicio sugerido" o resultados inline.
 *
 * Comportamiento:
 * - Toda la superficie de la card es tappable (touch target ≥ 48dp).
 * - Si `ejercicio.iconAsset` es `null` o desconocido, se renderiza un
 *   placeholder neutro (círculo con ícono genérico) — nunca se oculta
 *   el slot, para mantener la altura estable.
 * - Duración formateada como `"X min"` (X viene de `duracionMinutos`).
 */
@Composable
fun SolvyxExerciseCard(
    ejercicio: Ejercicio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SolvyxExerciseCardVariant = SolvyxExerciseCardVariant.Default
) {
    when (variant) {
        SolvyxExerciseCardVariant.Default -> ExerciseCardDefault(
            ejercicio = ejercicio,
            onClick = onClick,
            modifier = modifier
        )
        SolvyxExerciseCardVariant.Compact -> ExerciseCardCompact(
            ejercicio = ejercicio,
            onClick = onClick,
            modifier = modifier
        )
    }
}

// ── Default variant ──────────────────────────────────────────────────────────

@Composable
private fun ExerciseCardDefault(
    ejercicio: Ejercicio,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val iconRes = mapIconAsset(ejercicio.iconAsset)
    val openLabel = stringResource(R.string.ejercicio_card_action_iniciar, ejercicio.nombre)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { role = Role.Button },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SolvyxSpacing.lg)
        ) {
            // Header: ícono + duración
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExerciseIconBadge(iconRes = iconRes, size = 44.dp)
                DurationChip(
                    minutes = ejercicio.duracionMinutos,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(SolvyxSpacing.md))
            // Título
            Text(
                text = ejercicio.nombre,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(SolvyxSpacing.xs))
            // Descripción corta
            Text(
                text = ejercicio.descripcionCorta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(SolvyxSpacing.md))
            // CTA
            Text(
                text = stringResource(R.string.ejercicio_iniciar_corto),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.semantics {
                    contentDescription = openLabel
                }
            )
        }
    }
}

// ── Compact variant ──────────────────────────────────────────────────────────

@Composable
private fun ExerciseCardCompact(
    ejercicio: Ejercicio,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val iconRes = mapIconAsset(ejercicio.iconAsset)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(SolvyxSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExerciseIconBadge(
            iconRes = iconRes,
            size = 40.dp,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )
        Spacer(Modifier.width(SolvyxSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ejercicio.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${ejercicio.duracionMinutos} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(SolvyxSpacing.sm))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Helpers privados ─────────────────────────────────────────────────────────

@Composable
private fun ExerciseIconBadge(
    iconRes: Int?,
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.55f)
            )
        } else {
            // Placeholder genérico cuando el asset no resuelve.
            Icon(
                painter = painterResource(R.drawable.ic_activity),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

@Composable
private fun DurationChip(
    minutes: Int,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = SolvyxSpacing.sm, vertical = SolvyxSpacing.xs)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(SolvyxSpacing.xs))
        Text(
            text = "$minutes min",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

enum class SolvyxExerciseCardVariant { Default, Compact }

// ── Previews ─────────────────────────────────────────────────────────────────

private val sampleEjercicio = Ejercicio(
    id = 1,
    slug = "respiracion-4-7-8",
    nombre = "Respiración 4-7-8",
    tipo = "respiracion",
    duracionMinutos = 4,
    descripcionCorta = "Inhala 4, sostén 7, exhala 8. Induce calma profunda.",
    pasos = listOf("Inhala", "Sostén", "Exhala"),
    ttsText = emptyMap(),
    iconAsset = "ic_wind",
    orden = 1,
    activo = true
)

@Preview(name = "SolvyxExerciseCard / Default", showBackground = true)
@Composable
private fun SolvyxExerciseCardDefaultPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxExerciseCard(ejercicio = sampleEjercicio, onClick = {})
        }
    }
}

@Preview(name = "SolvyxExerciseCard / Compact", showBackground = true)
@Composable
private fun SolvyxExerciseCardCompactPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxExerciseCard(
                ejercicio = sampleEjercicio,
                onClick = {},
                variant = SolvyxExerciseCardVariant.Compact
            )
        }
    }
}

@Preview(name = "SolvyxExerciseCard / sin iconAsset", showBackground = true)
@Composable
private fun SolvyxExerciseCardNoIconPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxExerciseCard(
                ejercicio = sampleEjercicio.copy(iconAsset = null),
                onClick = {}
            )
        }
    }
}
