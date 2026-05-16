package com.solvyx.backend.presentation.viewmodel

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

    private val _preguntas =
        MutableStateFlow<List<Pregunta>>(emptyList())

    val preguntas:
            StateFlow<List<Pregunta>>
            = _preguntas.asStateFlow()

    private val _resultado =
        MutableStateFlow<ResultadoDiagnostico?>(null)

    val resultado:
            StateFlow<ResultadoDiagnostico?>
            = _resultado.asStateFlow()

    private val _historial =
        MutableStateFlow<List<ResultadoEntity>>(emptyList())

    val historial:
            StateFlow<List<ResultadoEntity>>
            = _historial.asStateFlow()

    private var sustanciaActual = ""

    fun cargarPreguntas(
        sustancia: String
    ) {

        sustanciaActual = sustancia

        _preguntas.value =
            repository.obtenerPreguntas(sustancia)
    }

    fun evaluarRespuestas(
        respuestas: List<Int>
    ) {

        viewModelScope.launch {

            val resultado =
                repository.evaluarYGuardar(
                    sustanciaId = sustanciaActual,
                    respuestas = respuestas
                )

            _resultado.value = resultado
        }
    }

    fun cargarHistorial() {

        viewModelScope.launch {

            repository.obtenerHistorial()
                .collect {

                    _historial.value = it
                }
        }
    }
}