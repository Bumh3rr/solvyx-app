package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SubstanceOption
import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import kotlinx.coroutines.delay

private const val GridColumns = 2
private const val SavedBadgeMillis = 1_600L
private const val SelectedTintAlpha = 0.1f
private const val IconCapsuleAlpha = 0.45f
private const val PressedScale = 0.96f

/**
 * Tracked substances as tappable tiles. A tap toggles and saves at once (no separate "edit" step),
 * and a short "Guardado" badge confirms each save. [savedTick] changes after every saved toggle.
 */
@Composable
fun SubstanceTrackingCard(
    selected: Set<String>,
    savedTick: Int,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Sustancias en seguimiento",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        text = "Toca para activar o quitar. Se guarda al instante.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SavedBadge(savedTick = savedTick)
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TrackedSubstances.chunked(GridColumns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { option ->
                            SubstanceToggleTile(
                                option = option,
                                selected = option.id in selected,
                                onToggle = { onToggle(option.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedBadge(savedTick: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(savedTick) {
        if (savedTick == 0) return@LaunchedEffect
        visible = true
        delay(SavedBadgeMillis)
        visible = false
    }
    AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = SelectedTintAlpha), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Guardado",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SubstanceToggleTile(
    option: SubstanceOption,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val background by animateColorAsState(
        if (selected) primary.copy(alpha = SelectedTintAlpha) else Color.Transparent,
        label = "tileBg"
    )
    val border by animateColorAsState(if (selected) primary else TealLight, label = "tileBorder")
    // Bounces back to full size each time the selection flips.
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) PressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { pressed = false },
        label = "tileScale"
    )
    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(14.dp))
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = {
                pressed = true
                onToggle()
            })
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(TealLight.copy(alpha = IconCapsuleAlpha), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = option.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
        AnimatedVisibility(visible = selected, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
