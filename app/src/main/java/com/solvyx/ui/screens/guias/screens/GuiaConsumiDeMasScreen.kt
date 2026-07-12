package com.solvyx.ui.screens.guias.screens

import com.solvyx.ui.screens.guias.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium

private val TABS = listOf("Alcohol", "Cristal", "Vape", "Tabaco")

private val SENALES: Map<String, List<String>> = mapOf(
    "Alcohol" to listOf(
        "Vómito que no para",
        "Confusión o desorientación",
        "Respiración lenta o irregular",
        "Piel fría o pálida",
        "No puedes mantenerte despierto/a"
    ),
    "Cristal" to listOf(
        "Corazón muy acelerado",
        "Sensación de calor extremo",
        "Paranoia o alucinaciones",
        "Temblores fuertes",
        "No puedes dormir desde hace más de 24h"
    ),
    "Vape" to listOf(
        "Tos persistente",
        "Falta de aire o dolor en el pecho",
        "Mareo o náuseas",
        "Pulso acelerado",
        "Sensación de adormecimiento en boca o garganta"
    ),
    "Tabaco" to listOf(
        "Náuseas o vómito intenso",
        "Ansiedad o pánico fuerte",
        "Sensación de despersonalización",
        "Pulso muy acelerado",
        "No puedes coordinar movimientos"
    )
)

@Composable
fun GuiaConsumiDeMasScreen(
    onBack: () -> Unit,
    onSos: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Alcohol") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark)
    ) {
        GuiaTopBar(
            title = "Consumí de más",
            onBack = onBack,
            useDarkBg = true
        )

        HeroSideBerto(
            mascot = R.drawable.berto_preocupado,
            title = "Que estés leyendo esto\nsignifica que te importas.",
            subtitle = "Eso importa mucho.",
            darkBg = true
        )

        GuiaPanel(Modifier.weight(1f)) {

            Text(
                text = "Pasó. Lo más importante ahora es que estés bien. No hay juicios aquí — solo información para cuidarte.",
                fontSize = 14.sp,
                color = TealDark,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(14.dp))

            // ── Tabs ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TABS.forEach { tab ->
                    val isActive = tab == activeTab
                    Text(
                        text = tab,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) Color.White else TealDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceDim
                            )
                            .then(
                                if (!isActive) Modifier.padding(1.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                else Modifier
                            )
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Señales de alerta para el tab activo ─────────────────────
            BorderCard(
                bg = Color(0xFFfef9c3),
                leftBorderColor = Color(0xFFd97706)
            ) {
                CardLabel(
                    iconRes = R.drawable.ic_alert_triangle,
                    text = "Cómo me siento — Señales de alerta",
                    color = Color(0xFFd97706)
                )
                SENALES[activeTab]?.forEach { señal ->
                    DotRow(color = Color(0xFFd97706), text = señal)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Cuídate ahora mismo ───────────────────────────────────────
            BorderCard(
                bg = MaterialTheme.colorScheme.primaryContainer,
                leftBorderColor = MaterialTheme.colorScheme.primary
            ) {
                CardLabel(R.drawable.ic_heart, "Cuídate ahora mismo")
                StepRow(1, "No te quedes en soledad. Avisa a alguien de confianza.")
                StepRow(2, "Si tienes náuseas, acuéstate de lado.")
                StepRow(3, "Toma agua a sorbos pequeños.")
                StepRow(4, "No tomes medicamentos sin saber qué consumiste.")
                StepRow(5, "No te duermas sin que alguien esté contigo.")
            }

            Spacer(Modifier.height(12.dp))

            // ── Cuándo llamar al 911 ──────────────────────────────────────
            BorderCard(
                bg = Color(0xFFfde8e8),
                leftBorderColor = Color(0xFFE24B4A)
            ) {
                CardLabel(
                    iconRes = R.drawable.ic_alert_octagon,
                    text = "Cuándo llamar al 911 ahora mismo",
                    color = Color(0xFFE24B4A)
                )
                Text(
                    text = "Si ves alguna de estas señales, no esperes:",
                    fontSize = 12.sp,
                    color = Color(0xFF991b1b),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Pérdida de consciencia")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Convulsiones")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Dificultad para respirar")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Labios o dedos azulados")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "Temperatura corporal muy alta")
                DotRow(color = Color(0xFFE24B4A), textColor = Color(0xFF991b1b), text = "No responde cuando le hablas")
            }

            Spacer(Modifier.height(20.dp))

            // ── Botón SOS ──────────────────────────────────────────────────
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
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 0.dp)
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "Avisar a mi red de apoyo ahora",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Líneas de apoyo ───────────────────────────────────────────
            BorderCard(radius = 14.dp, pad = 16.dp) {
                Text(
                    text = "Líneas de apoyo gratuitas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealDark,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                HelpLineRow("Línea de la Vida", "8009112000")
                HelpLineRow("SAPTEL", "5552598121")
            }
        }
    }
}
