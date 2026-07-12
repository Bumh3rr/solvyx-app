package com.solvyx.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.insights.AccionInsight
import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoAccion
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.backend.presentation.viewmodel.InsightsUiState
import com.solvyx.backend.presentation.viewmodel.InsightsViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxInsightBanner
import com.solvyx.ui.components.common.SolvyxInsightBannerAnimated
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Pantalla de **Insights de Berto**.
 *
 * Comportamiento:
 * - Carga inicial: ejecuta `evaluarAhora()` para mostrar el insight actual.
 * - Si hay uno fresco, se pinta con [SolvyxInsightBannerAnimated] arriba.
 * - Lista de "historial" (mock) abajo — placeholder hasta que
 *   `backend-data-architect` exponga `observeInsightHistory()`.
 * - Botón "Evaluar ahora" fuerza una nueva pasada del motor.
 */
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentInsight by viewModel.currentInsight.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.evaluarAhora()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(
            title = stringResource(R.string.insights_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            InsightsUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is InsightsUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.mensaje,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_retry),
                    onClick = viewModel::evaluarAhora
                )
            )

            InsightsUiState.Idle,
            InsightsUiState.SinInsightsNuevos -> InsightsIdleState(
                onEvaluar = viewModel::evaluarAhora
            )

            is InsightsUiState.InsightsDisponibles -> InsightsLoadedState(
                currentInsight = currentInsight,
                insights = s.insights,
                onAction = viewModel::evaluarAhora,
                onDismiss = viewModel::onDismiss
            )
        }
    }
}

@Composable
private fun InsightsLoadedState(
    currentInsight: Insight?,
    insights: List<Insight>,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = SolvyxSpacing.lg,
            vertical = SolvyxSpacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
    ) {
        // Insight actual destacado
        item("current") {
            SolvyxInsightBannerAnimated(
                insight = currentInsight,
                onAction = { onAction() },
                onDismiss = { onDismiss() }
            )
        }

        // Historial
        item("history_header") {
            Spacer(Modifier.height(SolvyxSpacing.md))
            Text(
                text = stringResource(R.string.insights_historial_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
        }

        // El motor expone `insights` ordenado por severidad; lo usamos
        // como "historial" para esta vista. En el futuro, este bloque
        // será sustituido por un Flow de insights pasados (Room/DataStore).
        if (insights.size <= 1) {
            item("empty_history") {
                SolvyxEmptyStateCard(
                    titulo = stringResource(R.string.insights_historial_vacio_title),
                    mensaje = stringResource(R.string.insights_historial_vacio_message)
                )
            }
        } else {
            items(insights.drop(1), key = { it.id }) { ins ->
                HistorialItemCard(insight = ins)
            }
        }
    }
}

@Composable
private fun HistorialItemCard(insight: Insight) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(SolvyxSpacing.md)
    ) {
        Column {
            Text(
                text = stringResource(R.string.insights_berto_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = insight.ventanaTexto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightsIdleState(onEvaluar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SolvyxSpacing.lg, vertical = SolvyxSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.insights_idle_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(SolvyxSpacing.sm))
        Text(
            text = stringResource(R.string.insights_idle_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(SolvyxSpacing.xl))
        SolvyxButton(
            text = stringResource(R.string.insights_btn_evaluar_ahora),
            onClick = onEvaluar
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

private val sampleInsight1 = Insight(
    id = "sueno_bajo",
    tipo = TipoInsight.OBSERVACION,
    severidad = Severidad.BAJA,
    ventanaTexto = "Esta semana dormiste menos y registraste más craving.",
    accion = AccionInsight(tipo = TipoAccion.VER_BITACORA)
)
private val sampleInsight2 = Insight(
    id = "racha_5",
    tipo = TipoInsight.RECONOCIMIENTO,
    severidad = Severidad.BAJA,
    ventanaTexto = "Llevas 5 días consecutivos registrando. Eso importa."
)

@Preview(showBackground = true)
@Composable
private fun InsightsLoadedStatePreview() {
    SolvyxappTheme {
        InsightsLoadedState(
            currentInsight = sampleInsight1,
            insights = listOf(sampleInsight1, sampleInsight2),
            onAction = {}, onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsIdleStatePreview() {
    SolvyxappTheme {
        InsightsIdleState(onEvaluar = {})
    }
}
