package com.solvyx.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Banner global para indicar estado "sin internet".
 *
 * Decisión de color:
 * - Usa los tokens de advertencia del proyecto (`WarnAmber` /
 *   `WarnAmberDark`) en lugar de `MaterialTheme.colorScheme.error` para
 *   evitar que se confunda con un estado de crisis. Es **informativo**,
 *   no alarmante.
 *
 * Animación:
 * - `slideInVertically` desde arriba (−fullHeight → 0) al aparecer.
 * - `slideOutVertically` simétrico al desaparecer.
 * - Se combina con `fadeIn` / `fadeOut` para suavizar la transición.
 *
 * Accesibilidad:
 * - Todo el banner se anuncia como un solo bloque con `mergeDescendants`.
 * - El texto se extrae de strings.xml para localización.
 * - Touch target mínimo 48dp (defaultMinSize).
 */
@Composable
fun SolvyxSinRedBanner(
    visible: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        SinRedBannerContent(onAction = onAction)
    }
}

@Composable
private fun SinRedBannerContent(onAction: () -> Unit) {
    val title = stringResource(R.string.sin_red_banner_title)
    val subtitle = stringResource(R.string.sin_red_banner_subtitle)
    val actionLabel = stringResource(R.string.sin_red_banner_action)
    val a11yText = "$title. $subtitle. $actionLabel."

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarnAmber)
            // defaultMinSize: touch target accesible (48dp).
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                onClick = onAction,
                onClickLabel = actionLabel
            )
            .semantics(mergeDescendants = true) {
                contentDescription = a11yText
                role = Role.Button
            }
            .padding(horizontal = SolvyxSpacing.lg, vertical = SolvyxSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(WarnAmberDark.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_wifi_off),
                contentDescription = null,
                tint = WarnAmberDark,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(SolvyxSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = WarnAmberDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WarnAmberDark.copy(alpha = 0.85f),
                maxLines = 2
            )
        }
        Spacer(Modifier.width(SolvyxSpacing.sm))
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = WarnAmberDark
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "SolvyxSinRedBanner / visible", showBackground = true)
@Composable
private fun SolvyxSinRedBannerVisiblePreview() {
    SolvyxappTheme {
        Column {
            SolvyxSinRedBanner(visible = true, onAction = {})
        }
    }
}

@Preview(name = "SolvyxSinRedBanner / oculto", showBackground = true)
@Composable
private fun SolvyxSinRedBannerOcultoPreview() {
    SolvyxappTheme {
        Column {
            SolvyxSinRedBanner(visible = false, onAction = {})
        }
    }
}
