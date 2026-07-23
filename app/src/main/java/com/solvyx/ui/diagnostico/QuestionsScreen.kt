package com.solvyx.ui.diagnostico

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton

@Composable
fun QuestionsScreen(
    viewModel: DiagnosticoViewModel,
    onAllCompleted: () -> Unit,
    onBack: () -> Unit
) {
    val preguntas by viewModel.preguntas.collectAsState()
    // rememberSaveable: sin esto, rotar la pantalla (el ViewModel sobrevive, pero este estado no)
    // borraba en silencio las respuestas de la sustancia en curso. listSaver porque List<Int> no
    // es Bundle-saveable directamente (a diferencia de un ArrayList real).
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var answers by rememberSaveable(stateSaver = listSaver(save = { it }, restore = { it })) {
        mutableStateOf(listOf<Int>())
    }

    // Reiniciar estado cuando cargan preguntas de la siguiente sustancia
    LaunchedEffect(preguntas) {
        if (preguntas.isNotEmpty()) {
            currentIndex = 0
            answers = List(preguntas.size) { -1 }
        }
    }

    val totalPreguntas = preguntas.size
    val preguntaActual = preguntas.getOrNull(currentIndex)
    val respuestaActual = answers.getOrElse(currentIndex) { -1 }
    val hayRespuesta = respuestaActual != -1
    val esUltimaPregunta = currentIndex == totalPreguntas - 1

    val progresoGlobal = if (viewModel.totalSustancias > 0 && totalPreguntas > 0) {
        (viewModel.sustanciaActualIndex.toFloat() +
                (currentIndex + 1f) / totalPreguntas) / viewModel.totalSustancias
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progresoGlobal,
        animationSpec = tween(500),
        label = "globalProgress"
    )

    val nombreSustanciaActual = when (viewModel.sustanciaActual) {
        "cigarro" -> "Tabaco"
        else -> viewModel.sustanciaActual.replaceFirstChar { it.uppercase() }
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
            SolvyxBackButton(onClick = onBack)
            Text(
                text = "Diagnóstico ASSIST",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$nombreSustanciaActual ${viewModel.sustanciaActualIndex + 1}/${viewModel.totalSustancias}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
            )
        }

        // ── PROGRESS BAR GLOBAL ───────────────────────────
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )

        // ── CONTENIDO ─────────────────────────────────────
        if (preguntaActual != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Berto strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.berto_preocupado),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "No hay respuestas malas, solo sé honesto/a",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                // Pill sustancia actual
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = iconParaSustancia(viewModel.sustanciaActual),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = nombreSustanciaActual,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Pregunta ${currentIndex + 1} de $totalPreguntas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Progress dots animados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(totalPreguntas) { i ->
                        val dotWidth by animateDpAsState(
                            targetValue = if (i == currentIndex) 28.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                            label = "dot_$i"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .width(dotWidth)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (i <= currentIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Card de pregunta con transición slide
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn(tween(200))) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally { -it } + fadeIn(tween(200))) togetherWith
                                    (slideOutHorizontally { it } + fadeOut(tween(200)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "question_slide"
                ) { idx ->
                    val q = preguntas.getOrNull(idx)
                    val respuestaIdx = answers.getOrElse(idx) { -1 }
                    if (q != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceDim
                            ),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                // Badge pregunta
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "P${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = q.texto,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )

                                // Opciones
                                q.opciones.forEachIndexed { optIdx, opcion ->
                                    val isSelected = respuestaIdx == opcion.puntaje
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                answers = answers
                                                    .toMutableList()
                                                    .also { it[idx] = opcion.puntaje }
                                            }
                                            .padding(vertical = 14.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                answers = answers
                                                    .toMutableList()
                                                    .also { it[idx] = opcion.puntaje }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary,
                                                unselectedColor = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = opcion.texto,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (optIdx < q.opciones.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        } else {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // ── NAVIGATION BAR ────────────────────────────────
        val textoBoton = when {
            !esUltimaPregunta -> "Siguiente →"
            !viewModel.esUltimaSustancia -> "Siguiente →"
            else -> "Finalizar"
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SolvyxOutlinedButton(
                    text = "← Anterior",
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    modifier = Modifier.weight(1f),
                    enabled = currentIndex > 0
                )
                SolvyxButton(
                    text = textoBoton,
                    onClick = {
                        if (!esUltimaPregunta) {
                            currentIndex++
                        } else {
                            val hayMas = viewModel.guardarYAvanzar(answers)
                            if (!hayMas) onAllCompleted()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hayRespuesta
                )
            }
            Text(
                text = "Tus respuestas se guardan de forma privada en tu cuenta",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
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
