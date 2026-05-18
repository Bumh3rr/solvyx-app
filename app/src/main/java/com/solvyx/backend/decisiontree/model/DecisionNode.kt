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

    // Mensaje extra (breve)
    val mensaje: String? = null,

    // Explicación profunda de por qué se siente así o por qué se da este consejo
    val porQue: String? = null,

    // Lista de acciones concretas y detalladas
    val recomendaciones: List<String> = emptyList(),

    // Estado visual de Berto para este nodo
    val bertoState: String = "TRANQUILO",

    // Tiempo de espera simulado para "escribir" (ms)
    val delayMs: Long = 1000L,

    // Indica si termina árbol
    val esFinal: Boolean = false
)