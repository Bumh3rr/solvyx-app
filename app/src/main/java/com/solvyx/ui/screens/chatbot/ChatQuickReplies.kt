package com.solvyx.ui.screens.chatbot

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.StaggeredAppear

private const val DISABLED_ALPHA = 0.45f
private const val CHIP_STAGGER_MS = 90L
private val ChipShape = RoundedCornerShape(50.dp)

/** Answer chips under Berto's latest message; they pop in one after another. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickRepliesRow(
    replies: List<String>,
    enabled: Boolean,
    onReplySelected: (String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = BertoBubbleIndent, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        replies.forEachIndexed { index, reply ->
            StaggeredAppear(index, stepMs = CHIP_STAGGER_MS) {
                Text(
                    reply,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .alpha(if (enabled) 1f else DISABLED_ALPHA)
                        .clip(ChipShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, ChipShape)
                        .clickable(enabled = enabled, role = Role.Button) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onReplySelected(reply)
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
