package com.solvyx.ui.screens.firstaid.crisischeck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.HelpLine
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.openDialer
import com.solvyx.ui.screens.firstaid.components.CheckableRow
import com.solvyx.ui.screens.firstaid.components.ToneIconBadge
import com.solvyx.ui.screens.firstaid.components.colors
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.screens.firstaid.model.GuideHero
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val FirstQuestionHint =
    "Son 3 preguntas rápidas. Marca todo lo que te pase ahora; al final te digo qué puedes hacer."
private const val NextQuestionHint = "Marca todo lo que aplique. Si nada de esto te pasa, sigue adelante."

/** Berto asks the question of step [index] in his speech bubble. */
fun questionHero(group: CrisisSignalGroup, index: Int) = GuideHero(
    mood = BertoMood.CALMING,
    message = group.question,
    supporting = if (index == 0) FirstQuestionHint else NextQuestionHint
)

/** The options of one question, as rows with a check circle so multi-select is obvious. */
@Composable
fun SignalQuestionCard(
    group: CrisisSignalGroup,
    selected: Set<CrisisSignal>,
    onToggle: (CrisisSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val markedHere = group.signals.count { it in selected }
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneIconBadge(icon = group.icon, tone = GuideTone.CALM)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        text = if (markedHere == 0) "Puedes marcar varias" else "Marcaste $markedHere",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            group.signals.forEach { signal ->
                CheckableRow(
                    text = signal.label,
                    checked = signal in selected,
                    onToggle = { onToggle(signal) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Shown the moment a red-flag sign is marked — the user shouldn't have to finish the questions to
 * be told they can get help right now.
 */
@Composable
fun RedFlagNotice(visible: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val line = HelpLine.LINEA_DE_LA_VIDA
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        SolvyxCard(modifier = Modifier.fillMaxWidth(), containerColor = CrisisRedLight) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ToneIconBadge(icon = R.drawable.ic_heart, tone = GuideTone.URGENT)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Gracias por decírmelo. No tienes que esperar al final: puedes " +
                            "hablar con alguien ahora mismo.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CrisisRedDark
                    )
                }
                Spacer(Modifier.height(12.dp))
                SolvyxButton(
                    text = "Llamar a ${line.displayName}",
                    onClick = { context.openDialer(line.dialNumber) },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CrisisRed,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_phone),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

/** Where each outcome sends the user. */
enum class ResultAction { GROUNDING, CRISIS_GUIDE }

/** Copy and next step for each self-check outcome. */
data class CrisisCheckResultCopy(
    val hero: GuideHero,
    val tone: GuideTone,
    val actionLabel: String,
    val action: ResultAction
)

fun CrisisCheckResult.toResultCopy(): CrisisCheckResultCopy = when (this) {
    CrisisCheckResult.NOTHING_SELECTED -> CrisisCheckResultCopy(
        hero = GuideHero(
            mood = BertoMood.WELCOMING,
            message = "No marcaste ninguna señal.",
            supporting = "Qué bueno. Si algo cambia, puedes volver a revisarte cuando quieras."
        ),
        tone = GuideTone.CALM,
        actionLabel = "Respirar con Berto",
        action = ResultAction.GROUNDING
    )
    CrisisCheckResult.SELF_CARE -> CrisisCheckResultCopy(
        hero = GuideHero(
            mood = BertoMood.CALMING,
            message = "Estás escuchando a tu cuerpo.",
            supporting = "Un ejercicio de respiración puede ayudarte a bajar la intensidad ahora mismo."
        ),
        tone = GuideTone.CALM,
        actionLabel = "Respirar con Berto",
        action = ResultAction.GROUNDING
    )
    CrisisCheckResult.POSSIBLE_CRISIS -> CrisisCheckResultCopy(
        hero = GuideHero(
            mood = BertoMood.WORRIED,
            message = "Puede que estés pasando por una crisis.",
            supporting = "Lo que marcaste puede ser señal de una crisis. Vamos paso a paso, no estás solo o sola."
        ),
        tone = GuideTone.WARNING,
        actionLabel = "Ver qué hacer ahora",
        action = ResultAction.CRISIS_GUIDE
    )
    CrisisCheckResult.URGENT -> CrisisCheckResultCopy(
        hero = GuideHero(
            mood = BertoMood.WORRIED,
            message = "Esto es importante.",
            supporting = "No tienes que pasarlo solo o sola. Avisa a tu red o llama a una línea de " +
                "apoyo: hay personas listas para escucharte ahora."
        ),
        tone = GuideTone.URGENT,
        actionLabel = "Ver qué hacer ahora",
        action = ResultAction.CRISIS_GUIDE
    )
}

/** Recap of what the user marked, so the result reads as an answer to *their* input. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedSignalsCard(
    selected: List<CrisisSignal>,
    tone: GuideTone,
    modifier: Modifier = Modifier
) {
    val colors = tone.colors()
    SolvyxCard(modifier = modifier.fillMaxWidth(), containerColor = colors.container) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = when (selected.size) {
                    0 -> "No marcaste señales"
                    1 -> "Marcaste 1 señal"
                    else -> "Marcaste ${selected.size} señales"
                },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.content
            )
            if (selected.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selected.forEach { SignalPill(it) }
                }
            }
        }
    }
}

@Composable
private fun SignalPill(signal: CrisisSignal) {
    val (background, content) = if (signal.isRedFlag) {
        CrisisRed to CrisisRedLight
    } else {
        TealLight.copy(alpha = 0.45f) to TealDark
    }
    Text(
        text = signal.label,
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = content
    )
}
