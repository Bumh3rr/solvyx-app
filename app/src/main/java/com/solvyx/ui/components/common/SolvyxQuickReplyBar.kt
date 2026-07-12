package com.solvyx.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme
import kotlinx.coroutines.delay

/**
 * Barra de respuesta rápida (quick replies) del chat con Berto.
 *
 * Renderiza **sticky** entre el flujo de mensajes y el input bar, NO inline
 * debajo de cada mensaje. Esto libera la lectura de la conversación y le da
 * al usuario un punto fijo de acción cuando Berto ofrece opciones.
 *
 * Comportamiento:
 * - Si [replies] está vacía, el componente no se renderiza
 *   (no deja espacio vacío). Defensa en profundidad: la pantalla
 *   también debe condicionar su llamada.
 * - Las opciones se exponen como chips horizontales en un `LazyRow`. Con
 *   4+ opciones la barra hace scroll lateral sin desbordar la pantalla.
 * - Cada chip aparece con `fadeIn + scaleIn` (200 ms) y un stagger de
 *   50 ms entre ellos, evitando una entrada abrupta.
 *
 * Diseño (tokens del Design System Solvyx):
 * - Contenedor: `MaterialTheme.colorScheme.surface`.
 * - Divisor inferior: 1dp en `MaterialTheme.colorScheme.outlineVariant`.
 *   Colocación: en el borde inferior del componente, es decir, la línea
 *   que lo separa visualmente del input bar inmediatamente debajo.
 * - Chip: fondo `primaryContainer`, borde 1dp `primary`, tipografía
 *   `bodyMedium / SemiBold`, color `onPrimaryContainer`.
 * - Forma del chip: `RoundedCornerShape(20.dp)` (más cuadrado que pill,
 *   look moderno) — no usa `MaterialTheme.shapes` porque el radio es
 *   un valor de marca del chip de chat, no de la escala estándar.
 * - Touch target: `heightIn(min = 48.dp)` cumple WCAG 2.5.5.
 * - Padding horizontal/vertical de los chips: 14dp / 10dp. Son valores
 *   deliberados para el chip (no tokens `SolvyxSpacing.*` por la misma
 *   razón que la forma: son de marca del componente, no de la escala
 *   general).
 *
 * Accesibilidad:
 * - Cada chip expone `contentDescription` = "Opción: <texto>"
 *   (`R.string.quick_reply_chip_a11y`) para que TalkBack lo lea
 *   como acción de botón.
 * - El `Box` usa `semantics(mergeDescendants = true)` para que el
 *   `Text` interno no duplique el anuncio.
 * - `Modifier.clickable` ya aporta `Role.Button` por defecto.
 *
 * @param replies lista de textos a mostrar como chips. Vacía → no se
 *   renderiza nada (early return).
 * @param onReplySelected callback invocado con el texto del chip tocado.
 * @param modifier modificador externo (por defecto `Modifier`).
 */
@Composable
fun SolvyxQuickReplyBar(
    replies: List<String>,
    onReplySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (replies.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SolvyxSpacing.md,
                    vertical = SolvyxSpacing.sm
                ),
            horizontalArrangement = Arrangement.spacedBy(SolvyxSpacing.sm),
            contentPadding = PaddingValues(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(
                items = replies,
                // El texto es único dentro de un mismo set de opciones
                // (las DecisionOption del árbol tienen texto distinto).
                key = { _, text -> text }
            ) { index, reply ->
                var visible by remember(reply) { mutableStateOf(false) }
                LaunchedEffect(reply) {
                    delay(index * 50L)
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(200)) +
                            scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(200)
                            )
                ) {
                    QuickReplyChip(
                        text = reply,
                        onClick = { onReplySelected(reply) }
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ── Chip privado (helper) ──────────────────────────────────────────────────

@Composable
private fun QuickReplyChip(
    text: String,
    onClick: () -> Unit
) {
    val a11yLabel = stringResource(R.string.quick_reply_chip_a11y, text)
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = a11yLabel
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

private val sampleRepliesShort = listOf(
    "Sí, avisa a mi red",
    "Dame técnicas de respiración",
    "Regresar al menú principal"
)

private val sampleRepliesLong = listOf(
    "Ansiedad por Alcohol",
    "Información de Alcohol",
    "Ansiedad por Cristal",
    "Información de Cristal",
    "Ansiedad por Vape",
    "Información de Vape",
    "Ansiedad por Cigarro",
    "Información de Cigarro"
)

@Preview(name = "SolvyxQuickReplyBar / 3 opciones", showBackground = true)
@Composable
private fun SolvyxQuickReplyBarPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxQuickReplyBar(
                replies = sampleRepliesShort,
                onReplySelected = {}
            )
        }
    }
}

@Preview(name = "SolvyxQuickReplyBar / scroll horizontal (8 opciones)", showBackground = true)
@Composable
private fun SolvyxQuickReplyBarScrollPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxQuickReplyBar(
                replies = sampleRepliesLong,
                onReplySelected = {}
            )
        }
    }
}

@Preview(name = "SolvyxQuickReplyBar / vacío (no se renderiza)", showBackground = true)
@Composable
private fun SolvyxQuickReplyBarEmptyPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxQuickReplyBar(
                replies = emptyList(),
                onReplySelected = {}
            )
        }
    }
}