package com.solvyx.ui.screens.bitacora

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RegistroViewModel @Inject constructor() : ViewModel() {

    var fechaSeleccionada by mutableStateOf(LocalDate.now())
        private set
    var estadoAnimo by mutableStateOf<String?>(null)
        private set
    var notaAnimo by mutableStateOf("")
        private set
    var consumo by mutableStateOf<Boolean?>(null)
        private set
    var sustanciaSeleccionada by mutableStateOf<String?>(null)
        private set
    var nivelAnsiedad by mutableFloatStateOf(5f)
        private set
    var showCalendar by mutableStateOf(false)
        private set
    var showSustanciaSheet by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set

    fun canSave(): Boolean =
        estadoAnimo != null && consumo != null &&
        (consumo == false || sustanciaSeleccionada != null)

    fun nivelAnsiedadLabel(): String = when (nivelAnsiedad.toInt()) {
        in 1..3  -> "Ansiedad baja"
        in 4..6  -> "Ansiedad moderada"
        in 7..10 -> "Ansiedad alta"
        else     -> ""
    }

    fun setFecha(fecha: LocalDate)          { fechaSeleccionada = fecha }
    fun updateEstadoAnimo(estado: String)   { estadoAnimo = estado }
    fun updateNotaAnimo(nota: String)       { if (nota.length <= 100) notaAnimo = nota }
    fun updateConsumo(value: Boolean) {
        consumo = value
        if (!value) sustanciaSeleccionada = null
        if (value) showSustanciaSheet = true
    }
    fun setSustancia(s: String)             { sustanciaSeleccionada = s }
    fun updateNivelAnsiedad(n: Float)       { nivelAnsiedad = n }
    fun toggleCalendar()               { showCalendar = !showCalendar }
    fun toggleSustanciaSheet()         { showSustanciaSheet = !showSustanciaSheet }
    fun guardarRegistro() {
        if (!canSave()) return
        isSaved = true
    }

    fun resetForm() {
        estadoAnimo = null
        notaAnimo = ""
        consumo = null
        sustanciaSeleccionada = null
        nivelAnsiedad = 5f
        isSaved = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val fechasConRegistro: Set<LocalDate> = setOf(
        LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(3),
        LocalDate.now().minusDays(7),
        LocalDate.now().minusDays(8),
        LocalDate.now().minusDays(14)
    )
}
