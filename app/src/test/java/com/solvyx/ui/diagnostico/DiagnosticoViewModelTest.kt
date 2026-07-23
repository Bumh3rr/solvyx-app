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
}
