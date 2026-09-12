package com.solvyx.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.drawer.model.CustomDrawerState

/**
 * Barra superior de Home: menú (drawer), el wordmark "Solvyx" centrado y la campana con su punto
 * de notificación. Presentacional: no decide nada, solo dispara los callbacks que recibe.
 */
@Composable
fun HomeTopBar(
    onOpenDrawer: () -> Unit,
    drawerState: CustomDrawerState,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onOpenDrawer,
            enabled = drawerState == CustomDrawerState.Closed
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Menú",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Solvyx",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.5f,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        // No unread-notifications dot here: there's no notifications feature/data source behind
        // this button yet, so a permanent "you have something new" badge would just be
        // misleading. Add it back once there's a real hasUnreadNotifications state to gate it on.
        IconButton(onClick = onNotificationsClick) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = "Notificaciones",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
