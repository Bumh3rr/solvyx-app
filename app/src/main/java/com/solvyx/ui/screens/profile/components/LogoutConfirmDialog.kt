package com.solvyx.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark

/**
 * Sign-out confirmation. For guests the copy is a clear warning: signing out as a guest deletes
 * everything permanently, and there's no way to get it back.
 */
@Composable
fun LogoutConfirmDialog(
    isAnonymous: Boolean,
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(CrisisRedLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = null,
                        tint = CrisisRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "¿Cerrar sesión?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isAnonymous) {
                        "Perderás todo tu progreso de forma permanente: sustancias, diagnóstico y " +
                            "bitácora no se pueden recuperar sin una cuenta."
                    } else {
                        "Tu información está respaldada en tu cuenta. Podrás verla de nuevo al iniciar sesión."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAnonymous) CrisisRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                )
                SolvyxButton(
                    text = "Sí, cerrar sesión",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CrisisRed
                )
                Spacer(Modifier.height(6.dp))
                SolvyxTextButton(text = "Cancelar", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
