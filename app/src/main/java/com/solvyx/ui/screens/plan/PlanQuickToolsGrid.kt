package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.solvyx.ui.theme.TealLight

private const val ToolColumns = 2
private const val IconCapsuleAlpha = 0.45f

private data class PlanTool(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val onClick: () -> Unit
)

/**
 * "Herramientas rápidas": the four things that help right now. Each one opens a screen that
 * already exists elsewhere (first-aid craving guide, breathing, 5-4-3-2-1, chat), so the plan
 * never duplicates their content.
 */
@Composable
fun PlanQuickToolsGrid(
    onOpenCravingGuide: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenGrounding: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        PlanTool("Tengo ganas ahora", "La ola pasa, te acompaño", R.drawable.ic_flame, onOpenCravingGuide),
        PlanTool("Respirar", "Respira conmigo", R.drawable.ic_wind, onOpenBreathing),
        PlanTool("5-4-3-2-1", "Vuelve al presente", R.drawable.ic_eye, onOpenGrounding),
        PlanTool("Hablar con Berto", "Disponible ahora", R.drawable.ic_chat, onOpenChat)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Herramientas rápidas",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tools.chunked(ToolColumns).forEach { rowTools ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTools.forEach { tool ->
                        PlanToolCard(
                            tool = tool,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanToolCard(tool: PlanTool, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier, onClick = tool.onClick) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(TealLight.copy(alpha = IconCapsuleAlpha), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(tool.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                tool.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                tool.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
