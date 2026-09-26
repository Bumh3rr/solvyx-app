package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.screens.profile.completion
import com.solvyx.ui.screens.profile.model.SafetyItem
import com.solvyx.ui.screens.profile.model.SafetyStep
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val ProgressFillMillis = 900
private const val PercentScale = 100
private const val IconCapsuleAlpha = 0.45f
private const val PendingRowAlpha = 0.2f

/**
 * "Tu red de seguridad": what the app needs so it can actually help in a crisis, pending steps
 * first. Each pending step is tappable and goes straight to where it gets done. Once everything
 * is set, the list collapses into a short confirmation from Berto.
 */
@Composable
fun SafetyChecklistCard(
    items: List<SafetyItem>,
    onStepClick: (SafetyStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val completion = items.completion()
    val sortedItems = remember(items) { items.sortedBy { it.done } }
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = completion >= 1f,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "safetyChecklist",
            modifier = Modifier.padding(16.dp)
        ) { allDone ->
            if (allDone) {
                AllSetContent()
            } else {
                Column {
                    ChecklistHeader(completion = completion)
                    Spacer(Modifier.height(8.dp))
                    sortedItems.forEach { item ->
                        SafetyStepRow(item = item, onClick = { onStepClick(item.step) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistHeader(completion: Float) {
    val animated by animateFloatAsState(completion, tween(ProgressFillMillis), label = "safetyProgress")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Tu red de seguridad",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                text = "Lo que le permite a Solvyx ayudarte en un momento difícil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animated },
                modifier = Modifier.size(52.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = TealLight.copy(alpha = IconCapsuleAlpha),
                strokeWidth = 5.dp
            )
            Text(
                text = "${(animated * PercentScale).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
        }
    }
}

@Composable
private fun SafetyStepRow(item: SafetyItem, onClick: () -> Unit) {
    val step = item.step
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (item.done) Modifier
                else Modifier
                    .background(TealLight.copy(alpha = PendingRowAlpha))
                    .clickable(role = Role.Button, onClick = onClick)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepMarker(item)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (item.done) step.doneTitle else step.pendingTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (item.done) FontWeight.Medium else FontWeight.Bold
                ),
                color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant else TealDark
            )
            if (!item.done) {
                Text(
                    text = step.hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!item.done) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun StepMarker(item: SafetyItem) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                if (item.done) primary else TealLight.copy(alpha = IconCapsuleAlpha),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(if (item.done) R.drawable.ic_check else item.step.icon),
            contentDescription = null,
            tint = if (item.done) Color.White else primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AllSetContent() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.berto_feliz),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Tu red de seguridad está lista",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                text = "Tienes contactos, diagnóstico y sustancias. Así puedo ayudarte mejor cuando lo necesites.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
