package com.solvyx.ui.screens.home.stage

import androidx.annotation.DrawableRes
import com.solvyx.R
import java.time.LocalDate
import kotlin.math.min

/**
 * Pure rules behind "El jardín de Berto", Home's stage: what Berto says, which idea he suggests,
 * and how the garden grows with the streak. No Compose here, so all of it is unit-tested.
 */

// ── Time of day ─────────────────────────────────────────────────────────────

enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

fun timeOfDayFor(hour: Int): TimeOfDay = when (hour) {
    in 5..11 -> TimeOfDay.MORNING
    in 12..18 -> TimeOfDay.AFTERNOON
    in 19..20 -> TimeOfDay.EVENING
    else -> TimeOfDay.NIGHT
}

// ── Berto's introduction ────────────────────────────────────────────────────

/** After this long away, Berto introduces himself again ("Hola, me llamo Berto…"). */
const val REINTRODUCE_AFTER_DAYS = 7L

/**
 * Whether Berto opens with his spoken introduction (Rive "Greet", ≈8 s) instead of just waving
 * ("Hi"): the first time this account reaches Home on the phone, and after a long absence.
 * Every day would wear the voice out; never again would make it easy to forget he's there.
 */
fun needsIntroduction(lastVisit: LocalDate?, today: LocalDate): Boolean =
    lastVisit == null || !lastVisit.plusDays(REINTRODUCE_AFTER_DAYS).isAfter(today)

// ── What Berto says ─────────────────────────────────────────────────────────

/** HomeViewModel's placeholder until the profile loads: never greet someone as "Usuario". */
const val PLACEHOLDER_NICKNAME = "Usuario"

/** Each line of the speech bubble. The text is resolved late ([lineText]) so a nickname or a
 *  streak that arrives after the line was chosen still shows up correctly. */
sealed interface StageLine {
    data object Greeting : StageLine
    data object Streak : StageLine
    data class Reaction(val mood: String) : StageLine
    data class Poke(val index: Int) : StageLine
    data object Suggesting : StageLine
    data object SuggestionTaken : StageLine
}

private val PokeLines = listOf(
    "¡Hey! Aquí estoy.",
    "Jeje, eso hace cosquillas.",
    "¿Me buscabas? Siempre ando por aquí.",
    "¡Choca esos cinco!"
)

val PokeLineCount: Int get() = PokeLines.size

fun lineText(line: StageLine, hour: Int, nickname: String?, streak: Int): String = when (line) {
    StageLine.Greeting -> greetingFor(hour, nickname)
    StageLine.Streak -> streakLineFor(streak)
    is StageLine.Reaction -> reactionLineFor(line.mood)
    is StageLine.Poke -> PokeLines[line.index.mod(PokeLines.size)]
    StageLine.Suggesting -> "Tengo una idea para ti."
    StageLine.SuggestionTaken -> "Bien pensado. Aquí te espero."
}

fun greetingFor(hour: Int, nickname: String?): String {
    val salute = when (timeOfDayFor(hour)) {
        TimeOfDay.MORNING -> "Buenos días"
        TimeOfDay.AFTERNOON -> "Buenas tardes"
        TimeOfDay.EVENING, TimeOfDay.NIGHT -> "Buenas noches"
    }
    val name = nickname?.trim()?.takeIf { it.isNotEmpty() && it != PLACEHOLDER_NICKNAME }
    return if (name == null) "¡$salute!" else "¡$salute, $name!"
}

/** Reads the streak as the garden: the bubble and the flowers tell the same story. */
fun streakLineFor(streak: Int): String = when (gardenStageFor(streak)) {
    GardenStage.SEEDS -> "Hoy es un buen día para sembrar algo nuevo."
    GardenStage.SPROUTS -> if (streak == 1) "Tu primer día ya brotó." else "Llevas $streak días. Tu jardín está brotando."
    GardenStage.BUDS -> "Llevas $streak días. Ya salieron capullos."
    GardenStage.BLOOM -> "Llevas $streak días. Tu jardín está floreciendo."
    GardenStage.BUTTERFLIES -> "Llevas $streak días. Hasta llegaron mariposas."
    GardenStage.FULL_BLOOM -> "Llevas $streak días. Tu jardín está en plena flor."
}

fun reactionLineFor(mood: String): String = when (mood) {
    "triste" -> "Gracias por contármelo. Aquí estoy contigo."
    "ansioso" -> "Te entiendo. Vamos a bajarle juntos."
    "neutral" -> "Un día tranquilo también cuenta."
    "bien" -> "¡Qué bueno! Me alegra mucho."
    "euforico" -> "¡Qué energía! Vamos a cuidarla bien."
    else -> "Gracias por contarme cómo estás."
}

/** Moods where Berto doesn't wait to be asked: after reacting he goes straight to an idea. */
fun suggestsRightAway(mood: String): Boolean = mood == "triste" || mood == "ansioso"

/** Moods the garden celebrates with a wiggle. */
fun celebrates(mood: String): Boolean = mood == "bien" || mood == "euforico"

// ── Suggestions ─────────────────────────────────────────────────────────────

enum class SuggestionAction { BREATHE, GROUNDING, TALK, SUPPORT_NETWORK, JOURNAL, SELF_CARE }

data class BertoSuggestion(
    val id: String,
    val title: String,
    val body: String,
    @DrawableRes val icon: Int,
    val action: SuggestionAction,
    val actionLabel: String
)

private val Breathe = BertoSuggestion(
    id = "breathe",
    title = "Respira conmigo",
    body = "Inhala en 4, sostén en 7 y suelta el aire en 8. Con tres vueltas ya se nota.",
    icon = R.drawable.ic_wind,
    action = SuggestionAction.BREATHE,
    actionLabel = "Respirar ahora"
)
private val Grounding = BertoSuggestion(
    id = "grounding",
    title = "Ancla tu mente",
    body = "Nombra 5 cosas que ves, 4 que tocas, 3 que oyes, 2 que hueles y 1 que saboreas.",
    icon = R.drawable.ic_eye,
    action = SuggestionAction.GROUNDING,
    actionLabel = "Hacer el 5-4-3-2-1"
)
private val Talk = BertoSuggestion(
    id = "talk",
    title = "Cuéntame qué pasa",
    body = "A veces ponerlo en palabras ya lo hace más ligero. Te escucho sin juzgar.",
    icon = R.drawable.ic_chat,
    action = SuggestionAction.TALK,
    actionLabel = "Hablar con Berto"
)
private val SupportNetwork = BertoSuggestion(
    id = "support",
    title = "Busca a tu gente",
    body = "Escríbele a alguien de confianza. No tienes que explicar todo, basta con un hola.",
    icon = R.drawable.ic_people,
    action = SuggestionAction.SUPPORT_NETWORK,
    actionLabel = "Ver mi red"
)
private val Water = BertoSuggestion(
    id = "water",
    title = "Un vaso de agua",
    body = "Tómalo despacio, sorbo a sorbo. Es una pausa pequeña para tu cuerpo.",
    icon = R.drawable.ic_droplet,
    action = SuggestionAction.SELF_CARE,
    actionLabel = "Lo haré"
)
private val Move = BertoSuggestion(
    id = "move",
    title = "Muévete un poco",
    body = "Camina cinco minutos o estira hombros y cuello. El cuerpo también ayuda a la mente.",
    icon = R.drawable.ic_footsteps,
    action = SuggestionAction.SELF_CARE,
    actionLabel = "Lo haré"
)
private val Journal = BertoSuggestion(
    id = "journal",
    title = "Guarda este momento",
    body = "Escribe en Mi camino qué salió bien hoy. Leerlo después te va a servir.",
    icon = R.drawable.ic_pencil,
    action = SuggestionAction.JOURNAL,
    actionLabel = "Ir a Mi camino"
)

/** Ideas for [mood], best fit first; "Otra idea" walks the list. */
fun suggestionsFor(mood: String?): List<BertoSuggestion> = when (mood) {
    "ansioso" -> listOf(Breathe, Grounding, Move, Talk)
    "triste" -> listOf(Talk, SupportNetwork, Move, Water)
    "neutral" -> listOf(Move, Water, Journal, Breathe)
    "bien" -> listOf(Journal, Move, SupportNetwork)
    "euforico" -> listOf(Breathe, Journal, SupportNetwork)
    else -> listOf(Breathe, Move, Water, Talk)
}

// ── Note of the day (the text Berto reads) ─────────────────────────────────

private val DailyNotes = listOf(
    "Un antojo sube como una ola y después baja. Si le das unos minutos, suele pasar.",
    "Soltar el aire más lento de lo que lo tomas le avisa a tu cuerpo que puede calmarse.",
    "Dormir bien hace que los días pesados pesen un poco menos.",
    "Hablar con alguien de confianza no resuelve todo, pero hace más ligero lo que cargas.",
    "Moverte diez minutos, aunque sea caminar, puede cambiar cómo te sientes.",
    "No tienes que hacerlo perfecto. Cada día cuenta por sí mismo.",
    "Comer algo y tomar agua también es cuidarte cuando todo se siente mucho."
)

fun dailyNoteFor(date: LocalDate): String = DailyNotes[date.dayOfYear % DailyNotes.size]

// ── Garden ──────────────────────────────────────────────────────────────────

/** The garden follows the streak milestones (3 · 7 · 15 · 30 days). */
enum class GardenStage { SEEDS, SPROUTS, BUDS, BLOOM, BUTTERFLIES, FULL_BLOOM }

private const val BUDS_FROM_DAY = 3
private const val BLOOM_FROM_DAY = 7
private const val BUTTERFLIES_FROM_DAY = 15
private const val FULL_BLOOM_FROM_DAY = 30

fun gardenStageFor(streak: Int): GardenStage = when {
    streak <= 0 -> GardenStage.SEEDS
    streak < BUDS_FROM_DAY -> GardenStage.SPROUTS
    streak < BLOOM_FROM_DAY -> GardenStage.BUDS
    streak < BUTTERFLIES_FROM_DAY -> GardenStage.BLOOM
    streak < FULL_BLOOM_FROM_DAY -> GardenStage.BUTTERFLIES
    else -> GardenStage.FULL_BLOOM
}

enum class PlantKind { SEED, SPROUT, BUD, TULIP, DAISY, SUNFLOWER }

/**
 * One plant: [x] is 0..1 across the stage, [height] 0..1 of the tallest plant, [colorIndex] picks
 * a petal color and [swayPhase] (radians) keeps neighbours from swaying in lockstep.
 */
data class GardenPlant(
    val x: Float,
    val kind: PlantKind,
    val height: Float,
    val colorIndex: Int,
    val swayPhase: Float
)

const val MAX_PLANTS = 14
private const val SEED_COUNT = 3
private const val SUNFLOWER_EVERY = 4

/** Berto stands in the middle: plants grow on both sides, never in front of him. */
const val GARDEN_CENTER_START = 0.36f
const val GARDEN_CENTER_END = 0.64f
private const val GARDEN_EDGE = 0.04f

/** One plant per day of streak (up to [MAX_PLANTS]); the kind reflects how far the streak got. */
fun gardenFor(streak: Int): List<GardenPlant> {
    val stage = gardenStageFor(streak)
    val count = if (stage == GardenStage.SEEDS) SEED_COUNT else min(streak, MAX_PLANTS)
    return List(count) { index ->
        val kind = plantKindFor(stage, index)
        GardenPlant(
            x = plantX(index, count),
            kind = kind,
            height = baseHeight(kind) * (0.85f + 0.15f * wobble(index, 1)),
            colorIndex = index,
            swayPhase = wobble(index, 2) * 6.28f
        )
    }
}

private fun plantKindFor(stage: GardenStage, index: Int): PlantKind = when (stage) {
    GardenStage.SEEDS -> PlantKind.SEED
    GardenStage.SPROUTS -> PlantKind.SPROUT
    GardenStage.BUDS -> PlantKind.BUD
    GardenStage.BLOOM -> if (index % 2 == 0) PlantKind.TULIP else PlantKind.DAISY
    GardenStage.BUTTERFLIES, GardenStage.FULL_BLOOM -> when {
        index % SUNFLOWER_EVERY == SUNFLOWER_EVERY - 1 -> PlantKind.SUNFLOWER
        index % 2 == 0 -> PlantKind.TULIP
        else -> PlantKind.DAISY
    }
}

private fun baseHeight(kind: PlantKind): Float = when (kind) {
    PlantKind.SEED -> 0.12f
    PlantKind.SPROUT -> 0.38f
    PlantKind.BUD -> 0.62f
    PlantKind.TULIP -> 0.78f
    PlantKind.DAISY -> 0.72f
    PlantKind.SUNFLOWER -> 1f
}

/** Alternates sides (even → left, odd → right) and spreads each side evenly, with a small jitter. */
private fun plantX(index: Int, count: Int): Float {
    val onLeft = index % 2 == 0
    val perSide = (count + 1) / 2
    val slot = index / 2
    val span = GARDEN_CENTER_START - GARDEN_EDGE
    val step = span / perSide
    val jitter = (wobble(index, 3) - 0.5f) * step * 0.5f
    val fromCenter = step * (slot + 0.5f) + jitter
    return if (onLeft) GARDEN_CENTER_START - fromCenter else GARDEN_CENTER_END + fromCenter
}

/** Stable pseudo-random 0..1 per plant, so the garden looks the same every time it's drawn. */
private fun wobble(index: Int, salt: Int): Float {
    val h = (index * 73_856_093) xor (salt * 19_349_663)
    return ((h ushr 8) and 0xFFFF) / 65_535f
}

fun butterfliesFor(streak: Int): Int = when (gardenStageFor(streak)) {
    GardenStage.BUTTERFLIES -> 2
    GardenStage.FULL_BLOOM -> 3
    else -> 0
}

// ── Streak fire ─────────────────────────────────────────────────────────────

private const val MAX_EMBERS = 18
private const val BASE_EMBERS = 4

/** Embers rising behind Berto: none without a streak, more as it grows. */
fun embersFor(streak: Int): Int = if (streak <= 0) 0 else min(BASE_EMBERS + streak, MAX_EMBERS)

/** Strength (0..1) of the warm glow behind Berto. */
fun glowFor(streak: Int): Float =
    if (streak <= 0) 0f else min(0.35f + streak / FULL_BLOOM_FROM_DAY.toFloat() * 0.65f, 1f)
