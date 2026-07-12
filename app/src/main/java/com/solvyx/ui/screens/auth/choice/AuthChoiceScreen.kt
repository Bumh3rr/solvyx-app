package com.solvyx.ui.screens.auth.choice

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.navigation.Routes
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

/**
 * Pantalla de bienvenida. El registro es OBLIGATORIO:
 * - "Crear cuenta" es el botón principal y único punto de entrada.
 * - "Iniciar sesión" se ofrece como enlace secundario, ya que
 *   Solvyx es 100% local y no hay backend de auth: si el usuario
 *   inicia sesión con credenciales guardadas localmente, sigue
 *   siendo su cuenta en este dispositivo.
 */
@Composable
fun AuthChoiceScreen(nav: NavHostController) {

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
                .padding(top = 28.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Tus datos viven solo en este teléfono.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Botón principal: Crear cuenta ──────────────
            SolvyxButton(
                text = "Crear cuenta",
                onClick = { nav.navigate(Routes.REGISTER) },
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

            Spacer(Modifier.height(8.dp))

            // ── Enlace secundario: Ya tienes cuenta ─────────
            TextButton(
                onClick = { nav.navigate(Routes.LOGIN) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Compromiso de privacidad ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "100% privado. Sin servidor. Tú controlas tus datos.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 14.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Términos
            Text(
                text = buildAnnotatedString {
                    append("Al continuar aceptas nuestros ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Términos de uso")
                    }
                    append(" y ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Política de privacidad")
                    }
                    append(".")
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
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
