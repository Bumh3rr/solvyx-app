package com.solvyx.ui.screens.firstaid.guides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.firstaid.components.ActionChecklistCard
import com.solvyx.ui.screens.firstaid.components.BertoSpeechHero
import com.solvyx.ui.screens.firstaid.components.GuideScaffold
import com.solvyx.ui.screens.firstaid.components.HelpLinesCard
import com.solvyx.ui.screens.firstaid.components.InfoSectionCard
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.content.PanicGuideContent
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.screens.firstaid.model.SosEmphasis
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

/** "Ansiedad y pánico": checklist first, then the guided 5-4-3-2-1 exercise as the main action. */
@Composable
fun PanicGuideScreen(
    onBack: () -> Unit,
    onSos: () -> Unit,
    onStartGrounding: () -> Unit
) {
    GuideScaffold(
        title = "Ansiedad y pánico",
        onBack = onBack,
        onSos = onSos,
        sosEmphasis = SosEmphasis.OUTLINED
    ) {
        BertoSpeechHero(hero = PanicGuideContent.hero)
        ActionChecklistCard(plan = PanicGuideContent.plan)
        GroundingExerciseCard(onStart = onStartGrounding)
        // Safety-critical: telling panic apart from a heart emergency is never hidden.
        InfoSectionCard(section = PanicGuideContent.heartWarning, collapsible = false)
        InfoSectionCard(section = PanicGuideContent.professionalHelp)
        HelpLinesCard()
    }
}

@Composable
private fun GroundingExerciseCard(onStart: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = R.drawable.ic_wind, tone = GuideTone.CALM)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "Ancla tu mente al presente",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        text = "Técnica 5-4-3-2-1, guiada con mi voz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PanicGuideContent.groundingSenses.forEach { (count, sense) ->
                    SenseTile(count = count, sense = sense, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(14.dp))
            SolvyxButton(
                text = "Hacerlo con Berto",
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_wind),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SenseTile(count: Int, sense: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(TealLight.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = sense,
            style = MaterialTheme.typography.labelSmall,
            color = TealDark,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
