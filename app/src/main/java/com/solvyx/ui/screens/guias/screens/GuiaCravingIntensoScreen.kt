package com.solvyx.ui.screens.guias.screens

import com.solvyx.ui.screens.guias.components.*
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium

@Composable
fun GuiaCravingIntensoScreen(
    onBack: () -> Unit,
    onSos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Craving muy intenso",
            onBack = onBack
        )

        HeroSideBerto(
            mascot = R.drawable.berto_preocupado,
            title = "Las ganas van a pasar.",
            subtitle = "El craving dura entre 15 y 20 minutos."
        )

        GuiaPanel(Modifier.weight(1f)) {

            // ── ¿Qué es el craving? ──────────────────────────────────────
            BorderCard(
                bg = MaterialTheme.colorScheme.primaryContainer,
                leftBorderColor = MaterialTheme.colorScheme.primary
            ) {
                CardLabel(R.drawable.ic_brain, "¿Qué es el craving?")
                Text(
                    text = "El craving es un deseo muy intenso y urgente de consumir. Es una respuesta del cerebro que suele durar entre 15 y 20 minutos. Pasará, aunque ahora mismo se sienta imposible de aguantar.",
                    fontSize = 13.sp,
                    color = TealDark,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Plan de acción inmediata ──────────────────────────────────
            BorderCard {
                CardLabel(R.drawable.ic_zap, "Plan de acción inmediata")
                StepRow(1, "Ponle nombre: 'Esto que siento es craving.'")
                StepRow(2, "Cambia de ambiente: sal del cuarto, muévete.")
                StepRow(3, "Toma agua y come algo si puedes.")
                StepRow(4, "Contacta a alguien de tu red de apoyo.")
            }

            Spacer(Modifier.height(12.dp))

            // ── Si decides consumir ────────────────────────────────────────
            BorderCard(leftBorderColor = MaterialTheme.colorScheme.secondary) {
                CardLabel(
                    iconRes = R.drawable.ic_shield,
                    text = "Si decides consumir",
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Sin juicios. Si vas a consumir, hazlo de la manera más segura posible.",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = TealMedium,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                )
                SafetyRow("No consumas en soledad")
                SafetyRow("No mezcles sustancias")
                SafetyRow("Toma agua y come antes")
                SafetyRow("Identifica un lugar seguro donde haya alguien de confianza")
            }

            Spacer(Modifier.height(12.dp))

            // ── Botón SOS ghost ────────────────────────────────────────────
            OutlinedButton(
                onClick = onSos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFFE24B4A)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE24B4A)
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_alert_triangle),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Avisar a mi red de apoyo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SafetyRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(TealLightest)
                .border(1.dp, TealLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shield),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(11.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(text = text, fontSize = 13.sp, color = TealDark, lineHeight = 18.sp)
    }
}
