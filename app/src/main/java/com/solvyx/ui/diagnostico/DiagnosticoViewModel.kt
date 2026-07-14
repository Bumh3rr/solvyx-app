package com.solvyx.ui.diagnostico

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.models.Pregunta
import com.solvyx.backend.models.ResultadoDiagnostico
import com.solvyx.backend.repository.AssessmentRepository
import com.solvyx.backend.repository.AssistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.plus

@HiltViewModel
class DiagnosticoViewModel @Inject constructor(
    private val repository: AssessmentRepository,
    private val assistRepository: AssistRepository
) : ViewModel() {

    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas.asStateFlow()

    private val _historial = MutableStateFlow<List<ResultadoDiagnostico>>(emptyList())
    val historial: StateFlow<List<ResultadoDiagnostico>> = _historial.asStateFlow()

    var sustanciasSeleccionadas by mutableStateOf<List<String>>(emptyList())
        private set
    var sustanciaActualIndex by mutableStateOf(0)
        private set
    var answersMap by mutableStateOf<Map<String, List<Int>>>(emptyMap())
        private set

    private val _resultados = MutableStateFlow<List<ResultadoDiagnostico>>(emptyList())
    val resultados: StateFlow<List<ResultadoDiagnostico>> = _resultados.asStateFlow()

    val sustanciaActual: String get() = sustanciasSeleccionadas.getOrElse(sustanciaActualIndex) { "" }
    val totalSustancias: Int get() = sustanciasSeleccionadas.size
    val esUltimaSustancia: Boolean get() = sustanciaActualIndex >= sustanciasSeleccionadas.lastIndex
    fun canContinue(): Boolean = sustanciasSeleccionadas.isNotEmpty()

    fun toggleSustancia(id: String) {
        sustanciasSeleccionadas = if (sustanciasSeleccionadas.contains(id))
            sustanciasSeleccionadas - id
        else
            sustanciasSeleccionadas + id
    }

    fun iniciarCuestionario() {
        sustanciaActualIndex = 0
        answersMap = emptyMap()
        _resultados.value = emptyList()
        cargarPreguntas(sustanciaActual)
    }

    fun guardarYAvanzar(answers: List<Int>): Boolean {
        val sustanciaGuardada = sustanciaActual
        answersMap = answersMap + (sustanciaGuardada to answers)
        viewModelScope.launch {
            val resultado = repository.evaluate(sustanciaGuardada, answers)
            _resultados.value = _resultados.value + resultado
            assistRepository.saveResult(resultado)
        }
        return if (esUltimaSustancia) false
        else { sustanciaActualIndex++; cargarPreguntas(sustanciaActual); true }
    }

    fun cargarPreguntas(sustancia: String) {
        _preguntas.value = repository.getQuestions(sustancia)
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _historial.value = assistRepository.getHistory()
        }
    }
}