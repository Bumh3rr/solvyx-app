package com.solvyx.ui.screens.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.TealLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Cards and chips appear staggered after the bubble; scroll again once they have their height.
private const val RESCROLL_AFTER_CONTENT_MS = 750L
private const val ENTRANCE_RISE_DP = 14f
private const val ENTRANCE_START_SCALE = 0.92f
private const val SCROLL_DOWN_ICON_ROTATION = 90f

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    activeInteractiveId: String?,
    choicesEnabled: Boolean,
    showTyping: Boolean,
    typingState: BertoState,
    isThinkingLong: Boolean,
    onQuickReply: (String) -> Unit,
    onTopicSelected: (TopicIntent, String?) -> Unit,
    onSubstanceSelected: (TopicIntent, String) -> Unit,
    onSupportAction: (SupportAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // Messages already shown with their entrance animation; scrolling back up must not replay it.
    val animatedIds = remember { mutableSetOf<String>() }
    // Index of the sentinel after the last message (and the typing bubble, when present).
    val bottomIndex = messages.size + if (showTyping) 1 else 0
    val isAwayFromBottom by remember { derivedStateOf { listState.canScrollForward } }

    LaunchedEffect(messages.size, showTyping) {
        if (messages.isEmpty()) return@LaunchedEffect
        listState.animateScrollToItem(bottomIndex)
        delay(RESCROLL_AFTER_CONTENT_MS)
        listState.animateScrollToItem(bottomIndex)
    }

    Box(modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(items = messages, key = { _, message -> message.id }) { index, message ->
                val animate = remember(message.id) { animatedIds.add(message.id) }
                val previous = messages.getOrNull(index - 1)
                val isFirstOfGroup = previous == null || !previous.isFromBerto || previous.avisoSistema != null
                MessageEntrance(animate = animate, fromBerto = message.isFromBerto) {
                    ChatMessageItem(
                        message = message,
                        isFirstOfGroup = isFirstOfGroup,
                        isActive = message.id == activeInteractiveId,
                        choicesEnabled = choicesEnabled,
                        onQuickReply = onQuickReply,
                        onTopicSelected = onTopicSelected,
                        onSubstanceSelected = onSubstanceSelected,
                        onSupportAction = onSupportAction
                    )
                }
            }
            if (showTyping) {
                item(key = "typing") {
                    MessageEntrance(animate = true, fromBerto = true) {
                        TypingBubble(state = typingState, isThinkingLong = isThinkingLong)
                    }
                }
            }
            // Sentinel: scrolling to it guarantees the cards and chips above are fully visible.
            item(key = "bottom_sentinel") { Spacer(Modifier.height(1.dp)) }
        }

        AnimatedVisibility(
            visible = isAwayFromBottom,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            ScrollToBottomButton(onClick = { scope.launch { listState.animateScrollToItem(bottomIndex) } })
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    isFirstOfGroup: Boolean,
    isActive: Boolean,
    choicesEnabled: Boolean,
    onQuickReply: (String) -> Unit,
    onTopicSelected: (TopicIntent, String?) -> Unit,
    onSubstanceSelected: (TopicIntent, String) -> Unit,
    onSupportAction: (SupportAction) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        val notice = message.avisoSistema
        if (notice != null) {
            SystemNoticeRow(aviso = notice, text = message.content)
        } else {
            MessageBubble(message = message, isFirstOfGroup = isFirstOfGroup)
        }
        when (val attachment = message.attachment) {
            is ChatAttachment.TopicPicker -> AnsweredCollapse(visible = isActive) {
                TopicPickerCards(
                    presetSubstanceId = attachment.substanceId,
                    enabled = choicesEnabled,
                    onSelected = { onTopicSelected(it, attachment.substanceId) }
                )
            }
            is ChatAttachment.SubstancePicker -> AnsweredCollapse(visible = isActive) {
                SubstancePickerGrid(
                    enabled = choicesEnabled,
                    onSelected = { onSubstanceSelected(attachment.intent, it) }
                )
            }
            // Never hidden: help must stay reachable anywhere in the conversation.
            is ChatAttachment.SupportActions -> SupportActionsCard(urgent = attachment.urgent, onAction = onSupportAction)
            null -> Unit
        }
        if (isActive && message.quickReplies.isNotEmpty()) {
            QuickRepliesRow(replies = message.quickReplies, enabled = choicesEnabled, onReplySelected = onQuickReply)
        }
    }
}

/** Pickers fold away once answered: the user's reply bubble already says what they chose. */
@Composable
private fun AnsweredCollapse(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
    ) { content() }
}

/** Berto's messages rise from his side, the user's from theirs, with a small bounce. */
@Composable
private fun MessageEntrance(animate: Boolean, fromBerto: Boolean, content: @Composable () -> Unit) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animate) progress.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
    }
    Box(
        Modifier.graphicsLayer {
            val p = progress.value
            alpha = p.coerceIn(0f, 1f)
            translationY = (1f - p) * ENTRANCE_RISE_DP * density
            val scale = ENTRANCE_START_SCALE + (1f - ENTRANCE_START_SCALE) * p
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(if (fromBerto) 0f else 1f, 1f)
        }
    ) { content() }
}

@Composable
private fun ScrollToBottomButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, TealLight, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = "Ir al final de la conversación",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(18.dp)
                .rotate(SCROLL_DOWN_ICON_ROTATION)
        )
    }
}
