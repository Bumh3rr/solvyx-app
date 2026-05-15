package com.solvyx.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.navigation.Routes
import com.solvyx.ui.theme.TealDark

@Composable
fun AuthChoiceScreen(nav: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ── Sección superior: hero teal ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f)
        ) {
            Image(
                painter = painterResource(R.drawable.background_choice_auth),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = painterResource(R.drawable.berto_saludando),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .aspectRatio(1f)
                )
            }
        }

        // ── Sección inferior: card blanca ────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Cómo quieres entrar?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Elige la opción para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
            SolvyxButton(
                text = "Iniciar Sesión",
                onClick = { nav.navigate(Routes.LOGIN) },
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
            Spacer(Modifier.weight(1f))
            Text(
                text = buildAnnotatedString {
                    append("Al continuar aceptas nuestros ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TealDark)) {
                        append("Términos de uso")
                    }
                    append(" y ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TealDark)) {
                        append("Política de privacidad")
                    }
                    append(".")
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
