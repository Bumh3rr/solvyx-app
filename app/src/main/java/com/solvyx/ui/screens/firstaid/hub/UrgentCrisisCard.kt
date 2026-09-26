package com.solvyx.ui.screens.firstaid.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight

/**
 * The most urgent entry point, set apart from the rest of the situations. It offers both the
 * crisis guide and the SOS itself, so notifying the support network is one tap from the hub.
 */
@Composable
fun UrgentCrisisCard(
    onOpenCrisisGuide: () -> Unit,
    onSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth(), containerColor = CrisisRedLight) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = R.drawable.ic_sos, tone = GuideTone.URGENT)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Estoy en crisis ahora mismo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = CrisisRedDark
                    )
                    Text(
                        text = "Para cuando eres tú quien necesita ayuda urgente",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrisisRedDark
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SolvyxOutlinedButton(
                    text = "Qué hacer",
                    onClick = onOpenCrisisGuide,
                    modifier = Modifier.weight(1f),
                    borderColor = CrisisRed,
                    textColor = CrisisRed
                )
                SolvyxButton(
                    text = "Avisar a mi red",
                    onClick = onSos,
                    modifier = Modifier.weight(1f),
                    containerColor = CrisisRed
                )
            }
        }
    }
}
