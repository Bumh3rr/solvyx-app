package com.solvyx.ui.screens.lecciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.ContenidoLeccion
import com.solvyx.backend.models.Leccion
import com.solvyx.backend.models.SeccionLeccion
import com.solvyx.backend.presentation.viewmodel.LeccionDetalleUiState
import com.solvyx.backend.presentation.viewmodel.LeccionDetalleViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Detalle de una [Leccion] de psicoeducación.
 *
 * - Cabecera con sustancia + tema + título + duración.
 * - **Introducción** en párrafo destacado.
 * - **Secciones** apiladas como tarjetas (`BorderCard`).
 * - **Conclusión** en párrafo final.
 * - Botón "Marcar como leída" (sticky al fondo si NO está leída).
 * - Si ya está leída: badge "Leída" junto al título y el botón se
 *   sustituye por "Volver".
 */
@Composable
fun LeccionDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: LeccionDetalleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(
            title = stringResource(R.string.leccion_detalle_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            LeccionDetalleUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is LeccionDetalleUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_back),
                    onClick = onNavigateBack
                )
            )

            is LeccionDetalleUiState.Loaded -> LeccionDetalleContent(
                leccion = s.leccion,
                yaLeida = s.yaLeida,
                onMarcarLeida = viewModel::onMarcarComoLeida,
                onVolver = onNavigateBack
            )
        }
    }
}

@Composable
private fun LeccionDetalleContent(
    leccion: Leccion,
    yaLeida: Boolean,
    onMarcarLeida: () -> Unit,
    onVolver: () -> Unit
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = SolvyxSpacing.lg)
            .padding(top = SolvyxSpacing.md, bottom = SolvyxSpacing.xxl)
    ) {
        // Header: sustancia · tema + título + (badge "Leída" si aplica)
        Text(
            text = stringResource(
                R.string.leccion_header_sustancia_tema,
                leccion.sustancia.replaceFirstChar { it.uppercase() },
                leccion.tema
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(SolvyxSpacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = leccion.titulo,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (yaLeida) {
                Spacer(Modifier.width(SolvyxSpacing.sm))
                LeidaBadge()
            }
        }

        Spacer(Modifier.height(SolvyxSpacing.sm))
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
                    R.string.leccion_duracion,
                    leccion.duracionLecturaMinutos
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Introducción
        if (leccion.contenido.introduccion.isNotBlank()) {
            Spacer(Modifier.height(SolvyxSpacing.lg))
            Text(
                text = leccion.contenido.introduccion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Secciones
        if (leccion.contenido.secciones.isNotEmpty()) {
            Spacer(Modifier.height(SolvyxSpacing.xl))
            leccion.contenido.secciones.forEach { seccion ->
                BorderCard {
                    Text(
                        text = seccion.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(Modifier.height(SolvyxSpacing.xs))
                    Text(
                        text = seccion.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(SolvyxSpacing.md))
            }
        }

        // Conclusión
        if (leccion.contenido.conclusion.isNotBlank()) {
            Spacer(Modifier.height(SolvyxSpacing.md))
            Text(
                text = leccion.contenido.conclusion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(SolvyxSpacing.xxl))

        // CTA
        if (!yaLeida) {
            SolvyxButton(
                text = stringResource(R.string.leccion_marcar_leida),
                onClick = onMarcarLeida,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        } else {
            SolvyxButton(
                text = stringResource(R.string.action_back),
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(SolvyxSpacing.lg))
    }
}

@Composable
private fun LeidaBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = SolvyxSpacing.sm, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = stringResource(R.string.leccion_leida),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(SolvyxSpacing.xs))
        Text(
            text = stringResource(R.string.leccion_leida),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

private val previewLeccion = Leccion(
    id = 1,
    slug = "alcohol-mitos",
    sustancia = "alcohol",
    tema = "Mitos",
    titulo = "5 mitos sobre el alcohol que conviene desmontar",
    contenido = ContenidoLeccion(
        introduccion = "El alcohol está rodeado de creencias populares que no se sostienen con la evidencia. Revisamos cinco.",
        secciones = listOf(
            SeccionLeccion("Mito 1: “Una cerveza no cuenta”", "Cualquier bebida alcohólica aporta etanol al cuerpo. La suma es lo que cuenta."),
            SeccionLeccion("Mito 2: “Tomar alcohol cura el frío”", "El alcohol produce una sensación de calor temporal pero en realidad dilata los vasos sanguíneos y puede bajar tu temperatura corporal."),
            SeccionLeccion("Mito 3: “Solo es problema si me vuelvo violentx”", "El alcohol también daña hígado, cerebro, sueño y relaciones, aun cuando el comportamiento externo parezca aceptable.")
        ),
        conclusion = "Tomar decisiones informadas empieza por distinguir creencia de evidencia. Si tienes dudas, hablarlo con alguien de confianza puede ayudar."
    ),
    duracionLecturaMinutos = 4,
    orden = 1,
    activo = true
)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LeccionDetalleContentNoLeidaPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Lección", onBack = {})
            LeccionDetalleContent(
                leccion = previewLeccion, yaLeida = false,
                onMarcarLeida = {}, onVolver = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LeccionDetalleContentLeidaPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Lección", onBack = {})
            LeccionDetalleContent(
                leccion = previewLeccion, yaLeida = true,
                onMarcarLeida = {}, onVolver = {}
            )
        }
    }
}
