package com.solvyx.ui.screens.journey

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    // Check-in is always TODAY (no backfill).
    val today: LocalDate get() = LocalDate.now()

    var mood by mutableStateOf<String?>(null)
        private set
    var note by mutableStateOf("")
        private set
    var used by mutableStateOf<Boolean?>(null)
        private set
    var substance by mutableStateOf<String?>(null)
        private set
    var showSubstanceSheet by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set
    var amount by mutableStateOf("")
        private set
    var context by mutableStateOf("")
        private set

    /** Current wizard step (0-based, see [com.solvyx.ui.screens.journey.WizardStep]). */
    var wizardStep by mutableIntStateOf(0)
        private set

    fun canAdvance(): Boolean =
        canAdvanceWizard(wizardStep, mood, used, substance)

    fun isLastStep(): Boolean = isLastWizardStep(wizardStep, used)

    fun totalSteps(): Int = totalWizardSteps(used)

    fun nextStep() {
        if (!canAdvance() || isLastStep()) return
        wizardStep++
    }

    fun prevStep() {
        if (wizardStep > 0) wizardStep--
    }

    fun resetWizard() { wizardStep = 0 }

    fun updateMood(value: String) { mood = value }
    fun updateNote(value: String) { if (value.length <= 100) note = value }
    fun updateAmount(value: String) { amount = value }
    fun updateContext(value: String) { if (value.length <= 200) context = value }
    fun updateUsed(value: Boolean) {
        used = value
        if (!value) substance = null
    }
    fun updateSubstance(value: String) { substance = value }
    fun toggleSubstanceSheet() { showSubstanceSheet = !showSubstanceSheet }

    /** Preloads the form with today's entry (for "Edit"). Resets the wizard to step 0. */
    fun loadToday() {
        viewModelScope.launch {
            // getEntry() already maps a failed read (e.g. offline) to null, same as "no entry yet".
            val entry = repository.getToday() ?: return@launch
            mood = entry.mood
            note = entry.note.orEmpty()
            used = entry.consumed
            substance = entry.substance
            amount = entry.cantidadAprox.orEmpty()
            context = entry.notaContexto.orEmpty()
            wizardStep = 0
            isSaved = false
        }
    }

    fun save() {
        if (mood == null || used == null ||
            (used == true && substance == null)
        ) return
        viewModelScope.launch {
            try {
                repository.save(
                    JournalEntry(
                        date = today,
                        mood = mood!!,
                        consumed = used!!,
                        substance = substance,
                        note = note.ifBlank { null },
                        cantidadAprox = amount.ifBlank { null },
                        notaContexto = context.ifBlank { null }
                    )
                )
                isSaved = true
            } catch (e: Exception) {
                // saveEntry() no tiene su propio try/catch: sin este, un fallo de red al guardar
                // el check-in tumbaba la app en vez de solo dejar isSaved en false para reintentar.
            }
        }
    }

    fun reset() {
        mood = null
        note = ""
        used = null
        substance = null
        isSaved = false
        amount = ""
        context = ""
        wizardStep = 0
    }
}
