package com.solvyx.ui.components.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolvyxDateField(
    value: String,
    onDateSelected: (String) -> Unit,
    placeholder: String,
    @DrawableRes leadingIconRes: Int,
    modifier: Modifier = Modifier,
    edadMinimaAnios: Int = 13
) {
    var mostrarPicker by remember { mutableStateOf(false) }

    // Un OutlinedTextField de solo lectura sigue consumiendo el toque para
    // posicionar el cursor/tomar foco, así que un clickable en su propio
    // modifier nunca llega a dispararse. Una capa transparente encima
    // intercepta el toque antes de que el campo lo reciba.
    Box(modifier = modifier) {
        SolvyxTextField(
            value = value,
            onValueChange = {},
            placeholder = placeholder,
            leadingIconRes = leadingIconRes,
            readOnly = true
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { mostrarPicker = true }
        )
    }

    if (mostrarPicker) {
        val fechaMaxima = remember {
            Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .apply { add(Calendar.YEAR, -edadMinimaAnios) }.timeInMillis
        }
        val fechaInicialSeleccionada = remember(value) {
            runCatching {
                if (value.isBlank()) return@runCatching null
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                formato.parse(value)?.time
            }.getOrNull()
        }
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicialSeleccionada,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= fechaMaxima
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let {
                        val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        onDateSelected(formatoSalida.format(Date(it)))
                    }
                    mostrarPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
