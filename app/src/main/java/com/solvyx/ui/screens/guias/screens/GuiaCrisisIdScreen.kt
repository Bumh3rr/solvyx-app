package com.solvyx.ui.screens.guias.screens

import com.solvyx.ui.screens.guias.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium

@Composable
fun GuiaCrisisIdScreen(
    onBack: () -> Unit,
    onSos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Cómo sé si estoy en crisis",
            onBack = onBack
        )

        HeroSideBerto(
            mascot = R.drawable.berto_preocupado,
            title = "Reconoce lo que sientes",
            subtitle = "Es el primer paso para pedir ayuda."
        )

        GuiaPanel(Modifier.weight(1f)) {

            Text(
                text = "Una crisis no siempre llega de golpe. A veces es un conjunto de señales que tu cuerpo, tus emociones y tus conductas mandan al mismo tiempo. Reconocerlas es el primer paso.",
                fontSize = 14.sp,
                color = TealDark,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(14.dp))

            BorderCard(
                bg = MaterialTheme.colorScheme.primaryContainer,
                leftBorderColor = MaterialTheme.colorScheme.primary
            ) {
                CardLabel(R.drawable.ic_heart_pulse, "Señales físicas")
                DotRow(text = "Corazón acelerado o palpitaciones")
                DotRow(text = "Sudoración sin calor")
                DotRow(text = "Temblores en manos o cuerpo")
                DotRow(text = "Sensación de ahogo o falta de aire")
                DotRow(text = "Mareo o náuseas")
                DotRow(text = "Entumecimiento en manos o cara")
            }

            Spacer(Modifier.height(12.dp))

            BorderCard(
                bg = Color(0xFFfef9c3),
                leftBorderColor = Color(0xFFd97706)
            ) {
                CardLabel(
                    iconRes = R.drawable.ic_mood_sad,
                    text = "Señales emocionales",
                    color = Color(0xFFd97706)
                )
                DotRow(color = Color(0xFFd97706), text = "Miedo intenso sin razón clara")
                DotRow(color = Color(0xFFd97706), text = "Sensación de que algo malo pasará")
                DotRow(color = Color(0xFFd97706), text = "Pensamientos de hacerte daño")
                DotRow(color = Color(0xFFd97706), text = "Sentirte atrapado/a sin salida")
                DotRow(color = Color(0xFFd97706), text = "Llanto sin poder parar")
            }

            Spacer(Modifier.height(12.dp))

            BorderCard(leftBorderColor = MaterialTheme.colorScheme.secondary) {
                CardLabel(R.drawable.ic_activity, "Señales conductuales")
                DotRow(text = "Querer consumir aunque no quieras")
                DotRow(text = "Aislarte de las personas cercanas")
                DotRow(text = "Dejar de responder mensajes o llamadas")
                DotRow(text = "No poder quedarte quieto/a")
            }

            Spacer(Modifier.height(12.dp))

            BorderCard(
                bg = Color(0xFFfde8e8),
                leftBorderColor = Color(0xFFE24B4A)
            ) {
                CardLabel(
                    iconRes = R.drawable.ic_alert_octagon,
                    text = "Cuándo llamar al 911",
                    color = Color(0xFFE24B4A)
                )
                Text(
                    text = "Si ves alguna de estas señales, llama al 911 de inmediato:",
                    fontSize = 12.sp,
                    color = Color(0xFF991b1b),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Pérdida de consciencia")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Convulsiones")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Dificultad severa para respirar")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Labios o dedos azulados")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Temperatura corporal extrema")
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onSos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE24B4A),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_alert_triangle),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Avisar a mi red de apoyo ahora",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Se enviará un SMS a tus contactos de confianza",
                fontSize = 11.sp,
                color = TealMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
