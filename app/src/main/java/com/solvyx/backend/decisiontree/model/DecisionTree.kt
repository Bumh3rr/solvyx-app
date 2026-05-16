package com.solvyx.backend.decisiontree.model

data class DecisionTree(

    // ID del árbol
    val id: String,

    // Nombre visible
    val nombre: String,

    // Nodo inicial
    val nodoInicialId: String,

    // Todos los nodos
    val nodos: Map<String, DecisionNode>
)