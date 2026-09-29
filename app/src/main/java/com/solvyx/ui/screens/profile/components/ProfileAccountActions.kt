package com.solvyx.ui.screens.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealLight

/** Guest-only call to create an account, then sign out and (with an account) delete it, at the end of Mi perfil. */
@Composable
fun ProfileAccountActions(
    isAnonymous: Boolean,
    onCreateAccount: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isAnonymous) {
            SolvyxButton(
                text = "Crear cuenta y guardar mi progreso",
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_user),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            Text(
                text = "Como invitado, tu progreso se borra al cerrar sesión.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 16.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        SolvyxOutlinedButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            borderColor = CrisisRed,
            textColor = CrisisRed,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    tint = CrisisRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        // Guests have nothing stored outside the phone: signing out already erases it.
        if (!isAnonymous) {
            SolvyxTextButton(
                text = "Eliminar mi cuenta",
                onClick = onDeleteAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
        Text(
            text = "Solvyx 2026 · Tecnologías para la Salud Humana",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TealLight,
            textAlign = TextAlign.Center
        )
    }
}
