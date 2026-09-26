package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.validation.Validadores
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxDateField
import com.solvyx.ui.components.common.SolvyxTextField
import com.solvyx.ui.screens.profile.NICKNAME_MAX_LENGTH
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark

/**
 * Edit nickname and birth date. Stays open while saving (button shows progress) and shows a
 * failure inline — a snackbar would be hidden underneath this sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    nickname: String,
    birthDate: String,
    isSaving: Boolean,
    saveFailed: Boolean,
    canSave: Boolean,
    onNicknameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BertoPoseAnimation(
                    pose = BertoPose.RIGHT,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.size(52.dp),
                    fallback = R.drawable.berto_saludando
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Editar perfil",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        text = "Se guarda en tu cuenta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            FieldLabel("¿Cómo quieres que te llame?")
            SolvyxTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                placeholder = "Ej: Alex, Mia, Riku…",
                leadingIconRes = R.drawable.ic_person,
                filtro = Validadores::filtrarNombre
            )
            Text(
                text = "${nickname.length}/$NICKNAME_MAX_LENGTH",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            FieldLabel("Fecha de nacimiento")
            SolvyxDateField(
                value = birthDate,
                onDateSelected = onBirthDateChange,
                placeholder = "DD/MM/AAAA",
                leadingIconRes = R.drawable.ic_calendar
            )
            AnimatedVisibility(
                visible = saveFailed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SaveErrorBanner()
            }
            Spacer(Modifier.height(20.dp))
            SolvyxButton(
                text = if (isSaving) "Guardando…" else "Guardar cambios",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
                leadingIcon = if (isSaving) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 6.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = TealDark
    )
}

@Composable
private fun SaveErrorBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .background(CrisisRedLight, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_alert_triangle),
            contentDescription = null,
            tint = CrisisRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "No se pudo guardar. Revisa tu conexión e intenta de nuevo.",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = CrisisRed
        )
    }
}
