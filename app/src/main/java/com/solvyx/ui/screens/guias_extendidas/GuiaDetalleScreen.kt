package com.solvyx.ui.screens.guias_extendidas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.ContenidoGuia
import com.solvyx.backend.models.GuiaExtendida
import com.solvyx.backend.models.LineaAyuda
import com.solvyx.backend.presentation.viewmodel.GuiaDetalleUiState
import com.solvyx.backend.presentation.viewmodel.GuiaDetalleViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.mapIconAsset
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.screens.guias.components.CardLabel
import com.solvyx.ui.screens.guias.components.DotRow
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.HelpLineRow
import com.solvyx.ui.screens.guias.components.StepRow
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Detalle de una [GuiaExtendida].
 *
 * Estructura:
 * - Top bar con título de la guía.
 * - Header con categoría · descripción corta.
 * - **Pasos** numerados (si los hay).
 * - **Señales de alerta** (si las hay) — BorderCard en `errorContainer`.
 * - **Cuándo llamar al 911** — BorderCard prominente en rojo.
 * - **Líneas de ayuda** — cada fila tappable (abre dialer con `tel:`).
 * - Botón "Volver" al fondo.
 */
@Composable
fun GuiaDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: GuiaDetalleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(
            title = stringResource(R.string.guia_detalle_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            GuiaDetalleUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is GuiaDetalleUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_back),
                    onClick = onNavigateBack
                )
            )

            is GuiaDetalleUiState.Loaded -> GuiaDetalleContent(
                guia = s.guia,
                onVolver = onNavigateBack
            )
        }
    }
}

@Composable
private fun GuiaDetalleContent(
    guia: GuiaExtendida,
    onVolver: () -> Unit
) {
    val scroll = rememberScrollState()
    val iconRes = mapIconAsset(guia.iconAsset) ?: R.drawable.ic_guide

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = SolvyxSpacing.lg)
            .padding(top = SolvyxSpacing.md, bottom = SolvyxSpacing.xl)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = guia.categoria.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(SolvyxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guia.categoria.replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = guia.titulo,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
            }
        }

        Spacer(Modifier.height(SolvyxSpacing.md))

        Text(
            text = guia.descripcionCorta,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (guia.contenido.introduccion.isNotBlank()) {
            Spacer(Modifier.height(SolvyxSpacing.md))
            Text(
                text = guia.contenido.introduccion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Pasos
        if (guia.contenido.pasos.isNotEmpty()) {
            Spacer(Modifier.height(SolvyxSpacing.xl))
            CardLabel(
                iconRes = iconRes,
                text = stringResource(R.string.guia_seccion_pasos),
                headingSemantics = true
            )
            Spacer(Modifier.height(SolvyxSpacing.xs))
            guia.contenido.pasos.forEachIndexed { idx, paso ->
                StepRow(n = idx + 1, text = "${paso.titulo} — ${paso.descripcion}")
            }
        }

        // Señales de alerta
        if (guia.contenido.senalesAlerta.isNotEmpty()) {
            Spacer(Modifier.height(SolvyxSpacing.xl))
            BorderCard(
                leftBorderColor = MaterialTheme.colorScheme.error
            ) {
                CardLabel(
                    iconRes = R.drawable.ic_alert_triangle,
                    text = stringResource(R.string.guia_seccion_senales_alerta),
                    color = MaterialTheme.colorScheme.error,
                    headingSemantics = true
                )
                Spacer(Modifier.height(SolvyxSpacing.xs))
                guia.contenido.senalesAlerta.forEach { senal ->
                    DotRow(text = senal)
                }
            }
        }

        // Cuándo llamar al 911
        if (guia.contenido.cuandoLlamar911.isNotEmpty()) {
            Spacer(Modifier.height(SolvyxSpacing.lg))
            BorderCard(
                bg = MaterialTheme.colorScheme.errorContainer,
                leftBorderColor = CrisisRed
            ) {
                CardLabel(
                    iconRes = R.drawable.ic_phone,
                    text = stringResource(R.string.guia_seccion_llamar_911),
                    color = CrisisRed,
                    headingSemantics = true
                )
                Spacer(Modifier.height(SolvyxSpacing.xs))
                guia.contenido.cuandoLlamar911.forEach { s ->
                    DotRow(text = s, color = CrisisRed)
                }
            }
        }

        // Líneas de ayuda
        if (guia.contenido.lineasAyuda.isNotEmpty()) {
            Spacer(Modifier.height(SolvyxSpacing.xl))
            CardLabel(
                iconRes = R.drawable.ic_phone,
                text = stringResource(R.string.guia_seccion_lineas_ayuda),
                headingSemantics = true
            )
            Spacer(Modifier.height(SolvyxSpacing.xs))
            BorderCard {
                guia.contenido.lineasAyuda.forEach { linea ->
                    HelpLineRowWithHorario(linea)
                    HorizontalDividerLine()
                }
            }
        }

        Spacer(Modifier.height(SolvyxSpacing.xxl))

        // Volver
        SolvyxButton(
            text = stringResource(R.string.action_back),
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(SolvyxSpacing.lg))
    }
}

@Composable
private fun HelpLineRowWithHorario(linea: LineaAyuda) {
    val context = LocalContext.current
    val callLabel = stringResource(R.string.guia_llamar_a, linea.nombre)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            // Toda la fila es tappable y anuncia "Llamar a <nombre>".
            // defaultMinSize asegura touch target ≥ 48dp (WCAG 2.5.5).
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${linea.telefono}"))
                    )
                },
                onClickLabel = callLabel
            )
            .semantics { role = Role.Button }
            .padding(vertical = SolvyxSpacing.xs),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = linea.nombre,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (linea.horario.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = linea.horario,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(SolvyxSpacing.sm))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = SolvyxSpacing.md, vertical = SolvyxSpacing.sm)
        ) {
            Text(
                text = linea.telefono,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(0.dp, MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun HorizontalDividerLine() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = SolvyxSpacing.sm),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

// ── Previews ────────────────────────────────────────────────────────────────

private val previewGuia = GuiaExtendida(
    id = 1,
    slug = "crisis-panico-agudo",
    titulo = "Estoy teniendo una crisis de pánico ahora",
    categoria = "crisis",
    descripcionCorta = "Pasos inmediatos para bajar el nivel de activación cuando sientes que el cuerpo se desborda.",
    contenido = ContenidoGuia(
        introduccion = "Una crisis de pánico es temporal. Estos pasos te ayudan a salir de ella en minutos.",
        pasos = listOf(
            com.solvyx.backend.models.PasoGuia("Detente", "Frena lo que estés haciendo. No necesitas actuar ahora."),
            com.solvyx.backend.models.PasoGuia("Respiración", "Inhala 4, sostén 4, exhala 6. Repite 5 veces."),
            com.solvyx.backend.models.PasoGuia("Anclaje", "Nombra 5 cosas que ves, 4 que tocas, 3 que oyes.")
        ),
        senalesAlerta = listOf(
            "Sientes que vas a perder el control",
            "Mareo severo o sensación de irrealidad",
            "Los síntomas no bajan después de 20 minutos"
        ),
        cuandoLlamar911 = listOf(
            "Dolor en el pecho intenso que se extiende al brazo",
            "Sensación de que vas a hacerte daño o a alguien más"
        ),
        lineasAyuda = listOf(
            LineaAyuda("Línea de la Vida (CONADIC)", "800 911 2000", "24/7 · gratis"),
            LineaAyuda("SAPTEL", "55 5259 8121", "Lun a Dom · 8 a 22 h")
        )
    ),
    iconAsset = null, orden = 1, activo = true
)

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GuiaDetalleContentPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Guía", onBack = {})
            GuiaDetalleContent(guia = previewGuia, onVolver = {})
        }
    }
}
