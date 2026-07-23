package com.solvyx.ui.screens.journey.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.TealLight

/**
 * Replaces the charts + "Berto dice" card when there's no check-in yet at all (new account,
 * 0 check-ins) — showing flat charts at zero doesn't communicate anything useful. No button of
 * its own: the check-in above (`CheckInCard`, on the same screen) already covers the action.
 */
@Composable
fun ProgressEmptyState(modifier: Modifier = Modifier) {
    SolvyxCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = TealLight.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_HELLO,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.size(96.dp),
                fallback = R.drawable.berto_saludando
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Tu progreso empieza hoy",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
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
        }
    }
}
