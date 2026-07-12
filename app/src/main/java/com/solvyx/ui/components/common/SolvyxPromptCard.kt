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
import com.solvyx.backend.models.PromptJournaling
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Tarjeta para presentar un [PromptJournaling] en listas de selección.
 *
 * Estructura:
 * - Chip de categoría arriba a la izquierda (sobre `secondaryContainer`).
 * - Texto del prompt como cuerpo (estilo `bodyLarge` con peso SemiBold).
 * - Ícono de "escribir" (`ic_pencil`) a la derecha como affordance de
 *   tap → abre la pantalla de escritura.
 *
 * La categoría se muestra **tal cual** viene del backend. El backend
 * (content-curator) es responsable de entregar categorías ya
 * capitalizadas/legibles; la UI no las transforma.
 */
@Composable
fun SolvyxPromptCard(
    prompt: PromptJournaling,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val promptLabel = stringResource(R.string.journaling_prompt_label, prompt.texto)
    val writeLabel = stringResource(R.string.journaling_escribir_prompt)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = promptLabel
                role = Role.Button
            },
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
                .clickable(
                    onClick = onClick,
                    onClickLabel = writeLabel
                )
                .padding(SolvyxSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Chip de categoría
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = SolvyxSpacing.sm, vertical = SolvyxSpacing.xs)
                ) {
                    Text(
                        text = prompt.categoria.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(SolvyxSpacing.sm))
                // Prompt
                Text(
                    text = prompt.texto,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(SolvyxSpacing.md))
            // Affordance: ícono "escribir" en círculo teal (decorativo).
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pencil),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val samplePrompt = PromptJournaling(
    id = 1,
    slug = "gratitud-001",
    categoria = "gratitud",
    texto = "Hoy lo mejor fue…",
    orden = 1,
    activo = true
)

private val samplePromptDificultad = PromptJournaling(
    id = 4,
    slug = "dificultad-001",
    categoria = "dificultad",
    texto = "Algo que me costó hoy fue… y lo que aprendí de eso fue…",
    orden = 1,
    activo = true
)

@Preview(name = "SolvyxPromptCard / gratitud", showBackground = true)
@Composable
private fun SolvyxPromptCardGratitudPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxPromptCard(prompt = samplePrompt, onClick = {})
        }
    }
}

@Preview(name = "SolvyxPromptCard / dificultad", showBackground = true)
@Composable
private fun SolvyxPromptCardDificultadPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxPromptCard(prompt = samplePromptDificultad, onClick = {})
        }
    }
}
