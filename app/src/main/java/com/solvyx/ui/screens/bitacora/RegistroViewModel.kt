package com.solvyx.ui.screens.bitacora

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.repository.JournalRepository
import com.solvyx.ui.screens.avances.canAdvanceWizard
import com.solvyx.ui.screens.avances.esRegistroCompleto
import com.solvyx.ui.screens.avances.isLastWizardStep
import com.solvyx.ui.screens.avances.totalWizardSteps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    // Registro siempre es HOY (sin backfill).
    val fecha: LocalDate get() = LocalDate.now()

    var estadoAnimo by mutableStateOf<String?>(null)
        private set
    var notaAnimo by mutableStateOf("")
        private set
    var consumo by mutableStateOf<Boolean?>(null)
        private set
    var sustanciaSeleccionada by mutableStateOf<String?>(null)
        private set
    var showSustanciaSheet by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set
    var cantidadAprox by mutableStateOf("")
        private set
    var notaContexto by mutableStateOf("")
        private set

    /** Paso actual del wizard (0-based, ver [com.solvyx.ui.screens.avances.WizardStep]). */
    var wizardStep by mutableIntStateOf(0)
        private set

    val historial: StateFlow<List<JournalEntry>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** True solo con registro completo de hoy (mood Y consumed no nulos); un ánimo rápido no cuenta. */
    val registroCompletoHoy: StateFlow<Boolean> =
        repository.observeAll()
            .map { esRegistroCompleto(it, LocalDate.now()) }
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun canAdvance(): Boolean =
        canAdvanceWizard(wizardStep, estadoAnimo, consumo, sustanciaSeleccionada)

    fun isLastStep(): Boolean = isLastWizardStep(wizardStep, consumo)

    fun totalSteps(): Int = totalWizardSteps(consumo)

    fun nextStep() {
        if (!canAdvance() || isLastStep()) return
        wizardStep++
    }

    fun prevStep() {
        if (wizardStep > 0) wizardStep--
    }

    fun resetWizard() { wizardStep = 0 }

    fun updateEstadoAnimo(estado: String) { estadoAnimo = estado }
    fun updateNotaAnimo(nota: String) { if (nota.length <= 100) notaAnimo = nota }
    fun updateCantidadAprox(v: String) { cantidadAprox = v }
    fun updateNotaContexto(v: String) { if (v.length <= 200) notaContexto = v }
    fun updateConsumo(value: Boolean) {
        consumo = value
        if (!value) sustanciaSeleccionada = null
    }
    fun setSustancia(s: String) { sustanciaSeleccionada = s }
    fun toggleSustanciaSheet() { showSustanciaSheet = !showSustanciaSheet }

    /** Precarga el formulario con el registro de hoy (para "Editar"). Reinicia el wizard al paso 0. */
    fun cargarRegistroDeHoy() {
        viewModelScope.launch {
            val hoy = repository.getToday() ?: return@launch
            estadoAnimo = hoy.mood
            notaAnimo = hoy.note.orEmpty()
            consumo = hoy.consumed
            sustanciaSeleccionada = hoy.substance
            cantidadAprox = hoy.cantidadAprox.orEmpty()
            notaContexto = hoy.notaContexto.orEmpty()
            wizardStep = 0
            isSaved = false
        }
    }

    fun guardarRegistro() {
        if (estadoAnimo == null || consumo == null ||
            (consumo == true && sustanciaSeleccionada == null)
        ) return
        viewModelScope.launch {
            repository.save(
                JournalEntry(
                    date = fecha,
                    mood = estadoAnimo!!,
                    consumed = consumo!!,
                    substance = sustanciaSeleccionada,
                    note = notaAnimo.ifBlank { null },
                    cantidadAprox = cantidadAprox.ifBlank { null },
                    notaContexto = notaContexto.ifBlank { null }
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
        cantidadAprox = ""
        notaContexto = ""
        wizardStep = 0
    }
}
