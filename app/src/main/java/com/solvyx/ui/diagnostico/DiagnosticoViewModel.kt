package com.solvyx.ui.diagnostico

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
    private val repository: AssessmentRepository,
    private val assistRepository: AssistRepository
) : ViewModel() {

    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas.asStateFlow()

    private val _historial = MutableStateFlow<List<ResultadoDiagnostico>>(emptyList())
    val historial: StateFlow<List<ResultadoDiagnostico>> = _historial.asStateFlow()

    // Respaldadas en SavedStateHandle: si el proceso muere a la mitad del cuestionario, el
    // NavController restaura la ruta "questions" pero con una instancia nueva de este ViewModel;
    // sin esto, sustanciasSeleccionadas volvería a estar vacía y nada volvería a cargar preguntas,
    // dejando QuestionsScreen en un spinner infinito (ver init más abajo).
    private val _sustanciasSeleccionadas = mutableStateOf(
        savedStateHandle.get<ArrayList<String>>(KEY_SUSTANCIAS)?.toList() ?: emptyList()
    )
    var sustanciasSeleccionadas: List<String>
        get() = _sustanciasSeleccionadas.value
        private set(value) {
            _sustanciasSeleccionadas.value = value
            savedStateHandle[KEY_SUSTANCIAS] = ArrayList(value)
        }

    private val _sustanciaActualIndex = mutableStateOf(savedStateHandle.get<Int>(KEY_INDEX) ?: 0)
    var sustanciaActualIndex: Int
        get() = _sustanciaActualIndex.value
        private set(value) {
            _sustanciaActualIndex.value = value
            savedStateHandle[KEY_INDEX] = value
        }

    var answersMap by mutableStateOf<Map<String, List<Int>>>(emptyMap())
        private set

    private val _resultados = MutableStateFlow<List<ResultadoDiagnostico>>(emptyList())
    val resultados: StateFlow<List<ResultadoDiagnostico>> = _resultados.asStateFlow()

    init {
        if (needsQuestionReload(sustanciasSeleccionadas, _preguntas.value)) {
            cargarPreguntas(sustanciaActual)
        }
    }

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

    companion object {
        private const val KEY_SUSTANCIAS = "sustanciasSeleccionadas"
        private const val KEY_INDEX = "sustanciaActualIndex"
    }
}

/**
 * True cuando hay sustancias restauradas (tras muerte de proceso) pero ninguna pregunta cargada
 * todavía — sin esto QuestionsScreen se queda en su spinner de carga para siempre, porque nada
 * más vuelve a llamar cargarPreguntas() en una instancia nueva del ViewModel.
 */
fun needsQuestionReload(sustanciasSeleccionadas: List<String>, preguntasActuales: List<Pregunta>): Boolean =
    sustanciasSeleccionadas.isNotEmpty() && preguntasActuales.isEmpty()