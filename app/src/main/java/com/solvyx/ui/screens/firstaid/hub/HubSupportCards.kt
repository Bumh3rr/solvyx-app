package com.solvyx.ui.screens.firstaid.hub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.firstaid.components.FirstAidBerto
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val OnPrimarySecondaryAlpha = 0.85f

/** Shortcut to the guided breathing/grounding exercise, for when the user just needs to calm down. */
@Composable
fun QuickCalmCard(onStartGrounding: () -> Unit, modifier: Modifier = Modifier) {
    SolvyxCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FirstAidBerto(mood = BertoMood.CALMING, modifier = Modifier.size(64.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "¿Necesitas calmarte ya?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = "Respira conmigo con el ejercicio 5-4-3-2-1. Toma unos minutos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = OnPrimarySecondaryAlpha)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onStartGrounding,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wind),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Respirar con Berto",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

/** Link to the (hardcoded, real) professional directory of Chilpancingo. */
@Composable
fun ProfessionalHelpCard(onOpenDirectory: () -> Unit, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier.fillMaxWidth(), onClick = onOpenDirectory) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToneIconBadge(icon = R.drawable.ic_building, tone = GuideTone.CALM)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Buscar ayuda profesional",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    text = "Centros y especialistas en Chilpancingo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
