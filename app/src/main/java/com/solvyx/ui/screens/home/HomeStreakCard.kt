package com.solvyx.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.backend.data.local.entity.AchievementEntity
import com.solvyx.ui.components.common.FireAnimation
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary

/** Alto de la ruta de hitos. Da aire a los nodos sin robarle peso al número de la racha. */
private val TrackHeight: Dp = 34.dp

/** Diámetro de un hito ya alcanzado / aún por alcanzar. */
private val NodeReached: Dp = 13.dp
private val NodePending: Dp = 9.dp

/** Halo del marcador que viaja sobre el riel. Contiene la llama, así que la envuelve con aire. */
private val HeadSize: Dp = 26.dp

/** Llama Lottie dentro del marcador. Menor que el halo para que el latido se note detrás. */
private val FlameHeadSize: Dp = 18.dp

/** Ancho fijo del slot de cada etiqueta, para poder centrarla sobre su nodo. */
private val LabelSlot: Dp = 24.dp

/**
 * Tarjeta de racha de Home.
 *
 * En vez de reportar números sueltos, dibuja el **camino completo** de hitos (3-7-10-15-30) para
 * que el usuario vea dónde está y qué sigue: la barra anterior solo mostraba "% hacia el próximo",
 * sin dar noción de viaje ni de lo ya recorrido.
 *
 * La llama es el marcador vivo sobre la ruta: se mueve al avanzar y solo respira mientras la racha
 * está activa. Con racha 0 se apaga a gris — no hay nada ardiendo que celebrar, y encender fuego
 * sobre un día de consumo sería tono sordo en una app de reducción de daños.
 */
@Composable
fun HomeStreakCard(
    streak: Int,
    bestStreak: Int,
    modifier: Modifier = Modifier
) {
    val milestones = AchievementEntity.MILESTONE_DAYS
    val maxMilestone = milestones.last()
    val isActive = streak > 0
    val hasSurpassedAll = streak >= maxMilestone

    // El hito y el avance se derivan de `streak`, no se confía en los parámetros: si el llamador
    // pasa un `streak` que no corresponde a su `nextMilestone`/`milestoneProgress` (p. ej. un valor
    // fijo para probar), la tarjeta mostraría un texto imposible como "faltan 0 días para el hito
    // de 3" con racha 10. Con la misma fuente (MILESTONE_DAYS) siempre son coherentes entre sí.
    val targetMilestone = milestones.firstOrNull { it > streak } ?: maxMilestone
    val prevMilestone = milestones.lastOrNull { it <= streak } ?: 0
    val stepProgress = if (hasSurpassedAll) 1f
        else ((streak - prevMilestone).toFloat() /
            (targetMilestone - prevMilestone).coerceAtLeast(1)).coerceIn(0f, 1f)

    // Progreso sobre la ruta COMPLETA (0..30), no hacia el próximo hito: es lo que permite que la
    // llama recorra el camino. `stepProgress` (avance dentro del tramo actual) es el del texto.
    val targetRouteProgress = (streak.toFloat() / maxMilestone).coerceIn(0f, 1f)

    // Se anima desde 0 en la primera composición: el trazo "se dibuja" al abrir Home. Sin esto el
    // camino aparece de golpe y se pierde la lectura de progreso.
    var startedRouteAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startedRouteAnim = true }
    val routeProgress by animateFloatAsState(
        targetValue = if (startedRouteAnim) targetRouteProgress else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "StreakRouteProgress"
    )

    val flameTint by animateColorAsState(
        targetValue = if (isActive) StreakFlame else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(500),
        label = "StreakFlameTint"
    )

    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StreakFlameBadge(isActive = isActive, tint = flameTint)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "Racha de bienestar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // El número es el protagonista: al cambiar, entra deslizando hacia arriba para que
                // el avance se sienta, no solo se lea.
                AnimatedContent(
                    targetState = streak,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { it } + fadeIn()) togetherWith
                                (slideOutVertically { -it } + fadeOut())
                        } else {
                            (slideInVertically { -it } + fadeIn()) togetherWith
                                (slideOutVertically { it } + fadeOut())
                        }
                    },
                    label = "StreakCount"
                ) { value ->
                    Text(
                        text = "$value ${diasLabel(value)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = subtitulo(streak, bestStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // El récord solo se muestra cuando dice algo que el usuario no ve ya en la racha
            // actual: repetir "Mejor racha: 1" junto a "1 días" es ruido.
            if (bestStreak > streak && bestStreak > 0) {
                BestStreakBadge(bestStreak)
            }
        }

        MilestoneTrack(
            milestones = milestones,
            streak = streak,
            routeProgress = routeProgress,
            isActive = isActive,
            flameTint = flameTint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp)
        )

        Text(
            text = textoAvance(streak, targetMilestone, stepProgress, hasSurpassedAll),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        )
    }
}

/** Insignia de la llama. Respira solo con racha activa: el latido comunica "sigue viva". */
@Composable
private fun StreakFlameBadge(isActive: Boolean, tint: Color) {
    val breathing = rememberInfiniteTransition(label = "StreakBreath")
    val pulse by breathing.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StreakBreathScale"
    )
    val halo by animateFloatAsState(
        targetValue = if (isActive) 0.35f else 0.10f,
        animationSpec = tween(500),
        label = "StreakHalo"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(if (isActive) pulse else 1f)
            .clip(CircleShape)
            .background(tint.copy(alpha = halo)),
        contentAlignment = Alignment.Center
    ) {
        FireAnimation(size = 24, isActive = isActive)
    }
}

@Composable
private fun BestStreakBadge(bestStreak: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Récord $bestStreak",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Ruta de hitos: línea recorrida + nodos. Es un [Canvas] y no una fila de composables porque la
 * llama tiene que poder pararse en cualquier punto intermedio del trazo, no solo sobre un nodo.
 *
 * El color cuenta un **viaje térmico**: el camino nace en el verde de la marca (constancia serena),
 * se entibia al avanzar y termina en el naranja del fuego bajo la llama. Cada nodo alcanzado toma
 * la temperatura de su punto del recorrido, así que los 5 hitos dejan de ser puntos idénticos y se
 * leen como una escala que progresa.
 */
@Composable
private fun MilestoneTrack(
    milestones: List<Int>,
    streak: Int,
    routeProgress: Float,
    isActive: Boolean,
    flameTint: Color,
    modifier: Modifier = Modifier
) {
    val maxMilestone = milestones.last()
    val trackColor = MaterialTheme.colorScheme.primaryContainer
    val pendingNode = TealMedium.copy(alpha = 0.45f)

    // Escala térmica del recorrido. Se apoya en colores que ya existen en el sistema:
    // TealPrimary (marca) → MoodNeutral/MoodBien (verdes de ánimo) → MoodAnsioso (ámbar) →
    // StreakFlame (el naranja de la racha). No introduce valores nuevos a la paleta.
    val heatStops = listOf(
        TealDark,        // salida: sereno, casi tinta
        TealPrimary,     // constancia
        MoodBien,        // verde vivo
        MoodAnsioso,     // ámbar: empieza a calentar
        StreakFlame      // fuego, justo bajo la llama
    )

    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TrackHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(TrackHeight)) {
                val y = size.height / 2f
                // Margen para que el primer y último nodo no se corten en los bordes.
                val startX = NodeReached.toPx() / 2f
                val endX = size.width - NodeReached.toPx() / 2f
                val usable = endX - startX

                // Riel completo
                drawLine(
                    color = trackColor,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Tramo recorrido: degradado térmico. El gradiente se mapea al recorrido REAL
                // (no al ancho total) para que la temperatura bajo la llama sea siempre la misma
                // sin importar cuánto se haya avanzado.
                val headX = startX + usable * routeProgress
                if (routeProgress > 0f) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = heatStops,
                            startX = startX,
                            endX = headX.coerceAtLeast(startX + 1f)
                        ),
                        start = Offset(startX, y),
                        end = Offset(headX, y),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Nodos de hito
                milestones.forEach { day ->
                    val ratio = day.toFloat() / maxMilestone
                    val x = startX + usable * ratio
                    val reached = streak >= day

                    if (reached) {
                        // Toma la temperatura de su punto del viaje: los hitos tempranos quedan
                        // verdes y los tardíos cálidos, formando una escala en vez de 5 puntos
                        // idénticos. El anillo claro lo separa del riel del mismo color.
                        val heat = heatAt(ratio, heatStops)
                        drawCircle(
                            color = trackColor,
                            radius = NodeReached.toPx() / 2f + 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = heat,
                            radius = NodeReached.toPx() / 2f,
                            center = Offset(x, y)
                        )
                    } else {
                        drawCircle(
                            color = trackColor,
                            radius = NodePending.toPx() / 2f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = pendingNode,
                            radius = NodePending.toPx() / 2f,
                            center = Offset(x, y),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1.5.dp.toPx()
                            )
                        )
                    }
                }
            }

            // Marcador vivo: viaja con el progreso. Solo aparece con racha activa — en 0 no hay
            // nada que marcar y el punto de salida ya lo comunica el nodo.
            if (isActive) {
                TrackFlameHead(
                    routeProgress = routeProgress,
                    tint = flameTint
                )
            }
        }

        // Etiquetas ancladas a la misma posición que sus nodos en el Canvas: mismo carril útil
        // (ancho menos el diámetro del nodo) y centradas sobre cada punto.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            val usable = maxWidth - NodeReached
            milestones.forEach { day ->
                val reached = streak >= day
                val x = NodeReached / 2 + usable * (day.toFloat() / maxMilestone)
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (reached) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(LabelSlot)
                        .offset(x = x - LabelSlot / 2),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Cabeza del marcador sobre el riel: la misma llama Lottie de la insignia, viajando por el camino.
 *
 * Se posiciona con un layout proporcional en vez de un offset en píxeles para que funcione a
 * cualquier ancho de pantalla, anclada al mismo carril útil que el [Canvas] (se descuenta el radio
 * del nodo en ambos extremos) para que caiga exactamente sobre el trazo.
 */
@Composable
private fun TrackFlameHead(routeProgress: Float, tint: Color) {
    val breathing = rememberInfiniteTransition(label = "TrackHead")
    val glow by breathing.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TrackHeadGlow"
    )

    BoxWithConstraints(Modifier.fillMaxWidth().height(TrackHeight)) {
        val usable = maxWidth - NodeReached
        val headX = NodeReached / 2 + usable * routeProgress.coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .offset(x = headX - HeadSize / 2)
                .size(HeadSize),
            contentAlignment = Alignment.Center
        ) {
            // El halo late detrás del fuego: lo despega del riel y de los nodos que va pisando.
            Box(
                modifier = Modifier
                    .size(HeadSize)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.30f * glow))
            )
            // Este composable solo se monta con racha activa, así que la llama siempre anima aquí.
            FireAnimation(size = FlameHeadSize.value.toInt(), isActive = true)
        }
    }
}

/**
 * Color de la escala térmica en [ratio] (0..1 del recorrido). Interpola entre los stops contiguos
 * igual que lo haría el gradiente del riel, para que un nodo pintado con esto caiga exactamente en
 * el tono que tiene la línea debajo.
 */
private fun heatAt(ratio: Float, stops: List<Color>): Color {
    if (stops.isEmpty()) return Color.Transparent
    if (stops.size == 1) return stops.first()
    val t = ratio.coerceIn(0f, 1f) * (stops.size - 1)
    val i = t.toInt().coerceAtMost(stops.size - 2)
    return lerp(stops[i], stops[i + 1], t - i)
}

// ── Copy ─────────────────────────────────────────────────────────────────────

/** Evita el "1 días" que mostraba la versión anterior. */
private fun diasLabel(value: Int): String = if (value == 1) "día" else "días"

private fun subtitulo(streak: Int, bestStreak: Int): String = when {
    streak == 0 && bestStreak == 0 -> "Tu primer día empieza cuando tú decidas."
    streak == 0 -> "Puedes volver a empezar hoy."
    streak == bestStreak -> "Estás en tu mejor racha."
    else -> "Vas construyendo tu constancia."
}

private fun textoAvance(
    streak: Int,
    nextMilestone: Int,
    milestoneProgress: Float,
    hasSurpassedAll: Boolean
): String {
    if (hasSurpassedAll) return "Superaste todos los hitos. $streak ${diasLabel(streak)} de constancia."
    val restantes = (nextMilestone - streak).coerceAtLeast(0)
    if (streak == 0) return "El primer hito son $nextMilestone días."
    val pct = (milestoneProgress * 100).toInt().coerceIn(0, 100)
    return if (restantes == 1) "Te falta 1 día para el hito de $nextMilestone · $pct%"
           else "Te faltan $restantes días para el hito de $nextMilestone · $pct%"
}
