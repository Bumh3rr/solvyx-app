package com.solvyx.ui.screens.firstaid.guides

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.screens.firstaid.components.ActionChecklistCard
import com.solvyx.ui.screens.firstaid.components.BertoSpeechHero
import com.solvyx.ui.screens.firstaid.components.EmergencySignsCard
import com.solvyx.ui.screens.firstaid.components.GuideScaffold
import com.solvyx.ui.screens.firstaid.components.HelpLinesCard
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.components.colors
import com.solvyx.ui.screens.firstaid.content.EmergencySigns
import com.solvyx.ui.screens.firstaid.content.OveruseGuideContent
import com.solvyx.ui.screens.firstaid.model.GuideSubstance
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.screens.firstaid.model.SosEmphasis
import com.solvyx.ui.screens.guias.components.DotRow
import com.solvyx.ui.theme.TealDark

private val SubstanceLabels = GuideSubstance.entries.map { it.label }

/** "Consumí de más": emergency signs first (safety), then per-substance warning signs and care. */
@Composable
fun OveruseGuideScreen(
    onBack: () -> Unit,
    onSos: () -> Unit
) {
    GuideScaffold(
        title = "Consumí de más",
        onBack = onBack,
        onSos = onSos,
        sosEmphasis = SosEmphasis.FILLED,
        headerColor = TealDark
    ) {
        BertoSpeechHero(hero = OveruseGuideContent.hero)
        EmergencySignsCard(signs = EmergencySigns)
        SubstanceWarningSignsCard()
        ActionChecklistCard(plan = OveruseGuideContent.plan)
        HelpLinesCard()
    }
}

@Composable
private fun SubstanceWarningSignsCard() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val colors = GuideTone.WARNING.colors()

    SolvyxCard(modifier = Modifier.fillMaxWidth(), containerColor = colors.container) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = R.drawable.ic_alert_triangle, tone = GuideTone.WARNING)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Señales de alerta",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.content
                    )
                    Text(
                        text = "¿Qué consumiste?",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.content
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SolvyxSegmentedControl(
                options = SubstanceLabels,
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it }
            )
            AnimatedContent(
                targetState = GuideSubstance.entries[selectedIndex],
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "substanceSigns"
            ) { substance ->
                Column(Modifier.padding(top = 4.dp)) {
                    OveruseGuideContent.warningSigns[substance].orEmpty().forEach { sign ->
                        DotRow(color = colors.accent, textColor = colors.content, text = sign)
                    }
                }
            }
        }
    }
}
