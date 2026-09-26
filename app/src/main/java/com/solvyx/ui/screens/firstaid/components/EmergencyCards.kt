package com.solvyx.ui.screens.firstaid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.EMERGENCY_NUMBER
import com.solvyx.ui.components.common.HelpLine
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.openDialer
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.screens.guias.components.DotRow
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

/**
 * Red-flag signs that need emergency services. Never collapsed: this is the one block of text in a
 * guide that must be readable at a glance, and it carries its own "call 911" action.
 */
@Composable
fun EmergencySignsCard(signs: List<String>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    SolvyxCard(modifier = modifier.fillMaxWidth(), containerColor = CrisisRedLight) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = R.drawable.ic_alert_octagon, tone = GuideTone.URGENT)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Llama al 911 si ves esto",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = CrisisRedDark
                    )
                    Text(
                        text = "No esperes a que se pase solo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrisisRedDark
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            signs.forEach { DotRow(color = CrisisRed, textColor = CrisisRedDark, text = it) }
            Spacer(Modifier.height(14.dp))
            SolvyxButton(
                text = "Llamar al 911",
                onClick = { context.openDialer(EMERGENCY_NUMBER) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = CrisisRed,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

/** Free 24/7 support lines, each one a tap away from the dialer. */
@Composable
fun HelpLinesCard(modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = R.drawable.ic_phone, tone = GuideTone.CALM)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Líneas de apoyo",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        text = "Gratuitas y disponibles 24/7",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HelpLine.entries.forEachIndexed { index, line ->
                if (index > 0) HorizontalDivider(color = TealLight.copy(alpha = 0.5f))
                HelpLineCallRow(line)
            }
        }
    }
}

@Composable
private fun HelpLineCallRow(line: HelpLine) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = "Llamar") { context.openDialer(line.dialNumber) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = line.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Text(
                text = line.displayNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_phone),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Llamar",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
