package com.solvyx.ui.screens.journey.diary.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.common.goals.DayGoalNote
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.berto.BertoReactTrigger
import com.solvyx.ui.components.berto.BertoReactsAnimation
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.components.common.substanceIcon
import com.solvyx.ui.components.common.substanceLabel
import com.solvyx.ui.screens.journey.diary.DiaryStory
import com.solvyx.ui.screens.journey.diary.relativeDay
import com.solvyx.ui.theme.TealDark
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

private val DateFormat = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "MX"))
private val CardShape = RoundedCornerShape(28.dp)
private const val MIN_PAGE_SCALE = 0.86f
private const val MIN_PAGE_ALPHA = 0.4f
private const val PAGE_TILT_DEGREES = 8f
private const val STORY_FADE = 0.45f

/** What Berto remembers about a day, by mood. Warm, never judging. */
private fun bertoMemory(moodId: String?): String = when (moodId) {
    "triste" -> "Ese día fue difícil, y aun así lo escribiste."
    "ansioso" -> "Hubo nervios ese día. Lo atravesaste."
    "neutral" -> "Un día tranquilo también cuenta."
    "bien" -> "¡Ese fue un buen día!"
    "euforico" -> "¡Ese día tenías muchísima energía!"
    else -> "Aquí está tu día."
}

/**
 * A day as a full-screen story. Days go left to right in time (older to the left), the background
 * takes the day's mood color and Berto replays that day's reaction every time you land on it.
 * Only today can be edited: the check-in only edits today's entry.
 */
@Composable
fun DayStory(
    story: DiaryStory,
    today: LocalDate,
    goalNotes: Map<LocalDate, DayGoalNote>,
    onEditToday: () -> Unit,
    onClose: () -> Unit,
    // False while the story plays its exit animation, so a second back press does not close twice.
    isOpen: Boolean = true
) {
    val pagerState = rememberPagerState(initialPage = story.startIndex) { story.days.size }
    val scope = rememberCoroutineScope()
    val current = story.days[pagerState.currentPage]
    val mood = moodOption(current.mood)
    val backdrop by animateColorAsState(mood.color, tween(500), label = "StoryBackdrop")
    val screen = MaterialTheme.colorScheme.background
    // Opaque all the way down: the diary behind must not show through.
    val gradient = Brush.verticalGradient(listOf(backdrop, lerp(backdrop, screen, STORY_FADE), screen))
    BackHandler(enabled = isOpen, onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            // Swallows taps so nothing behind the story reacts.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            StoryTopBar(position = pagerState.currentPage, total = story.days.size, onClose = onClose)
            BertoRemembers(entry = current)
            HorizontalPager(
                state = pagerState,
                // Newest day is page 0; reversed, older days sit to the left like a timeline.
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 28.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                StoryCard(
                    entry = story.days[page],
                    today = today,
                    goalNote = goalNotes[story.days[page].date],
                    modifier = Modifier.pageDepth(pagerState, page)
                )
            }
            StoryActions(
                canGoOlder = pagerState.currentPage < story.days.lastIndex,
                canGoNewer = pagerState.currentPage > 0,
                isToday = current.date == today,
                onOlder = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                onNewer = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onEditToday = onEditToday
            )
        }
    }
}

/** Neighbour pages shrink, fade and tilt a little, so swiping feels like turning cards. */
private fun Modifier.pageDepth(state: PagerState, page: Int): Modifier = graphicsLayer {
    val offset = ((state.currentPage - page) + state.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
    val scale = 1f - (1f - MIN_PAGE_SCALE) * offset
    scaleX = scale
    scaleY = scale
    alpha = 1f - (1f - MIN_PAGE_ALPHA) * offset
    rotationY = PAGE_TILT_DEGREES * offset * if (page > state.currentPage) 1f else -1f
}

@Composable
private fun StoryTopBar(position: Int, total: Int, onClose: () -> Unit) {
    // Story-style progress in time order: the oldest day is 1 of N, today fills the bar.
    val dayNumber = total - position
    val progress by animateFloatAsState(dayNumber.toFloat() / total, label = "StoryProgress")
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Día $dayNumber de $total",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_circle_x), contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
            )
        }
    }
}

/** Berto replays the day's reaction (a new trigger every time the day changes) and says a line. */
@Composable
private fun BertoRemembers(entry: JournalEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BertoReactsAnimation(
            reaction = BertoReactTrigger.forMood(entry.mood),
            reactionKey = entry.date,
            isReading = false,
            lookX = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.width(8.dp))
        AnimatedContent(
            targetState = bertoMemory(entry.mood),
            transitionSpec = {
                (fadeIn(tween(250)) + slideInVertically { it / 3 }) togetherWith (fadeOut(tween(150)) + slideOutVertically { -it / 3 })
            },
            modifier = Modifier.weight(1f),
            label = "BertoMemory"
        ) { line ->
            Text(
                line,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun StoryCard(entry: JournalEntry, today: LocalDate, goalNote: DayGoalNote?, modifier: Modifier) {
    val mood = moodOption(entry.mood)
    // The card is as tall as its content (scrolls if the note is long) and sits at the top of its page.
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                relativeDay(entry.date, today),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = mood.color
            )
            Text(
                entry.date.format(DateFormat).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(mood.color.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(mood.icon), null, tint = mood.color, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Me sentí ${mood.label.lowercase()}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
        }
        UseLine(entry)
        entry.note?.takeIf { it.isNotBlank() }?.let { note ->
            Text(
                "“$note”",
                style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                color = TealDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(mood.color.copy(alpha = 0.08f))
                    .padding(16.dp)
            )
        }
        goalNote?.completedTitles?.forEach { title ->
            GoalNoteLine(R.drawable.ic_trophy, "Cumpliste tu meta: “$title”")
        }
        if (goalNote?.advanced == true && goalNote.completedTitles.isEmpty()) {
            GoalNoteLine(R.drawable.ic_check_circle, "Avanzaste en tu meta")
        }
    }
    }
}

@Composable
private fun GoalNoteLine(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun UseLine(entry: JournalEntry) {
    val used = entry.consumed == true
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(if (used) entry.substance?.let(::substanceIcon) ?: R.drawable.ic_droplet else R.drawable.ic_check_circle),
            null,
            tint = if (used) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                if (used) "Consumí ${entry.substance?.let(::substanceLabel)?.lowercase() ?: ""}".trim() else "Día sin consumo",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            listOfNotNull(entry.cantidadAprox, entry.notaContexto)
                .filter { it.isNotBlank() }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    Text(it.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
        }
    }
}

@Composable
private fun StoryActions(
    canGoOlder: Boolean,
    canGoNewer: Boolean,
    isToday: Boolean,
    onOlder: () -> Unit,
    onNewer: () -> Unit,
    onEditToday: () -> Unit
) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        if (isToday) {
            SolvyxButton(text = "Editar mi día", onClick = onEditToday, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StoryNavButton("Día anterior", enabled = canGoOlder, onClick = onOlder)
            StoryNavButton("Día siguiente", enabled = canGoNewer, onClick = onNewer)
        }
    }
}

@Composable
private fun StoryNavButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = TealDark.copy(alpha = if (enabled) 1f else 0.3f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
