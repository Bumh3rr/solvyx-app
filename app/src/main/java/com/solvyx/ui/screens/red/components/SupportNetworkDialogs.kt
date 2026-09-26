package com.solvyx.ui.screens.red.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.solvyx.ui.theme.TealDark

/**
 * Shown right before Android's SMS permission prompt, so the user knows why it's asked and that no
 * message is ever sent unless they press SOS.
 */
@Composable
fun SmsPermissionRationaleDialog(onContinue: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        title = { DialogTitle("Un permiso más") },
        text = {
            Text(
                text = "Para avisar a tus contactos cuando presiones SOS, Android te pedirá permiso " +
                    "para enviar SMS. Solo se envía un mensaje si tú presionas SOS, nunca antes.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = { TextButton(onClick = onContinue) { DialogAction("Entendido") } },
        dismissButton = { TextButton(onClick = onDismiss) { DialogAction("Ahora no") } }
    )
}

/** Confirms skipping the support network during setup, stating plainly what that means for SOS. */
@Composable
fun SkipSupportNetworkDialog(onAddNow: () -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAddNow,
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        title = { DialogTitle("¿Configurar después?") },
        text = {
            Text(
                text = "Sin contactos, el botón SOS no podrá avisar a nadie. Puedes agregarlos cuando " +
                    "quieras desde Mi perfil.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = { TextButton(onClick = onAddNow) { DialogAction("Agregar ahora") } },
        dismissButton = { TextButton(onClick = onSkip) { DialogAction("Sí, después") } }
    )
}

@Composable
private fun DialogTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = TealDark
    )
}

@Composable
private fun DialogAction(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
}
