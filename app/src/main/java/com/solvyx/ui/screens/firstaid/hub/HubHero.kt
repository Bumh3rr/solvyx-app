package com.solvyx.ui.screens.firstaid.hub

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.screens.firstaid.components.FirstAidBerto
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private val HeroBertoSize = 108.dp

/** Berto asks what's going on — the hub opens with a question, not with a list of articles. */
@Composable
fun HubHero(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FirstAidBerto(mood = BertoMood.WELCOMING, modifier = Modifier.size(HeroBertoSize))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "¿Qué está pasando?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Elige lo que más se parece a lo que sientes. Te acompaño paso a paso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OfflineChip()
        }
    }
}

@Composable
private fun OfflineChip() {
    Row(
        modifier = Modifier
            .background(TealLight.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_wifi_off),
            contentDescription = null,
            tint = TealDark,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Funciona sin internet",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
    }
}
