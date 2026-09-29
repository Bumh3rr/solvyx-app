package com.solvyx.ui.screens.red

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.red.components.ContactCard
import com.solvyx.ui.screens.red.components.LocationShareCard
import com.solvyx.ui.screens.red.components.SkipSupportNetworkDialog
import com.solvyx.ui.screens.red.components.SmsPermissionRationaleDialog

private const val SmsDeniedMessage =
    "Sin permiso de SMS no podremos avisarles automáticamente, pero podrás llamarles desde el SOS."
private val DashLength = floatArrayOf(8f, 6f)

/**
 * Support network: up to 3 SOS contacts. Used standalone ("Mi red de apoyo"), as the last setup
 * step ([isSetupMode], optionally [esOmitible]) and after a high-risk ASSIST. Inside the setup
 * wizard the shared step header replaces this screen's own bar ([showTopBar] = false).
 */
@Composable
fun RedApoyoScreen(
    isSetupMode: Boolean,
    onBack: () -> Unit,
    onFinishSetup: () -> Unit,
    esOmitible: Boolean = false,
    showTopBar: Boolean = true,
    viewModel: RedApoyoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showSmsRationale by rememberSaveable { mutableStateOf(false) }
    var showSkipConfirm by rememberSaveable { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, SmsDeniedMessage, Toast.LENGTH_LONG).show()
        viewModel.guardar()
    }
    val save = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.guardar() else showSmsRationale = true
    }

    LaunchedEffect(viewModel.savedSuccessfully) {
        if (!viewModel.savedSuccessfully) return@LaunchedEffect
        viewModel.resetSaved()
        if (isSetupMode) onFinishSetup()
        else Toast.makeText(context, "Contactos guardados", Toast.LENGTH_SHORT).show()
    }

    if (showSmsRationale) {
        SmsPermissionRationaleDialog(
            onContinue = {
                showSmsRationale = false
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            },
            onDismiss = {
                showSmsRationale = false
                Toast.makeText(context, SmsDeniedMessage, Toast.LENGTH_LONG).show()
                viewModel.guardar()
            }
        )
    }
    if (showSkipConfirm) {
        SkipSupportNetworkDialog(
            onAddNow = { showSkipConfirm = false },
            onSkip = {
                showSkipConfirm = false
                onFinishSetup()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        if (showTopBar) {
            SolvyxTopBar(
                title = if (isSetupMode) "Red de apoyo" else "Mi red de apoyo",
                navigationButton = { SolvyxBackButton(onClick = onBack) }
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BertoSpeechRow(
                message = if (isSetupMode) "¿Quién estará contigo?" else "Tu red de apoyo",
                supporting = if (isSetupMode) {
                    "Si algún día presionas SOS, les avisaré por SMS. Solo ellos lo sabrán."
                } else {
                    "Puedes cambiar tus contactos cuando quieras."
                }
            ) {
                BertoPoseAnimation(
                    pose = BertoPose.RIGHT,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.fillMaxSize(),
                    fallback = R.drawable.berto_saludando
                )
            }
            viewModel.contactos.forEachIndexed { index, contacto ->
                ContactCard(
                    index = index,
                    contact = contacto,
                    onChange = { viewModel.setContacto(index, it) },
                    onRemove = { viewModel.removeContacto(index) }
                )
            }
            if (viewModel.contactos.size < MAX_CONTACTS) {
                AddContactButton(onClick = viewModel::addContacto)
            }
            val shareLocation by viewModel.shareLocation.collectAsStateWithLifecycle()
            LocationShareCard(optedIn = shareLocation, onOptInChange = viewModel::setShareLocation)
            PrivacyNote()
        }
        SaveBar(
            isSetupMode = isSetupMode,
            canSave = viewModel.canSave(),
            isSaving = viewModel.isSaving,
            showSkip = isSetupMode && esOmitible,
            onSave = save,
            onSkip = { showSkipConfirm = true }
        )
    }
}

@Composable
private fun AddContactButton(onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    color = primary,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(DashLength, 0f)),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add_user),
            contentDescription = null,
            tint = primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Añadir otro contacto (opcional)",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = primary
        )
    }
}

@Composable
private fun PrivacyNote() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Solo se envía un SMS si tú presionas SOS. Nunca antes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SaveBar(
    isSetupMode: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    showSkip: Boolean,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!canSave) {
            Text(
                text = "Completa el contacto principal para continuar",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        SolvyxButton(
            text = when {
                isSaving -> "Guardando…"
                isSetupMode -> "Guardar y continuar"
                else -> "Guardar cambios"
            },
            onClick = onSave,
            enabled = canSave && !isSaving,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = if (isSaving) {
                { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) }
            } else null
        )
        if (showSkip) SolvyxTextButton(text = "Configurar después", onClick = onSkip)
    }
}
