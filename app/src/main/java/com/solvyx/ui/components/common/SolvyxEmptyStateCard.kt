package com.solvyx.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Estado vacío reutilizable para listas y pantallas sin contenido.
 *
 * Anatomía:
 * - Berto ilustrado arriba (120dp). Se carga desde
 *   [SolvyxEmptyStateIllustration] que se puede sobreescribir vía
 *   `ilustracionRes` para casos puntuales (ej. empty state de
 *   chat con un Berto diferente).
 * - Título en `headlineMedium` con peso Bold (marcado como heading).
 * - Mensaje en `bodyMedium` con textAlign center y maxWidth limitado
 *   para que las líneas no se estiren demasiado.
 * - Botón opcional de acción. Si [SolvyxAction] es `null`, no se
 *   renderiza ningún botón (es la decisión por defecto: no empujar
 *   al usuario a nada si no hay un siguiente paso claro).
 *
 * Accesibilidad:
 * - La imagen de Berto se anuncia como "Berto" para que TalkBack la
 *   identifique por nombre (es personaje, no decoración).
 * - El título se marca como heading para navegación por encabezados.
 */
@Composable
fun SolvyxEmptyStateCard(
    titulo: String,
    mensaje: String,
    accion: SolvyxAction? = null,
    modifier: Modifier = Modifier,
    ilustracionRes: Int = R.drawable.berto_tranquilo
) {
    val bertoDesc = stringResource(R.string.insights_berto_label)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = SolvyxSpacing.xl, vertical = SolvyxSpacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(ilustracionRes),
                // Berto es personaje: TalkBack lo nombra para que el
                // usuario sepa que sigue "presente" aunque no haya
                // contenido.
                contentDescription = bertoDesc,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(SolvyxSpacing.lg))
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(SolvyxSpacing.sm))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )
            if (accion != null) {
                Spacer(Modifier.height(SolvyxSpacing.xl))
                SolvyxButton(
                    text = accion.label,
                    onClick = accion.onClick,
                    modifier = Modifier.widthIn(min = 200.dp)
                )
            }
        }
    }
}

/**
 * Acción opcional del empty state. Se modela como data class en lugar de
 * dos parámetros (`label`, `onClick`) para que la firma del componente
 * escale limpia cuando se agregue un segundo botón en el futuro.
 */
data class SolvyxAction(
    val label: String,
    val onClick: () -> Unit
)

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "SolvyxEmptyStateCard / con acción", showBackground = true)
@Composable
private fun SolvyxEmptyStateCardConAccionPreview() {
    SolvyxappTheme {
        SolvyxEmptyStateCard(
            titulo = "Aún no tienes ejercicios",
            mensaje = "Cuando empieces a registrar, Berto te sugerirá ejercicios según lo que aparezca en tu bitácora.",
            accion = SolvyxAction(label = "Ver ejercicios", onClick = {})
        )
    }
}

@Preview(name = "SolvyxEmptyStateCard / sin acción", showBackground = true)
@Composable
private fun SolvyxEmptyStateCardSinAccionPreview() {
    SolvyxappTheme {
        SolvyxEmptyStateCard(
            titulo = "No hay guías para esta categoría",
            mensaje = "Estamos trabajando en más contenido. Vuelve pronto."
        )
    }
}
