package com.solvyx.ui.components.berto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.rive.RivePointerInputMode
import com.solvyx.R

private const val HAS_GREETED_TODAY = "hasGreetedToday"
private const val TRIGGER_SUGGESTING = "triggerSuggesting"

/**
 * Something Berto does once on Home: a mood reaction or "suggesting" (≈5 s explaining an idea).
 * [id] makes the same cue play again, e.g. two taps in a row that both land on "good".
 */
data class BertoCue(val trigger: String, val id: Long) {
    companion object {
        fun reaction(reaction: BertoReactTrigger, id: Long) = BertoCue(reaction.riveName, id)
        fun suggesting(id: Long) = BertoCue(TRIGGER_SUGGESTING, id)
    }
}

/**
 * Berto on Home (`homemv.riv`): the same rig as the reacts file plus his greetings and
 * Suggesting. From Entry the state machine plays "Greet" (spoken introduction, ≈8 s) when
 * `hasGreetedToday` is false and "Hi" (a wave) when it's true. [introduce] is read once, when Berto
 * appears; later changes (marking the visit) must not replay it. Touches pass through so the
 * stage below handles taps and the page still scrolls over Berto.
 */
@Composable
fun BertoHomeAnimation(
    introduce: Boolean,
    cue: BertoCue?,
    isReading: Boolean,
    lookX: Float?,
    modifier: Modifier = Modifier
) {
    val introduceWhenShown = remember { introduce }
    BertoRiveHost(
        riveRes = R.raw.homemv,
        trigger = cue?.trigger,
        triggerKey = cue?.id,
        isReading = isReading,
        lookX = lookX,
        modifier = modifier,
        fallback = R.drawable.berto_saludando,
        initialBooleans = mapOf(HAS_GREETED_TODAY to !introduceWhenShown),
        pointerInputMode = RivePointerInputMode.PassThrough
    )
}
