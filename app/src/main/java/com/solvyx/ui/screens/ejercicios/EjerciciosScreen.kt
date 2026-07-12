package com.solvyx.ui.screens.ejercicios

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.Ejercicio
import com.solvyx.backend.presentation.viewmodel.EjerciciosEffect
import com.solvyx.backend.presentation.viewmodel.EjerciciosUiState
import com.solvyx.backend.presentation.viewmodel.EjerciciosViewModel
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxExerciseCard
import com.solvyx.ui.components.common.SolvyxExerciseCardVariant
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Catálogo de ejercicios de regulación emocional.
 *
 * Anatomía:
 * - Top bar teal con back (`GuiaTopBar`).
 * - Filtros horizontales (chips) por tipo: Todos · Respiración · Body Scan ·
 *   Grounding · Activación · Lugar Seguro.
 * - Grid 2 columnas con `SolvyxExerciseCard` (variant Default).
 * - Empty state con CTA cuando no hay resultados en el filtro actual.
 *
 * Estados: Loading · Loaded · Error.
 *
 * Recibe por argumento dos lambdas de navegación (al detalle y atrás).
 * El `viewModel` se inyecta vía Hilt solo en el root.
 */
@Composable
fun EjerciciosScreen(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EjerciciosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    // El VM emite cada effect una sola vez (Channel.BUFFERED +
    // receiveAsFlow). Aquí los consumimos y navegamos.
    LaunchedEffect(effect) {
        when (effect) {
            is EjerciciosEffect.NavigateToDetalle ->
                onNavigateToDetalle((effect as EjerciciosEffect.NavigateToDetalle).slug)
            is EjerciciosEffect.ShowMessage -> {
                /* Snackbar global si se requiere — la app actual no
                   tiene host de snackbar fuera de MainScreen, por lo que
                   se omite sin bloquear al usuario. */
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
            title = stringResource(R.string.ejercicios_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            EjerciciosUiState.Loading -> EjerciciosLoading(Modifier.fillMaxSize())
            is EjerciciosUiState.Error -> EjerciciosError(
                mensaje = s.message,
                onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            )
            is EjerciciosUiState.Loaded -> EjerciciosContent(
                ejercicios = s.ejercicios,
                filtroActivo = s.filtroActivo,
                onFiltroChange = viewModel::onFiltroChange,
                onEjercicioClick = viewModel::onEjercicioClick,
                onLimpiarFiltro = { viewModel.onFiltroChange(null) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ── Contenido: filtros + grid ────────────────────────────────────────────────

@Composable
private fun EjerciciosContent(
    ejercicios: List<Ejercicio>,
    filtroActivo: String?,
    onFiltroChange: (String?) -> Unit,
    onEjercicioClick: (String) -> Unit,
    onLimpiarFiltro: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filtros con labels extraídos a strings.xml (lenguaje neutral,
    // formato consistente entre fuentes) y estado anunciado a TalkBack.
    val filtros = listOf(
        null to stringResource(R.string.filtro_ejercicios_todos),
        "respiracion" to stringResource(R.string.filtro_ejercicios_respiracion),
        "body_scan" to stringResource(R.string.filtro_ejercicios_body_scan),
        "grounding" to stringResource(R.string.filtro_ejercicios_grounding),
        "activacion" to stringResource(R.string.filtro_ejercicios_activacion),
        "lugar_seguro" to stringResource(R.string.filtro_ejercicios_lugar_seguro)
    )
    val selectedLabel = stringResource(R.string.state_selected)
    val notSelectedLabel = stringResource(R.string.state_not_selected)

    Column(modifier = modifier) {
        // Chips de filtro
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = SolvyxSpacing.lg, vertical = SolvyxSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(SolvyxSpacing.sm)
        ) {
            items(filtros.size) { idx ->
                val (tipo, label) = filtros[idx]
                val selected = filtroActivo == tipo
                FilterChip(
                    selected = selected,
                    onClick = { onFiltroChange(tipo) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    // Anuncia el estado del filtro a TalkBack.
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

        if (ejercicios.isEmpty()) {
            SolvyxEmptyStateCard(
                titulo = stringResource(R.string.ejercicios_empty_title),
                mensaje = stringResource(R.string.ejercicios_empty_message),
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.ejercicios_empty_action_ver_todos),
                    onClick = onLimpiarFiltro
                )
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = SolvyxSpacing.lg,
                    vertical = SolvyxSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
            ) {
                items(ejercicios, key = { it.slug }) { ejercicio ->
                    SolvyxExerciseCard(
                        ejercicio = ejercicio,
                        onClick = { onEjercicioClick(ejercicio.slug) },
                        variant = SolvyxExerciseCardVariant.Default
                    )
                }
            }
        }
    }
}

// ── Estados de borde ────────────────────────────────────────────────────────

@Composable
private fun EjerciciosLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EjerciciosError(
    mensaje: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SolvyxEmptyStateCard(
            titulo = stringResource(R.string.error_generic_title),
            mensaje = mensaje,
            accion = com.solvyx.ui.components.common.SolvyxAction(
                label = stringResource(R.string.action_retry),
                onClick = onRetry
            )
        )
    }
}

// ── Preview ─────────────────────────────────────────────────────────────────

private val previewEjercicios = listOf(
    Ejercicio(
        id = 1, slug = "respiracion-4-7-8", nombre = "Respiración 4-7-8",
        tipo = "respiracion", duracionMinutos = 4,
        descripcionCorta = "Inhala 4, sostén 7, exhala 8. Induce calma profunda.",
        pasos = listOf("Inhala", "Sostén", "Exhala"),
        ttsText = emptyMap(), iconAsset = "ic_wind", orden = 1, activo = true
    ),
    Ejercicio(
        id = 2, slug = "body-scan", nombre = "Body scan",
        tipo = "body_scan", duracionMinutos = 8,
        descripcionCorta = "Recorre tu cuerpo de pies a cabeza para soltar tensión.",
        pasos = listOf("Pies", "Piernas", "Abdomen"),
        ttsText = emptyMap(), iconAsset = "ic_activity", orden = 2, activo = true
    )
)

@Preview(showBackground = true)
@Composable
private fun EjerciciosContentPreview() {
    SolvyxappTheme {
        EjerciciosContent(
            ejercicios = previewEjercicios,
            filtroActivo = "respiracion",
            onFiltroChange = {},
            onEjercicioClick = {},
            onLimpiarFiltro = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EjerciciosContentEmptyPreview() {
    SolvyxappTheme {
        EjerciciosContent(
            ejercicios = emptyList(),
            filtroActivo = "grounding",
            onFiltroChange = {},
            onEjercicioClick = {},
            onLimpiarFiltro = {}
        )
    }
}
