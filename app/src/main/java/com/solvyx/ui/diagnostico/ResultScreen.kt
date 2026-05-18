package com.solvyx.ui.diagnostico

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.backend.models.ResultadoDiagnostico
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLightest

@Composable
fun ResultScreen(
    viewModel: DiagnosticoViewModel,
    onReiniciar: () -> Unit,
    onVerHistorial: () -> Unit,
    onFinish: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToBitacora: () -> Unit = {},
    onNavigateToAvances: () -> Unit = {},
    onNavigateToMisMetas: () -> Unit = {},
    onNavigateToMisDetonantes: () -> Unit = {},
    onNavigateToDirectorio: () -> Unit = {},
    onNavigateToRedApoyo: () -> Unit = {}
) {
    val resultados by viewModel.resultados.collectAsState()

    val peorNivel = when {
        resultados.any { it.nivel == NivelRiesgo.ALTO }     -> NivelRiesgo.ALTO
        resultados.any { it.nivel == NivelRiesgo.MODERADO } -> NivelRiesgo.MODERADO
        else                                                  -> NivelRiesgo.BAJO
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── TOP BAR ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tu diagnóstico ASSIST",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }

        // ── BERTO + MENSAJE ───────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bertoDrawable = when (peorNivel) {
                NivelRiesgo.BAJO -> R.drawable.berto_saludando
                else             -> R.drawable.berto_preocupado
            }
            Image(
                painter = painterResource(bertoDrawable),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .padding(12.dp)
            ) {
                val mensaje = when (peorNivel) {
                    NivelRiesgo.BAJO     -> "Estos son tus resultados. Recuerda que siempre puedes hablar conmigo."
                    NivelRiesgo.MODERADO -> "Gracias por tu honestidad. Hay recursos que pueden ayudarte."
                    NivelRiesgo.ALTO     -> "Eres valiente por hacer esto. Te recomiendo buscar apoyo profesional."
                }
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

        // ── LISTA DE RESULTADOS ───────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(resultados) { resultado ->
                ResultadoCard(resultado)
            }
            item {
                AccionesSugeridas(
                    nivel = peorNivel,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToBitacora = onNavigateToBitacora,
                    onNavigateToAvances = onNavigateToAvances,
                    onNavigateToMisMetas = onNavigateToMisMetas,
                    onNavigateToMisDetonantes = onNavigateToMisDetonantes,
                    onNavigateToDirectorio = onNavigateToDirectorio,
                    onNavigateToRedApoyo = onNavigateToRedApoyo
                )
            }
        }

        // ── BOTTOM ACTIONS ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SolvyxButton(
                text = "Continuar",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SolvyxOutlinedButton(
                    text = "Evaluar de nuevo",
                    onClick = onReiniciar,
                    modifier = Modifier.weight(1f)
                )
                SolvyxOutlinedButton(
                    text = "Ver historial",
                    onClick = onVerHistorial,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ResultadoCard(resultado: ResultadoDiagnostico) {
    val nombreSustancia = when (resultado.sustanciaId) {
        "cigarro" -> "Tabaco"
        else      -> resultado.sustanciaId.replaceFirstChar { it.uppercase() }
    }
    val nivelColor = when (resultado.nivel) {
        NivelRiesgo.BAJO     -> MaterialTheme.colorScheme.primary
        NivelRiesgo.MODERADO -> Color(0xFFd97706)
        NivelRiesgo.ALTO     -> Color(0xFFE24B4A)
    }
    val nivelBg = when (resultado.nivel) {
        NivelRiesgo.BAJO     -> MaterialTheme.colorScheme.primaryContainer
        NivelRiesgo.MODERADO -> Color(0xFFfef9c3)
        NivelRiesgo.ALTO     -> Color(0xFFfde8e8)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
        border = BorderStroke(
            width = if (resultado.nivel == NivelRiesgo.ALTO) 1.5.dp else 0.5.dp,
            color = nivelColor.copy(alpha = 0.6f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(nivelBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = iconParaSustancia(resultado.sustanciaId),
                        contentDescription = null,
                        tint = nivelColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = nombreSustancia,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(nivelBg)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = resultado.nivel.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = nivelColor
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            // Puntaje + recomendación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = resultado.recomendacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = resultado.puntaje.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = nivelColor
                    )
                    Text(
                        text = "pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AccionesSugeridas(
    nivel: NivelRiesgo,
    onNavigateToChat: () -> Unit,
    onNavigateToBitacora: () -> Unit,
    onNavigateToAvances: () -> Unit,
    onNavigateToMisMetas: () -> Unit,
    onNavigateToMisDetonantes: () -> Unit,
    onNavigateToDirectorio: () -> Unit,
    onNavigateToRedApoyo: () -> Unit
) {
    data class AccionItem(val icon: Int, val title: String, val description: String, val onClick: () -> Unit)

    val acciones: List<AccionItem> = when (nivel) {
        NivelRiesgo.BAJO -> listOf(
            AccionItem(R.drawable.ic_trending_up, "Registra tu estado cada día",
                "La bitácora te ayuda a ver tus patrones", onNavigateToBitacora),
            AccionItem(R.drawable.ic_chat, "Conoce a Berto",
                "Siempre disponible para escucharte", onNavigateToChat),
            AccionItem(R.drawable.ic_trophy, "Conoce tus avances",
                "Mira cuánto has progresado", onNavigateToAvances)
        )
        NivelRiesgo.MODERADO -> listOf(
            AccionItem(R.drawable.ic_flag, "Crea tu primera meta",
                "Un paso concreto hacia el cambio", onNavigateToMisMetas),
            AccionItem(R.drawable.ic_brain, "Identifica tus detonantes",
                "Conocerlos es el primer paso", onNavigateToMisDetonantes),
            AccionItem(R.drawable.ic_chat, "Habla con Berto cuando lo necesites",
                "Siempre disponible para escucharte", onNavigateToChat)
        )
        NivelRiesgo.ALTO -> listOf(
            AccionItem(R.drawable.ic_building, "Habla con un profesional",
                "Encuentra apoyo cerca de ti", onNavigateToDirectorio),
            AccionItem(R.drawable.ic_people, "Configura tu botón SOS",
                "Agrega contactos de confianza", onNavigateToRedApoyo),
            AccionItem(R.drawable.ic_chat, "Berto puede acompañarte",
                "No estás solo en esto", onNavigateToChat)
        )
    }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "¿Qué puedo hacer ahora?",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TealDark,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        acciones.forEach { accion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { accion.onClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealLightest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(accion.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = accion.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TealDark
                    )
                    Text(
                        text = accion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun iconParaSustancia(sustancia: String): Painter =
    painterResource(
        when (sustancia) {
            "alcohol" -> R.drawable.ic_bottle
            "cristal" -> R.drawable.ic_gem
            "vape"    -> R.drawable.ic_vape
            "cigarro" -> R.drawable.ic_cigarette
            else      -> R.drawable.ic_bottle
        }
    )
