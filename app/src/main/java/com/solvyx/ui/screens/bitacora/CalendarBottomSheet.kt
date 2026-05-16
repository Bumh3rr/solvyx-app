package com.solvyx.ui.screens.bitacora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarBottomSheet(
    fechaSeleccionada: LocalDate,
    fechasConRegistro: Set<LocalDate>,
    onFechaSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mesActual by remember { mutableStateOf(YearMonth.from(fechaSeleccionada)) }
    var tempFecha by remember { mutableStateOf(fechaSeleccionada) }
    val today = LocalDate.now()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Month navigation
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left),
                        contentDescription = "Mes anterior",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    mesActual.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))
                    ).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val canGoNext = !mesActual.isAfter(YearMonth.now().minusMonths(1))
                IconButton(onClick = { if (canGoNext) mesActual = mesActual.plusMonths(1) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = "Mes siguiente",
                        tint = if (canGoNext) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day of week header
            Row(Modifier.fillMaxWidth()) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach { dia ->
                    Text(
                        dia,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar grid
            val primerDia = mesActual.atDay(1)
            val offsetInicio = (primerDia.dayOfWeek.value - 1) % 7
            val totalDias = mesActual.lengthOfMonth()
            val celdas = List(offsetInicio) { null } + (1..totalDias).map { it }
            celdas.chunked(7).forEach { fila ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val dia = fila.getOrNull(col)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dia != null) {
                                val fecha = mesActual.atDay(dia)
                                val esSeleccionado = fecha == tempFecha
                                val esHoy = fecha == today
                                val esFuturo = fecha.isAfter(today)
                                val tieneRegistro = fecha in fechasConRegistro

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                esSeleccionado -> MaterialTheme.colorScheme.primary
                                                esHoy -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable(enabled = !esFuturo) { tempFecha = fecha },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        dia.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (esSeleccionado || esHoy) FontWeight.Bold
                                                         else FontWeight.Normal
                                        ),
                                        color = when {
                                            esSeleccionado -> Color.White
                                            esFuturo -> MaterialTheme.colorScheme.outline
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (tieneRegistro && !esSeleccionado) {
                                        Box(
                                            Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SolvyxButton(
                text = "Seleccionar fecha",
                onClick = { onFechaSelected(tempFecha) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
