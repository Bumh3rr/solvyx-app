package com.solvyx.ui.screens.home.stage

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.solvyx.ui.components.berto.BertoCue
import com.solvyx.ui.components.berto.BertoReactTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * How long each Rive state keeps Berto busy (it can't jump to another one meanwhile). The first
 * greeting of the day ("saludo") is his spoken introduction: 8.1 s of voice plus a margin.
 */
internal const val LONG_GREETING_MS = 8_600L
internal const val SHORT_GREETING_MS = 2_000L
internal const val REACTION_MS = 2_600L
internal const val SUGGESTING_MS = 5_000L

/** The greeting bubble stays at least this long (or the whole greeting) before the streak line. */
internal const val MIN_GREETING_LINE_MS = 2_600L

/** Time to read the streak line before Berto looks down at the note. */
internal const val STREAK_LINE_BEFORE_READING_MS = 1_200L

/** Leaving reading_idle takes a moment; a trigger fired before that lands nowhere. */
internal const val READING_EXIT_MS = 250L

/** After the last word Berto keeps his eyes on the note a little before looking up. */
internal const val READ_HOLD_MS = 900L

private const val READING_RETRY_MS = 300L

/**
 * Choreography of Berto on Home. The Rive state machine only accepts a reaction or Suggesting
 * from Idle, so everything that makes him move goes through one queue ([perform]): it waits for
 * the current state to finish, stops reading, fires the cue and books the time the new state
 * takes. Mood reactions queue (they must never get lost, e.g. tapped during the greeting);
 * pokes on Berto are dropped while he's busy, like a real "not now".
 */
@Stable
class BertoStageState internal constructor(
    private val scope: CoroutineScope,
    private val clock: () -> Long
) {
    var cue by mutableStateOf<BertoCue?>(null)
        private set
    var line by mutableStateOf<StageLine>(StageLine.Greeting)
        private set
    /** Bumps every time Berto says something, so even a repeated line types out again. */
    var lineKey by mutableLongStateOf(0L)
        private set
    var suggestion by mutableStateOf<BertoSuggestion?>(null)
        private set
    /** Bumps when Berto starts explaining an idea: restarts the card's bar. */
    var suggestionKey by mutableLongStateOf(0L)
        private set
    var isReading by mutableStateOf(false)
        private set
    var lookX by mutableStateOf<Float?>(null)
        private set
    /** 0 until Berto reads the note for the first time; each bump reads it again. */
    var readingKey by mutableLongStateOf(0L)
        private set
    var celebrationKey by mutableIntStateOf(0)
        private set

    private val queue = Mutex()
    private var busyUntil = 0L
    private var nextCueId = 0L
    private var pokes = 0
    private var suggestionMood: String? = null
    private var suggestionIndex = 0
    private var started = false
    private var pendingActions = 0

    private val isBusy: Boolean get() = queue.isLocked || clock() < busyUntil

    /** Runs once per visit to Home: greeting, the streak line, then Berto reads the note. */
    fun start(longGreeting: Boolean) {
        if (started) return
        started = true
        val greetingMs = if (longGreeting) LONG_GREETING_MS else SHORT_GREETING_MS
        busyUntil = clock() + greetingMs
        say(StageLine.Greeting)
        scope.launch {
            delay(maxOf(greetingMs, MIN_GREETING_LINE_MS))
            if (line == StageLine.Greeting) say(StageLine.Streak)
            delay(STREAK_LINE_BEFORE_READING_MS)
            readNote()
        }
    }

    fun reactTo(mood: String) {
        val reaction = BertoReactTrigger.forMood(mood) ?: return
        perform(REACTION_MS) {
            say(StageLine.Reaction(mood))
            fire(BertoCue.reaction(reaction, nextCueId++))
            if (celebrates(mood)) celebrationKey++
        }
        if (suggestsRightAway(mood)) perform(SUGGESTING_MS) { explain(mood, restart = true) }
    }

    /** A tap on Berto. Alternates two happy reactions so repeated taps don't feel canned. */
    fun poke() {
        if (isBusy) return
        val reaction = if (pokes % 2 == 0) BertoReactTrigger.GOOD else BertoReactTrigger.EUPHORIC
        val index = pokes++
        perform(REACTION_MS) {
            say(StageLine.Poke(index))
            fire(BertoCue.reaction(reaction, nextCueId++))
        }
    }

    fun askForIdea(mood: String?) {
        if (suggestion != null && clock() < busyUntil) {
            nextIdea()
            return
        }
        perform(SUGGESTING_MS) { explain(mood, restart = true) }
    }

    /** "Otra idea": while Berto still explains, only the card changes; after, he explains again. */
    fun nextIdea() {
        val ideas = suggestionsFor(suggestionMood)
        suggestionIndex = (suggestionIndex + 1) % ideas.size
        if (clock() < busyUntil) {
            suggestion = ideas[suggestionIndex]
        } else {
            perform(SUGGESTING_MS) { explain(suggestionMood, restart = false) }
        }
    }

    fun dismissIdea() {
        suggestion = null
    }

    /** An idea without a screen to open ("Lo haré"): Berto thanks the user once he's free. */
    fun acceptSelfCare() {
        suggestion = null
        perform(REACTION_MS) {
            say(StageLine.SuggestionTaken)
            fire(BertoCue.reaction(BertoReactTrigger.GOOD, nextCueId++))
        }
    }

    /**
     * Berto lowers his head and reads the note of the day; the note reports his eye position.
     * Reading is the lowest priority: it waits until no reaction or idea is pending, instead of
     * starting only to be cut off a moment later.
     */
    fun readNote() {
        scope.launch {
            while (true) {
                val beganReading = queue.withLock {
                    awaitIdle()
                    if (pendingActions > 0) return@withLock false
                    isReading = true
                    readingKey++
                    true
                }
                if (beganReading) return@launch
                delay(READING_RETRY_MS)
            }
        }
    }

    fun onReadingProgress(x: Float) {
        if (isReading) lookX = x
    }

    fun onReadingFinished() {
        scope.launch {
            delay(READ_HOLD_MS)
            stopReading()
        }
    }

    private fun perform(durationMs: Long, action: () -> Unit) {
        pendingActions++
        scope.launch {
            queue.withLock {
                awaitIdle()
                if (isReading) {
                    stopReading()
                    delay(READING_EXIT_MS)
                }
                action()
                busyUntil = clock() + durationMs
                pendingActions--
            }
        }
    }

    private suspend fun awaitIdle() {
        val wait = busyUntil - clock()
        if (wait > 0) delay(wait)
    }

    private fun explain(mood: String?, restart: Boolean) {
        if (restart) {
            suggestionMood = mood
            suggestionIndex = 0
        }
        suggestion = suggestionsFor(suggestionMood)[suggestionIndex]
        suggestionKey++
        say(StageLine.Suggesting)
        fire(BertoCue.suggesting(nextCueId++))
    }

    private fun stopReading() {
        isReading = false
        lookX = null
    }

    private fun say(newLine: StageLine) {
        line = newLine
        lineKey++
    }

    private fun fire(newCue: BertoCue) {
        cue = newCue
    }
}

@Composable
fun rememberBertoStageState(): BertoStageState {
    val scope = rememberCoroutineScope()
    return remember { BertoStageState(scope) { SystemClock.uptimeMillis() } }
}
