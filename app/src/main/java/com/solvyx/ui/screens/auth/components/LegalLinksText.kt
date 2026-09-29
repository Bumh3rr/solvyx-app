package com.solvyx.ui.screens.auth.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

/** Link labels: the same names as the screens they open. */
private const val TERMS_LABEL = "Términos y condiciones"
private const val PRIVACY_LABEL = "Política de privacidad"

/** "[prefix] Términos y condiciones y la Política de privacidad.", both names tappable. */
@Composable
fun LegalLinksText(
    prefix: String,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val linkStyle = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    val text = buildAnnotatedString {
        append("$prefix ")
        withLink(LinkAnnotation.Clickable(tag = "terms", linkInteractionListener = { onOpenTerms() })) {
            withStyle(linkStyle) { append(TERMS_LABEL) }
        }
        append(" y la ")
        withLink(LinkAnnotation.Clickable(tag = "privacy", linkInteractionListener = { onOpenPrivacy() })) {
            withStyle(linkStyle) { append(PRIVACY_LABEL) }
        }
        append(".")
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(textAlign = textAlign, color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier
    )
}
