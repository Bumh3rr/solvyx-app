package com.solvyx.ui.screens.profile.legal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.APP_TAGLINE
import com.solvyx.R
import com.solvyx.backend.repository.SUPPORT_EMAIL
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.components.common.openEmail
import com.solvyx.ui.components.common.rememberAppVersionName
import com.solvyx.ui.screens.firstaid.components.HelpLinesCard
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealPrimary

private const val EMAIL_SUBJECT = "Solvyx — contacto"

private data class Feature(@DrawableRes val icon: Int, val title: String, val description: String)

private val Features = listOf(
    Feature(R.drawable.ic_chat, "Berto", "Te escucha y te acompaña con temas guiados, y con IA si la activas."),
    Feature(R.drawable.ic_footsteps, "Mi camino", "Registra tu día y mira tu semana, tu diario y tus logros."),
    Feature(R.drawable.ic_heart_pulse, "Primeros auxilios", "Qué hacer en una crisis, con ansiedad o si consumiste de más."),
    Feature(R.drawable.ic_sos, "Red de apoyo y SOS", "Avisa a tus contactos de confianza con un solo toque."),
    Feature(R.drawable.ic_building, "Directorio", "Profesionales y centros de ayuda en Chilpancingo.")
)

/** What Solvyx is, what it offers, where to get help now, who made it and how to reach us. */
@Composable
fun AboutScreen(onBack: () -> Unit, onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GuiaTopBar(title = "Acerca de Solvyx", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AboutHero()
            SectionTitle("Qué es Solvyx")
            Text(
                "Una app de acompañamiento y reducción de daños para jóvenes de 13 a 24 años de " +
                    "Chilpancingo, Guerrero. Te ayuda a entender y cuidar tu relación con el alcohol, el " +
                    "vape, el cristal y el tabaco, sin juicios. Usa el cuestionario ASSIST de la " +
                    "Organización Mundial de la Salud para conocer tu nivel de riesgo.",
                style = MaterialTheme.typography.bodyMedium,
                color = TealDark
            )
            SectionTitle("Qué encuentras aquí")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Features.forEachIndexed { index, feature ->
                    StaggeredAppear(index = index) { FeatureRow(feature) }
                }
            }
            SectionTitle("¿Necesitas ayuda ahora?")
            HelpLinesCard()
            SectionTitle("Quiénes lo hacemos")
            Text(
                "Solvyx fue creado por el Equipo Solvyx — Tecnologías para la Salud Humana, para " +
                    "InnovaTec 2026. Solvyx es una herramienta de apoyo y no sustituye la atención de " +
                    "profesionales de la salud.",
                style = MaterialTheme.typography.bodyMedium,
                color = TealDark
            )
            ContactCard(onOpenTerms = onOpenTerms, onOpenPrivacy = onOpenPrivacy)
            Text(
                "© 2026 Solvyx — Tecnologías para la Salud Humana.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AboutHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(TealPrimary, TealDark)))
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_logo),
            contentDescription = "Logo de Solvyx",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White.copy(alpha = 0.7f), CircleShape)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Solvyx",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
        Text(
            APP_TAGLINE,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = Color.White.copy(alpha = 0.85f)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Versión ${rememberAppVersionName()}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.18f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = TealDark,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun FeatureRow(feature: Feature) {
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(feature.icon), null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    feature.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(feature.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Email (selectable, in case the phone has no email app) plus the legal documents. */
@Composable
private fun ContactCard(onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit) {
    val context = LocalContext.current
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Contacto",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                "Dudas, sugerencias o para pedir el borrado de tus datos:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SelectionContainer {
                Text(
                    SUPPORT_EMAIL,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TealPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button) { context.openEmail(SUPPORT_EMAIL, EMAIL_SUBJECT) }
                        .padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            LinkRow(R.drawable.ic_clipboard, "Términos y condiciones", onOpenTerms)
            LinkRow(R.drawable.ic_shield, "Política de privacidad", onOpenPrivacy)
        }
    }
}

@Composable
private fun LinkRow(@DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), null, tint = TealPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TealDark,
            modifier = Modifier.weight(1f)
        )
        Icon(painterResource(R.drawable.ic_chevron_right), null, tint = TealLight, modifier = Modifier.size(16.dp))
    }
}
