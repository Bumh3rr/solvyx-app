package com.solvyx.backend.models

data class ResultadoDiagnostico(
    val sustanciaId: String,
    val puntaje: Int,
    val nivel: NivelRiesgo,
    val recomendacion: String
)
