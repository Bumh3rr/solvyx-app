package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.common.goals.GoalSuggestion
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalType
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.theme.MedalGold
import com.solvyx.ui.theme.TealLight

private const val IconCapsuleAlpha = 0.45f
private const val CompletedPreview = 3

@DrawableRes
fun goalIcon(type: GoalType): Int = when (type) {
    GoalType.SIN_CONSUMO -> R.drawable.ic_target
    GoalType.REDUCIR_FRECUENCIA -> R.drawable.ic_calendar
}

/** "Mis metas (n de 3)": one card per active goal. Hidden when there are none. */
@Composable
fun ActiveGoalsSection(goals: List<GoalCardUi>, onOpenGoal: (String) -> Unit) {
    if (goals.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Mis metas", trailing = "${goals.size} de ${Goal.MAX_ACTIVE}")
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            goals.forEach { card -> GoalCard(card, onClick = { onOpenGoal(card.goal.id) }) }
        }
    }
}

@Composable
private fun GoalCard(card: GoalCardUi, onClick: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCapsule(goalIcon(card.goal.type))
                Spacer(Modifier.width(12.dp))
                Text(
                    card.goal.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            GoalProgressBar(card.fraction)
            Spacer(Modifier.height(8.dp))
            GoalProgressTexts(card)
        }
    }
}

@Composable
fun GoalProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(targetValue = fraction, label = "goalProgress")
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = TealLight.copy(alpha = 0.4f),
        drawStopIndicator = {}
    )
}

/** Progress label plus, for reduce goals, how this week is going. */
@Composable
fun GoalProgressTexts(card: GoalCardUi) {
    Text(
        card.progressLabel,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )
    card.weekLine?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    card.weekHint?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** Berto's ideas with "Aceptar", plus the professional link when some ASSIST level is high. */
@Composable
fun SuggestionsSection(
    suggestions: List<GoalSuggestion>,
    suggestProfessional: Boolean,
    onAccept: (GoalSuggestion) -> Unit,
    onOpenDirectory: () -> Unit
) {
    if (suggestions.isEmpty() && !suggestProfessional) return
    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Sugerencias de Berto")
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            suggestions.forEach { suggestion -> SuggestionCard(suggestion, onAccept = { onAccept(suggestion) }) }
            if (suggestProfessional) ProfessionalCard(onClick = onOpenDirectory)
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: GoalSuggestion, onAccept: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCapsule(goalIcon(suggestion.type))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    suggestion.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    suggestionSubtitle(suggestion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Aceptar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun suggestionSubtitle(suggestion: GoalSuggestion): String = when (suggestion.type) {
    GoalType.SIN_CONSUMO -> "No tienen que ser seguidos"
    GoalType.REDUCIR_FRECUENCIA -> "Durante ${unitCount(suggestion.target, suggestion.type)}"
}

@Composable
private fun ProfessionalCard(onClick: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCapsule(R.drawable.ic_hospital)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Hablar con un profesional",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Alguien que sabe de esto puede acompañarte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Opens "Crear mi propia meta"; with 3 active goals it stays visible but off, saying why. */
@Composable
fun CreateGoalButton(enabled: Boolean, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        SolvyxOutlinedButton(
            text = "+ Crear mi propia meta",
            onClick = onClick,
            enabled = enabled,
            borderColor = if (enabled) null else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            textColor = if (enabled) null else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        )
        if (!enabled) {
            Text(
                "Tienes ${Goal.MAX_ACTIVE} metas en marcha. Cuando cumplas o archives una, puedes crear otra.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}

/** "Metas cumplidas": the newest first, the rest behind "Ver todas". Hidden when empty. */
@Composable
fun CompletedGoalsSection(goals: List<Goal>) {
    if (goals.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shown = if (expanded) goals else goals.take(CompletedPreview)
    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Metas cumplidas", trailing = goals.size.toString())
        Spacer(Modifier.height(12.dp))
        SolvyxCard(modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()) {
            Column(Modifier.padding(vertical = 4.dp)) {
                shown.forEachIndexed { index, goal ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }
                    CompletedGoalRow(goal)
                }
                if (goals.size > CompletedPreview) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            if (expanded) "Ver menos" else "Ver todas (${goals.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedGoalRow(goal: Goal) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconCapsule(R.drawable.ic_trophy, tint = MedalGold, background = MedalGold.copy(alpha = 0.15f))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                goal.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                completedLabel(goal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Anonymous users: goals need an account; the tools and substance info below still work. */
@Composable
fun GoalsAccountInvite(onCreateAccount: () -> Unit) {
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCapsule(R.drawable.ic_target)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Guarda tus metas con una cuenta",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Así tu progreso no se pierde.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            SolvyxButton(
                text = "Crear cuenta",
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        trailing?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IconCapsule(
    @DrawableRes icon: Int,
    tint: Color = MaterialTheme.colorScheme.primary,
    background: Color = TealLight.copy(alpha = IconCapsuleAlpha)
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(background, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
