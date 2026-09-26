package com.solvyx.ui.screens.profilesetup.steps

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.profilesetup.ProfileSetupStep
import com.solvyx.ui.screens.profilesetup.components.SetupScaffold
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val IconCapsuleAlpha = 0.45f

private data class AssistFact(@DrawableRes val icon: Int, val title: String, val body: String)

/**
 * Step 2, before the first question: what the ASSIST is and what to expect, so the questionnaire
 * doesn't start cold. [substanceCount] adapts the "how long" line.
 */
@Composable
fun AssistIntroScreen(substanceCount: Int, onStart: () -> Unit, onBack: (() -> Unit)?) {
    val substancesText = if (substanceCount == 1) "la sustancia que elegiste" else "cada una de las $substanceCount sustancias"
    val facts = listOf(
        AssistFact(R.drawable.ic_shield, "Creado por la OMS", "Es un cuestionario usado por profesionales de la salud en todo el mundo."),
        AssistFact(R.drawable.ic_lock, "Confidencial", "Tus respuestas se guardan de forma privada en tu cuenta."),
        AssistFact(R.drawable.ic_clock, "Toma pocos minutos", "Son 6 preguntas sobre $substancesText (7 para cristal)."),
        AssistFact(R.drawable.ic_heart, "No hay respuestas malas", "Contesta con honestidad; sirve para darte mejores recomendaciones.")
    )
    SetupScaffold(
        step = ProfileSetupStep.ASSIST,
        onBack = onBack,
        primaryLabel = "Comenzar",
        onPrimary = onStart
    ) {
        BertoSpeechRow(
            message = "Ahora, unas preguntas rápidas",
            supporting = "Me ayudan a entender cómo te afecta cada sustancia y a acompañarte mejor."
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.fillMaxSize(),
                fallback = R.drawable.berto_tranquilo
            )
        }
        SolvyxCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 6.dp)) {
                facts.forEachIndexed { index, fact ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = TealLight.copy(alpha = 0.5f)
                        )
                    }
                    FactRow(fact)
                }
            }
        }
    }
}

@Composable
private fun FactRow(fact: AssistFact) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(TealLight.copy(alpha = IconCapsuleAlpha), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(fact.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = fact.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Text(
                text = fact.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
