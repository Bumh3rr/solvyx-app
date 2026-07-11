package com.solvyx.ui.screens.auth.choice

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.navigation.Routes
import com.solvyx.ui.navigation.aRuta
import com.solvyx.ui.theme.SolvyxappTheme

@Preview(name = "Login — Light", showSystemUi = true)
@Composable
private fun AuthChoiceScreenPreviewLight() {
    SolvyxappTheme(darkTheme = false) {
        AuthChoiceScreen(nav = NavHostController(LocalContext.current))
    }
}

@Preview(name = "Login — Dark", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthChoiceScreenPreviewDark() {
    SolvyxappTheme(darkTheme = true) {
        AuthChoiceScreen(nav = NavHostController(LocalContext.current))
    }
}

@Composable
fun AuthChoiceScreen(
    nav: NavHostController,
    viewModel: AuthChoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.destino) {
        uiState.destino?.let { destino ->
            nav.navigate(destino.aRuta()) {
                popUpTo(Routes.AUTH_CHOICE) { inclusive = true }
            }
        }
    }

    if (uiState.mostrarSheet) {
        AnonimoConfirmSheet(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onEntrarSinCuenta = { viewModel.entrarComoAnonimo() },
            onCrearCuenta = {
                viewModel.cerrarSheet()
                nav.navigate(Routes.REGISTER)
            },
            onDismiss = { viewModel.cerrarSheet() }
        )
    }

    // ── ROOT: Box permite overlap entre hero y card ──────
    Box(modifier = Modifier.fillMaxSize()) {

        // ── HERO: ocupa 62% superior de la pantalla ──────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            // Formas decorativas de fondo (cuadros y círculos)
            Image(
                painter = painterResource(R.drawable.ic_decorations_hero_1),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(top = 15.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillWidth,
                alpha = 1f
            )

            // Contenido del hero centrado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp, bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Wordmark
                Text(
                    text = "Solvyx",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Tu mente, tu red, tu libertad",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Berto con halo concéntrico
                BertoWithHalo()
            }
        }

        // Capa semitransparente que "sobresale"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .fillMaxHeight(0.475f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Cómo quieres entrar?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Elige la opción para continuar",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            SolvyxButton(
                text = "Iniciar Sesión",
                onClick = { nav.navigate(Routes.LOGIN) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_login),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            SolvyxOutlinedButton(
                text = "Crear cuenta",
                onClick = { nav.navigate(Routes.REGISTER) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_user),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            SolvyxTextButton(
                text = "Entrar sin cuenta",
                onClick = { viewModel.abrirSheet() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            // Términos
            val terminosPrivacidadTexto = buildAnnotatedString {
                append("Al continuar aceptas nuestros ")
                pushStringAnnotation(tag = "terminos", annotation = Routes.TERMINOS)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("Términos de uso")
                }
                pop()
                append(" y ")
                pushStringAnnotation(tag = "privacidad", annotation = Routes.PRIVACIDAD)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("Política de privacidad")
                }
                pop()
                append(".")
            }
            ClickableText(
                text = terminosPrivacidadTexto,
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { offset ->
                    terminosPrivacidadTexto.getStringAnnotations(tag = "terminos", start = offset, end = offset)
                        .firstOrNull()?.let { nav.navigate(it.item) }
                    terminosPrivacidadTexto.getStringAnnotations(tag = "privacidad", start = offset, end = offset)
                        .firstOrNull()?.let { nav.navigate(it.item) }
                }
            )
        }
    }
}

@Composable
private fun BertoWithHalo() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(350.dp)
    ) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        )
        Box(
            modifier = Modifier
                .size(168.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Image(
            painter = painterResource(R.drawable.berto_saludando),
            contentDescription = "Berto",
            modifier = Modifier.size(230.dp),
            contentScale = ContentScale.Fit
        )
    }
}
