package com.solvyx.ui.screens.chatbot

import java.text.Normalizer

private val Diacritics = Regex("\\p{Mn}+")
private val NonAlphanumeric = Regex("[^a-z0-9]+")

/**
 * Lowercase, accent-free and single-spaced, so "No puedo MÁS!!" and "no puedo mas" match the same
 * phrase. NFD splits "ñ" into "n" + tilde, so "daño" and "dano" both become "dano".
 */
internal fun normalizeForMatching(text: String): String =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(Diacritics, "")
        .replace(NonAlphanumeric, " ")
        .trim()

/** Whole-word phrase patterns over normalized text; `\w*` suffixes cover conjugations. */
internal fun phrases(vararg patterns: String): List<Regex> = patterns.map { Regex("\\b$it\\b") }

internal fun List<Regex>.anyIn(normalized: String): Boolean = any { it.containsMatchIn(normalized) }

/**
 * Local detection of risk messages. Runs before the AI so a crisis never depends on the network.
 * Errs on the side of false positives: offering support by mistake is cheap, missing a crisis is not.
 */
object CrisisDetector {

    private val patterns = phrases(
        // Suicide and wanting to die
        "suicid\\w*",
        "(me )?quiero morir\\w*",
        "quisiera morir\\w*",
        "ganas de morir\\w*",
        "prefiero morir\\w*",
        "morirme",
        "(me )?(quiero|voy a) matar(me)?",
        "matarme",
        "quitarme la vida",
        "acabar con (todo|mi vida)",
        "no quiero (vivir|seguir viviendo|seguir|estar aqui)",
        "no vale la pena (vivir|seguir)",
        "(quiero|quisiera) desaparecer",
        "ya no (puedo|aguanto) mas",
        "no puedo mas",
        // Self-harm
        "hacer(me)? dano",
        "lastimarme",
        "cortarme",
        "herirme",
        "autolesion\\w*",
        // Medical emergency
        "sobredosis",
        "no puedo respirar",
        "me estoy desmayando",
        // Explicit calls for help
        "crisis",
        "emergencia",
        "socorro",
        "auxilio"
    )

    fun isCrisis(text: String): Boolean = patterns.anyIn(normalizeForMatching(text))
}

/** Berto's reaction to how the user sounds in free text; `null` keeps the current state. */
object MoodDetector {

    // Checked first: an explicit "ya estoy bien" wins even after a "no," ("no, ya estoy bien").
    // "(?<!no )" keeps "no me siento muy bien" / "no me ayudó" out.
    private val clearlyBetter = phrases(
        "ya (me siento|estoy|ando) (\\w+ )?(bien|mejor|tranquil[oa]|calmad[oa])",
        "(?<!no )(me siento|estoy|ando) (mucho|muy|super|bastante|un poco) (mejor|bien|tranquil[oa])",
        "(ya )?se me (paso|quito)", "ya (me )?calme",
        "(?<!no )me (ayudo|sirvio)"
    )

    // Before the positives: "no estoy bien" / "no me siento mejor" must not read as "bien" / "mejor".
    private val distress = phrases(
        "no (\\w+ ){0,3}(bien|mejor|tranquil[oa])", "no me (ayudo|sirvio)", "peor", "fatal", "horrible", "mal",
        "ansiedad", "ansios[oa]", "angustia\\w*", "miedo", "panico", "nervios[oa]?",
        "estresad[oa]", "estres", "craving", "ganas de (consumir|tomar|beber|fumar|vapear)",
        "trist\\w*", "deprimid[oa]", "llor\\w*", "(me siento|estoy) (muy )?(sol[oa]|vaci[oa])"
    )

    private val positive = phrases(
        "logre", "lo consegui", "gracias", "mejor", "bien", "racha", "feliz", "orgullos[oa]",
        "tranquil[oa]", "calmad[oa]", "relajad[oa]", "content[oa]", "alegre", "genial", "chid[oa]",
        "excelente", "increible"
    )

    fun detect(text: String): BertoState? {
        val normalized = normalizeForMatching(text)
        return when {
            clearlyBetter.anyIn(normalized) -> BertoState.CELEBRANDO
            distress.anyIn(normalized) -> BertoState.PREOCUPADO
            positive.anyIn(normalized) -> BertoState.CELEBRANDO
            else -> null
        }
    }
}

/** "hola", "buenas", "qué onda": a hello deserves a hello back, not "elige un tema". */
object GreetingDetector {

    private val greetings = phrases("hola\\w*", "buen[oa]s( (dias|tardes|noches))?", "que onda", "que tal", "hey", "saludos")

    fun isGreeting(text: String): Boolean = greetings.anyIn(normalizeForMatching(text))
}

/** What the user's message changed about a crisis, so the ViewModel can answer accordingly. */
enum class CrisisShift {
    NONE,
    /** Risk words, even if already in crisis: Berto answers with the support actions again. */
    DETECTED,
    EASED,
    STEPPED_DOWN
}

/** Berto's next state after a free-text message, and how it moved a crisis if there was one. */
data class MoodReaction(val state: BertoState, val shift: CrisisShift = CrisisShift.NONE)

/**
 * How Berto's mood follows the conversation, including how a crisis winds down. A crisis used to
 * last until the user tapped "Ya estoy mejor", even while they wrote that they felt better:
 * - risk words → CRISIS, from any state;
 * - in crisis, a clearly positive message → TRANQUILO (calm, no confetti right after a crisis);
 * - in crisis, distress keeps the crisis; [CALM_MESSAGES_TO_STEP_DOWN] neutral messages in a row
 *   lower it to PREOCUPADO (still attentive, without the red alert);
 * - outside a crisis, the detected mood simply replaces the current one.
 */
class ConversationMood {

    private var calmMessagesInCrisis = 0

    fun react(current: BertoState, text: String): MoodReaction {
        if (CrisisDetector.isCrisis(text)) {
            calmMessagesInCrisis = 0
            return MoodReaction(BertoState.CRISIS, CrisisShift.DETECTED)
        }
        val mood = MoodDetector.detect(text)
        if (current != BertoState.CRISIS) return MoodReaction(mood ?: current)

        return when (mood) {
            BertoState.CELEBRANDO -> leaveCrisis(BertoState.TRANQUILO, CrisisShift.EASED)
            BertoState.PREOCUPADO -> {
                calmMessagesInCrisis = 0
                MoodReaction(BertoState.CRISIS)
            }
            else -> {
                calmMessagesInCrisis++
                if (calmMessagesInCrisis >= CALM_MESSAGES_TO_STEP_DOWN) {
                    leaveCrisis(BertoState.PREOCUPADO, CrisisShift.STEPPED_DOWN)
                } else {
                    MoodReaction(BertoState.CRISIS)
                }
            }
        }
    }

    /** The user left the crisis through a button ("Ya estoy mejor", "Un poco mejor"). */
    fun reset() {
        calmMessagesInCrisis = 0
    }

    private fun leaveCrisis(state: BertoState, shift: CrisisShift): MoodReaction {
        calmMessagesInCrisis = 0
        return MoodReaction(state, shift)
    }

    companion object {
        const val CALM_MESSAGES_TO_STEP_DOWN = 2
    }
}
