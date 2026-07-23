package com.solvyx.ui.screens.plan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.theme.TealDark

/** "Metas sugeridas" carousel: rotates through [metasList] and lets the user mark today's goal as achieved. */
@Composable
fun PlanGoalCard(
    metaIndex: Int,
    metasList: List<String>,
    metaLogradaHoy: Boolean,
    onSiguienteMeta: () -> Unit,
    onToggleMetaLograda: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_flag),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Metas sugeridas",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TealDark
            )
        }
        Text(
            "${metaIndex + 1} / ${metasList.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(8.dp))
    BorderCard {
        AnimatedContent(
            targetState = metaIndex,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                (slideOutHorizontally { -it } + fadeOut())
            },
            label = "MetaPlanCarousel"
        ) { idx ->
            Text(
                text = metasList[idx],
                style = MaterialTheme.typography.bodyMedium,
                color = TealDark,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                metasList.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == metaIndex) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == metaIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                    )
                }
            }
            TextButton(
                onClick = onSiguienteMeta,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    "Siguiente →",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Acción principal del plan: comprometerse con la meta de hoy.
        // Persiste en `plan.goalAchievedToday` (Room + Firestore).
        if (metaLogradaHoy) {
            SolvyxOutlinedButton(
                text = "Lograda hoy",
                onClick = onToggleMetaLograda,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        } else {
            SolvyxButton(
                text = "Marcar como lograda hoy",
                onClick = onToggleMetaLograda,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
