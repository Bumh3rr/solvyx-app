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
fun TerminosCondicionesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(title = "Términos y condiciones", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Términos y condiciones",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Al utilizar Solvyx, aceptas que esta aplicación es una herramienta de apoyo y no sustituye la atención médica profesional. En caso de emergencia, comunícate con los servicios de emergencias locales o una línea de crisis.",
                style = MaterialTheme.typography.bodyMedium,
                color = TealDark
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Solvyx no se hace responsable de decisiones tomadas con base exclusiva en la información provista por la aplicación. Consulta siempre a un profesional de salud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "© 2026 Solvyx — Tecnologías para la Salud Humana. Todos los derechos reservados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
