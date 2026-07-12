package com.solvyx.ui.screens.guias_extendidas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.ContenidoGuia
import com.solvyx.backend.models.GuiaExtendida
import com.solvyx.backend.presentation.viewmodel.GuiasExtendidasEffect
import com.solvyx.backend.presentation.viewmodel.GuiasExtendidasUiState
import com.solvyx.backend.presentation.viewmodel.GuiasExtendidasViewModel
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxGuiaCard
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Catálogo de **Guías de primeros auxilios extendidas** (crisis, craving,
 * consumo, noches, familia, violencia, post-consumo).
 *
 * Componentes:
 * - Top bar teal con back.
 * - Filtros horizontales (chips) por categoría. La opción `null` = "Todas".
 * - Lista vertical de [SolvyxGuiaCard].
 * - Empty state si la combinación de filtros no devuelve guías.
 */
@Composable
fun GuiasExtendidasScreen(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GuiasExtendidasViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        when (effect) {
            is GuiasExtendidasEffect.NavigateToDetalle -> {
                val s = effect as GuiasExtendidasEffect.NavigateToDetalle
                onNavigateToDetalle(s.slug)
            }
            null -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(
            title = stringResource(R.string.guias_extendidas_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            GuiasExtendidasUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is GuiasExtendidasUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_retry),
                    onClick = viewModel::refresh
                )
            )

            is GuiasExtendidasUiState.Loaded -> GuiasExtendidasContent(
                guias = s.guias,
                filtroActivo = s.categoriaFiltro,
                onFiltroChange = viewModel::onFiltroChange,
                onGuiaClick = viewModel::onGuiaClick,
                onLimpiarFiltro = { viewModel.onFiltroChange(null) }
            )
        }
    }
}

@Composable
private fun GuiasExtendidasContent(
    guias: List<GuiaExtendida>,
    filtroActivo: String?,
    onFiltroChange: (String?) -> Unit,
    onGuiaClick: (String) -> Unit,
    onLimpiarFiltro: () -> Unit
) {
    val filtros = listOf(
        null to stringResource(R.string.filtro_guias_todas),
        "crisis" to stringResource(R.string.filtro_guias_crisis),
        "craving" to stringResource(R.string.filtro_guias_craving),
        "consumo" to stringResource(R.string.filtro_guias_consumo),
        "post_consumo" to stringResource(R.string.filtro_guias_post_consumo),
        "noches" to stringResource(R.string.filtro_guias_noches),
        "familia" to stringResource(R.string.filtro_guias_familia),
        "violencia" to stringResource(R.string.filtro_guias_violencia)
    )
    val selectedLabel = stringResource(R.string.state_selected)
    val notSelectedLabel = stringResource(R.string.state_not_selected)

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = SolvyxSpacing.lg, vertical = SolvyxSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(SolvyxSpacing.sm)
        ) {
            items(filtros.size) { idx ->
                val (cat, label) = filtros[idx]
                val selected = filtroActivo == cat
                FilterChip(
                    selected = selected,
                    onClick = { onFiltroChange(cat) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    // Anuncia el estado del filtro a TalkBack (cumple WCAG 4.1.2).
                    modifier = Modifier.semantics {
                        stateDescription = if (selected) selectedLabel else notSelectedLabel
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )

        if (guias.isEmpty()) {
            SolvyxEmptyStateCard(
                titulo = stringResource(R.string.guias_empty_title),
                mensaje = stringResource(R.string.guias_empty_message),
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.guias_empty_action_ver_todas),
                    onClick = onLimpiarFiltro
                ),
                modifier = Modifier.padding(SolvyxSpacing.lg)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = SolvyxSpacing.lg,
                    vertical = SolvyxSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
            ) {
                items(guias, key = { it.slug }) { guia ->
                    SolvyxGuiaCard(guia = guia, onClick = { onGuiaClick(guia.slug) })
                }
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

private val previewGuias = listOf(
    GuiaExtendida(
        id = 1, slug = "crisis-panico-agudo", titulo = "Estoy teniendo una crisis de pánico ahora",
        categoria = "crisis",
        descripcionCorta = "Pasos inmediatos para bajar el nivel de activación.",
        contenido = ContenidoGuia("", emptyList(), emptyList(), emptyList(), emptyList()),
        iconAsset = null, orden = 1, activo = true
    ),
    GuiaExtendida(
        id = 2, slug = "craving-intenso", titulo = "Tengo un craving intenso ahora",
        categoria = "craving",
        descripcionCorta = "Estrategias para resistir la urgencia sin ponerte en riesgo.",
        contenido = ContenidoGuia("", emptyList(), emptyList(), emptyList(), emptyList()),
        iconAsset = null, orden = 2, activo = true
    )
)

@Preview(showBackground = true)
@Composable
private fun GuiasExtendidasContentPreview() {
    SolvyxappTheme {
        GuiasExtendidasContent(
            guias = previewGuias,
            filtroActivo = "crisis",
            onFiltroChange = {},
            onGuiaClick = {},
            onLimpiarFiltro = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuiasExtendidasEmptyPreview() {
    SolvyxappTheme {
        GuiasExtendidasContent(
            guias = emptyList(),
            filtroActivo = "violencia",
            onFiltroChange = {},
            onGuiaClick = {},
            onLimpiarFiltro = {}
        )
    }
}
