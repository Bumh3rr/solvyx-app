package com.solvyx.ui.screens.chatbot

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.theme.BertoVisorCalm
import com.solvyx.ui.theme.BertoVisorCelebr
import com.solvyx.ui.theme.BertoVisorCrisis
import com.solvyx.ui.theme.BertoVisorWorried
import com.solvyx.ui.theme.ChatWarmAccent
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealPrimary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * How Berto looks on the chat stage, with the lines he "says" under it. The stage rotates through
 * [captions] while the expression lasts; the first one is the main line, shown as soon as it changes.
 * Crisis lines stay short and calm: they are read by someone who may be having a very hard time.
 */
enum class BertoExpression(vararg lines: String) {
    GREETING("¡Hola! Qué bueno verte", "¿Cómo va tu día?", "Me da gusto que estés aquí"),
    CALM("Aquí estoy para ti", "Cuéntame lo que quieras", "Sin prisa, a tu ritmo", "Estoy para escucharte"),
    LISTENING("Te escucho…", "Tómate tu tiempo…", "Aquí sigo, leyéndote…"),
    THINKING("Pensando en lo que me dijiste…", "Buscando las mejores palabras…"),
    THINKING_LONG("Dame un momento más…", "Ya casi…"),
    WORRIED(
        "Aquí contigo, paso a paso",
        "Te escucho sin juzgarte",
        "Hablarlo ya es un gran paso",
        "Las ganas suben, pero también bajan"
    ),
    HAPPY("¡Me alegra mucho!", "¡Así se hace!", "¡Qué buena noticia!", "Me encanta verte así"),
    SUPPORTIVE(
        "Estoy aquí contigo",
        "Respira despacio, sin prisa",
        "Lo que sientes importa",
        "Pedir ayuda es de valientes",
        "Vamos un paso a la vez",
        "Esto que sientes puede pasar"
    ),
    OFFLINE("Sin internet, pero sigo contigo", "Sigo aquí aunque no haya señal", "Los temas guiados funcionan sin internet"),
    BREATHING("Respira conmigo");

    val captions: List<String> = lines.toList()
}

/** Priority matters: a crisis beats everything, and "thinking" beats "listening". */
fun stageExpression(
    state: BertoState,
    isThinking: Boolean,
    isThinkingLong: Boolean,
    isUserTyping: Boolean,
    isOffline: Boolean,
    isWelcome: Boolean
): BertoExpression = when {
    state == BertoState.CRISIS -> BertoExpression.SUPPORTIVE
    isThinking -> if (isThinkingLong) BertoExpression.THINKING_LONG else BertoExpression.THINKING
    isUserTyping -> BertoExpression.LISTENING
    state == BertoState.PREOCUPADO -> BertoExpression.WORRIED
    state == BertoState.CELEBRANDO -> BertoExpression.HAPPY
    isOffline -> BertoExpression.OFFLINE
    isWelcome -> BertoExpression.GREETING
    else -> BertoExpression.CALM
}

private sealed interface BertoArt {
    data class Animated(val pose: BertoPose, @DrawableRes val fallback: Int) : BertoArt
    data class Still(@DrawableRes val drawable: Int) : BertoArt
}

/**
 * Single mapping point between an expression and its artwork. Still drawables stand in until
 * `berto_poses.riv` ships the matching pose (see "Berto — Animaciones pendientes" in Obsidian):
 * LISTENING → `Reading`, THINKING → `Thinking`, WORRIED → `Worried`, HAPPY → `Celebrate`,
 * SUPPORTIVE → `Supportive`, BREATHING → `Breathing`. Point each one to its new [BertoPose] here.
 */
private fun BertoExpression.art(): BertoArt = when (this) {
    BertoExpression.GREETING -> BertoArt.Animated(BertoPose.CENTER_IDLE_HELLO, R.drawable.berto_saludando)
    BertoExpression.CALM -> BertoArt.Animated(BertoPose.CENTER_IDLE, R.drawable.berto_tranquilo)
    // LeftSimple walks Berto out of frame at stage size; the tilt in [motion] reads as listening.
    BertoExpression.LISTENING -> BertoArt.Animated(BertoPose.CENTER_IDLE, R.drawable.berto_tranquilo)
    BertoExpression.BREATHING -> BertoArt.Animated(BertoPose.CENTER_IDLE, R.drawable.berto_tranquilo)
    BertoExpression.THINKING, BertoExpression.THINKING_LONG -> BertoArt.Still(R.drawable.berto_pregunta)
    BertoExpression.WORRIED, BertoExpression.SUPPORTIVE -> BertoArt.Still(R.drawable.berto_preocupado)
    // berto_euforico reads as startled, not happy: the smiling wave plus the hop in [motion] does it better.
    BertoExpression.HAPPY -> BertoArt.Animated(BertoPose.CENTER_IDLE_HELLO, R.drawable.berto_feliz)
    BertoExpression.OFFLINE -> BertoArt.Still(R.drawable.berto_sin_internet)
}

private const val MOTION_CYCLE_MS = 2_400
private const val RIVE_CONTENT_KEY = -1
private const val FULL_TURN = 2 * PI.toFloat()

/**
 * Berto for [expression]: crossfades between artworks and adds a small body motion on top (breathing,
 * a tilted head while thinking, hops when happy) so even the still drawables feel alive.
 */
@Composable
fun BertoChatFigure(expression: BertoExpression, modifier: Modifier = Modifier) {
    val cycle by rememberInfiniteTransition(label = "BertoMotion").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(MOTION_CYCLE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "BertoMotionCycle"
    )
    // A small "pop" every time the expression changes, so the change is noticed.
    val pop = remember { Animatable(1f) }
    LaunchedEffect(expression) {
        pop.snapTo(0.88f)
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    AnimatedContent(
        targetState = expression,
        transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
        // All Rive poses share one key: the same player just switches pose instead of reloading.
        contentKey = { expression -> (expression.art() as? BertoArt.Still)?.drawable ?: RIVE_CONTENT_KEY },
        modifier = modifier.graphicsLayer {
            val wave = sin(cycle * FULL_TURN)
            val motion = expression.motion(wave, cycle)
            scaleX = pop.value * motion.scale
            scaleY = pop.value * motion.scale
            rotationZ = motion.rotation
            translationX = motion.offsetX * density
            translationY = motion.offsetY * density
        },
        label = "BertoArt"
    ) { target ->
        BertoArtwork(target.art(), Modifier.fillMaxSize())
    }
}

private data class BertoMotion(
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

/** [wave] goes -1..1 once per cycle; offsets are in dp. */
private fun BertoExpression.motion(wave: Float, cycle: Float): BertoMotion = when (this) {
    BertoExpression.GREETING, BertoExpression.CALM, BertoExpression.OFFLINE ->
        BertoMotion(scale = 1f + 0.03f * wave)
    BertoExpression.BREATHING -> BertoMotion(scale = 1f + 0.06f * wave)
    BertoExpression.LISTENING -> BertoMotion(rotation = -6f + 2f * wave, offsetY = 2f * wave)
    BertoExpression.THINKING, BertoExpression.THINKING_LONG ->
        BertoMotion(rotation = 5f * wave, offsetY = -3f * abs(wave))
    BertoExpression.WORRIED, BertoExpression.SUPPORTIVE -> BertoMotion(offsetX = 3f * wave)
    BertoExpression.HAPPY -> BertoMotion(offsetY = -10f * abs(sin(cycle * 2 * FULL_TURN)))
}

@Composable
private fun BertoArtwork(art: BertoArt, modifier: Modifier) {
    when (art) {
        is BertoArt.Animated -> BertoPoseAnimation(
            pose = art.pose,
            riveFileRes = R.raw.berto_poses,
            modifier = modifier,
            fallback = art.fallback
        )
        is BertoArt.Still -> Image(
            painter = painterResource(art.drawable),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

// ── Per-state visuals ────────────────────────────────────────────────────

fun BertoState.visorColor(): Color = when (this) {
    BertoState.TRANQUILO -> BertoVisorCalm
    BertoState.PREOCUPADO -> BertoVisorWorried
    BertoState.CELEBRANDO -> BertoVisorCelebr
    BertoState.CRISIS -> BertoVisorCrisis
}

@Composable
fun BertoState.accentColor(): Color = when (this) {
    BertoState.TRANQUILO -> MaterialTheme.colorScheme.outline
    BertoState.PREOCUPADO -> ChatWarmAccent
    BertoState.CELEBRANDO -> TealPrimary
    BertoState.CRISIS -> CrisisRed
}

@DrawableRes
private fun BertoState.stillDrawable(): Int = when (this) {
    BertoState.TRANQUILO -> R.drawable.berto_tranquilo
    BertoState.PREOCUPADO, BertoState.CRISIS -> R.drawable.berto_preocupado
    BertoState.CELEBRANDO -> R.drawable.berto_feliz
}

/** Small round Berto next to his messages, in the mood he had when he wrote them. */
@Composable
fun BertoAvatar(state: BertoState, modifier: Modifier = Modifier, size: Dp = 30.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(state.visorColor()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(state.stillDrawable()),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(size / 8),
            contentScale = ContentScale.Fit
        )
    }
}
