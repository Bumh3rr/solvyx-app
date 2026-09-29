package com.solvyx.ui.screens.journey.checkin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.common.goals.CheckInGoalOutcome
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.repository.GoalRepository
import com.solvyx.backend.repository.JournalRepository
import com.solvyx.ui.screens.journey.WizardStep
import com.solvyx.ui.screens.plan.BertoLine
import com.solvyx.ui.screens.plan.advancedLine
import com.solvyx.ui.screens.journey.canAdvanceWizard
import com.solvyx.ui.screens.journey.isLastWizardStep
import com.solvyx.ui.screens.journey.totalWizardSteps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import javax.inject.Inject

/** Nav argument: true to open the check-in pre-filled with today's entry. */
const val CHECK_IN_EDIT_ARG = "edit"

const val NOTE_MAX_LENGTH = 100
const val CONTEXT_MAX_LENGTH = 200

/** How long Berto reacts to the chosen mood before the check-in moves on by itself. */
private const val MOOD_REACTION_MILLIS = 1_100L

/**
 * Firestore only completes a write once the server confirms it; offline it never does, although
 * the entry is already safe in the local cache. After this wait, the save is shown as done
 * ("pending sync") instead of spinning forever.
 */
private const val SAVE_CONFIRMATION_TIMEOUT_MILLIS = 4_000L

/** Goals are read from the local cache; this only guards against a cache that never answers. */
private const val GOALS_TIMEOUT_MILLIS = 2_000L

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    /** [streak] is null when it couldn't be recalculated yet (e.g. offline). */
    data class Saved(val streak: Int?, val pendingSync: Boolean) : SaveState
    data object Failed : SaveState
}

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: JournalRepository,
    private val goalRepository: GoalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var mood by mutableStateOf<String?>(null)
        private set
    var note by mutableStateOf("")
        private set
    var used by mutableStateOf<Boolean?>(null)
        private set
    var substance by mutableStateOf<String?>(null)
        private set
    var amount by mutableStateOf("")
        private set
    var context by mutableStateOf("")
        private set
    var step by mutableIntStateOf(WizardStep.MOOD.ordinal)
        private set
    var saveState by mutableStateOf<SaveState>(SaveState.Idle)
        private set

    /** "Avanzaste en tu meta" for the result screen; null when the day added to no goal. */
    var advanced by mutableStateOf<BertoLine?>(null)
        private set

    /** Goals this check-in completed, celebrated full screen one at a time after saving. */
    var celebrations by mutableStateOf<List<Goal>>(emptyList())
        private set

    val currentStep: WizardStep get() = WizardStep.entries[step]
    val totalSteps: Int get() = totalWizardSteps(used)
    val isLastStep: Boolean get() = isLastWizardStep(step, used)
    val canAdvance: Boolean get() = canAdvanceWizard(step, mood, used, substance)

    private var autoAdvanceJob: Job? = null

    init {
        if (savedStateHandle.get<Boolean>(CHECK_IN_EDIT_ARG) == true) loadToday()
    }

    /** Selecting a mood lets Berto react, then moves on unless the user picks again meanwhile. */
    fun selectMood(value: String) {
        mood = value
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            delay(MOOD_REACTION_MILLIS)
            if (currentStep == WizardStep.MOOD) step++
        }
    }

    fun updateNote(value: String) { if (value.length <= NOTE_MAX_LENGTH) note = value }

    /** Appends a quick-idea chip to the note, separated by a comma, if it still fits. */
    fun appendToNote(idea: String) {
        val updated = if (note.isBlank()) idea else "${note.trimEnd()}, ${idea.lowercase()}"
        updateNote(updated)
    }

    fun updateUsed(value: Boolean) {
        used = value
        if (!value) {
            substance = null
            amount = ""
            context = ""
        }
    }

    fun updateSubstance(value: String) { substance = value }
    fun updateAmount(value: String) { amount = value }
    fun updateContext(value: String) { if (value.length <= CONTEXT_MAX_LENGTH) context = value }

    fun next() {
        autoAdvanceJob?.cancel()
        if (canAdvance && !isLastStep) step++
    }

    fun back() {
        autoAdvanceJob?.cancel()
        if (step > 0) step--
    }

    fun save() {
        val chosenMood = mood ?: return
        val chosenUse = used ?: return
        if (chosenUse && substance == null) return
        saveState = SaveState.Saving
        val entry = JournalEntry(
            date = LocalDate.now(),
            mood = chosenMood,
            consumed = chosenUse,
            substance = substance,
            note = note.ifBlank { null },
            cantidadAprox = amount.ifBlank { null },
            notaContexto = context.ifBlank { null }
        )
        viewModelScope.launch {
            // Goals first, from the cache and before the entry is written: this way the check-in is
            // the one that celebrates the goal it completes (Plan would otherwise race to it).
            withTimeoutOrNull(GOALS_TIMEOUT_MILLIS) { goalOutcome(entry) }?.let { outcome ->
                advanced = advancedLine(outcome.advanced)
                celebrations = outcome.completed
            }
            // The write keeps going in its own coroutine even if we stop waiting for it.
            val write = async { runCatching { repository.save(entry) } }
            val result = withTimeoutOrNull(SAVE_CONFIRMATION_TIMEOUT_MILLIS) { write.await() }
            saveState = when {
                result == null -> SaveState.Saved(streak = null, pendingSync = true)
                result.isSuccess -> SaveState.Saved(streak = result.getOrNull()?.current, pendingSync = false)
                else -> SaveState.Failed
            }
        }
    }

    /** Null when the goals couldn't be read; the check-in is saved either way. */
    private suspend fun goalOutcome(entry: JournalEntry): CheckInGoalOutcome? = try {
        goalRepository.afterCheckIn(entry)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    fun celebrationDone() {
        celebrations = celebrations.drop(1)
    }

    private fun loadToday() {
        viewModelScope.launch {
            // getEntry() already maps a failed read (e.g. offline) to null, same as "no entry yet".
            val entry = repository.getToday() ?: return@launch
            mood = entry.mood
            note = entry.note.orEmpty()
            used = entry.consumed
            substance = entry.substance
            amount = entry.cantidadAprox.orEmpty()
            context = entry.notaContexto.orEmpty()
        }
    }
}
