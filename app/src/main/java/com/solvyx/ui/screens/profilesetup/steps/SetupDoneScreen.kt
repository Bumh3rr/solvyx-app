package com.solvyx.ui.screens.profilesetup.steps

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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.ConfettiBurst
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark

private val DoneBertoSize = 170.dp

private data class SummaryLine(val title: String, val detail: String, val done: Boolean)

/**
 * End of setup: a celebration plus a recap of what's ready. If the support network was skipped,
 * that line stays pending and explains the consequence, instead of silently dropping the user
 * on Home.
 */
@Composable
fun SetupDoneScreen(substanceCount: Int, contactCount: Int, onFinish: () -> Unit) {
    val lines = listOf(
        SummaryLine(
            title = "Sustancias",
            detail = if (substanceCount == 1) "1 en seguimiento" else "$substanceCount en seguimiento",
            done = substanceCount > 0
        ),
        SummaryLine(title = "Diagnóstico ASSIST", detail = "Completado", done = true),
        SummaryLine(
            title = "Red de apoyo",
            detail = if (contactCount > 0) {
                if (contactCount == 1) "1 contacto de confianza" else "$contactCount contactos de confianza"
            } else {
                "Pendiente: sin contactos, el botón SOS no avisa a nadie. Agrégalos en Mi perfil."
            },
            done = contactCount > 0
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ConfettiBurst(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BertoPoseAnimation(
                    pose = BertoPose.CENTER_IDLE_HELLO,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.size(DoneBertoSize),
                    fallback = R.drawable.berto_feliz
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "¡Tu espacio está listo!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Desde ahora te acompaño cada día. Empieza registrando cómo te sientes.",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                SolvyxCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        lines.forEach { SummaryRow(it) }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            SolvyxButton(
                text = "Ir a mi inicio",
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(line: SummaryLine) {
    val primary = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (line.done) primary else WarnAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(if (line.done) R.drawable.ic_check else R.drawable.ic_alert_triangle),
                contentDescription = null,
                tint = if (line.done) Color.White else WarnAmberDark,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = line.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Text(
                text = line.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (line.done) MaterialTheme.colorScheme.onSurfaceVariant else WarnAmberDark
            )
        }
    }
}
