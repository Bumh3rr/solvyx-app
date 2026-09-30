package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.components.common.EMERGENCY_NUMBER
import com.solvyx.ui.components.common.HelpLine
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.components.common.openDialer
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.components.navigation.SolvyxBottomNavClearance
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import dev.chrisbanes.haze.haze

/**
 * "Conoce tu sustancia": the user's substances first, and for each one five sections (the
 * essentials, body and mind, how to take care, risky mixes, when to ask for help) with the help
 * lines, the directory and a way to keep asking Berto. The content lives in [SubstanceInfos].
 */
@Composable
fun InfoSustanciaScreen(
    onBack: () -> Unit,
    onAskBerto: (substanceId: String) -> Unit,
    onOpenDirectory: () -> Unit,
    viewModel: InfoSustanciaViewModel = hiltViewModel()
) {
    val substances by viewModel.substances.collectAsStateWithLifecycle()
    // Until the user picks one, the first of the list (their own substance) is shown.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = substances.firstOrNull { it.id == selectedId } ?: substances.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = "Conoce tu sustancia",
            navigationButton = { SolvyxBackButton(onClick = onBack) }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                // El contenido es la fuente del blur del bottom nav (ver LocalHazeState).
                .haze(
                    LocalHazeState.current,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    tint = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    blurRadius = 16.dp
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = SolvyxBottomNavClearance),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BertoSpeechRow(message = SubstanceInfoIntroTitle, supporting = SubstanceInfoIntro) {
                BertoPoseAnimation(
                    pose = BertoPose.CENTER_IDLE,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.fillMaxSize(),
                    fallback = R.drawable.berto_tranquilo
                )
            }
            SolvyxSegmentedControl(
                options = substances.map { it.label },
                selectedIndex = substances.indexOf(selected),
                onSelect = { selectedId = substances[it].id }
            )
            AnimatedContent(
                targetState = selected,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "substanceInfo"
            ) { info ->
                SubstanceSections(info = info, onOpenDirectory = onOpenDirectory)
            }
            SolvyxButton(
                text = "Pregúntale a Berto sobre ${selected.askBerto}",
                onClick = { onAskBerto(selected.id) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_chat), contentDescription = null, modifier = Modifier.size(20.dp))
                }
            )
            Text(
                SubstanceInfoDisclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val SubstanceInfoIntroTitle = "Conoce tu sustancia"

@Composable
private fun SubstanceSections(info: SubstanceInfo, onOpenDirectory: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoSection(R.drawable.ic_info_circle, "Lo esencial") {
            BodyText(info.essentials)
        }
        InfoSection(R.drawable.ic_brain, "Qué le hace a tu cuerpo y mente") {
            info.bodyAndMind.forEach { Bullet(it) }
        }
        InfoSection(R.drawable.ic_shield, info.careTitle) {
            info.careTips.forEach { Bullet(it) }
        }
        InfoSection(R.drawable.ic_alert_triangle, "Mezclas riesgosas") {
            info.riskyMixes.forEach { mix ->
                Bullet(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${mix.with}: ") }
                        append(mix.why)
                    }
                )
            }
        }
        InfoSection(R.drawable.ic_heart_pulse, "Señales para pedir ayuda") {
            EmergencyBox(info.emergency)
            info.helpTips.forEach { Bullet(it) }
            HelpLines(onOpenDirectory = onOpenDirectory)
        }
    }
}

@Composable
private fun InfoSection(@DrawableRes icon: Int, title: String, content: @Composable ColumnScope.() -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCapsule(icon)
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TealDark,
                    modifier = Modifier.weight(1f)
                )
            }
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun Bullet(text: String) = Bullet(AnnotatedString(text))

@Composable
private fun Bullet(text: AnnotatedString) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** What should make someone call 911, with a button that opens the dialer (it never calls by itself). */
@Composable
private fun EmergencyBox(text: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrisisRedLight)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                painterResource(R.drawable.ic_alert_octagon),
                contentDescription = null,
                tint = CrisisRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = CrisisRedDark
            )
        }
        Button(
            onClick = { context.openDialer(EMERGENCY_NUMBER) },
            colors = ButtonDefaults.buttonColors(containerColor = CrisisRed),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(painterResource(R.drawable.ic_phone), contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Llamar al $EMERGENCY_NUMBER", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** The free 24/7 lines (tap opens the dialer, no permission needed) and the help directory. */
@Composable
private fun HelpLines(onOpenDirectory: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HelpLineRow(HelpLine.LINEA_DE_LA_VIDA, note = "Gratis, 24 horas") { context.openDialer(it) }
        HelpLineRow(HelpLine.SAPTEL, note = "24 horas") { context.openDialer(it) }
        TextButton(onClick = onOpenDirectory, modifier = Modifier.align(Alignment.End)) {
            Text(
                "Ver directorio de ayuda →",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HelpLineRow(line: HelpLine, note: String, onDial: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button) { onDial(line.dialNumber) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconCapsule(R.drawable.ic_phone)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${line.displayName} · ${line.displayNumber}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
