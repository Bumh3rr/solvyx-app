package com.solvyx.ui.screens.chatbot

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.theme.ChatWarmAccent
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary

private const val CARD_STAGGER_MS = 80L
private const val DISABLED_ALPHA = 0.5f
private val PickerShape = RoundedCornerShape(18.dp)
private val SlideFromBerto =
    fadeIn(tween(250)) + slideInHorizontally(
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    ) { -it / 5 }

private fun TopicIntent.accent(): Color = when (this) {
    TopicIntent.CRAVING -> ChatWarmAccent
    TopicIntent.INFO -> TealPrimary
    TopicIntent.FEELING_BAD -> CrisisRed
}

/** First guided step: what is going on. With [presetSubstanceId] only craving/info make sense. */
@Composable
fun TopicPickerCards(
    presetSubstanceId: String?,
    enabled: Boolean,
    onSelected: (TopicIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val intents = if (presetSubstanceId == null) TopicIntent.entries else TopicIntent.entries - TopicIntent.FEELING_BAD
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = BertoBubbleIndent, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        intents.forEachIndexed { index, intent ->
            StaggeredAppear(index, stepMs = CARD_STAGGER_MS, enter = SlideFromBerto) {
                TopicCard(intent = intent, enabled = enabled, onClick = { onSelected(intent) })
            }
        }
    }
}

@Composable
private fun TopicCard(intent: TopicIntent, enabled: Boolean, onClick: () -> Unit) {
    val accent = intent.accent()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(PickerShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, accent.copy(alpha = 0.35f), PickerShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(intent.icon), null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                intent.label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                intent.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            null,
            tint = TealLight,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Second guided step: the substance, as a 2×2 grid of big, tappable tiles. */
@Composable
fun SubstancePickerGrid(enabled: Boolean, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = BertoBubbleIndent, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrackedSubstances.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { column, substance ->
                    StaggeredAppear(row * 2 + column, Modifier.weight(1f), stepMs = CARD_STAGGER_MS) {
                        SubstanceTile(
                            label = substance.label,
                            icon = substance.icon,
                            enabled = enabled,
                            onClick = { onSelected(substance.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubstanceTile(label: String, icon: Int, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(PickerShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, TealLight, PickerShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(TealPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), null, tint = TealPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
    }
}
