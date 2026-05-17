package com.solvyx.ui.screens.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary
import kotlinx.coroutines.delay

private val LINEA_VIDA = "8002900024"

@Composable
fun SosOverlayScreen(
    contactos: List<String>,
    telefonos: List<String> = emptyList(),
    onCancel: () -> Unit,
    onHablarConBerto: () -> Unit,
    onClose: () -> Unit,
    viewModel: SosViewModel = hiltViewModel()
) {
    val sosState = viewModel.sosState

    LaunchedEffect(Unit) {
        viewModel.startCountdown(telefonos)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ConcentricRings()

        AnimatedContent(
            targetState = sosState,
            transitionSpec = {
                fadeIn(tween(350)) togetherWith fadeOut(tween(200))
            },
            label = "SosContent"
        ) { state ->
            when (state) {
                SosState.COUNTDOWN -> SosCountdown(
                    contactos = contactos,
                    countdown = viewModel.countdown,
                    onCancel = {
                        viewModel.cancel()
                        onCancel()
                    }
                )

                SosState.SENT -> {
                    val context = LocalContext.current
                    SosPostSend(
                        viewModel = viewModel,
                        onHablarConBerto = onHablarConBerto,
                        onLlamar = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$LINEA_VIDA"))
                            context.startActivity(intent)
                        },
                        onClose = onClose
                    )
                }
            }
        }
    }
}

// ── ConcentricRings ───────────────────────────────────────────────────────────

@Composable
fun ConcentricRings() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        listOf(280.dp.toPx(), 200.dp.toPx(), 120.dp.toPx()).forEach { r ->
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

// ── SosCountdown ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SosCountdown(
    contactos: List<String>,
    countdown: Int,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Restart),
        label = "PulseAlpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.12f))

        // Berto preocupado con anillo pulsante
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = pulseAlpha), CircleShape)
            )
            val desaturate = ColorMatrix().also { it.setToSaturation(0.45f) }
            Image(
                painter = painterResource(R.drawable.berto_preocupado),
                contentDescription = "Berto",
                modifier = Modifier.size(72.dp),
                colorFilter = ColorFilter.colorMatrix(desaturate)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enviando alerta SMS a tus contactos seguros…",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            contactos.forEach { nombre ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = nombre,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        CountdownRing(value = countdown, total = 3, size = 160.dp)

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .border(1.5.dp, Color.White.copy(0.50f), RoundedCornerShape(50.dp))
                .clickable { onCancel() }
                .padding(horizontal = 40.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Cancelar",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Solo disponible durante la cuenta regresiva",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.40f)
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// ── CountdownRing ─────────────────────────────────────────────────────────────

@Composable
fun CountdownRing(value: Int, total: Int, size: Dp) {
    val animatedFraction by animateFloatAsState(
        targetValue = value / total.toFloat(),
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "CountdownArc"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        // Fondo oscuro semitransparente
        Box(
            modifier = Modifier
                .size(size - 12.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Canvas(modifier = Modifier.size(size)) {
            val stroke = 6.dp.toPx()
            val r = (this.size.width - stroke) / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f

            // Track
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(stroke)
            )

            // Arco activo — se depleta conforme avanza el countdown
            if (animatedFraction > 0f) {
                drawArc(
                    color = CrisisRed,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFraction,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "seg",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

// ── SosPostSend ───────────────────────────────────────────────────────────────

@Composable
fun SosPostSend(
    viewModel: SosViewModel,
    onHablarConBerto: () -> Unit,
    onLlamar: () -> Unit,
    onClose: () -> Unit
) {
    var badgeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200L)
        badgeVisible = true
        viewModel.speakInitialGuide()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.08f))

        // Badge de confirmación con entrada animada
        AnimatedVisibility(
            visible = badgeVisible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Alerta enviada.",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-0.3).sp
        )

        Text(
            text = "Tus contactos han sido notificados.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Berto + burbuja
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            val desaturate = ColorMatrix().also { it.setToSaturation(0.45f) }
            Image(
                painter = painterResource(R.drawable.berto_preocupado),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.colorMatrix(desaturate)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 180.dp)
            ) {
                Text(
                    text = "Intenta respirar con este círculo.",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BreathingCircle(
            onPhaseChange = { phaseName -> viewModel.speakPhase(phaseName) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Respiración 4 — 7 — 8",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.50f),
            letterSpacing = 0.04.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botones
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    viewModel.stopTts()
                    onHablarConBerto()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .widthIn(max = 300.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chat),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hablar con Berto ahora",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.widthIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLlamar,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Llamar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.stopTts()
                        onClose()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cerrar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Solvyx 2026",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(52.dp))
    }
}

// ── BreathingCircle ───────────────────────────────────────────────────────────

private data class BreathPhase(val name: String, val durationMs: Int, val targetSize: Dp)

private val BREATH_PHASES = listOf(
    BreathPhase("Inhala", 4_000, 150.dp),
    BreathPhase("Mantén", 7_000, 150.dp),
    BreathPhase("Exhala", 8_000, 80.dp)
)

@Composable
fun BreathingCircle(onPhaseChange: (String) -> Unit = {}) {
    var phaseIndex by remember { mutableIntStateOf(0) }
    var currentPhase by remember { mutableStateOf(BREATH_PHASES[0]) }
    var countdown by remember { mutableIntStateOf(BREATH_PHASES[0].durationMs / 1000) }

    LaunchedEffect(phaseIndex) {
        val phase = BREATH_PHASES[phaseIndex]
        currentPhase = phase
        onPhaseChange(phase.name)
        for (s in phase.durationMs / 1000 downTo 1) {
            countdown = s
            delay(1000L)
        }
        phaseIndex = (phaseIndex + 1) % BREATH_PHASES.size
    }

    val animatedDiameter by animateDpAsState(
        targetValue = currentPhase.targetSize,
        animationSpec = tween(
            durationMillis = currentPhase.durationMs,
            easing = when (phaseIndex) {
                1 -> LinearEasing
                else -> EaseInOut
            }
        ),
        label = "BreathDiameter"
    )

    val isExpanded = animatedDiameter > 110.dp
    val circleColor by animateColorAsState(
        targetValue = if (isExpanded) TealMedium.copy(alpha = 0.30f)
        else TealPrimary.copy(alpha = 0.40f),
        animationSpec = tween(600),
        label = "CircleColor"
    )

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Anillo exterior fijo de referencia
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White.copy(alpha = 0.30f), CircleShape)
        )

        // Círculo animado de respiración
        Box(
            modifier = Modifier
                .size(animatedDiameter)
                .clip(CircleShape)
                .background(circleColor)
                .border(2.dp, Color.White.copy(alpha = 0.60f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = BREATH_PHASES[phaseIndex].name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.02.sp
                )
                Text(
                    text = countdown.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
