package com.solvyx.ui.screens.profile.legal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

/** One block of a legal document. [urgent] paints it red (emergency numbers). */
data class LegalSection(
    val title: String,
    @DrawableRes val icon: Int,
    val paragraphs: List<String> = emptyList(),
    val bullets: List<String> = emptyList(),
    val urgent: Boolean = false
)

data class LegalDocumentContent(
    val title: String,
    val lastUpdated: String,
    val intro: String,
    val sections: List<LegalSection>,
    val footer: String
)

/** Terms and privacy share this layout: a short intro, then one card per topic, easy to scan. */
@Composable
fun LegalDocumentScreen(document: LegalDocumentContent, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(title = document.title, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Última actualización: ${document.lastUpdated}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(document.intro, style = MaterialTheme.typography.bodyLarge, color = TealDark)
            document.sections.forEachIndexed { index, section ->
                StaggeredAppear(index = index) { SectionCard(section) }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                document.footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(section: LegalSection) {
    val accent = if (section.urgent) CrisisRed else TealPrimary
    SolvyxCard(modifier = Modifier.fillMaxWidth(), containerColor = if (section.urgent) CrisisRedLight else null) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(section.icon), null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
            }
            section.paragraphs.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            section.bullets.forEach { Bullet(it, accent) }
        }
    }
}

@Composable
private fun Bullet(text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
