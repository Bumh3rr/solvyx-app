package com.solvyx.ui.screens.guias.screens.hub

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium

@Composable
fun GuiasHubScreen(
    onOpenDrawer: () -> Unit,
    onOpenGuide: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Primeros Auxilios",
            onBack = onOpenDrawer,
            showBertoBadge = true,
            isMenuButton = true
        )

        // ── Hero offline banner ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 44.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TealDark)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_wifi_off),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Funciona sin internet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Todo está guardado en tu teléfono",
                        fontSize = 12.sp,
                        color = TealLight.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // ── Panel blanco ──────────────────────────────────────────────────
        GuiaPanel(Modifier.weight(1f)) {
            Text(
                text = "¿Qué necesitas en este momento?",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TealDark
            )
            Text(
                text = "Toca una guía para leerla completa",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TealMedium
            )

            Spacer(Modifier.height(14.dp))
            GuideCard(
                id = "crisis",
                iconRes = R.drawable.ic_sos,
                iconColor = Color(0xFFE24B4A),
                title = "Estoy en crisis ahora mismo",
                subtitle = "Para cuando eres tú quien necesita ayuda urgente",
                bg = Color(0xFFfde8e8),
                titleColor = Color(0xFF991b1b),
                onOpenGuide = onOpenGuide
            )
            Spacer(Modifier.height(10.dp))
            GuideCard(
                id = "panic",
                iconRes = R.drawable.ic_brain,
                iconColor = MaterialTheme.colorScheme.primary,
                title = "Ansiedad y ataque de pánico",
                subtitle = "Cómo calmarte cuando sientes que pierdes el control",
                bg = null,
                titleColor = null,
                onOpenGuide = onOpenGuide
            )
            Spacer(Modifier.height(10.dp))
            GuideCard(
                id = "craving",
                iconRes = R.drawable.ic_flame_2,
                iconColor = TealMedium,
                title = "Craving muy intenso",
                subtitle = "Qué hacer cuando las ganas de consumir son muy fuertes",
                bg = null,
                titleColor = null,
                onOpenGuide = onOpenGuide
            )
            Spacer(Modifier.height(10.dp))
            GuideCard(
                id = "overuse",
                iconRes = R.drawable.ic_alert_triangle,
                iconColor = Color(0xFFd97706),
                title = "Consumí de más",
                subtitle = "Señales de alerta y cómo cuidarte ahora mismo",
                bg = Color(0xFFfffbeb),
                titleColor = Color(0xFF991b1b),
                onOpenGuide = onOpenGuide
            )
            Spacer(Modifier.height(10.dp))
            GuideCard(
                id = "crisisId",
                iconRes = R.drawable.ic_info,
                iconColor = MaterialTheme.colorScheme.primary,
                title = "Cómo sé si estoy en crisis",
                subtitle = "Señales físicas, emocionales y qué hacer",
                bg = null,
                titleColor = null,
                onOpenGuide = onOpenGuide
            )

            Spacer(Modifier.height(16.dp))

            // ── Líneas de apoyo ───────────────────────────────────────────
            BorderCard(radius = 16.dp, pad = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Líneas de apoyo en México · Gratuitas 24/7",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealDark
                    )
                }
                Spacer(Modifier.height(8.dp))
                HelpLineRow("Línea de la Vida", "8009112000")
                HelpLineRow("SAPTEL", "5552598121")
                HorizontalDivider(
                    color = TealLightest,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CIJ — Ver centro más cercano",
                        fontSize = 13.sp,
                        color = TealDark,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideCard(
    id: String,
    iconRes: Int,
    iconColor: Color,
    title: String,
    subtitle: String,
    bg: Color?,
    titleColor: Color?,
    onOpenGuide: (String) -> Unit
) {
    val cardBg = bg ?: MaterialTheme.colorScheme.surfaceDim
    val titleC = titleColor ?: TealDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(0.5.dp, TealLight, RoundedCornerShape(16.dp))
            .clickable { onOpenGuide(id) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TealLightest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = titleC
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TealMedium,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TealLight,
            modifier = Modifier.size(16.dp)
        )
    }
}
