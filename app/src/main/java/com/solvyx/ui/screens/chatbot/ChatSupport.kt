package com.solvyx.ui.screens.chatbot

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.EMERGENCY_NUMBER
import com.solvyx.ui.components.common.HelpLine
import com.solvyx.ui.components.common.openDialer
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary

private val CardShape = RoundedCornerShape(20.dp)
private val TileShape = RoundedCornerShape(16.dp)
private val MinTileHeight = 104.dp

/** The number a call action dials; `null` for actions that are not calls. */
private fun SupportAction.dialNumber(): String? = when (this) {
    SupportAction.CALL_LIFELINE -> HelpLine.LINEA_DE_LA_VIDA.dialNumber
    SupportAction.CALL_SAPTEL -> HelpLine.SAPTEL.dialNumber
    SupportAction.CALL_EMERGENCY -> EMERGENCY_NUMBER
    else -> null
}

private fun SupportAction.subtitle(): String = when (this) {
    SupportAction.BREATHE -> "Un minuto, a mi ritmo"
    SupportAction.GROUND -> "Ejercicio 5-4-3-2-1"
    SupportAction.CALL_LIFELINE -> "${HelpLine.LINEA_DE_LA_VIDA.displayNumber} · gratis, 24 h"
    SupportAction.CALL_SAPTEL -> "${HelpLine.SAPTEL.displayNumber} · 24 h"
    SupportAction.CALL_EMERGENCY -> "Si tu vida corre peligro"
    SupportAction.ALERT_NETWORK -> "Mando un mensaje a tus contactos"
    SupportAction.KEEP_TALKING -> ""
}

@DrawableRes
private fun SupportAction.icon(): Int = when (this) {
    SupportAction.BREATHE -> R.drawable.ic_wind
    SupportAction.GROUND -> R.drawable.ic_footsteps
    SupportAction.CALL_LIFELINE, SupportAction.CALL_SAPTEL -> R.drawable.ic_phone
    SupportAction.CALL_EMERGENCY -> R.drawable.ic_alert_octagon
    SupportAction.ALERT_NETWORK -> R.drawable.ic_people
    SupportAction.KEEP_TALKING -> R.drawable.ic_chat
}

/** Dials call actions (the user still confirms in the dialer) and always tells the ViewModel. */
@Composable
private fun rememberSupportActionHandler(onAction: (SupportAction) -> Unit): (SupportAction) -> Unit {
    val context = LocalContext.current
    return remember(context, onAction) {
        { action ->
            action.dialNumber()?.let { context.openDialer(it) }
            onAction(action)
        }
    }
}

/**
 * Calm-down and help actions inside the conversation. The urgent version leads with a call to
 * Línea de la Vida and adds 911; the soft one ("me siento mal") leads with breathing.
 */
@Composable
fun SupportActionsCard(urgent: Boolean, onAction: (SupportAction) -> Unit, modifier: Modifier = Modifier) {
    val handle = rememberSupportActionHandler(onAction)
    val tiles = if (urgent) {
        listOf(SupportAction.BREATHE, SupportAction.GROUND, SupportAction.ALERT_NETWORK, SupportAction.CALL_SAPTEL)
    } else {
        listOf(SupportAction.BREATHE, SupportAction.GROUND, SupportAction.CALL_LIFELINE, SupportAction.ALERT_NETWORK)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = BertoBubbleIndent, top = 8.dp)
            .clip(CardShape)
            .background(if (urgent) CrisisRedLight else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (urgent) CrisisRed.copy(alpha = 0.25f) else TealLight, CardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (urgent) {
            StaggeredAppear(index = 0) {
                PrimaryCallButton(SupportAction.CALL_LIFELINE, onClick = { handle(SupportAction.CALL_LIFELINE) })
            }
        }
        tiles.chunked(2).forEachIndexed { row, pair ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { column, action ->
                    StaggeredAppear(
                        index = 1 + row * 2 + column,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        fillHeight = true
                    ) {
                        SupportTile(action = action, urgent = urgent, onClick = { handle(action) })
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (urgent) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (urgent) EmergencyLink(onClick = { handle(SupportAction.CALL_EMERGENCY) })
            TextAction(
                label = SupportAction.KEEP_TALKING.label,
                color = TealPrimary,
                onClick = { handle(SupportAction.KEEP_TALKING) }
            )
        }
    }
}

@Composable
private fun PrimaryCallButton(action: SupportAction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TileShape)
            .background(CrisisRed)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(action.icon()), null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                action.label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text(action.subtitle(), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun SupportTile(action: SupportAction, urgent: Boolean, onClick: () -> Unit) {
    val accent = if (action.dialNumber() != null) CrisisRed else TealPrimary
    Column(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = MinTileHeight)
            .clip(TileShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, if (urgent) CrisisRed.copy(alpha = 0.15f) else TealLight.copy(alpha = 0.7f), TileShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(action.icon()), null, tint = accent, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            action.label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
        Text(
            action.subtitle(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmergencyLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, CrisisRed, RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_phone), null, tint = CrisisRed, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            SupportAction.CALL_EMERGENCY.label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = CrisisRedDark
        )
    }
}

@Composable
private fun TextAction(label: String, color: Color, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}

/**
 * Pinned under Berto's stage while in crisis support: the call is one tap away no matter how far
 * the conversation scrolls, and "Ya estoy mejor" is the way out of support mode.
 */
@Composable
fun CrisisSupportBar(onAction: (SupportAction) -> Unit, onFeelBetter: () -> Unit, modifier: Modifier = Modifier) {
    val handle = rememberSupportActionHandler(onAction)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CrisisRedLight)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "¿Necesitas hablar con alguien ya?",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = CrisisRedDark,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(CrisisRed)
                .clickable(role = Role.Button) { handle(SupportAction.CALL_LIFELINE) }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_phone), null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Llamar",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }
        Spacer(Modifier.width(4.dp))
        TextAction(label = BertoScripts.LEAVE_CRISIS, color = CrisisRedDark, onClick = onFeelBetter)
    }
}
