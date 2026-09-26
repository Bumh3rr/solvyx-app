package com.solvyx.ui.screens.journey.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val CardTintAlpha = 0.35f
private val BertoHaloSize = 120.dp
private val BertoSize = 96.dp

/** What the user unlocks on this tab once there's history — previewed as chips. */
private data class UpcomingFeature(@DrawableRes val icon: Int, val label: String)

private val UpcomingFeatures = listOf(
    UpcomingFeature(R.drawable.ic_heart_pulse, "Tu bienestar"),
    UpcomingFeature(R.drawable.ic_calendar, "Semana y mes"),
    UpcomingFeature(R.drawable.ic_trending_up, "Tu constancia")
)

/**
 * Replaces the charts + "Berto dice" card when there's no check-in yet at all (new account,
 * 0 check-ins) — showing flat charts at zero doesn't communicate anything useful. No button of
 * its own: the check-in above (`CheckInCard`, on the same screen) already covers the action.
 */
@Composable
fun ProgressEmptyState(modifier: Modifier = Modifier) {
    SolvyxCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = TealLight.copy(alpha = CardTintAlpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BertoWithHalo()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tu progreso empieza hoy",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Registra tu día arriba para empezar a ver tu bienestar y tu " +
                    "constancia a lo largo del tiempo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            UpcomingFeatureChips()
        }
    }
}

@Composable
private fun BertoWithHalo() {
    Box(
        modifier = Modifier
            .size(BertoHaloSize)
            .background(MaterialTheme.colorScheme.surfaceDim, CircleShape)
            .border(2.dp, TealLight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        BertoPoseAnimation(
            pose = BertoPose.CENTER_IDLE_HELLO,
            riveFileRes = R.raw.berto_poses,
            modifier = Modifier.size(BertoSize),
            fallback = R.drawable.berto_saludando
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpcomingFeatureChips() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UpcomingFeatures.forEach { UpcomingFeatureChip(it) }
    }
}

@Composable
private fun UpcomingFeatureChip(feature: UpcomingFeature) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceDim, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(feature.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = feature.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
    }
}
