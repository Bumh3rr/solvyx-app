package com.solvyx.ui.screens.red.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.solvyx.R
import com.solvyx.backend.location.hasLocationPermission
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.TealDark

private const val LocationDeniedMessage =
    "Sin permiso de ubicación el SOS se enviará sin ella. Puedes darlo en los ajustes del teléfono."
private val LocationPermissions =
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

/**
 * Opt-in to add a maps link with the user's position to the SOS text. Turning it on asks for the
 * location permission here, never during a crisis. It shows as on only while the user opted in AND
 * the permission is still granted (it is re-checked on resume, it can be revoked from Settings).
 */
@Composable
fun LocationShareCard(optedIn: Boolean, onOptInChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    LifecycleResumeEffect(Unit) {
        hasPermission = context.hasLocationPermission()
        onPauseOrDispose { }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results.values.any { it }
        if (hasPermission) onOptInChange(true)
        else Toast.makeText(context, LocationDeniedMessage, Toast.LENGTH_LONG).show()
    }
    LocationShareRow(checked = optedIn && hasPermission) { enabled ->
        when {
            !enabled -> onOptInChange(false)
            hasPermission -> onOptInChange(true)
            else -> permissionLauncher.launch(LocationPermissions)
        }
    }
}

@Composable
private fun LocationShareRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_map_pin), null, tint = primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Incluir mi ubicación en el SOS",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    text = "Tus contactos recibirán un enlace de Google Maps con dónde estás. Solo va en " +
                        "el SMS; no se guarda en ningún servidor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            // The row handles the toggle, so the switch itself only mirrors the state.
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}
