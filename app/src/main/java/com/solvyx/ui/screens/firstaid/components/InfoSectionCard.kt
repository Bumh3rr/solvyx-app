package com.solvyx.ui.screens.firstaid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.firstaid.model.InfoSection
import com.solvyx.ui.screens.guias.components.DotRow

private const val ChevronExpandedRotation = 90f

/**
 * Supporting info for a guide. Collapsible by default so the screen leads with actions, not text;
 * pass `collapsible = false` for safety-critical content that must always be visible.
 */
@Composable
fun InfoSectionCard(
    section: InfoSection,
    modifier: Modifier = Modifier,
    collapsible: Boolean = true
) {
    var expanded by rememberSaveable(section.title) { mutableStateOf(!collapsible) }
    val colors = section.tone.colors()
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) ChevronExpandedRotation else 0f,
        label = "sectionChevron"
    )

    SolvyxCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = colors.container,
        onClick = if (collapsible) ({ expanded = !expanded }) else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = section.icon, tone = section.tone)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = section.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.content
                )
                if (collapsible) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = if (expanded) "Ocultar" else "Ver más",
                        tint = colors.accent,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation)
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InfoSectionBody(section = section)
            }
        }
    }
}

@Composable
private fun InfoSectionBody(section: InfoSection) {
    val colors = section.tone.colors()
    Column(Modifier.padding(top = 8.dp)) {
        section.paragraph?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.content
            )
        }
        section.bullets.forEach { bullet ->
            DotRow(color = colors.accent, textColor = colors.content, text = bullet)
        }
    }
}
