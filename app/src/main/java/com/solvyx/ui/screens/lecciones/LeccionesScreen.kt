package com.solvyx.ui.screens.lecciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.ContenidoLeccion
import com.solvyx.backend.models.Leccion
import com.solvyx.backend.presentation.viewmodel.LeccionesEffect
import com.solvyx.backend.presentation.viewmodel.LeccionesUiState
import com.solvyx.backend.presentation.viewmodel.LeccionesViewModel
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxLessonCard
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Catálogo de **lecciones de psicoeducación** organizado por sustancia.
 *
 * Anatomía:
 * - Top bar ("Psicoeducación") + back.
 * - `TabRow` con las 4 sustancias canónicas (Alcohol · Vape · Cristal · Tabaco).
 * - Lista vertical con `SolvyxLessonCard`. **Nota**: el VM actual no
 *   expone el flag "leída" en el listado; la insignia sólo aparecerá al
 *   volver del detalle.
 * - Empty state para "sin lecciones en esta sustancia".
 */
@Composable
fun LeccionesScreen(
    onNavigateToDetalle: (sustancia: String, slug: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LeccionesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        when (effect) {
            is LeccionesEffect.NavigateToDetalle -> {
                val e = effect as LeccionesEffect.NavigateToDetalle
                onNavigateToDetalle(e.sustancia, e.slug)
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
            title = stringResource(R.string.lecciones_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            LeccionesUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is LeccionesUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_retry),
                    onClick = viewModel::refresh
                )
            )

            is LeccionesUiState.Loaded -> LeccionesContent(
                leccionesPorSustancia = s.leccionesPorSustancia,
                onLeccionClick = viewModel::onLeccionClick
            )
        }
    }
}

private val SUSTANCIAS_TABS = listOf("alcohol", "vape", "cristal", "tabaco")

@Composable
private fun LeccionesContent(
    leccionesPorSustancia: Map<String, List<Leccion>>,
    onLeccionClick: (sustancia: String, slug: String) -> Unit
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val sustancia = SUSTANCIAS_TABS.getOrNull(tabIndex) ?: "alcohol"
    val lecciones = leccionesPorSustancia[sustancia].orEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        val selectedTabLabel = stringResource(R.string.state_selected)
        val notSelectedTabLabel = stringResource(R.string.state_not_selected)
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(positions[tabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            SUSTANCIAS_TABS.forEachIndexed { idx, sust ->
                Tab(
                    selected = idx == tabIndex,
                    onClick = { tabIndex = idx },
                    // Anuncia a TalkBack si la pestaña está seleccionada.
                    modifier = Modifier.semantics {
                        stateDescription = if (idx == tabIndex) selectedTabLabel else notSelectedTabLabel
                        role = Role.Tab
                    },
                    text = {
                        Text(
                            text = sust.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (idx == tabIndex) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (idx == tabIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        if (lecciones.isEmpty()) {
            SolvyxEmptyStateCard(
                titulo = stringResource(R.string.lecciones_empty_title),
                mensaje = stringResource(R.string.lecciones_empty_message, sustancia),
                modifier = Modifier.padding(SolvyxSpacing.lg)
            )
        } else {
            // Agrupamos por tema dentro de la sustancia para pintar headers.
            val porTema = lecciones.groupBy { it.tema }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = SolvyxSpacing.lg,
                    vertical = SolvyxSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
            ) {
                porTema.forEach { (tema, lecs) ->
                    item(key = "header_${sustancia}_$tema") {
                        Text(
                            text = tema,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(top = SolvyxSpacing.sm, bottom = SolvyxSpacing.xs)
                                .semantics { heading() }
                        )
                    }
                    items(lecs, key = { it.slug }) { lec ->
                        SolvyxLessonCard(leccion = lec, onClick = {
                            onLeccionClick(lec.sustancia, lec.slug)
                        })
                    }
                }
                // Espacio final
                item { Spacer(Modifier.height(SolvyxSpacing.lg)) }
            }
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

private val sampleLeccion = Leccion(
    id = 1, slug = "alcohol-mitos", sustancia = "alcohol", tema = "Mitos",
    titulo = "5 mitos sobre el alcohol que conviene desmontar",
    contenido = ContenidoLeccion("", emptyList(), ""),
    duracionLecturaMinutos = 4, orden = 1, activo = true
)

private val sampleMap = mapOf(
    "alcohol" to listOf(sampleLeccion, sampleLeccion.copy(slug = "alcohol-2", titulo = "Efectos en tu cuerpo")),
    "vape" to listOf(sampleLeccion.copy(sustancia = "vape", slug = "vape-1", tema = "Riesgos", titulo = "Qué le hace el vape a tus pulmones")),
    "cristal" to emptyList<Leccion>(),
    "tabaco" to listOf(sampleLeccion.copy(sustancia = "tabaco", slug = "tabaco-1", tema = "Adicción", titulo = "Por qué es tan difícil dejarlo"))
)

@Preview(showBackground = true)
@Composable
private fun LeccionesContentPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Educación", onBack = {})
            LeccionesContent(
                leccionesPorSustancia = sampleMap,
                onLeccionClick = { _, _ -> }
            )
        }
    }
}
