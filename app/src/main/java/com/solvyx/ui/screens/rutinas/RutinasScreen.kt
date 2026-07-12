package com.solvyx.ui.screens.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.Rutina
import com.solvyx.backend.presentation.viewmodel.RutinasEffect
import com.solvyx.backend.presentation.viewmodel.RutinasUiState
import com.solvyx.backend.presentation.viewmodel.RutinasViewModel
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.mapIconAsset
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Listado de **rutinas** sugeridas (matutina, nocturna, etc.).
 *
 * - Top bar con back.
 * - Lista vertical de cards. Cada card pinta: ícono + nombre + descripción
 *   + hora sugerida (formato 24h).
 * - Tap → navega al detalle de la rutina.
 */
@Composable
fun RutinasScreen(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RutinasViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        when (effect) {
            is RutinasEffect.NavigateToDetalle -> {
                val e = effect as RutinasEffect.NavigateToDetalle
                onNavigateToDetalle(e.slug)
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
            title = stringResource(R.string.rutinas_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            RutinasUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is RutinasUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_retry),
                    onClick = viewModel::refresh
                )
            )

            is RutinasUiState.Loaded -> RutinasContent(
                rutinas = s.rutinas,
                onRutinaClick = viewModel::onRutinaClick
            )
        }
    }
}

@Composable
private fun RutinasContent(
    rutinas: List<Rutina>,
    onRutinaClick: (String) -> Unit
) {
    if (rutinas.isEmpty()) {
        SolvyxEmptyStateCard(
            titulo = stringResource(R.string.rutinas_empty_title),
            mensaje = stringResource(R.string.rutinas_empty_message)
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = SolvyxSpacing.lg,
            vertical = SolvyxSpacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
    ) {
        items(rutinas, key = { it.slug }) { r ->
            RutinaListCard(rutina = r, onClick = { onRutinaClick(r.slug) })
        }
    }
}

@Composable
private fun RutinaListCard(rutina: Rutina, onClick: () -> Unit) {
    val iconRes = mapIconAsset(rutina.iconAsset) ?: R.drawable.ic_calendar
    val openLabel = stringResource(R.string.rutinas_abrir, rutina.nombre)

    Card(
        onClick = onClick,
        // Anuncia "Abrir rutina <nombre>" como acción al tocar.
        // El chevron y el ícono de la izquierda son decorativos.
        modifier = Modifier
            .fillMaxWidth()
            .semantics { role = Role.Button },
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
                    onClickLabel = openLabel
                )
                .padding(SolvyxSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(SolvyxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rutina.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = rutina.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(SolvyxSpacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_clock),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(SolvyxSpacing.xs))
                    Text(
                        text = stringResource(R.string.rutinas_hora_sugerida, formatHora(rutina.horaSugerida)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Convierte `1445` → `"14:45"`. Si ya está formateado lo devuelve igual. */
private fun formatHora(hora: Int): String {
    if (hora <= 0) return "—"
    val h = hora / 100
    val m = hora % 100
    return "%02d:%02d".format(h.coerceIn(0, 23), m.coerceIn(0, 59))
}

// ── Preview ────────────────────────────────────────────────────────────────

private val sampleRutinas = listOf(
    Rutina(
        id = 1, slug = "matutina", nombre = "Rutina matutina",
        descripcion = "Empieza el día con intención: respiración, hidratación y una meta clara.",
        horaSugerida = 715, pasos = emptyList(),
        iconAsset = "ic_sun", activo = true
    ),
    Rutina(
        id = 2, slug = "nocturna", nombre = "Rutina nocturna",
        descripcion = "Cierra el día con calma: revisión del día, gratitud y descanso.",
        horaSugerida = 2200, pasos = emptyList(),
        iconAsset = "ic_moon", activo = true
    )
)

@Preview(showBackground = true)
@Composable
private fun RutinasContentPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Rutinas", onBack = {})
            RutinasContent(rutinas = sampleRutinas, onRutinaClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RutinasContentEmptyPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Rutinas", onBack = {})
            RutinasContent(rutinas = emptyList(), onRutinaClick = {})
        }
    }
}
