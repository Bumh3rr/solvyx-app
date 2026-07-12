package com.solvyx.ui.screens.auth.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.PageIndicator
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.navigation.Routes
import kotlinx.coroutines.launch

private data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        imageRes = R.drawable.berto_saludando,
        title = "¡Hola, soy Berto!",
        description = "Tu compañero de bolsillo. Estoy aquí para escucharte sin juzgarte, a cualquier hora, aunque no tengas internet."
    ),
    OnboardingPage(
        imageRes = R.drawable.berto_tranquilo,
        title = "Tu espacio seguro",
        description = "Crea tu cuenta para guardar tu progreso. Tu información está protegida y nunca la compartimos con nadie."
    ),
    OnboardingPage(
        imageRes = R.drawable.berto_mira_moriposa,
        title = "Siempre aquí para ti",
        description = "Habla con Berto cuando algo no esté bien, registra cómo te sientes y activa ayuda si la necesites."
    ),
    OnboardingPage(
        imageRes = R.drawable.berto_sin_internet,
        title = "Sin internet, sin problema",
        description = "Solvyx funciona aunque no tengas datos ni WiFi. Berto siempre está disponible."
    )
)

@Composable
fun OnboardingScreen(
    nav: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val onFinish = {
        viewModel.markDone()
        nav.navigate(Routes.LOGIN) {
            popUpTo(Routes.ONBOARDING) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(16.dp)
        )

        OnboardingButtons(
            pagerState = pagerState,
            pageCount = pages.size,
            onFinish = onFinish,
            onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
            onSkip = { scope.launch { pagerState.animateScrollToPage(pages.size - 1) } }
        )
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .aspectRatio(1f)
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = page.title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = page.description,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun OnboardingButtons(
    pagerState: PagerState,
    pageCount: Int,
    onFinish: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pagerState.currentPage < pageCount - 1) {
            SolvyxTextButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = onSkip
            )
            Spacer(Modifier.width(12.dp))
            SolvyxButton(
                text = stringResource(R.string.onboarding_next),
                onClick = onNext,
                modifier = Modifier.weight(1f)
            )
        } else {
            SolvyxButton(
                text = stringResource(R.string.onboarding_start),
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}