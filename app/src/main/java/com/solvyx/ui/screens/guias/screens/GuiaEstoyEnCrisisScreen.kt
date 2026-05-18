package com.solvyx.ui.screens.guias.screens

import com.solvyx.ui.screens.guias.components.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealMedium

@Composable
fun GuiaEstoyEnCrisisScreen(
    onBack: () -> Unit,
    onSos: () -> Unit,
    onHablarConBerto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark)
    ) {
        GuiaTopBar(
            title = "Estoy en crisis",
            onBack = onBack,
            useDarkBg = true
        )

        // ── Hero oscuro con speech bubble ─────────────────────────────────
        CrisisHero()

        GuiaPanel(Modifier.weight(1f)) {

            // ── Pasos inmediatos ─────────────────────────────────────────
            BorderCard(
                bg = MaterialTheme.colorScheme.primaryContainer,
                leftBorderColor = MaterialTheme.colorScheme.primary,
                radius = 14.dp,
                pad = 16.dp
            ) {
                CardLabel(R.drawable.ic_zap, "Haz esto ahora mismo", size = 15)
                StepRow(1, "Pon el teléfono en un lugar seguro y siéntate.", textSize = 14)
                StepRow(2, "Toma 3 respiraciones lentas. Inhala por la nariz, exhala por la boca.", textSize = 14)
                StepRow(3, "Dite a ti mismo/a: 'Este momento va a pasar.'", textSize = 14)
                StepRow(4, "Avisa a alguien de tu red de apoyo.", textSize = 14)
            }

            Spacer(Modifier.height(16.dp))

            // ── Botón SOS PROMINENTE ──────────────────────────────────────
            Button(
                onClick = onSos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
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

            Spacer(Modifier.height(12.dp))

            // ── Card: hablar con Berto ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickable { onHablarConBerto() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.berto_saludando),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "¿Quieres hablar con Berto?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealDark
                    )
                    Text(
                        text = "Está disponible ahora, sin internet.",
                        fontSize = 12.sp,
                        color = TealMedium
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TealLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Lo que sientes tiene nombre ───────────────────────────────
            BorderCard(radius = 16.dp, pad = 16.dp) {
                Text(
                    text = "Lo que sientes tiene nombre",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealDark
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "La crisis no dura para siempre, aunque en este momento se sienta así. Lo que sientes es real y tiene solución. No tienes que resolverlo solo/a.",
                    fontSize = 13.sp,
                    color = TealDark,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Líneas de apoyo ───────────────────────────────────────────
            BorderCard(radius = 14.dp, pad = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Líneas de apoyo gratuitas 24/7",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealDark
                    )
                }
                Spacer(Modifier.height(4.dp))
                HelpLineRow("Línea de la Vida", "8009112000")
                HelpLineRow("SAPTEL", "5552598121")
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Solvyx no reemplaza atención profesional.\nEs un puente para que llegues a quien puede ayudarte.",
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = TealMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CrisisHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TealDark)
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.berto_preocupado),
            contentDescription = null,
            modifier = Modifier.size(92.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(12.dp))

        Box(contentAlignment = Alignment.TopCenter) {
            // Triangle pointing up
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .offset(y = (-6).dp)
                    .rotate(45f)
                    .background(Color.White)
            )

            // Speech bubble
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "\"Que estés leyendo esto ya es un acto de valentía.\nRespira. Aquí estamos contigo.\"",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = TealDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
