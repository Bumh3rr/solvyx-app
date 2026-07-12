package com.solvyx.ui.screens.rutinas

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.Rutina
import com.solvyx.backend.models.RutinaPaso
import com.solvyx.backend.presentation.viewmodel.RutinaDetalleEffect
import com.solvyx.backend.presentation.viewmodel.RutinaDetalleUiState
import com.solvyx.backend.presentation.viewmodel.RutinaDetalleViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxRutinaStepItem
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Detalle de una [Rutina].
 *
 * - Resumen arriba: "X de Y pasos completados hoy".
 * - Lista de [SolvyxRutinaStepItem] con checkbox (toggle on/off).
 * - Botón "Volver" al fondo.
 */
@Composable
fun RutinaDetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: RutinaDetalleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    // Si el VM pide navegar a la versión activa (placeholder actual:
    // navegar atrás para no quedar atrapado en una pantalla no
    // implementada). Cuando el módulo TTS lo implemente, esta effect
    // navegará a `Routes.EjercicioActivo`.
    LaunchedEffect(effect) {
        if (effect is RutinaDetalleEffect.NavigateToActivo) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(
            title = stringResource(R.string.rutina_detalle_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            RutinaDetalleUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is RutinaDetalleUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_back),
                    onClick = onNavigateBack
                )
            )

            is RutinaDetalleUiState.Loaded -> RutinaDetalleContent(
                rutina = s.rutina,
                pasosCompletadosHoy = s.pasosCompletadosHoy,
                onPasoToggle = viewModel::onPasoCompletado,
                onVolver = onNavigateBack
            )
        }
    }
}

@Composable
private fun RutinaDetalleContent(
    rutina: Rutina,
    pasosCompletadosHoy: Set<Int>,
    onPasoToggle: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val scroll = rememberScrollState()
    val totalPasos = rutina.pasos.size
    val completados = rutina.pasos.count { it.id in pasosCompletadosHoy }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = SolvyxSpacing.lg)
            .padding(top = SolvyxSpacing.md, bottom = SolvyxSpacing.xxl)
    ) {
        // Hero
        Text(
            text = rutina.nombre,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(SolvyxSpacing.sm))
        Text(
            text = rutina.descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(SolvyxSpacing.lg))

        // Resumen de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(SolvyxSpacing.md)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.rutina_detalle_progreso,
                        completados,
                        totalPasos
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Indicador de progreso
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                ) {
                    val ratio = if (totalPasos == 0) 0f else completados.toFloat() / totalPasos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(Modifier.height(SolvyxSpacing.lg))

        // Pasos
        rutina.pasos.forEach { paso ->
            SolvyxRutinaStepItem(
                paso = paso,
                completado = paso.id in pasosCompletadosHoy,
                onToggle = { onPasoToggle(paso.id) },
                modifier = Modifier.padding(vertical = SolvyxSpacing.xs)
            )
        }

        Spacer(Modifier.height(SolvyxSpacing.xxl))

        SolvyxButton(
            text = stringResource(R.string.action_back),
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(SolvyxSpacing.lg))
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

private val sampleRutina = Rutina(
    id = 1, slug = "matutina", nombre = "Rutina matutina",
    descripcion = "Empieza el día con intención: respiración, hidratación y una meta clara.",
    horaSugerida = 715,
    pasos = listOf(
        RutinaPaso(
            id = 11, rutinaId = 1, orden = 1,
            titulo = "Respiración 4-7-8", descripcion = "Cuatro ciclos de inhalar 4, sostener 7, exhalar 8.",
            duracionSegundos = 120, iconAsset = "ic_wind"
        ),
        RutinaPaso(
            id = 12, rutinaId = 1, orden = 2,
            titulo = "Hidratación", descripcion = "Un vaso de agua antes del celular.",
            duracionSegundos = 30, iconAsset = null
        ),
        RutinaPaso(
            id = 13, rutinaId = 1, orden = 3,
            titulo = "Anotar meta del día", descripcion = "Una sola cosa importante.",
            duracionSegundos = 60, iconAsset = null
        )
    ),
    iconAsset = "ic_calendar", activo = true
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RutinaDetalleContentPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Rutina", onBack = {})
            RutinaDetalleContent(
                rutina = sampleRutina,
                pasosCompletadosHoy = setOf(11),
                onPasoToggle = {},
                onVolver = {}
            )
        }
    }
}
