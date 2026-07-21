package com.solvyx.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.TealDark

@Composable
fun PrivacidadDatosScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(title = "Privacidad y datos", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Privacidad y datos",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tu información se guarda de forma privada en tu cuenta y nunca se comparte con terceros.",
                style = MaterialTheme.typography.bodyMedium,
                color = TealDark
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Solvyx no recopila datos personales, no tiene acceso a tu historial y no requiere conexión a internet para funcionar. Tu privacidad es una prioridad en cada decisión de diseño de esta aplicación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
