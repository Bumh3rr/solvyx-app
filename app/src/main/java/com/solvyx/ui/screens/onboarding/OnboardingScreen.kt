package com.solvyx.ui.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.navigation.Routes
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary
import kotlinx.coroutines.launch

// ── Tipos de animación de Berto ─────────────────────────
enum class BertoAnimation { FLOAT, BOUNCE, PULSE, WAVE }

private data class OnboardingPage(
    @DrawableRes val bertoDrawable: Int,
    val backgroundColor: Color,
    val badge: String,
    val title: String,
    val description: String,
    val animationType: BertoAnimation
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPage(
        bertoDrawable = R.drawable.berto_saludando,
        backgroundColor = TealPrimary,
        badge = "Bienvenido a Solvyx",
        title = "Tu espacio seguro",
        description = "Un lugar privado para entenderte mejor, registrar cómo te sientes y recibir apoyo cuando más lo necesitas.",
        animationType = BertoAnimation.WAVE
    ),
    OnboardingPage(
        bertoDrawable = R.drawable.berto_preocupado,
        backgroundColor = TealDark,
        badge = "Diagnóstico ASSIST · OMS",
        title = "Conoce tu nivel de riesgo",
        description = "Un cuestionario clínico basado en la OMS evalúa tu relación con el alcohol, tabaco, vape y cristal. Sin juicios, solo información.",
        animationType = BertoAnimation.PULSE
    ),
    OnboardingPage(
        bertoDrawable = R.drawable.berto_saludando,
        backgroundColor = TealPrimary,
        badge = "Botón SOS",
        title = "Ayuda en segundos",
        description = "Si estás en crisis, un toque avisa a tus contactos de confianza por SMS. Sin internet, sin demoras.",
        animationType = BertoAnimation.BOUNCE
    ),
    OnboardingPage(
        bertoDrawable = R.drawable.berto_tranquilo,
        backgroundColor = TealDark,
        badge = "100% Privado · Sin internet",
        title = "Todo se queda en tu teléfono",
        description = "Tus registros nunca salen del dispositivo. Funciona sin conexión. Tu información es tuya y solo tuya.",
        animationType = BertoAnimation.FLOAT
    )
)

@Composable
fun OnboardingScreen(
    nav: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGES.size })
    val scope = rememberCoroutineScope()

    val onFinish = {
        viewModel.markDone()
        nav.navigate(Routes.AUTH_CHOICE) {
            popUpTo(Routes.ONBOARDING) { inclusive = true }
        }
    }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(
                page = ONBOARDING_PAGES[pageIndex],
                isVisible = pagerState.currentPage == pageIndex
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dots animados
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(ONBOARDING_PAGES.size) { i ->
                    val width by animateDpAsState(
                        targetValue = if (i == pagerState.currentPage) 28.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "DotWidth$i"
                    )
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                Color.White.copy(alpha = if (i == pagerState.currentPage) 1f else 0.4f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            val isLastPage = pagerState.currentPage == ONBOARDING_PAGES.lastIndex

            AnimatedContent(
                targetState = isLastPage,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { it / 2 })
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "ButtonTransition"
            ) { lastPage ->
                Button(
                    onClick = {
                        if (lastPage) onFinish()
                        else scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = TealPrimary
                    )
                ) {
                    Text(
                        if (lastPage) "Comenzar ahora" else "Siguiente",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    )
                    if (!lastPage) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_right),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200))
            ) {
                TextButton(onClick = { onFinish() }) {
                    Text(
                        "Saltar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

// ── Contenido de cada página ─────────────────────────────
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isVisible: Boolean
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "ContentAlpha"
    )
    val contentSlide by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ContentSlide"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(page.backgroundColor)
    ) {
        DecorativeCircles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.08f))

            BertoAnimated(
                drawable = page.bertoDrawable,
                animationType = page.animationType,
                isVisible = isVisible,
                modifier = Modifier
                    .size(240.dp)
                    .alpha(contentAlpha)
            )

            Spacer(Modifier.weight(0.06f))

            Box(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .offset(y = contentSlide)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    page.badge,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                page.title,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .offset(y = contentSlide),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            Text(
                page.description,
                modifier = Modifier
                    .alpha(contentAlpha * 0.85f)
                    .offset(y = contentSlide),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(0.35f))
        }
    }
}

// ── Berto con animación según tipo ──────────────────────
@Composable
private fun BertoAnimated(
    @DrawableRes drawable: Int,
    animationType: BertoAnimation,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "BertoInfinite")

    val floatOffset by inf.animateFloat(
        initialValue = 0f,
        targetValue = if (animationType == BertoAnimation.FLOAT) -16f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )
    val bounceOffset by inf.animateFloat(
        initialValue = 0f,
        targetValue = if (animationType == BertoAnimation.BOUNCE) -20f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BounceOffset"
    )
    val pulseScale by inf.animateFloat(
        initialValue = 1f,
        targetValue = if (animationType == BertoAnimation.PULSE) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val waveRotation by inf.animateFloat(
        initialValue = if (animationType == BertoAnimation.WAVE) -4f else 0f,
        targetValue = if (animationType == BertoAnimation.WAVE) 4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveRotation"
    )

    val entryScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "EntryScale"
    )

    val yOffset = when (animationType) {
        BertoAnimation.FLOAT  -> floatOffset
        BertoAnimation.BOUNCE -> bounceOffset
        else                  -> 0f
    }

    Image(
        painter = painterResource(drawable),
        contentDescription = "Berto",
        modifier = modifier
            .offset(y = yOffset.dp)
            .scale(entryScale * pulseScale)
            .rotate(waveRotation),
        contentScale = ContentScale.Fit
    )
}

// ── Círculos decorativos de fondo ───────────────────────
@Composable
private fun DecorativeCircles() {
    val inf = rememberInfiniteTransition(label = "Circles")
    val alpha by inf.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "CircleAlpha"
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha))
        )
        Box(
            Modifier
                .size(200.dp)
                .offset(x = 260.dp, y = 400.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha * 0.7f))
        )
        Box(
            Modifier
                .size(120.dp)
                .offset(x = 300.dp, y = 50.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha * 0.5f))
        )
    }
}
