package com.solvyx.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.insights.AccionInsight
import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoAccion
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Banner para mostrar un [Insight] generado por el motor offline de Berto.
 *
 * Variantes de fondo (mapeo automático desde el modelo, no se elige manualmente):
 * - [InsightBannerVariant.Info]    → observación / sugerencia neutra
 *   (`surfaceVariant`). El caso por defecto.
 * - [InsightBannerVariant.Success] → reconocimiento positivo
 *   (`secondaryContainer`).
 * - [InsightBannerVariant.Warning] → `severidad = ALTA` (`errorContainer`).
 *
 * Accesibilidad:
 * - El bloque completo lleva un `contentDescription` que concatena
 *   "Insight de Berto. <texto>" para que TalkBack lo lea de corrido.
 * - Los dos botones ("Ver más" / "Descartar") exponen su `label` como
 *   descripción del click target.
 *
 * Animación: usa la sobrecarga [SolvyxInsightBannerAnimated] que envuelve
 * la card en `AnimatedVisibility` (expandVertically + fadeIn).
 */
@Composable
fun SolvyxInsightBanner(
    insight: Insight,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val variant = insight.toBannerVariant()
    val colors = variant.toColors()
    // Strings extraídos a strings.xml (lenguaje neutro).
    val a11yText = stringResource(R.string.insights_berto_anuncio, insight.ventanaTexto)
    val dismissLabel = stringResource(R.string.action_dismiss)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.background)
            .semantics { contentDescription = a11yText }
            .padding(SolvyxSpacing.lg)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                // Berto mini badge (decorativo: el bloque ya tiene
                // contentDescription que nombra "Insight de Berto").
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.onBackground.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.berto_tranquilo),
                        contentDescription = null,
                        tint = colors.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(SolvyxSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.insights_berto_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onBackground.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = insight.ventanaTexto,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onBackground
                    )
                }
                // Botón de cierre con touch target 48dp
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable(
                            onClick = onDismiss,
                            onClickLabel = dismissLabel
                        )
                        .semantics { contentDescription = dismissLabel },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_circle_x),
                        contentDescription = null,
                        tint = colors.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(SolvyxSpacing.md))
            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (insight.accion != null && insight.accion.tipo != TipoAccion.NINGUNA) {
                    TextButton(onClick = onAction) {
                        Text(
                            text = stringResource(R.string.action_view_more),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onBackground
                        )
                    }
                    Spacer(Modifier.width(SolvyxSpacing.sm))
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.action_dismiss),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onBackground.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

/**
 * Variante animada del banner. Mantiene la misma API pero envuelve en
 * [AnimatedVisibility] para que las pantallas que listen `Flow<List<Insight>>`
 * puedan hacer appear/disappear con transición suave.
 *
 * @param insight insight a mostrar. Si es `null`, el banner está oculto y
 *   la transición es de salida.
 */
@Composable
fun SolvyxInsightBannerAnimated(
    insight: Insight?,
    onAction: (Insight) -> Unit,
    onDismiss: (Insight) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = insight != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        if (insight != null) {
            SolvyxInsightBanner(
                insight = insight,
                onAction = { onAction(insight) },
                onDismiss = { onDismiss(insight) }
            )
        }
    }
}

enum class InsightBannerVariant { Info, Success, Warning }

private data class BannerColors(
    val background: Color,
    val onBackground: Color
)

@Composable
private fun InsightBannerVariant.toColors(): BannerColors = when (this) {
    InsightBannerVariant.Info -> BannerColors(
        background = MaterialTheme.colorScheme.surfaceVariant,
        onBackground = MaterialTheme.colorScheme.onSurfaceVariant
    )
    InsightBannerVariant.Success -> BannerColors(
        background = MaterialTheme.colorScheme.secondaryContainer,
        onBackground = MaterialTheme.colorScheme.onSecondaryContainer
    )
    InsightBannerVariant.Warning -> BannerColors(
        background = MaterialTheme.colorScheme.errorContainer,
        onBackground = MaterialTheme.colorScheme.onErrorContainer
    )
}

private fun Insight.toBannerVariant(): InsightBannerVariant = when {
    tipo == TipoInsight.RECONOCIMIENTO -> InsightBannerVariant.Success
    severidad == Severidad.ALTA -> InsightBannerVariant.Warning
    else -> InsightBannerVariant.Info
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val sampleInsightInfo = Insight(
    id = "sueno_bajo_esta_semana",
    tipo = TipoInsight.OBSERVACION,
    severidad = Severidad.BAJA,
    ventanaTexto = "Esta semana dormiste menos y registraste más craving. No es causalidad, pero conviene mirar.",
    accion = AccionInsight(tipo = TipoAccion.VER_BITACORA)
)

private val sampleInsightSuccess = Insight(
    id = "racha_5_dias",
    tipo = TipoInsight.RECONOCIMIENTO,
    severidad = Severidad.BAJA,
    ventanaTexto = "Llevas 5 días consecutivos registrando. Eso importa, incluso si la semana fue difícil.",
    accion = null
)

private val sampleInsightWarning = Insight(
    id = "animo_bajo_reiterado",
    tipo = TipoInsight.OBSERVACION,
    severidad = Severidad.ALTA,
    ventanaTexto = "Hemos visto varios registros con ánimo bajo. Si quieres, podemos hablarlo.",
    accion = AccionInsight(tipo = TipoAccion.HABLAR_BERTO)
)

@Preview(name = "SolvyxInsightBanner / Info", showBackground = true)
@Composable
private fun SolvyxInsightBannerInfoPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxInsightBanner(
                insight = sampleInsightInfo,
                onAction = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "SolvyxInsightBanner / Success", showBackground = true)
@Composable
private fun SolvyxInsightBannerSuccessPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxInsightBanner(
                insight = sampleInsightSuccess,
                onAction = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "SolvyxInsightBanner / Warning", showBackground = true)
@Composable
private fun SolvyxInsightBannerWarningPreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxInsightBanner(
                insight = sampleInsightWarning,
                onAction = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "SolvyxInsightBanner / animated visible", showBackground = true)
@Composable
private fun SolvyxInsightBannerAnimatedVisiblePreview() {
    SolvyxappTheme {
        Box(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            SolvyxInsightBannerAnimated(
                insight = sampleInsightInfo,
                onAction = {},
                onDismiss = {}
            )
        }
    }
}
