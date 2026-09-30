package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoHomeAnimation
import com.solvyx.ui.components.common.ConfettiBurst
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

private val StageHeight = 340.dp
private val StageShape = RoundedCornerShape(28.dp)
private val BertoSize = 230.dp
/** Lifts Berto so his feet sit just behind the top of the front hill (see FRONT_HILL_*). */
private val BertoLift = 24.dp
private val StagePadding = 14.dp

/**
 * "El jardín de Berto": Home's living header. Layers, back to front: sky for the time of day,
 * the streak's glow and embers, Berto, the garden that grows with the streak, and on top his
 * speech bubble and the flame badge. Below: the idea he's suggesting and the note he reads.
 * Tapping the stage greets Berto; all the timing lives in [BertoStageState].
 */
@Composable
fun HomeStage(
    state: BertoStageState,
    streak: Int,
    nickname: String,
    moodToday: String?,
    introduceBerto: Boolean?,
    onGreeted: () -> Unit,
    onSuggestionAction: (SuggestionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = remember { LocalTime.now().hour }
    val palette = remember(hour) { stagePaletteFor(timeOfDayFor(hour)) }
    val note = remember { dailyNoteFor(LocalDate.now()) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(introduceBerto != null) {
        val introduce = introduceBerto ?: return@LaunchedEffect
        // Rive only plays once the screen is resumed; starting the timers earlier (under the
        // splash's exit transition) put the bubble and the reading ahead of Berto.
        lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
        state.start(longGreeting = introduce)
        onGreeted()
    }

    Column(modifier) {
        Box {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(StageHeight)
                    .clip(StageShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClickLabel = "Saludar a Berto",
                        onClick = state::poke
                    )
                    .semantics { contentDescription = "Berto en su jardín" }
            ) {
                StageSky(palette)
                StreakGlow(streak)
                StageBerto(
                    state = state,
                    introduce = introduceBerto,
                    modifier = Modifier
                        .size(BertoSize)
                        .align(Alignment.BottomCenter)
                        .offset(y = -BertoLift)
                )
                StreakGarden(streak = streak, palette = palette, celebrationKey = state.celebrationKey)
                key(state.celebrationKey) {
                    if (state.celebrationKey > 0) ConfettiBurst(Modifier.fillMaxSize())
                }
                if (streak > 0) {
                    StageFlameBadge(streak, Modifier.align(Alignment.TopEnd).padding(StagePadding))
                }
                StageSpeechBubble(
                    text = lineText(state.line, hour, nickname, streak),
                    lineKey = state.lineKey,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(StagePadding)
                        .widthIn(max = 200.dp)
                )
            }
            IdeaPill(
                onClick = { state.askForIdea(moodToday) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 18.dp)
            )
        }
        Spacer(Modifier.height(30.dp))
        val lastSuggestion = rememberLastNonNull(state.suggestion)
        AnimatedVisibility(
            visible = state.suggestion != null,
            enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.96f),
            exit = fadeOut() + shrinkVertically() + scaleOut(targetScale = 0.96f)
        ) {
            // The last idea stays drawn while the card collapses.
            val suggestion = lastSuggestion ?: return@AnimatedVisibility
            BertoSuggestionCard(
                suggestion = suggestion,
                suggestionKey = state.suggestionKey,
                onAction = {
                    if (suggestion.action == SuggestionAction.SELF_CARE) {
                        state.acceptSelfCare()
                    } else {
                        state.dismissIdea()
                        onSuggestionAction(suggestion.action)
                    }
                },
                onAnotherIdea = state::nextIdea,
                onDismiss = state::dismissIdea,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        BertoReadingNote(
            text = note,
            isReading = state.isReading,
            readingKey = state.readingKey,
            onCaret = state::onReadingProgress,
            onFinished = state::onReadingFinished,
            onReadAgain = state::readNote
        )
    }
}

private class LastValue<T : Any> {
    var value: T? = null
}

/** The latest non-null [value]: lets an exit animation keep showing what just went away. */
@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val holder = remember { LastValue<T>() }
    if (value != null) holder.value = value
    return holder.value
}

/** Berto waits for [introduce]: it decides which greeting plays the moment he appears. */
@Composable
private fun StageBerto(state: BertoStageState, introduce: Boolean?, modifier: Modifier) {
    if (introduce == null) {
        Image(
            painter = painterResource(R.drawable.berto_saludando),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
        return
    }
    BertoHomeAnimation(
        introduce = introduce,
        cue = state.cue,
        isReading = state.isReading,
        lookX = state.lookX,
        modifier = modifier
    )
}

@Composable
private fun IdeaPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.ic_zap),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Pídele una idea a Berto",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
