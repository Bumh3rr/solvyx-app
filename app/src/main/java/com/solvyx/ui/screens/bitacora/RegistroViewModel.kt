package com.solvyx.ui.screens.bitacora

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.backend.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

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
    var showCalendar by mutableStateOf(false)
        private set
    var showSustanciaSheet by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set

    val historial: StateFlow<List<JournalEntity>> =
        repository.observe()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val fechasConRegistro: StateFlow<Set<LocalDate>> =
        repository.observeDates()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun canSave(): Boolean =
        estadoAnimo != null && consumo != null &&
        (consumo == false || sustanciaSeleccionada != null)

    fun setFecha(fecha: LocalDate) { fechaSeleccionada = fecha }
    fun updateEstadoAnimo(estado: String) { estadoAnimo = estado }
    fun updateNotaAnimo(nota: String) { if (nota.length <= 100) notaAnimo = nota }
    fun updateConsumo(value: Boolean) {
        consumo = value
        if (!value) sustanciaSeleccionada = null
        if (value) showSustanciaSheet = true
    }
    fun setSustancia(s: String) { sustanciaSeleccionada = s }
    fun toggleCalendar() { showCalendar = !showCalendar }
    fun toggleSustanciaSheet() { showSustanciaSheet = !showSustanciaSheet }

    fun guardarRegistro() {
        if (!canSave()) return
        viewModelScope.launch {
            repository.save(
                JournalEntity(
                    date = fechaSeleccionada
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
                    mood = estadoAnimo!!,
                    consumed = consumo!!,
                    substance = sustanciaSeleccionada,
                    note = notaAnimo.ifBlank { null }
                )
            )
            isSaved = true
        }
    }

    fun resetForm() {
        estadoAnimo = null
        notaAnimo = ""
        consumo = null
        sustanciaSeleccionada = null
        isSaved = false
    }
}
