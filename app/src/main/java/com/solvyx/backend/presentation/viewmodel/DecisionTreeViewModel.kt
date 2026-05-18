package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.solvyx.backend.decisiontree.engine.DecisionTreeEngine
import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.repository.DecisionTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DecisionTreeViewModel @Inject constructor(

    private val repository: DecisionTreeRepository

) : ViewModel() {

    // Nodo actual mostrado en UI
    private val _nodoActual =
        MutableStateFlow<DecisionNode?>(null)

    val nodoActual:
            StateFlow<DecisionNode?>
            = _nodoActual.asStateFlow()

    // Historial de respuestas
    private val _respuestas =
        MutableStateFlow<List<String>>(emptyList())

    val respuestas:
            StateFlow<List<String>>
            = _respuestas.asStateFlow()

    // Engine actual
    private var engine:
            DecisionTreeEngine? = null

    // Árbol actual
    private var currentTreeId: String = ""

    // =========================
    // INICIAR ÁRBOL
    // =========================

    fun iniciarArbol(
        treeId: String
    ) {

        currentTreeId = treeId

        val tree =
            repository.obtenerArbol(treeId)

        engine =
            DecisionTreeEngine(tree)

        _nodoActual.value =
            engine?.obtenerNodoInicial()

        _respuestas.value =
            emptyList()
    }

    // =========================
    // RESPONDER
    // =========================

    fun responder(
        opcionSeleccionada: String
    ) {

        val nodo =
            _nodoActual.value
                ?: return

        val nextNode =
            engine?.responder(
                nodoActualId = nodo.id,
                opcionSeleccionada = opcionSeleccionada
            )

        // Guardar historial
        _respuestas.value =
            _respuestas.value + opcionSeleccionada

        // Actualizar UI
        _nodoActual.value = nextNode
    }

    // =========================
    // REINICIAR
    // =========================

    fun reiniciar() {

        if (currentTreeId.isNotBlank()) {

            iniciarArbol(currentTreeId)
        }
    }
}