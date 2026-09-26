package com.solvyx.ui.screens.profile.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.rememberAppVersionName
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val IconCapsuleAlpha = 0.45f
private const val DisabledRowAlpha = 0.55f
private const val DividerAlpha = 0.5f

/** Account settings and app information, as two labeled groups. */
@Composable
fun ProfileSettingsCard(
    contactCount: Int,
    onOpenSupportNetwork: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val version = rememberAppVersionName()
    Column(modifier = modifier.fillMaxWidth()) {
        GroupLabel("Cuenta")
        SolvyxCard(modifier = Modifier.fillMaxWidth()) {
            SettingsRow(
                icon = R.drawable.ic_people,
                label = "Mi red de apoyo",
                onClick = onOpenSupportNetwork,
                trailing = { ContactsBadge(contactCount) }
            )
            RowDivider()
            // Placeholder until reminders exist: visibly off and labeled, never a fake working toggle.
            SettingsRow(
                icon = R.drawable.ic_bell,
                label = "Notificaciones",
                enabled = false,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SoftPill("Próximamente")
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = false, onCheckedChange = null, enabled = false)
                    }
                }
            )
        }
        GroupLabel("Información")
        SolvyxCard(modifier = Modifier.fillMaxWidth()) {
            SettingsRow(R.drawable.ic_shield, "Privacidad y datos", onOpenPrivacy)
            RowDivider()
            SettingsRow(
                icon = R.drawable.ic_info_circle,
                label = "Acerca de Solvyx",
                onClick = onOpenAbout,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (version.isNotBlank()) {
                            Text(
                                text = "v$version",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Chevron()
                    }
                }
            )
            RowDivider()
            SettingsRow(R.drawable.ic_clipboard, "Términos y condiciones", onOpenTerms)
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SettingsRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit = { Chevron() }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier
            )
            .alpha(if (enabled) 1f else DisabledRowAlpha)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(TealLight.copy(alpha = IconCapsuleAlpha), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TealDark
        )
        trailing()
    }
}

/** No contacts means SOS can't notify anyone, so that case is flagged instead of shown in grey. */
@Composable
private fun ContactsBadge(contactCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (contactCount == 0) {
            Text(
                text = "Sin contactos",
                modifier = Modifier
                    .background(CrisisRedLight, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CrisisRed
            )
        } else {
            Text(
                text = if (contactCount == 1) "1 contacto" else "$contactCount contactos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(6.dp))
        Chevron()
    }
}

@Composable
private fun SoftPill(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .background(TealLight.copy(alpha = IconCapsuleAlpha), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = TealDark
    )
}

@Composable
private fun Chevron() {
    Icon(
        painter = painterResource(R.drawable.ic_chevron_right),
        contentDescription = null,
        tint = TealLight,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = TealLight.copy(alpha = DividerAlpha),
        thickness = 0.5.dp
    )
}
