package com.solvyx.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard

/**
 * Bloque de cierre de Home: recordatorio de Berto + botón para abrir el chat. El botón va a todo
 * el ancho debajo del texto, así el título no se corta y nada queda en la esquina derecha donde
 * flota el SOS del bottom nav (que es el único botón SOS de la pantalla).
 */
@Composable
fun HomeBertoFooter(
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BertoPoseAnimation(
                    pose = BertoPose.CENTER_IDLE_TO_RIGHT,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.size(56.dp),
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
            }
            Spacer(Modifier.height(14.dp))
            SolvyxButton(
                text = "Hablar con Berto",
                onClick = onNavigateToChat,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
