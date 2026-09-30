package com.solvyx.ui.screens.journey.checkin.steps

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.WarnAmberDark

private const val SelectedScale = 1.03f
private const val UnselectedScale = 0.96f
private const val SelectedTintAlpha = 0.1f
private const val IconTintAlpha = 0.14f

/**
 * "¿Consumiste hoy?" as two big, judgment-free cards: teal for no, warm amber for yes — never the
 * SOS red, which would read as an alarm for an honest answer.
 */
@Composable
fun UseStep(used: Boolean?, onSelect: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    val select: (Boolean) -> Unit = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onSelect(it)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UseCard(
            title = "No consumí",
            subtitle = "Hoy fue un día sin consumo",
            icon = R.drawable.ic_check_circle,
            accent = MaterialTheme.colorScheme.primary,
            isSelected = used == false,
            anySelected = used != null,
            onClick = { select(false) },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        UseCard(
            title = "Sí consumí",
            subtitle = "Está bien decirlo",
            icon = R.drawable.ic_droplet,
            accent = WarnAmberDark,
            isSelected = used == true,
            anySelected = used != null,
            onClick = { select(true) },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun UseCard(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    accent: Color,
    isSelected: Boolean,
    anySelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isSelected -> SelectedScale
            anySelected -> UnselectedScale
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "useScale"
    )
    val background by animateColorAsState(
        if (isSelected) accent.copy(alpha = SelectedTintAlpha) else MaterialTheme.colorScheme.surfaceDim,
        label = "useBg"
    )
    val border by animateColorAsState(if (isSelected) accent else TealLight, label = "useBorder")
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(background)
            .border(if (isSelected) 2.dp else 1.dp, border, shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(if (isSelected) accent else accent.copy(alpha = IconTintAlpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (isSelected) Color.White else accent,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = if (isSelected) accent else TealDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
