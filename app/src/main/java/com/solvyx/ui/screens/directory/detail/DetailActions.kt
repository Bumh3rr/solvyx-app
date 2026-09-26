package com.solvyx.ui.screens.directory.detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.formatMexicanPhone
import com.solvyx.ui.components.common.openDialer
import com.solvyx.ui.components.common.openDirections
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val OnPrimarySecondaryAlpha = 0.85f

/**
 * The two things people come here to do, as big tiles. Directions only appear when the entry has
 * a location, and are disabled (with the reason) while offline; calling always works.
 */
@Composable
fun DetailActions(entry: DirectoryEntry, isOnline: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionTile(
            icon = R.drawable.ic_phone,
            title = "Llamar",
            subtitle = formatMexicanPhone(entry.phone),
            filled = true,
            enabled = true,
            onClick = { context.openDialer(entry.phone) },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        val latitude = entry.latitude
        val longitude = entry.longitude
        if (latitude != null && longitude != null) {
            ActionTile(
                icon = R.drawable.ic_map_pin,
                title = "Cómo llegar",
                subtitle = if (isOnline) "Abrir en Maps" else "Necesitas internet",
                filled = false,
                enabled = isOnline,
                onClick = { context.openDirections(latitude, longitude) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ActionTile(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val contentColor = when {
        filled -> Color.White
        enabled -> TealDark
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconBackground = when {
        filled -> Color.White.copy(alpha = 0.2f)
        enabled -> primary.copy(alpha = 0.12f)
        else -> TealLight.copy(alpha = 0.35f)
    }
    SolvyxCard(
        modifier = modifier,
        containerColor = if (filled) primary else null,
        onClick = if (enabled) onClick else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (filled) Color.White else if (enabled) primary else contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (filled) Color.White.copy(alpha = OnPrimarySecondaryAlpha) else contentColor
            )
        }
    }
}
