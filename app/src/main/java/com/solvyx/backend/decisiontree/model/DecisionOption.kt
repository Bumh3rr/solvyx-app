package com.solvyx.backend.decisiontree.model

data class DecisionOption(

    // Texto mostrado en botón
    val texto: String,

    // Nodo al que navega
    val siguienteNodoId: String,

    // Una frase corta de validación (ej: "Entiendo, es difícil...", "Gracias por ser honesto")
    val reaccion: String? = null
)