package com.solvyx.ui.components.drawer

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.drawer.model.NavigationItem

@Composable
fun NavigationItemView(
    navigationItem: NavigationItem,
    selected: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val textColor = when {
        isDestructive -> Color(0xFFfca5a5)
        selected      -> Color.White
        else          -> Color.White.copy(alpha = 0.70f)
    }
    val iconTint = when {
        isDestructive -> Color(0xFFfca5a5)
        selected      -> Color.White
        else          -> Color.White.copy(alpha = 0.70f)
    }
    val bgColor = when {
        isDestructive -> Color.Transparent
        selected      -> Color.White.copy(alpha = 0.15f)
        else          -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(navigationItem.icon),
            contentDescription = navigationItem.title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = navigationItem.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
            ),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
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
