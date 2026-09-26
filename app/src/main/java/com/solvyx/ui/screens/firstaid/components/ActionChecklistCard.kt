package com.solvyx.ui.screens.firstaid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.firstaid.model.ActionPlan
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

/**
 * "Do this now" as a checklist instead of a static list: each step is tapped once done, a bar
 * shows how far the user got, and Berto cheers when every step is ticked. Progress is saved per
 * plan, so it survives rotation and coming back from another screen of the module.
 */
@Composable
fun ActionChecklistCard(plan: ActionPlan, modifier: Modifier = Modifier) {
    var checkedSteps by rememberSaveable(plan.title) { mutableStateOf(emptySet<Int>()) }
    ActionChecklistCard(
        plan = plan,
        checkedSteps = checkedSteps,
        onToggleStep = { index ->
            checkedSteps = if (index in checkedSteps) checkedSteps - index else checkedSteps + index
        },
        modifier = modifier
    )
}

@Composable
private fun ActionChecklistCard(
    plan: ActionPlan,
    checkedSteps: Set<Int>,
    onToggleStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val total = plan.steps.size
    val done = checkedSteps.size
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else done.toFloat() / total,
        label = "checklistProgress"
    )

    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            ChecklistHeader(title = plan.title, done = done, total = total)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = TealLight.copy(alpha = 0.4f),
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(8.dp))
            plan.steps.forEachIndexed { index, step ->
                CheckableRow(
                    text = step,
                    checked = index in checkedSteps,
                    onToggle = { onToggleStep(index) },
                    modifier = Modifier.padding(top = 6.dp),
                    marker = (index + 1).toString(),
                    dimWhenChecked = true
                )
            }
            AnimatedVisibility(
                visible = total > 0 && done == total,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CompletionRow(message = plan.completionMessage)
            }
        }
    }
}

@Composable
private fun ChecklistHeader(title: String, done: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ToneIconBadge(icon = R.drawable.ic_zap, tone = GuideTone.CALM)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                text = "Toca cada paso cuando lo hagas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$done de $total",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(TealLight.copy(alpha = 0.35f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CompletionRow(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(TealLight.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FirstAidBerto(mood = BertoMood.PROUD, modifier = Modifier.size(44.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TealDark
        )
    }
}
