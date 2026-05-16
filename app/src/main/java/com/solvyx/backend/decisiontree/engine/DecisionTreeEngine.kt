package com.solvyx.backend.decisiontree.engine

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionTree

class DecisionTreeEngine(

    private val tree: DecisionTree

) {

    // Obtener nodo inicial
    fun obtenerNodoInicial(): DecisionNode {

        return tree.nodos[tree.nodoInicialId]
            ?: error("Nodo inicial no encontrado")
    }

    // Obtener nodo por ID
    fun obtenerNodo(
        nodeId: String
    ): DecisionNode {

        return tree.nodos[nodeId]
            ?: error("Nodo no encontrado: $nodeId")
    }

    // Navegar al siguiente nodo
    fun responder(

        nodoActualId: String,

        opcionSeleccionada: String

    ): DecisionNode {

        val nodoActual =
            obtenerNodo(nodoActualId)

        val opcion =
            nodoActual.opciones.find {

                it.texto == opcionSeleccionada
            }

                ?: error(
                    "Opción no válida"
                )

        return obtenerNodo(
            opcion.siguienteNodoId
        )
    }
}