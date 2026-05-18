package com.solvyx.ui.screens.guias.screens.panico

import com.solvyx.ui.screens.guias.components.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium

@Composable
fun GuiaPanicoScreen(
    onBack: () -> Unit,
    onSos: () -> Unit
) {
    // El ViewModel se crea al mostrar esta pantalla, iniciando TTS de inmediato
    // así tiene tiempo de cargar antes de que el usuario pulse "Iniciar ejercicio"
    val viewModel: EjercicioGuiadoViewModel = hiltViewModel()
    var showEjercicio by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            GuiaTopBar(title = "Ansiedad y pánico", onBack = onBack)

            HeroSideBerto(
                mascot   = R.drawable.berto_preocupado,
                title    = "Lo que sientes es real",
                subtitle = "Pero no es permanente. Aquí estamos juntos."
            )

            GuiaPanel(Modifier.weight(1f)) {

                BorderCard {
                    CardLabel(iconRes = R.drawable.ic_info_circle, text = "¿Pánico o emergencia cardíaca?")
                    Text(
                        text = "Un ataque de pánico puede sentirse igual que un infarto. La diferencia: el pánico alcanza su pico en unos 10 minutos y luego disminuye. Si el dolor de pecho persiste o se irradia al brazo, llama al 911.",
                        fontSize   = 13.sp,
                        color      = TealDark,
                        lineHeight = 20.sp,
                        modifier   = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                BorderCard(
                    bg              = MaterialTheme.colorScheme.primaryContainer,
                    leftBorderColor = MaterialTheme.colorScheme.primary
                ) {
                    CardLabel(R.drawable.ic_zap, "Haz esto AHORA")
                    StepRow(1, "Siéntate o recuéstate en un lugar seguro.")
                    StepRow(2, "Inhala 4 segundos, exhala 6 segundos. Lento.")
                    StepRow(3, "Repite en tu mente: 'Esto va a pasar. Estoy a salvo.'")
                    StepRow(4, "No luches contra el pánico — déjalo pasar.")
                }

                Spacer(Modifier.height(12.dp))

                BorderCard {
                    CardLabel(R.drawable.ic_globe, "Técnica 5-4-3-2-1")
                    Text(
                        text     = "Ancla tu mente al presente.",
                        fontSize = 12.sp,
                        color    = TealMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    SensesRow("5", "VER",      " cosas que puedes ver ahora mismo")
                    SensesRow("4", "TOCAR",    " cosas que puedes tocar")
                    SensesRow("3", "ESCUCHAR", " cosas que puedes escuchar")
                    SensesRow("2", "OLER",     " cosas que puedes oler")
                    SensesRow("1", "SABOREAR", " cosa que puedes saborear")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            showEjercicio = true
                            viewModel.startExercise()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter     = painterResource(R.drawable.ic_wind),
                            contentDescription = null,
                            modifier    = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Iniciar ejercicio guiado",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                BorderCard {
                    CardLabel(R.drawable.ic_user_check, "Cuándo buscar ayuda profesional")
                    Text(
                        text = "Si los ataques son frecuentes o interfieren con tu vida diaria, es momento de hablar con un profesional.",
                        fontSize   = 13.sp,
                        color      = TealDark,
                        lineHeight = 20.sp,
                        modifier   = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = "Ver líneas de apoyo",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            painter     = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint        = MaterialTheme.colorScheme.primary,
                            modifier    = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick  = onSos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE24B4A)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE24B4A))
                ) {
                    Icon(
                        painter     = painterResource(R.drawable.ic_alert_triangle),
                        contentDescription = null,
                        modifier    = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text       = "Avisar a mi red de apoyo",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 15.sp
                    )
                }
            }
        }

        // Overlay del ejercicio guiado
        AnimatedVisibility(
            visible = showEjercicio,
            enter   = fadeIn(tween(350)),
            exit    = fadeOut(tween(250))
        ) {
            EjercicioGuiadoScreen(
                viewModel = viewModel,
                onFinish  = { showEjercicio = false }
            )
        }
    }
}

// ── Composables de la guía ───────────────────────────────────────────────────

@Composable
private fun SensesRow(number: String, sense: String, rest: String) {
    Row(
        modifier          = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = number,
            fontSize   = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle  = androidx.compose.ui.text.font.FontStyle.Italic,
            color      = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.padding(end = 8.dp)
        )
        Text(
            text       = buildSensesAnnotated(sense, rest),
            fontSize   = 13.sp,
            color      = TealDark,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun buildSensesAnnotated(
    bold: String,
    rest: String
): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        pushStyle(
            androidx.compose.ui.text.SpanStyle(
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        )
        append(bold)
        pop()
        append(rest)
    }
}
