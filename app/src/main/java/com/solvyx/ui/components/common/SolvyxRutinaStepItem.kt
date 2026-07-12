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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.models.RutinaPaso
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Item de un paso de rutina. Se usa dentro de listas verticales de
 * pasos (matutina, nocturna) bajo un `LazyColumn`.
 *
 * - Toda la fila es tappable con touch target ≥ 48dp. Tap = `onToggle()`.
 * - Cuando [completado] es `true`:
 *   - El checkbox queda activo.
 *   - El título se renderiza tachado.
 *   - Toda la fila baja a alpha 0.55 para reforzar visualmente que ya
 *     está "en el pasado" pero sigue visible (la idea es que el usuario
 *     vea su progreso).
 * - La duración (`duracionSegundos`) se muestra como chip a la derecha.
 *   Si dura 60s o menos, se muestra "Xs"; si dura más, "Xm Ys".
 */
@Composable
fun SolvyxRutinaStepItem(
    paso: RutinaPaso,
    completado: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerAlpha = if (completado) 0.55f else 1f
    val titleDecoration = if (completado) TextDecoration.LineThrough else TextDecoration.None
    val completedLabel = stringResource(R.string.state_completed)
    val pendingLabel = stringResource(R.string.state_pending)
    val a11yText = stringResource(
        R.string.rutina_paso_label,
        paso.orden,
        paso.titulo,
        paso.descripcion,
        paso.duracionSegundos,
        if (completado) completedLabel else pendingLabel
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClick = onToggle,
                // Anuncia acción: alternar estado del paso.
                onClickLabel = a11yText
            )
            .semantics(mergeDescendants = true) {
                contentDescription = a11yText
                stateDescription = if (completado) completedLabel else pendingLabel
            }
            .padding(SolvyxSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = completado,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(Modifier.width(SolvyxSpacing.sm))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = containerAlpha))
        ) {
            Text(
                text = paso.titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = titleDecoration
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (paso.descripcion.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = paso.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(SolvyxSpacing.sm))
        DuracionChip(seconds = paso.duracionSegundos)
    }
}

@Composable
private fun DuracionChip(seconds: Int) {
    val label = formatDuration(seconds)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = SolvyxSpacing.sm, vertical = SolvyxSpacing.xs)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(SolvyxSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Int): String =
    if (seconds < 60) "${seconds}s"
    else {
        val m = seconds / 60
        val s = seconds % 60
        if (s == 0) "${m}m" else "${m}m ${s}s"
    }

// ── Previews ─────────────────────────────────────────────────────────────────

private val samplePaso = RutinaPaso(
    id = 1,
    rutinaId = 1,
    orden = 1,
    titulo = "Respiración 4-7-8",
    descripcion = "Cuatro ciclos de inhalar 4, sostener 7, exhalar 8.",
    duracionSegundos = 120,
    iconAsset = "ic_wind"
)

private val samplePasoCorto = RutinaPaso(
    id = 2,
    rutinaId = 1,
    orden = 2,
    titulo = "Anotar ánimo",
    descripcion = "",
    duracionSegundos = 30,
    iconAsset = null
)

@Preview(name = "SolvyxRutinaStepItem / pendiente", showBackground = true)
@Composable
private fun SolvyxRutinaStepItemPendientePreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxRutinaStepItem(paso = samplePaso, completado = false, onToggle = {})
        }
    }
}

@Preview(name = "SolvyxRutinaStepItem / completado", showBackground = true)
@Composable
private fun SolvyxRutinaStepItemCompletadoPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxRutinaStepItem(paso = samplePaso, completado = true, onToggle = {})
        }
    }
}

@Preview(name = "SolvyxRutinaStepItem / 30s sin desc", showBackground = true)
@Composable
private fun SolvyxRutinaStepItem30sPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxRutinaStepItem(paso = samplePasoCorto, completado = false, onToggle = {})
        }
    }
}
