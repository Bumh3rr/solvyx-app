package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.components.common.SolvyxTextField
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark

private val WhatGetsDeleted = listOf(
    "Tu perfil, sustancias y resultados del ASSIST",
    "Toda tu bitácora, tu racha y tus logros",
    "Tu cuenta: no podrás volver a entrar con este correo sin registrarte de nuevo"
)

/**
 * Account deletion, permanent. Asks for the password (Firebase requires a recent sign-in to delete
 * a user, and it makes the decision deliberate). Can't be dismissed while deleting.
 */
@Composable
fun DeleteAccountDialog(
    password: String,
    isDeleting: Boolean,
    error: String?,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(CrisisRedLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_trash), null, tint = CrisisRed, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "¿Eliminar tu cuenta?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Esto no se puede deshacer. Se borrará para siempre:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrisisRedDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WhatGetsDeleted.forEach { item ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier
                                    .padding(top = 7.dp)
                                    .size(6.dp)
                                    .background(CrisisRed, CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Escribe tu contraseña para confirmar",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TealDark,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                SolvyxTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = "Contraseña",
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    readOnly = isDeleting
                )
                AnimatedVisibility(visible = error != null) {
                    Text(
                        error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CrisisRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                SolvyxButton(
                    text = if (isDeleting) "Eliminando…" else "Eliminar mi cuenta",
                    onClick = onConfirm,
                    enabled = password.isNotBlank() && !isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CrisisRed
                )
                Spacer(Modifier.height(6.dp))
                SolvyxTextButton(text = "Cancelar", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
