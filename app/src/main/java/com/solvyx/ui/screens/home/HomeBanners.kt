package com.solvyx.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark

/**
 * Banner ámbar: el botón SOS no tiene contactos configurados. Franja de acento a la izquierda,
 * icono, texto, enlace de acción y botón de descartar.
 */
@Composable
fun SosWarningBanner(
    onConfigClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ámbar de advertencia intermedio: ni el fondo claro (WarnAmber) ni el marrón del texto
    // (WarnAmberDark). No tiene constante propia en la paleta; se deja aquí como literal local.
    val amberStroke = Color(0xFFD97706)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, amberStroke.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(WarnAmber),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(amberStroke)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_triangle),
                contentDescription = null,
                tint = amberStroke,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Tu botón SOS no tiene contactos",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = WarnAmberDark
                )
                Text(
                    text = "Sin contactos, nadie recibirá aviso en una emergencia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnAmberDark.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            BannerAction(text = "Configurar →", tint = amberStroke, onClick = onConfigClick)
            Spacer(Modifier.width(10.dp))
            BannerDismiss(tint = amberStroke, onClick = onDismiss)
        }
    }
}

/**
 * Banner informativo en el color de marca: se usa para "completa el ASSIST" y "crea una cuenta".
 * Mismo esqueleto que [SosWarningBanner], en tono primary.
 */
@Composable
fun InfoBanner(
    titulo: String,
    subtitulo: String,
    accion: String,
    onAccionClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(primary)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_flag),
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            BannerAction(text = accion, tint = primary, onClick = onAccionClick)
            Spacer(Modifier.width(10.dp))
            BannerDismiss(tint = primary, onClick = onDismiss)
        }
    }
}

/** Enlace de acción de un banner (texto en negrita, sin ripple). */
@Composable
private fun BannerAction(text: String, tint: Color, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = tint,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    )
}

/** Botón "x" de descartar de un banner. */
@Composable
private fun BannerDismiss(tint: Color, onClick: () -> Unit) {
    Icon(
        painter = painterResource(R.drawable.ic_circle_x),
        contentDescription = "Descartar",
        tint = tint,
        modifier = Modifier
            .size(16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    )
}
