package com.solvyx.ui.diagnostico

import com.solvyx.backend.repository.AssessmentRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// BUG: tras la muerte de proceso, el NavController restaura la ruta "questions" pero
// DiagnosticoViewModel nace vacío (sin SavedStateHandle) y nada vuelve a llamar cargarPreguntas(),
// dejando QuestionsScreen en un spinner infinito. needsQuestionReload() decide cuándo hace falta.
class DiagnosticoViewModelTest {

    private val repository = AssessmentRepository()

    @Test
    fun `needs reload when substances were restored but no questions are loaded`() {
        assertTrue(needsQuestionReload(sustanciasSeleccionadas = listOf("alcohol"), preguntasActuales = emptyList()))
    }

    @Test
    fun `does not need reload when questions are already loaded`() {
        val yaCargadas = repository.getQuestions("alcohol")
        assertFalse(needsQuestionReload(sustanciasSeleccionadas = listOf("alcohol"), preguntasActuales = yaCargadas))
    }

    @Test
    fun `does not need reload when there are no substances selected`() {
        assertFalse(needsQuestionReload(sustanciasSeleccionadas = emptyList(), preguntasActuales = emptyList()))
    }

    @Test
    fun `should preselect when onboarding, nothing chosen yet, and there is something to preselect`() {
        assertTrue(shouldPreselectSubstances(
            isOnboarding = true,
            currentSelection = emptyList(),
            preselected = setOf("alcohol")
        ))
    }

    @Test
    fun `should not preselect outside onboarding even with nothing chosen yet`() {
        assertFalse(shouldPreselectSubstances(
            isOnboarding = false,
            currentSelection = emptyList(),
            preselected = setOf("alcohol")
        ))
    }

    @Test
    fun `should not preselect when a selection already exists (process-death restore)`() {
        // This is the case that actually matters: without this guard, restoring
        // sustanciasSeleccionadas via SavedStateHandle after a process death mid-questionnaire would
        // trigger iniciarCuestionario() again, resetting the question index and losing progress.
        assertFalse(shouldPreselectSubstances(
            isOnboarding = true,
            currentSelection = listOf("alcohol"),
            preselected = setOf("alcohol")
        ))
    }

    @Test
    fun `should not preselect when there is nothing preselected to use`() {
        assertFalse(shouldPreselectSubstances(
            isOnboarding = true,
            currentSelection = emptyList(),
            preselected = emptySet()
        ))
    }
}
