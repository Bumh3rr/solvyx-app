package com.solvyx.ui.screens.chatbot

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.components.common.StaggeredAppear

private data class Capability(
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
    val isCrisis: Boolean = false
)

private val Capabilities = listOf(
    Capability(R.drawable.ic_chat, "Escucharte", "Cuéntale cómo te sientes o elige un tema guiado"),
    Capability(R.drawable.ic_wind, "Técnicas de calma", "Respirar con Berto, anclarte al presente (5-4-3-2-1) y manejar las ganas"),
    Capability(
        R.drawable.ic_alert_triangle,
        "Apoyo en crisis",
        "Si detecta que estás en riesgo, te acerca a Línea de la Vida, al 911 y a tu red de apoyo",
        isCrisis = true
    ),
    Capability(
        R.drawable.ic_shield,
        "Privado",
        "Sin tu permiso nada sale de tu teléfono. Si activas la IA, solo se envía el texto de tu mensaje"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BertoCapabilitiesSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.berto_mira_moriposa),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "¿Qué puede hacer Berto?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Capabilities.forEachIndexed { index, capability ->
                StaggeredAppear(index) { CapabilityRow(capability) }
            }
        }
    }
}

@Composable
private fun CapabilityRow(capability: Capability) {
    val tint = if (capability.isCrisis) CrisisRed else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (capability.isCrisis) CrisisRed.copy(alpha = 0.10f) else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(capability.icon), null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                capability.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (capability.isCrisis) CrisisRed else MaterialTheme.colorScheme.onBackground
            )
            Text(
                capability.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
