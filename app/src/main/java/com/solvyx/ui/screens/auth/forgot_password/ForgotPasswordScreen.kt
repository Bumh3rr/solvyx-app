package com.solvyx.ui.screens.auth.forgot_password

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextField
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.screens.auth.register.RegisterScreen
import com.solvyx.ui.theme.SolvyxappTheme

@Preview(name = "Login — Light", showSystemUi = true)
@Composable
private fun ForgotPasswordScreenPreviewLight() {
    SolvyxappTheme(darkTheme = false) {
        ForgotPasswordScreen(nav = NavHostController(LocalContext.current))
    }
}

@Preview(name = "Login — Dark", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ForgotPasswordScreenPreviewDark() {
    SolvyxappTheme(darkTheme = true) {
        ForgotPasswordScreen(nav = NavHostController(LocalContext.current))
    }
}

@Composable
fun ForgotPasswordScreen(
    nav: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── HERO ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.52f)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_decorations_hero_2_cubos),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(top = 15.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillWidth
            )

            SolvyxBackButton(
                onClick = { nav.navigateUp() },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            )

            Text(
                text = "Solvyx",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 20.dp)
                    .align(Alignment.TopCenter)
            )

            BertoPreocupadoWithHalo()
        }

        // ── PEEK LAYER ────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .fillMaxHeight(0.515f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
        )

        // ── CARD PRINCIPAL ────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.50f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 40.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Ingresa tu correo y te enviaremos un enlace para restablecerla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(32.dp))

            SolvyxTextField(
                value = viewModel.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Correo electrónico",
                leadingIconRes = R.drawable.ic_email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
            Spacer(Modifier.height(20.dp))

            SolvyxButton(
                text = "Enviar enlace de recuperación",
                onClick = { viewModel.sendRecoveryEmail { nav.navigateUp() } },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            Spacer(Modifier.height(8.dp))

            SolvyxTextButton(
                text = "Volver a iniciar sesión",
                onClick = { nav.navigateUp() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "2026 Solvyx ®",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BertoPreocupadoWithHalo() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
            .padding(top = 140.dp)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
        )
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Image(
            painter = painterResource(R.drawable.berto_preocupado),
            contentDescription = "Berto",
            modifier = Modifier
                .size(180.dp)
                .offset(y = 32.dp),
            contentScale = ContentScale.Fit
        )
    }
}
