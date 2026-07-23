package com.solvyx.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxCard

/**
 * Bloque de cierre de Home: recordatorio de Berto + botón para abrir el chat. Sin botón SOS propio
 * (el bottom nav ya tiene uno siempre visible).
 */
@Composable
fun HomeBertoFooter(
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_TO_RIGHT,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.size(48.dp),
                fallback = R.drawable.berto_dedo_der
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Berto está aquí para ti",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Recuerda que un mal momento no define tu progreso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onNavigateToChat,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    "Hablar con Berto",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
