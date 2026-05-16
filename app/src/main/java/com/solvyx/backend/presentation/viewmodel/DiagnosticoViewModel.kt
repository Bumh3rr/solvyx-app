package com.solvyx.backend.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.ResultadoEntity
import com.solvyx.backend.models.Pregunta
import com.solvyx.backend.models.ResultadoDiagnostico
import com.solvyx.backend.repository.DiagnosticoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticoViewModel @Inject constructor(
    private val repository: DiagnosticoRepository
) : ViewModel() {

    // ── Existente ─────────────────────────────────────────
    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoDiagnostico?>(null)
    val resultado: StateFlow<ResultadoDiagnostico?> = _resultado.asStateFlow()

    private val _historial = MutableStateFlow<List<ResultadoEntity>>(emptyList())
    val historial: StateFlow<List<ResultadoEntity>> = _historial.asStateFlow()

    // ── Selección múltiple ────────────────────────────────
    var sustanciasSeleccionadas by mutableStateOf<List<String>>(emptyList())
        private set

    var sustanciaActualIndex by mutableStateOf(0)
        private set

    var answersMap by mutableStateOf<Map<String, List<Int>>>(emptyMap())
        private set

    private val _resultados = MutableStateFlow<List<ResultadoDiagnostico>>(emptyList())
    val resultados: StateFlow<List<ResultadoDiagnostico>> = _resultados.asStateFlow()

    // ── Computed ──────────────────────────────────────────
    val sustanciaActual: String
        get() = sustanciasSeleccionadas.getOrElse(sustanciaActualIndex) { "" }

    val totalSustancias: Int
        get() = sustanciasSeleccionadas.size

    val esUltimaSustancia: Boolean
        get() = sustanciaActualIndex >= sustanciasSeleccionadas.lastIndex

    fun canContinue(): Boolean = sustanciasSeleccionadas.isNotEmpty()

    // ── Selección en pantalla de sustancias ───────────────
    fun toggleSustancia(id: String) {
        sustanciasSeleccionadas = if (sustanciasSeleccionadas.contains(id))
            sustanciasSeleccionadas - id
        else
            sustanciasSeleccionadas + id
    }

    // ── Iniciar cuestionario ──────────────────────────────
    fun iniciarCuestionario() {
        sustanciaActualIndex = 0
        answersMap = emptyMap()
        _resultados.value = emptyList()
        cargarPreguntas(sustanciaActual)
    }

    // ── Guardar respuestas y avanzar a la siguiente sustancia
    // Retorna true si hay más sustancias, false si terminó todo
    fun guardarYAvanzar(answers: List<Int>): Boolean {
        val sustanciaGuardada = sustanciaActual
        answersMap = answersMap + (sustanciaGuardada to answers)

        viewModelScope.launch {
            val resultado = repository.evaluarYGuardar(sustanciaGuardada, answers)
            _resultados.value = _resultados.value + resultado
        }

        return if (esUltimaSustancia) {
            false
        } else {
            sustanciaActualIndex++
            cargarPreguntas(sustanciaActual)
            true
        }
    }

    // ── Métodos existentes ────────────────────────────────
    fun cargarPreguntas(sustancia: String) {
        _preguntas.value = repository.obtenerPreguntas(sustancia)
    }

    fun evaluarRespuestas(respuestas: List<Int>) {
        viewModelScope.launch {
            _resultado.value = repository.evaluarYGuardar(
                sustanciaId = sustanciaActual,
                respuestas = respuestas
            )
        }
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            repository.obtenerHistorial().collect { _historial.value = it }
        }
    }
}
