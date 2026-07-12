package com.solvyx.ui.components.common

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.models.Leccion
import com.solvyx.backend.models.ContenidoLeccion
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Tarjeta para una lección del módulo de psicoeducación.
 *
 * Decisiones de diseño:
 * - El ícono se elige por `sustancia` (no por `tipo`), porque visualmente
 *   el usuario identifica mejor la sustancia que el tema. Si la sustancia
 *   no está en el mapa, se usa un ícono genérico (`ic_info_circle`).
 * - Insignia **"Leída"** se pinta arriba a la derecha cuando
 *   [leida] es `true`. El texto tiene `contentDescription` claro para
 *   lectores de pantalla.
 * - Tap = `onClick()`. Toda la superficie es clickeable.
 */
@Composable
fun SolvyxLessonCard(
    leccion: Leccion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leida: Boolean = false
) {
    val iconRes = sustanciaToDrawable(leccion.sustancia)
    val openLabel = stringResource(R.string.leccion_abrir, leccion.titulo)
    val completedLabel = stringResource(R.string.state_completed)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = openLabel
                stateDescription = if (leida) completedLabel else ""
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SolvyxSpacing.lg)
        ) {
            // Header: ícono sustancia + (sustancia · tema) + badge "Leída"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(SolvyxSpacing.md))
                    Text(
                        text = "${leccion.sustancia.replaceFirstChar { it.uppercase() }} · ${leccion.tema}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (leida) LeidaBadge()
            }

            Spacer(Modifier.height(SolvyxSpacing.md))

            // Título
            Text(
                text = leccion.titulo,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(SolvyxSpacing.sm))

            // Duración de lectura
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(SolvyxSpacing.xs))
                Text(
                    text = stringResource(
                        R.string.ejercicio_duracion_lectura,
                        leccion.duracionLecturaMinutos
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LeidaBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = SolvyxSpacing.sm, vertical = SolvyxSpacing.xs)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(SolvyxSpacing.xs))
        Text(
            text = stringResource(R.string.leccion_leida),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Mapea la sustancia de la lección al drawable representativo.
 *
 * El agente-ui-screen-flow-builder y los seeds existentes manejan estas
 * cuatro sustancias canónicas. Cualquier valor fuera de este set cae a
 * un ícono genérico — la card nunca debe romperse visualmente.
 */
@DrawableRes
private fun sustanciaToDrawable(sustancia: String): Int = when (sustancia.lowercase()) {
    "alcohol" -> R.drawable.ic_bottle
    "vape" -> R.drawable.ic_vape
    "cristal" -> R.drawable.ic_gem
    "tabaco" -> R.drawable.ic_cigarette
    else -> R.drawable.ic_info_circle
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val sampleLeccion = Leccion(
    id = 1,
    slug = "alcohol-mitos",
    sustancia = "alcohol",
    tema = "Mitos",
    titulo = "5 mitos sobre el alcohol que conviene desmontar",
    contenido = ContenidoLeccion(
        introduccion = "",
        secciones = emptyList(),
        conclusion = ""
    ),
    duracionLecturaMinutos = 4,
    orden = 1,
    activo = true
)

@Preview(name = "SolvyxLessonCard / no leída", showBackground = true)
@Composable
private fun SolvyxLessonCardNoLeidaPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxLessonCard(leccion = sampleLeccion, onClick = {}, leida = false)
        }
    }
}

@Preview(name = "SolvyxLessonCard / leída", showBackground = true)
@Composable
private fun SolvyxLessonCardLeidaPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxLessonCard(leccion = sampleLeccion, onClick = {}, leida = true)
        }
    }
}

@Preview(name = "SolvyxLessonCard / cristal", showBackground = true)
@Composable
private fun SolvyxLessonCardCristalPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxLessonCard(
                leccion = sampleLeccion.copy(sustancia = "cristal", tema = "Adicción"),
                onClick = {}
            )
        }
    }
}
