package com.solvyx.ui.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextField
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.navigation.Routes
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TextMuted

@Composable
fun LoginScreen(
    nav: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight
        val heroH = screenH * 0.40f

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Sección superior: hero teal ──────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroH)
            ) {
                Image(
                    painter = painterResource(R.drawable.background_login),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Botón volver
                SolvyxBackButton(
                    onClick = { nav.navigateUp() },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 12.dp, start = 16.dp)
                )

                // Título y subtítulo
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Solvyx",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Tu mente, tu red, tu libertad",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Berto peeking desde la derecha (offset extiende hacia la card)
                Image(
                    painter = painterResource(R.drawable.berto_tranquilo),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = 36.dp)
                        .width(heroH * 0.52f)
                        .wrapContentHeight()
                )
            }

            // ── Sección inferior: card blanca ────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido de nuevo",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Ingresa tus datos a continuación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(Modifier.height(28.dp))

                SolvyxTextField(
                    value = viewModel.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "Correo electrónico",
                    leadingIconRes = R.drawable.ic_email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(14.dp))

                SolvyxTextField(
                    value = viewModel.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Contraseña",
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    imeAction = ImeAction.Done
                )
                Spacer(Modifier.height(20.dp))

                SolvyxButton(
                    text = "Iniciar sesión",
                    onClick = { viewModel.login { nav.navigate(Routes.HOME) } },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_login),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))

                SolvyxTextButton(
                    text = "¿Olvidaste tu contraseña?",
                    onClick = { /* TODO: forgot password */ },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "2026 Solvyx ®",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
