package com.solvyx.ui.screens.journaling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import com.solvyx.backend.models.JournalingEntry
import com.solvyx.backend.models.PromptJournaling
import com.solvyx.backend.presentation.viewmodel.JournalingEffect
import com.solvyx.backend.presentation.viewmodel.JournalingUiState
import com.solvyx.backend.presentation.viewmodel.JournalingViewModel
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxPromptCard
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Banco de **prompts de journaling** + entradas recientes del usuario.
 *
 * Estructura:
 * - Top bar.
 * - Tabs por categoría (Gratitud · Dificultad · Curiosidad · Emociones · Cravings · Planes).
 * - Lista de [SolvyxPromptCard] filtrada por la categoría activa.
 * - Sección inferior: "Mis entradas recientes" si el usuario ya escribió.
 * - FAB "+" abajo a la derecha: abre el editor en modo **entrada libre**.
 * - Tocar un prompt: abre el editor precargado con `promptSlug`.
 */
@Composable
fun JournalingScreen(
    onNavigateToEditor: (promptSlug: String?, promptTexto: String?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: JournalingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        when (effect) {
            is JournalingEffect.NavigateToEditor -> {
                // El VM sólo conoce (promptId, promptTexto) — no el slug.
                // Para abrir como "entrada libre" pasamos null aquí. La
                // lista usa `onPromptClickWithSlug` para entrar con prompt.
                onNavigateToEditor(null, null)
            }
            is JournalingEffect.ShowMessage -> { /* host snackbar ausente; se ignora */ }
            null -> Unit
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null, null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pencil),
                    contentDescription = stringResource(R.string.journaling_fab_nueva_entrada),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            GuiaTopBar(
                title = stringResource(R.string.journaling_title),
                onBack = onNavigateBack
            )

            when (val s = state) {
                JournalingUiState.Loading -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                is JournalingUiState.Error -> SolvyxEmptyStateCard(
                    titulo = stringResource(R.string.error_generic_title),
                    mensaje = s.message,
                    accion = com.solvyx.ui.components.common.SolvyxAction(
                        label = stringResource(R.string.action_retry),
                        onClick = viewModel::refresh
                    )
                )

                is JournalingUiState.Loaded -> JournalingContent(
                    promptsPorCategoria = s.promptsPorCategoria,
                    entries = s.entries,
                    onPromptClick = viewModel::onPromptClick,
                    onPromptClickWithSlug = { slug, texto -> onNavigateToEditor(slug, texto) }
                )
            }
        }
    }
}

private val CATEGORIAS_TABS = listOf(
    "gratitud", "dificultad", "curiosidad", "emociones", "cravings", "planes"
)

@Composable
private fun JournalingContent(
    promptsPorCategoria: Map<String, List<PromptJournaling>>,
    entries: List<JournalingEntry>,
    onPromptClick: (promptId: Int?, promptTexto: String?) -> Unit,
    onPromptClickWithSlug: (slug: String, texto: String) -> Unit
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val categoria = CATEGORIAS_TABS.getOrNull(tabIndex) ?: "gratitud"
    val prompts = promptsPorCategoria[categoria].orEmpty()

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
            CATEGORIAS_TABS.forEachIndexed { idx, cat ->
                Tab(
                    selected = idx == tabIndex,
                    onClick = { tabIndex = idx },
                    // Anuncia estado de la pestaña a TalkBack.
                    modifier = Modifier.semantics {
                        stateDescription = if (idx == tabIndex) selectedTabLabel else notSelectedTabLabel
                        role = Role.Tab
                    },
                    text = {
                        Text(
                            text = cat.replaceFirstChar { it.uppercase() },
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = SolvyxSpacing.lg,
                vertical = SolvyxSpacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
        ) {
            if (prompts.isEmpty()) {
                item {
                    SolvyxEmptyStateCard(
                        titulo = stringResource(R.string.journaling_empty_prompts_title),
                        mensaje = stringResource(R.string.journaling_empty_prompts_message)
                    )
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.journaling_prompts_seccion),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(top = SolvyxSpacing.sm, bottom = SolvyxSpacing.xs)
                            .semantics { heading() }
                    )
                }
                items(prompts, key = { it.slug }) { p ->
                    SolvyxPromptCard(
                        prompt = p,
                        onClick = { onPromptClickWithSlug(p.slug, p.texto) }
                    )
                }
            }

            if (entries.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(SolvyxSpacing.md))
                    HorizontalDivider(thickness = 0.5.dp)
                    Text(
                        text = stringResource(R.string.journaling_entradas_recientes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(top = SolvyxSpacing.md, bottom = SolvyxSpacing.xs)
                            .semantics { heading() }
                    )
                }
                items(entries, key = { it.id }) { e ->
                    EntryRecienteCard(entry = e)
                }
            }

            // Padding inferior para que el FAB no tape contenido.
            item { Spacer(Modifier.height(SolvyxSpacing.xxl + 56.dp)) }
        }
    }
}

@Composable
private fun EntryRecienteCard(entry: JournalingEntry) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(SolvyxSpacing.md)) {
            if (entry.promptTexto != null) {
                Text(
                    text = entry.promptTexto,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(SolvyxSpacing.xs))
            }
            Text(
                text = entry.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4
            )
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

private val samplePrompt = PromptJournaling(
    id = 1, slug = "gratitud-001", categoria = "gratitud", texto = "Hoy lo mejor fue…",
    orden = 1, activo = true
)

private val sampleEntry = JournalingEntry(
    id = 1L, fecha = 0L, promptId = null, promptTexto = null,
    contenido = "Hoy me sentí tranquila y pude terminar mi rutina nocturna sin ansiedad.", createdAt = 0L
)

@Preview(showBackground = true)
@Composable
private fun JournalingContentPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Journaling", onBack = {})
            JournalingContent(
                promptsPorCategoria = mapOf("gratitud" to listOf(samplePrompt)),
                entries = listOf(sampleEntry),
                onPromptClick = { _, _ -> },
                onPromptClickWithSlug = { _, _ -> }
            )
        }
    }
}
