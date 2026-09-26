package com.solvyx.ui.screens.firstaid.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val CheckedRowTintAlpha = 0.08f

/**
 * Full-width tappable row with a check circle — reads as "select this" at a glance, unlike a chip.
 * [marker] is shown inside the empty circle (e.g. a step number); when null the circle is hollow.
 * [dimWhenChecked] fades the text for "done" semantics (checklists) rather than "selected" (options).
 */
@Composable
fun CheckableRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    marker: String? = null,
    dimWhenChecked: Boolean = false
) {
    val primary = MaterialTheme.colorScheme.primary
    val circleColor by animateColorAsState(
        targetValue = when {
            checked -> primary
            marker != null -> TealLight.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        label = "checkCircle"
    )
    val rowTint by animateColorAsState(
        targetValue = if (checked) primary.copy(alpha = CheckedRowTintAlpha) else Color.Transparent,
        label = "checkRow"
    )
    val textColor = if (checked && dimWhenChecked) MaterialTheme.colorScheme.onSurfaceVariant else TealDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowTint)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(circleColor, CircleShape)
                .then(
                    if (!checked && marker == null) Modifier.border(2.dp, TealLight, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                checked -> Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                marker != null -> Text(
                    text = marker,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TealDark
                )
            }
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}
