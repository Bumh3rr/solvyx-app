package com.solvyx.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.screens.guias.components.CardLabel
import com.solvyx.ui.screens.guias.components.DotRow
import com.solvyx.ui.screens.guias.components.GuiaPanel
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.HeroSideBerto
import com.solvyx.ui.theme.TealDark

@Composable
fun InfoSustanciaScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Alcohol", "Cristal", "Vape", "Tabaco")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Info por sustancia",
            onBack = onBack,
            showBertoBadge = false
        )

        HeroSideBerto(
            mascot = R.drawable.berto_tranquilo,
            title = "Información",
            subtitle = "Conoce cómo actúa cada sustancia"
        )

        GuiaPanel(modifier = Modifier.weight(1f)) {

            // ── Tab row ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    tabs.forEachIndexed { i, tab ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (i == selectedTab) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { selectedTab = i }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (i == selectedTab) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Content sections per tab ──────────────────────────────────
            when (selectedTab) {
                0 -> AlcoholContent()
                1 -> CristalContent()
                2 -> VapeContent()
                3 -> TabacoContent()
            }
        }
    }
}

// ── Alcohol ───────────────────────────────────────────────────────────────────

@Composable
private fun AlcoholContent() {
    BorderCard {
        CardLabel(iconRes = R.drawable.ic_heart_pulse, text = "Qué le hace a tu cuerpo")
        DotRow(text = "Afecta el hígado: el consumo frecuente puede causar hígado graso, hepatitis y cirrosis.")
        DotRow(text = "Altera el cerebro: deteriora la memoria, el juicio y la coordinación motriz.")
        DotRow(text = "Daña el sistema cardiovascular: aumenta el riesgo de presión alta y problemas del corazón.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_brain, text = "Por qué genera dependencia")
        DotRow(text = "Eleva la dopamina en el cerebro, creando una sensación placentera que el cuerpo quiere repetir.")
        DotRow(text = "La abstinencia provoca ansiedad, temblores e irritabilidad, lo que impulsa a consumir más.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_alert_triangle, text = "Señales de alerta")
        DotRow(text = "Necesitar beber para funcionar con normalidad o calmarse.")
        DotRow(text = "Aumentar la cantidad sin darse cuenta y no poder parar.")
        DotRow(text = "Descuidar responsabilidades o relaciones por el consumo.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_flag, text = "Reducción de daños")
        DotRow(text = "Nunca consumas con el estómago vacío y alterna con agua entre bebidas.")
        DotRow(text = "Evita mezclar alcohol con otros depresores como tranquilizantes o somníferos.")
        DotRow(text = "Establece un límite antes de empezar y comparte tu plan con alguien de confianza.")
    }
}

// ── Cristal ───────────────────────────────────────────────────────────────────

@Composable
private fun CristalContent() {
    BorderCard {
        CardLabel(iconRes = R.drawable.ic_heart_pulse, text = "Qué le hace a tu cuerpo")
        DotRow(text = "Acelera el sistema nervioso central: aumenta la presión arterial, la frecuencia cardíaca y la temperatura corporal.")
        DotRow(text = "Deteriora el cerebro: daña neuronas dopaminérgicas, afectando el placer, el movimiento y el aprendizaje.")
        DotRow(text = "Provoca insomnio severo, pérdida de peso y envejecimiento prematuro de la piel y los dientes.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_brain, text = "Por qué genera dependencia")
        DotRow(text = "Libera una cantidad masiva de dopamina, mucho mayor que el alcohol o el tabaco, produciendo euforia intensa.")
        DotRow(text = "El cerebro deja de producir dopamina por sí mismo, haciendo que sin cristal sea imposible sentir placer.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_alert_triangle, text = "Señales de alerta")
        DotRow(text = "Paranoia, alucinaciones o comportamientos erráticos durante o después del consumo.")
        DotRow(text = "Perderse días enteros ('benders') sin dormir ni comer.")
        DotRow(text = "Daño dentario severo ('meth mouth') o lesiones en la piel por rascado compulsivo.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_flag, text = "Reducción de daños")
        DotRow(text = "No consumas solo: ten siempre a alguien de confianza cerca.")
        DotRow(text = "Hidratación es crítica: la metanfetamina causa deshidratación severa, bebe agua frecuentemente.")
        DotRow(text = "Espacía los consumos tanto como puedas para permitir que el cerebro se recupere parcialmente.")
    }
}

// ── Vape ──────────────────────────────────────────────────────────────────────

@Composable
private fun VapeContent() {
    BorderCard {
        CardLabel(iconRes = R.drawable.ic_heart_pulse, text = "Qué le hace a tu cuerpo")
        DotRow(text = "Irrita y daña el tejido pulmonar: los aerosoles contienen partículas finas y metales pesados.")
        DotRow(text = "Entrega nicotina de forma muy eficiente, generando dependencia tan rápido como los cigarros.")
        DotRow(text = "En personas jóvenes, interfiere con el desarrollo del cerebro en áreas de atención y control de impulsos.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_brain, text = "Por qué genera dependencia")
        DotRow(text = "La nicotina activa receptores de acetilcolina y libera dopamina, creando un ciclo de recompensa rápido.")
        DotRow(text = "Los vapes de alta concentración (sales de nicotina) generan dependencia más rápido que los cigarros convencionales.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_alert_triangle, text = "Señales de alerta")
        DotRow(text = "Sentir ansiedad, irritabilidad o dificultad para concentrarse sin vapear.")
        DotRow(text = "Vapear en lugares o situaciones donde antes no lo hacías (trabajo, escuela, en la cama).")
        DotRow(text = "Tos crónica, dificultad para respirar o sensación de presión en el pecho.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_flag, text = "Reducción de daños")
        DotRow(text = "Si vas a vapear, evita los productos con sabores artificiales que pueden contener compuestos más irritantes.")
        DotRow(text = "Reduce la concentración de nicotina gradualmente para facilitar la eventual cesación.")
        DotRow(text = "Nunca uses vapes modificados o de procedencia desconocida; el riesgo de daño pulmonar agudo aumenta.")
    }
}

// ── Tabaco ────────────────────────────────────────────────────────────────────

@Composable
private fun TabacoContent() {
    BorderCard {
        CardLabel(iconRes = R.drawable.ic_heart_pulse, text = "Qué le hace a tu cuerpo")
        DotRow(text = "Daño pulmonar progresivo: el alquitrán destruye los alvéolos, reduciendo la capacidad respiratoria con el tiempo.")
        DotRow(text = "Aumenta significativamente el riesgo de enfermedades cardiovasculares, infartos y accidentes cerebrovasculares.")
        DotRow(text = "El monóxido de carbono reduce el oxígeno en sangre, causando fatiga crónica y disminución del rendimiento físico.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_brain, text = "Por qué genera dependencia")
        DotRow(text = "La nicotina llega al cerebro en segundos, liberando dopamina y creando un refuerzo inmediato muy potente.")
        DotRow(text = "La abstinencia causa irritabilidad, dificultad para concentrarse y ansiedad intensa, lo que dificulta dejarlo.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_alert_triangle, text = "Señales de alerta")
        DotRow(text = "Fumar el primer cigarro dentro de los 30 minutos de despertar, señal de dependencia severa a la nicotina.")
        DotRow(text = "Intentos previos de dejar de fumar fallidos por síntomas de abstinencia.")
        DotRow(text = "Tos matutina persistente con flema o episodios frecuentes de bronquitis.")
    }

    Spacer(Modifier.height(12.dp))

    BorderCard {
        CardLabel(iconRes = R.drawable.ic_flag, text = "Reducción de daños")
        DotRow(text = "Evita fumar en interiores o cerca de otras personas, especialmente niños y embarazadas.")
        DotRow(text = "Reduce gradualmente la cantidad diaria de cigarros antes de intentar dejar completamente.")
        DotRow(text = "Los sustitutos de nicotina (parches, chicles) pueden reducir síntomas de abstinencia si planeas dejar de fumar.")
    }
}
