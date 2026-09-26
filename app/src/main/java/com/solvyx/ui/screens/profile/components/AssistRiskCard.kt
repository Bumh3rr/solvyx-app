package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.AssistRiskLevel
import com.solvyx.ui.screens.profile.model.AssistSummary
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private val MarkerSize = 18.dp
private val ZoneHeight = 10.dp
private const val InactiveZoneAlpha = 0.3f
private const val TagTintAlpha = 0.45f

/**
 * Latest ASSIST (WHO) result as a three-band gauge whose marker slides into the user's band.
 * With no assessment yet, it invites the user to take one instead of implying any risk level.
 */
@Composable
fun AssistRiskCard(
    assist: AssistSummary?,
    onOpenAssessment: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ASSIST · OMS",
                    modifier = Modifier
                        .background(TealLight.copy(alpha = TagTintAlpha), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                assist?.let { LevelPill(it.level) }
            }
            Spacer(Modifier.height(12.dp))
            if (assist == null) NoAssessmentContent(onOpenAssessment) else AssessmentContent(assist, onOpenAssessment)
        }
    }
}

@Composable
private fun LevelPill(level: AssistRiskLevel) {
    Text(
        text = level.label.uppercase(),
        modifier = Modifier
            .background(level.container, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = level.accent
    )
}

@Composable
private fun NoAssessmentContent(onOpenAssessment: () -> Unit) {
    Text(
        text = "Aún no tienes un diagnóstico",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TealDark
    )
    Text(
        text = "Son unas preguntas rápidas y confidenciales para conocer tu nivel de riesgo.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(14.dp))
    SolvyxButton(text = "Hacer diagnóstico", onClick = onOpenAssessment, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun AssessmentContent(assist: AssistSummary, onOpenAssessment: () -> Unit) {
    Text(
        text = "Riesgo ${assist.level.label.lowercase()} · ${assist.substanceLabel}",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TealDark
    )
    Text(
        text = "Última evaluación: ${assist.date} · Puntaje ${assist.score}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))
    RiskGauge(level = assist.level)
    Spacer(Modifier.height(14.dp))
    SolvyxOutlinedButton(
        text = "Repetir diagnóstico",
        onClick = onOpenAssessment,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
    Text(
        text = "Tu resultado es orientativo, no un diagnóstico médico.",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

/** Three equal bands (Bajo · Moderado · Alto); the marker springs into the user's band. */
@Composable
private fun RiskGauge(level: AssistRiskLevel) {
    val position = remember { Animatable(0f) }
    LaunchedEffect(level) {
        position.animateTo(
            level.zoneCenter,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
        )
    }
    Column {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val markerX = maxWidth * position.value - MarkerSize / 2
            Column {
                Spacer(Modifier.height(MarkerSize / 2))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistRiskLevel.entries.forEach { zone ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(ZoneHeight)
                                .background(
                                    if (zone == level) zone.accent else zone.accent.copy(alpha = InactiveZoneAlpha),
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .offset(x = markerX, y = ZoneHeight / 2)
                    .size(MarkerSize)
                    .background(Color.White, CircleShape)
                    .border(4.dp, level.accent, CircleShape)
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            AssistRiskLevel.entries.forEach { zone ->
                Text(
                    text = zone.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (zone == level) FontWeight.ExtraBold else FontWeight.Normal
                    ),
                    color = if (zone == level) zone.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
