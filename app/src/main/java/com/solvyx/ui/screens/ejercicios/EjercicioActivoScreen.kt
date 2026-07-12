package com.solvyx.ui.screens.ejercicios

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.models.Ejercicio
import com.solvyx.backend.presentation.viewmodel.EjercicioActivoUiState
import com.solvyx.backend.presentation.viewmodel.EjercicioActivoViewModel
import com.solvyx.backend.presentation.viewmodel.Fase
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxEmptyStateCard
import com.solvyx.ui.components.common.SolvyxInsightBanner
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Versión **activa** de un ejercicio.
 *
 * - Círculo central crece/decrece según la fase (`INHALA → SOSTEN → EXHALA`).
 * - Texto principal cambia por fase y por paso.
 * - Controles inferiores: **Silenciar/Reanudar voz** · **Pausar/Reanudar**.
 * - Botón **Salir** siempre visible abajo.
 * - Al terminar todos los pasos, se muestra [SolvyxInsightBanner] con un
 *   reconocimiento. Cerrarlo no cierra la pantalla: el usuario sale con "Salir".
 *
 * El TTS lo gestiona [EjercicioActivoViewModel] a través del
 * singleton compartido `TtsEngine` (igual voz femenina es-MX que Berto).
 *
 * Cleanup: cuando el composable sale de la composición (usuario pulsa
 * "Salir" o back), el [DisposableEffect] llama a
 * [EjercicioActivoViewModel.finalizar] que detiene audio y cancela el
 * ciclo de fases. **No** se hace shutdown del engine porque es
 * compartido.
 */
@Composable
fun EjercicioActivoScreen(
    onNavigateBack: () -> Unit,
    viewModel: EjercicioActivoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Cleanup al salir de la pantalla: detiene audio y cancela ciclo.
    // NO se hace shutdown del TtsEngine — es singleton reusado por
    // otras pantallas (Berto, EjercicioGuiado, ...).
    DisposableEffect(Unit) {
        onDispose { viewModel.finalizar() }
    }

    when {
        state.cargando -> Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

        state.error != null -> SolvyxEmptyStateCard(
            titulo = stringResource(R.string.error_generic_title),
            mensaje = state.error.orEmpty(),
            accion = com.solvyx.ui.components.common.SolvyxAction(
                label = stringResource(R.string.action_back),
                onClick = onNavigateBack
            )
        )

        else -> EjercicioActivoContent(
            state = state,
            onTogglePausa = viewModel::togglePausa,
            onToggleSilenciado = viewModel::toggleSilenciado,
            onSalir = onNavigateBack,
            onCerrarInsight = viewModel::consumirCompletionInsight
        )
    }
}

@Composable
private fun EjercicioActivoContent(
    state: EjercicioActivoUiState,
    onTogglePausa: () -> Unit,
    onToggleSilenciado: () -> Unit,
    onSalir: () -> Unit,
    onCerrarInsight: () -> Unit
) {
    val ej = state.ejercicio ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = SolvyxSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(SolvyxSpacing.xl))

        // Título
        Text(
            text = ej.nombre,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(SolvyxSpacing.xs))
        Text(
            text = stringResource(
                R.string.ejercicio_paso_x_de_y,
                state.pasoActual + 1,
                ej.pasos.size.coerceAtLeast(1)
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        // Círculo respiratorio animado
        BreathingCircle(
            fase = state.fase,
            duracionFaseMs = state.duracionFaseMs,
            enPausa = state.enPausa
        )

        Spacer(Modifier.height(SolvyxSpacing.xl))

        // Texto de la fase
        val textoFase = textoParaFase(state.fase, ej.pasos.getOrNull(state.pasoActual).orEmpty())
        Text(
            text = textoFase,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(Modifier.weight(1f))

        // Insight de completado (si aplica)
        if (state.completionInsight != null && state.fase == Fase.COMPLETADO) {
            SolvyxInsightBanner(
                insight = state.completionInsight,
                onAction = onCerrarInsight,
                onDismiss = onCerrarInsight
            )
            Spacer(Modifier.height(SolvyxSpacing.md))
        }

        // Botones
        if (state.fase == Fase.COMPLETADO) {
            SolvyxButton(
                text = stringResource(R.string.ejercicio_salir_cta),
                onClick = onSalir,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ControlesActivos(
                silenciado = state.silenciado,
                enPausa = state.enPausa,
                onToggleSilenciado = onToggleSilenciado,
                onTogglePausa = onTogglePausa
            )
            Spacer(Modifier.height(SolvyxSpacing.md))
            // Botón "Salir" como TextButton-like con touch target mínimo
            // 48dp (defaultMinSize garantiza el área clickeable incluso
            // con fontScale 2.0).
            Text(
                text = stringResource(R.string.ejercicio_salir_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        onClick = onSalir,
                        onClickLabel = stringResource(R.string.ejercicio_salir_label)
                    )
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(vertical = SolvyxSpacing.sm)
                    .semantics { role = Role.Button },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(SolvyxSpacing.lg))
    }
}

@Composable
private fun ControlesActivos(
    silenciado: Boolean,
    enPausa: Boolean,
    onToggleSilenciado: () -> Unit,
    onTogglePausa: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
    ) {
        ControlButton(
            iconRes = if (silenciado) R.drawable.ic_volume_on else R.drawable.ic_volume_off,
            label = if (silenciado) stringResource(R.string.ejercicio_reanudar_voz)
                    else stringResource(R.string.ejercicio_silenciar_voz),
            onClick = onToggleSilenciado,
            modifier = Modifier.weight(1f)
        )
        ControlButton(
            iconRes = if (enPausa) R.drawable.ic_check else R.drawable.ic_activity,
            label = if (enPausa) stringResource(R.string.ejercicio_reanudar)
                    else stringResource(R.string.ejercicio_pausar),
            onClick = onTogglePausa,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ControlButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            // defaultMinSize garantiza touch target ≥ 48dp incluso con
            // fontScale alto (cumple WCAG 2.5.5).
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                onClick = onClick,
                onClickLabel = label
            )
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(iconRes),
                // Decorativo: la acción del botón ya la anuncia el label
                // del Text y el onClickLabel del clickable.
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(SolvyxSpacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Círculo central que crece (INHALA) → sostiene (SOSTEN) → decrece
 * (EXHALA). La animación es un `rememberInfiniteTransition` con `Reverse`.
 * En pausa congelamos la escala actual con `animateFloatAsState`.
 */
@Composable
private fun BreathingCircle(
    fase: Fase,
    duracionFaseMs: Long,
    enPausa: Boolean
) {
    val targetScale = when (fase) {
        Fase.INHALA -> 1f
        Fase.SOSTEN -> 1f
        Fase.EXHALA -> 0.55f
        Fase.COMPLETADO -> 0.85f
    }
    val duracionTween = (duracionFaseMs.toInt()).coerceIn(400, 8000)

    val infiniteScale by rememberInfiniteTransition(label = "breath")
        .animateFloat(
            initialValue = 0.78f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duracionTween, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath_scale"
        )

    val animatedScale by animateFloatAsState(
        targetValue = if (enPausa) targetScale else infiniteScale,
        animationSpec = tween(durationMillis = 600),
        label = "breath_target"
    )

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
        )
        Box(
            modifier = Modifier
                .size((180 * animatedScale).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
        )
        Box(
            modifier = Modifier
                .size((120 * animatedScale).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_wind),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Texto principal mostrado debajo del círculo.
 *
 * Para INHALA usamos el texto del paso si la lista de pasos trae algo
 * específico; en su defecto caemos al copy genérico.
 */
@Composable
private fun textoParaFase(fase: Fase, paso: String): String = when (fase) {
    Fase.INHALA -> paso.ifBlank { stringResource(R.string.fase_inhala) }
    Fase.SOSTEN -> stringResource(R.string.fase_sosten)
    Fase.EXHALA -> stringResource(R.string.fase_exhala)
    Fase.COMPLETADO -> stringResource(R.string.fase_completado)
}

// ── Previews ────────────────────────────────────────────────────────────────

private val previewEj = Ejercicio(
    id = 1, slug = "respiracion-4-7-8", nombre = "Respiración 4-7-8",
    tipo = "respiracion", duracionMinutos = 4,
    descripcionCorta = "Inhala 4, sostén 7, exhala 8.",
    pasos = listOf("Inhala", "Sostén", "Exhala"),
    ttsText = emptyMap(), iconAsset = "ic_wind", orden = 1, activo = true
)

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun EjercicioActivoInhalaPreview() {
    SolvyxappTheme {
        EjercicioActivoContent(
            state = EjercicioActivoUiState(
                ejercicio = previewEj, cargando = false, pasoActual = 0,
                fase = Fase.INHALA, duracionFaseMs = 4000L
            ),
            onTogglePausa = {}, onToggleSilenciado = {}, onSalir = {}, onCerrarInsight = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun EjercicioActivoSostenPreview() {
    SolvyxappTheme {
        EjercicioActivoContent(
            state = EjercicioActivoUiState(
                ejercicio = previewEj, cargando = false, pasoActual = 1,
                fase = Fase.SOSTEN, duracionFaseMs = 4000L, enPausa = true
            ),
            onTogglePausa = {}, onToggleSilenciado = {}, onSalir = {}, onCerrarInsight = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun EjercicioActivoCompletadoPreview() {
    SolvyxappTheme {
        EjercicioActivoContent(
            state = EjercicioActivoUiState(
                ejercicio = previewEj, cargando = false, pasoActual = 3,
                fase = Fase.COMPLETADO, duracionFaseMs = 4000L
            ),
            onTogglePausa = {}, onToggleSilenciado = {}, onSalir = {}, onCerrarInsight = {}
        )
    }
}
