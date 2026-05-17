package com.solvyx.ui.screens.guias.screens.panico

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium
import kotlinx.coroutines.launch

private data class SenseData(
    val number: Int,
    val word: String,
    val description: String,
    val iconRes: Int
)

private val SENSE_STEPS = listOf(
    SenseData(5, "VER",      "cosas que puedes ver a tu alrededor",  R.drawable.ic_globe),
    SenseData(4, "TOCAR",    "cosas que puedes tocar ahora mismo",    R.drawable.ic_heart_pulse),
    SenseData(3, "ESCUCHAR", "sonidos que puedes oír",                R.drawable.ic_mic),
    SenseData(2, "OLER",     "aromas que puedes detectar",            R.drawable.ic_wind),
    SenseData(1, "SABOREAR", "sabor que puedes identificar",          R.drawable.ic_droplet)
)

// ── Pantalla del ejercicio ───────────────────────────────────────────────────

@Composable
internal fun EjercicioGuiadoScreen(
    viewModel: EjercicioGuiadoViewModel,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark)
    ) {
        // z=1: fondo pulsante persistente en todos los pasos
        BreathingBackground()

        // z=2: contenido del paso actual
        AnimatedContent(
            targetState = viewModel.step,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally { -it } + fadeOut(tween(200))
            },
            label = "exercise_step"
        ) { currentStep ->
            when {
                currentStep == -1 -> IntroStep(onStart = { viewModel.nextStep() })
                currentStep < 5  -> SenseStep(
                    stepIndex = currentStep,
                    sense     = SENSE_STEPS[currentStep],
                    tapped    = viewModel.tapped,
                    onTap     = { i -> viewModel.tapBubble(i) },
                    onNext    = { viewModel.nextStep() }
                )
                else -> CompletionStep(onFinish = { viewModel.stopSpeaking(); onFinish() })
            }
        }

        // z=3: botón cerrar (siempre visible encima del contenido)
        Box(
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable { viewModel.stopSpeaking(); onFinish() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_circle_x),
                contentDescription = "Cerrar",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }

        // z=4: indicador de voz activa
        SpeakingWave(
            isSpeaking = viewModel.isSpeaking,
            modifier   = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
        )
    }
}

// ── Fondo pulsante ───────────────────────────────────────────────────────────

@Composable
private fun BreathingBackground() {
    val inf = rememberInfiniteTransition(label = "bg")
    val sc1 by inf.animateFloat(
        initialValue = 0.72f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bg_s1"
    )
    val sc2 by inf.animateFloat(
        initialValue = 1f, targetValue = 0.68f,
        animationSpec = infiniteRepeatable(tween(4600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bg_s2"
    )
    val sc3 by inf.animateFloat(
        initialValue = 0.85f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bg_s3"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color  = TealMedium.copy(alpha = 0.055f),
            radius = size.minDimension * 0.72f * sc1,
            center = Offset(size.width * 0.12f, size.height * 0.22f)
        )
        drawCircle(
            color  = TealMedium.copy(alpha = 0.04f),
            radius = size.minDimension * 0.65f * sc2,
            center = Offset(size.width * 0.88f, size.height * 0.78f)
        )
        drawCircle(
            color  = TealMedium.copy(alpha = 0.03f),
            radius = size.minDimension * 0.45f * sc3,
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )
    }
}

// ── Indicador de voz ─────────────────────────────────────────────────────────

@Composable
private fun SpeakingWave(isSpeaking: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = isSpeaking,
        enter   = fadeIn(tween(300)),
        exit    = fadeOut(tween(400)),
        modifier = modifier
    ) {
        val inf = rememberInfiniteTransition(label = "wave")
        val h0 by inf.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "w0"
        )
        val h1 by inf.animateFloat(
            initialValue = 1f, targetValue = 0.3f,
            animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "w1"
        )
        val h2 by inf.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(640, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "w2"
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_mic),
                contentDescription = null,
                tint = TealMedium,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(2.dp))
            listOf(h0, h1, h2).forEach { h ->
                Box(
                    Modifier
                        .width(3.dp)
                        .height((14.dp * h).coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(2.dp))
                        .background(TealMedium.copy(alpha = 0.85f))
                )
            }
        }
    }
}

// ── Paso de introducción ─────────────────────────────────────────────────────

@Composable
private fun IntroStep(onStart: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val inf = rememberInfiniteTransition(label = "intro")
    val breatheScale by inf.animateFloat(
        initialValue = 0.82f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "intro_scale"
    )
    val breatheAlpha by inf.animateFloat(
        initialValue = 0.18f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "intro_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(200.dp * breatheScale)
                    .clip(CircleShape)
                    .background(TealMedium.copy(alpha = breatheAlpha * 0.35f))
            )
            Box(
                Modifier
                    .size(148.dp * breatheScale)
                    .clip(CircleShape)
                    .background(TealMedium.copy(alpha = breatheAlpha * 0.6f))
            )
            Box(
                Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_wind),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "TÉCNICA 5-4-3-2-1",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = TealMedium
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Ancla tu mente\nal presente",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "Vamos a recorrer tus sentidos uno a uno.\nTómate el tiempo que necesites en cada paso.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(44.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary)
        ) {
            Icon(painterResource(R.drawable.ic_zap), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Comenzar",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Respira profundo antes de empezar",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.35f)
        )
    }
}

// ── Paso de sentido ──────────────────────────────────────────────────────────

@Composable
private fun SenseStep(
    stepIndex: Int,
    sense: SenseData,
    tapped: Set<Int>,
    onTap: (Int) -> Unit,
    onNext: () -> Unit
) {
    val primary    = MaterialTheme.colorScheme.primary
    val isComplete = tapped.size >= sense.number
    val progressAnim by animateFloatAsState(
        targetValue   = tapped.size.toFloat() / sense.number.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "ring_progress"
    )

    val inf = rememberInfiniteTransition(label = "sense_pulse")
    val pulseScale by inf.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    val pulseAlpha by inf.animateFloat(
        initialValue = 0f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 64.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pastillas de progreso
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(5) { i ->
                val dotWidth by animateDpAsState(
                    targetValue   = if (i == stepIndex) 22.dp else 8.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label         = "dot_$i"
                )
                Box(
                    Modifier
                        .size(width = dotWidth, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                i < stepIndex  -> TealMedium.copy(alpha = 0.6f)
                                i == stepIndex -> primary
                                else           -> Color.White.copy(alpha = 0.18f)
                            }
                        )
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Anillo de progreso con halo pulsante
        Box(Modifier.size(196.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(164.dp * pulseScale)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = pulseAlpha))
            )
            Canvas(modifier = Modifier.size(164.dp)) {
                val strokePx = 9.dp.toPx()
                val radius   = size.width / 2f - strokePx / 2f
                drawCircle(color = Color.White.copy(alpha = 0.1f), radius = radius, style = Stroke(strokePx))
                if (progressAnim > 0f) {
                    drawArc(
                        color      = primary,
                        startAngle = -90f,
                        sweepAngle = 360f * progressAnim,
                        useCenter  = false,
                        style      = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painterResource(sense.iconRes),
                    contentDescription = null,
                    tint     = TealMedium.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    sense.number.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 72.sp
                    ),
                    color      = Color.White,
                    lineHeight = 72.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            sense.word,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            ),
            color = TealMedium
        )

        Text(
            "Identifica ${sense.number} ${sense.description}",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color.White.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        // Burbujas táctiles
        val bubbleSizeDp: Dp = when (sense.number) {
            1    -> 80.dp
            2    -> 68.dp
            3    -> 62.dp
            4    -> 56.dp
            else -> 52.dp
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(sense.number) { i ->
                TapBubble(
                    index       = i,
                    bubbleSizeDp = bubbleSizeDp,
                    isTapped    = i in tapped,
                    primary     = primary,
                    onTap       = { onTap(i) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = !isComplete) {
            Text(
                "Toca cada burbuja cuando identifiques una",
                style     = MaterialTheme.typography.bodySmall,
                color     = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(bottom = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(if (isComplete) primary else Color.White.copy(alpha = 0.08f))
                .clickable(enabled = isComplete) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (stepIndex < 4) "Siguiente sentido" else "Terminar ejercicio",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 15.sp
                    ),
                    color = if (isComplete) Color.White else Color.White.copy(alpha = 0.25f)
                )
                Icon(
                    painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint     = if (isComplete) Color.White else Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Burbuja táctil con ripple ────────────────────────────────────────────────

@Composable
private fun TapBubble(
    index: Int,
    bubbleSizeDp: Dp,
    isTapped: Boolean,
    primary: Color,
    onTap: () -> Unit
) {
    val scope       = rememberCoroutineScope()
    val rippleScale = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }

    val bgColor by animateColorAsState(
        targetValue   = if (isTapped) primary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "bubble_bg_$index"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isTapped) primary else Color.White.copy(alpha = 0.3f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "bubble_border_$index"
    )
    val bubbleScale by animateFloatAsState(
        targetValue   = if (isTapped) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "bubble_scale_$index"
    )

    // El Box ocupa solo el espacio de la burbuja en el layout
    Box(
        modifier         = Modifier.size(bubbleSizeDp),
        contentAlignment = Alignment.Center
    ) {
        // El canvas del ripple usa requiredSize para dibujar fuera de los límites del Box
        // sin afectar el layout de la Row que lo contiene
        Canvas(modifier = Modifier.requiredSize(bubbleSizeDp * 2.4f)) {
            if (rippleAlpha.value > 0f) {
                drawCircle(
                    color  = primary.copy(alpha = rippleAlpha.value),
                    radius = size.minDimension / 2f * rippleScale.value,
                    style  = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier
                .size(bubbleSizeDp * bubbleScale)
                .clip(CircleShape)
                .background(bgColor)
                .border(2.dp, borderColor, CircleShape)
                .clickable(enabled = !isTapped) {
                    scope.launch {
                        rippleScale.snapTo(0.55f)
                        rippleAlpha.snapTo(0.75f)
                        launch { rippleScale.animateTo(1f, tween(500)) }
                        rippleAlpha.animateTo(0f, tween(500))
                    }
                    onTap()
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState  = isTapped,
                transitionSpec = {
                    scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            fadeIn(tween(150)) togetherWith fadeOut(tween(80))
                },
                label = "bubble_content_$index"
            ) { tappedState ->
                if (tappedState) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(bubbleSizeDp * 0.42f)
                    )
                } else {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ── Paso de completado ───────────────────────────────────────────────────────

@Composable
private fun CompletionStep(onFinish: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val inf = rememberInfiniteTransition(label = "done_pulse")
    val pulseScale by inf.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "done_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 56.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(140.dp * pulseScale).clip(CircleShape).background(TealMedium.copy(alpha = 0.12f)))
            Box(Modifier.size(104.dp).clip(CircleShape).background(TealMedium.copy(alpha = 0.22f)))
            Box(
                Modifier.size(76.dp).clip(CircleShape).background(primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_check_circle), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        Image(
            painterResource(R.drawable.berto_feliz),
            contentDescription = null,
            modifier     = Modifier.height(140.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "¡Lo lograste!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )

        Text(
            "Tu mente está anclada al presente",
            style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color    = TealMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                stringResource(R.string.tts_completion),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = onFinish,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(50.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = primary)
        ) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Cerrar ejercicio",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 15.sp
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Recuerda: puedes hacer este ejercicio en cualquier momento",
            style     = MaterialTheme.typography.bodySmall,
            color     = Color.White.copy(alpha = 0.30f),
            textAlign = TextAlign.Center
        )
    }
}
