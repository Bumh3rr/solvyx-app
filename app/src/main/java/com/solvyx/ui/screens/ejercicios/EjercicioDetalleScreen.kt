package com.solvyx.ui.screens.ejercicios

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.Ejercicio
import com.solvyx.backend.presentation.viewmodel.EjercicioDetalleEffect
import com.solvyx.backend.presentation.viewmodel.EjercicioDetalleUiState
import com.solvyx.backend.presentation.viewmodel.EjercicioDetalleViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.mapIconAsset
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.StepRow
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Detalle de un [Ejercicio].
 *
 * Permite leer qué pasos componen la técnica y lanzar la versión activa
 * (TTS-guided) vía `onNavigateToActivo`.
 *
 * Comportamiento:
 * - Carga perezosa: el VM usa `SavedStateHandle` para resolver el `slug`.
 * - Si falla → EmptyState + back.
 * - El botón "Iniciar ejercicio" emite un [EjercicioDetalleEffect.NavigateToActivo]
 *   que el root consume para navegar.
 */
@Composable
fun EjercicioDetalleScreen(
    onNavigateToActivo: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EjercicioDetalleViewModel = hiltViewModel()
) {
    // Necesitamos conocer el slug para navegar a la versión activa. El VM
    // lo guarda en `SavedStateHandle`; lo levantamos desde el VM via
    // reflection-safe path: usamos `Ejercicio.slug` del UiState cuando llega.
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect, state) {
        if (effect is EjercicioDetalleEffect.NavigateToActivo) {
            val s = state
            if (s is EjercicioDetalleUiState.Loaded) {
                onNavigateToActivo(s.ejercicio.slug)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(
            title = stringResource(R.string.ejercicio_detalle_title),
            onBack = onNavigateBack
        )

        when (val s = state) {
            EjercicioDetalleUiState.Loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is EjercicioDetalleUiState.Error -> SolvyxEmptyStateCard(
                titulo = stringResource(R.string.error_generic_title),
                mensaje = s.message,
                accion = com.solvyx.ui.components.common.SolvyxAction(
                    label = stringResource(R.string.action_back),
                    onClick = onNavigateBack
                )
            )
            is EjercicioDetalleUiState.Loaded -> EjercicioDetalleContent(
                ejercicio = s.ejercicio,
                onIniciar = viewModel::onIniciar
            )
        }
    }
}

// ── Contenido ────────────────────────────────────────────────────────────────

@Composable
private fun EjercicioDetalleContent(
    ejercicio: Ejercicio,
    onIniciar: () -> Unit
) {
    val iconRes = mapIconAsset(ejercicio.iconAsset) ?: R.drawable.ic_activity
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = SolvyxSpacing.lg)
            .padding(top = SolvyxSpacing.md, bottom = SolvyxSpacing.xxl)
    ) {
        // Hero: ícono + título + duración
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.width(SolvyxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombre,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    // Heading para que TalkBack anuncie "Encabezado, ..." y permita navegación por headings.
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(SolvyxSpacing.xs))
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
                            R.string.ejercicio_duracion_min,
                            ejercicio.duracionMinutos
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(SolvyxSpacing.lg))

        // Descripción corta
        Text(
            text = ejercicio.descripcionCorta,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(SolvyxSpacing.xl))

        // Pasos numerados
        Text(
            text = stringResource(R.string.ejercicio_pasos_label),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(SolvyxSpacing.sm))

        ejercicio.pasos.forEachIndexed { idx, paso ->
            StepRow(n = idx + 1, text = paso)
            Spacer(Modifier.height(SolvyxSpacing.xs))
        }

        Spacer(Modifier.height(SolvyxSpacing.xl))

        // CTA principal
        SolvyxButton(
            text = stringResource(R.string.ejercicio_iniciar_cta),
            onClick = onIniciar,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_zap),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        Spacer(Modifier.height(SolvyxSpacing.md))
        Text(
            text = stringResource(R.string.ejercicio_privacidad_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

private val previewEjercicio = Ejercicio(
    id = 1,
    slug = "respiracion-4-7-8",
    nombre = "Respiración 4-7-8",
    tipo = "respiracion",
    duracionMinutos = 4,
    descripcionCorta = "Inhala 4, sostén 7, exhala 8. Induce calma profunda.",
    pasos = listOf(
        "Inhala por la nariz contando hasta 4",
        "Sostén la respiración contando hasta 7",
        "Exhala por la boca contando hasta 8",
        "Repite el ciclo 4 veces"
    ),
    ttsText = emptyMap(),
    iconAsset = "ic_wind",
    orden = 1,
    activo = true
)

@Preview(showBackground = true)
@Composable
private fun EjercicioDetalleContentPreview() {
    SolvyxappTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            GuiaTopBar(title = "Ejercicio", onBack = {})
            EjercicioDetalleContent(ejercicio = previewEjercicio, onIniciar = {})
        }
    }
}
