package com.solvyx.backend.decisiontree.model

data class DecisionNode(

    // ID único
    val id: String,

    // Texto principal
    val texto: String,

    // Tipo de nodo
    val tipo: NodeType,

    // Opciones disponibles
    val opciones: List<DecisionOption> = emptyList(),

    // Mensaje extra opcional
    val mensaje: String? = null,

    // Indica si termina árbol
    val esFinal: Boolean = false
)