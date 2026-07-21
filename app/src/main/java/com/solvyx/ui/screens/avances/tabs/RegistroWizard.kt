package com.solvyx.ui.screens.avances.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.screens.avances.WizardStep
import com.solvyx.ui.screens.avances.components.RegistroExitosoDialog
import com.solvyx.ui.screens.bitacora.RegistroViewModel
import com.solvyx.ui.screens.bitacora.SustanciaBottomSheet

@Composable
fun RegistroWizard(
    viewModel: RegistroViewModel,
    modifier: Modifier = Modifier
) {
    if (viewModel.isSaved) {
        RegistroExitosoDialog(
            estadoAnimo = viewModel.estadoAnimo,
            consumio = viewModel.consumo == true,
            sustancia = viewModel.sustanciaSeleccionada,
            onDismiss = { viewModel.resetForm() }
        )
    }
    if (viewModel.showSustanciaSheet) {
        SustanciaBottomSheet(
            sustanciaSeleccionada = viewModel.sustanciaSeleccionada,
            onSustanciaSelected = { viewModel.setSustancia(it); viewModel.toggleSustanciaSheet() },
            onDismiss = { viewModel.toggleSustanciaSheet() }
        )
    }

    val step = WizardStep.entries[viewModel.wizardStep]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Progreso (dots) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val total = viewModel.totalSteps()
            repeat(total) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i <= viewModel.wizardStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                )
            }
        }

        // ── Berto + contenido del paso (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.berto_saludando),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = tituloPaso(step),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            when (step) {
                WizardStep.ANIMO -> PasoAnimo(viewModel)
                WizardStep.NOTA -> PasoNota(viewModel)
                WizardStep.CONSUMO -> PasoConsumo(viewModel)
                WizardStep.SUSTANCIA -> PasoSustancia(viewModel)
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Barra Atrás / Siguiente|Guardar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = SolvyxBottomNavHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (viewModel.wizardStep > 0) {
                SolvyxButton(
                    text = "Atrás",
                    onClick = { viewModel.prevStep() },
                    modifier = Modifier.weight(1f)
                )
            }
            SolvyxButton(
                text = if (viewModel.isLastStep()) "Guardar" else "Siguiente",
                onClick = {
                    if (viewModel.isLastStep()) viewModel.guardarRegistro() else viewModel.nextStep()
                },
                enabled = viewModel.canAdvance(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun tituloPaso(step: WizardStep): String = when (step) {
    WizardStep.ANIMO -> "¿Cómo te sientes hoy?"
    WizardStep.NOTA -> "¿Quieres agregar una nota?"
    WizardStep.CONSUMO -> "¿Consumiste alguna sustancia hoy?"
    WizardStep.SUSTANCIA -> "Cuéntame un poco más"
}

@Composable
private fun PasoAnimo(viewModel: RegistroViewModel) {
    val emociones = listOf(
        "triste" to R.drawable.ic_face_sad, "ansioso" to R.drawable.ic_face_anxious,
        "neutral" to R.drawable.ic_face_neutral, "bien" to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val labels = listOf("Triste", "Ansioso", "Neutral", "Bien", "Eufórico")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        emociones.forEachIndexed { idx, (id, icono) ->
            val sel = viewModel.estadoAnimo == id
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { viewModel.updateEstadoAnimo(id) }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .then(
                            if (sel) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icono),
                        contentDescription = labels[idx],
                        tint = if (sel) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = labels[idx],
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (sel) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PasoNota(viewModel: RegistroViewModel) {
    OutlinedTextField(
        value = viewModel.notaAnimo,
        onValueChange = { viewModel.updateNotaAnimo(it) },
        placeholder = {
            Text(
                "Escribe cómo te sientes (opcional)...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background
        ),
        suffix = {
            Text(
                "${viewModel.notaAnimo.length}/100",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        maxLines = 4
    )
}

@Composable
private fun PasoConsumo(viewModel: RegistroViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val noSel = viewModel.consumo == false
        OpcionConsumo(
            texto = "No",
            icono = R.drawable.ic_circle_x,
            seleccionado = noSel,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        ) { viewModel.updateConsumo(false) }

        val siSel = viewModel.consumo == true
        val sosRed = Color(0xFFE24B4A)
        OpcionConsumo(
            texto = "Sí",
            icono = R.drawable.ic_alert_circle,
            seleccionado = siSel,
            color = sosRed,
            modifier = Modifier.weight(1f)
        ) { viewModel.updateConsumo(true) }
    }
}

@Composable
private fun OpcionConsumo(
    texto: String,
    icono: Int,
    seleccionado: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (seleccionado) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background
            )
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) color else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(icono),
                contentDescription = texto,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun PasoSustancia(viewModel: RegistroViewModel) {
    Column(Modifier.fillMaxWidth()) {
        // Selector de sustancia (abre el bottom sheet)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { viewModel.toggleSustanciaSheet() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.sustanciaSeleccionada?.replaceFirstChar { it.uppercase() }
                    ?: "Elige la sustancia",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        // cantidad_aprox
        OutlinedTextField(
            value = viewModel.cantidadAprox,
            onValueChange = { viewModel.updateCantidadAprox(it) },
            placeholder = { Text("Cantidad aprox. (ej. 2 cervezas)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background
            )
        )
        Spacer(Modifier.height(12.dp))
        // nota_contexto
        OutlinedTextField(
            value = viewModel.notaContexto,
            onValueChange = { viewModel.updateNotaContexto(it) },
            placeholder = { Text("¿Qué pasó? Contexto (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background
            ),
            suffix = {
                Text(
                    "${viewModel.notaContexto.length}/200",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            maxLines = 4
        )
    }
}
