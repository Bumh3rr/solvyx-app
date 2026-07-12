package com.solvyx.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.theme.CrisisRed

/**
 * Renderiza un item del drawer.
 *
 * Si `showNewBadge == true` y el item está marcado como nuevo en
 * `NavigationItem.isNew`, se muestra un badge "NUEVO" a la derecha.
 *
 * Si `selected == true`, se aplica fondo translúcido y borde sutil.
 */
@Composable
fun NavigationItemView(
    navigationItem: NavigationItem,
    selected: Boolean,
    isDestructive: Boolean = false,
    showNewBadge: Boolean = true,
    onClick: () -> Unit
) {
    val textColor = when {
        isDestructive -> CrisisRed
        selected      -> Color.White
        else          -> Color.White.copy(alpha = 0.95f)
    }
    val iconTint = when {
        isDestructive -> CrisisRed
        selected      -> Color.White
        else          -> Color.White.copy(alpha = 0.95f)
    }
    val bgColor = when {
        isDestructive -> Color.Transparent
        selected      -> Color.White.copy(alpha = 0.14f)
        else          -> Color.Transparent
    }

    val shape = RoundedCornerShape(12.dp)
    val showBadge = showNewBadge && navigationItem.isNew && !selected

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (bgColor == Color.Transparent) 0.dp else 1.dp,
                color = if (bgColor == Color.Transparent) Color.Transparent else Color.White.copy(alpha = 0.18f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 15.dp)
            .semantics {
                contentDescription = if (showBadge) {
                    "${navigationItem.title} (nuevo)"
                } else {
                    navigationItem.title
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(navigationItem.icon),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = navigationItem.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (showBadge) {
            NewBadge()
            Spacer(Modifier.width(8.dp))
        }
        if (selected && !isDestructive) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
