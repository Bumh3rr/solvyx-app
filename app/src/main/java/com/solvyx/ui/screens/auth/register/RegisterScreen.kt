package com.solvyx.ui.screens.auth.register

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxTextField
import com.solvyx.ui.navigation.Routes
import com.solvyx.ui.screens.auth.choice.AuthChoiceScreen
import com.solvyx.ui.theme.SolvyxappTheme

@Preview(name = "Login — Light", showSystemUi = true)
@Composable
private fun RegisterScreenPreviewLight() {
    SolvyxappTheme(darkTheme = false) {
        RegisterScreen(nav = NavHostController(LocalContext.current))
    }
}

@Preview(name = "Login — Dark", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegisterScreenPreviewDark() {
    SolvyxappTheme(darkTheme = true) {
        RegisterScreen(nav = NavHostController(LocalContext.current))
    }
}

@Composable
fun RegisterScreen(
    nav: NavHostController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── HERO ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.29f)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_decorations_hero_1),
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

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 40.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
            }

            // ── PEEK LAYER ────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .fillMaxHeight(0.16f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            )
            Image(
                painter = painterResource(R.drawable.berto_sentado_mirando_izquieda),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = -25.dp)
                    .width(80.dp)
                    .wrapContentHeight()
            )
        }

        // ── CARD PRINCIPAL ────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 36.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Completa tus datos para comenzar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(24.dp))

            SolvyxTextField(
                value = viewModel.nickname,
                onValueChange = viewModel::onNicknameChange,
                placeholder = "Apodo",
                leadingIconRes = R.drawable.ic_person,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(12.dp))

            SolvyxTextField(
                value = viewModel.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Correo electrónico",
                leadingIconRes = R.drawable.ic_email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(12.dp))

            SolvyxTextField(
                value = viewModel.birthdate,
                onValueChange = viewModel::onBirthdateChange,
                placeholder = "Fecha de nacimiento",
                leadingIconRes = R.drawable.ic_calendar,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(12.dp))

            SolvyxTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Contraseña",
                leadingIconRes = R.drawable.ic_lock,
                isPassword = true,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(12.dp))

            SolvyxTextField(
                value = viewModel.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = "Confirmar contraseña",
                leadingIconRes = R.drawable.ic_lock,
                isPassword = true,
                imeAction = ImeAction.Done
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.acceptedTerms,
                    onCheckedChange = viewModel::onTermsChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Acepto los ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("Términos de uso")
                        }
                        append(" y las ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("Políticas de privacidad")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(20.dp))

            SolvyxButton(
                text = "Registrarme",
                onClick = {
                    viewModel.register {
                        nav.navigate(Routes.DIAGNOSTICO) {
                            popUpTo(Routes.AUTH_CHOICE) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_user),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        nav.navigate(Routes.LOGIN) {
                            popUpTo(Routes.AUTH_CHOICE) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}
