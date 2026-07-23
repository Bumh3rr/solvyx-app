package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.screens.guias.components.CardLabel
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest

/** "Herramientas para este momento": quick links to craving management and substance info. */
@Composable
fun PlanQuickTools(
    onNavigateToManejoCraving: () -> Unit,
    onNavigateToInfoSustancia: () -> Unit
) {
    CardLabel(iconRes = R.drawable.ic_brain, text = "Herramientas para este momento")
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HerramientaCard(
            iconRes = R.drawable.ic_flame,
            titulo = "Manejo del craving",
            subtitulo = "Técnicas para momentos difíciles",
            onClick = onNavigateToManejoCraving,
            modifier = Modifier.weight(1f)
        )
        HerramientaCard(
            iconRes = R.drawable.ic_info,
            titulo = "Info por sustancia",
            subtitulo = "Efectos y reducción de daños",
            onClick = onNavigateToInfoSustancia,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HerramientaCard(
    @DrawableRes iconRes: Int,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
            .border(
                width = 0.5.dp,
                color = TealLight,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TealLightest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
