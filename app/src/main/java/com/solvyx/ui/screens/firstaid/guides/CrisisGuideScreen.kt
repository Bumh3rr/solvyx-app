package com.solvyx.ui.screens.firstaid.guides

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.firstaid.components.ActionChecklistCard
import com.solvyx.ui.screens.firstaid.components.BertoSpeechHero
import com.solvyx.ui.screens.firstaid.components.FirstAidBerto
import com.solvyx.ui.screens.firstaid.components.GuideScaffold
import com.solvyx.ui.screens.firstaid.components.HelpLinesCard
import com.solvyx.ui.screens.firstaid.components.InfoSectionCard
import com.solvyx.ui.screens.firstaid.components.ProfessionalCareDisclaimer
import com.solvyx.ui.screens.firstaid.content.CrisisGuideContent
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.screens.firstaid.model.SosEmphasis
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

/** "Estoy en crisis ahora mismo": the most urgent guide, so the SOS bar is at full emphasis. */
@Composable
fun CrisisGuideScreen(
    onBack: () -> Unit,
    onSos: () -> Unit,
    onTalkToBerto: () -> Unit
) {
    GuideScaffold(
        title = "Estoy en crisis",
        onBack = onBack,
        onSos = onSos,
        sosEmphasis = SosEmphasis.FILLED,
        headerColor = TealDark
    ) {
        BertoSpeechHero(hero = CrisisGuideContent.hero)
        ActionChecklistCard(plan = CrisisGuideContent.plan)
        TalkToBertoCard(onClick = onTalkToBerto)
        InfoSectionCard(section = CrisisGuideContent.feelings)
        HelpLinesCard()
        ProfessionalCareDisclaimer()
    }
}

@Composable
private fun TalkToBertoCard(onClick: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FirstAidBerto(mood = BertoMood.POINTING, modifier = Modifier.size(52.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "¿Quieres hablar conmigo?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    text = "Estoy disponible ahora, sin internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
