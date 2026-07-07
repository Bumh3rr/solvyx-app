package com.solvyx.backend.models

data class ResultadoDiagnostico(
    val sustanciaId: String,
    val p2Frecuencia: Int,
    val p3Craving: Int,
    val p4Problemas: Int,
    val p5Obligaciones: Int,
    val p6Preocupacion: Int,
    val p7Intentos: Int,
    val p8Inyectado: Int? = null,
    val puntaje: Int,
    val nivel: NivelRiesgo,
    val recomendacion: String,
    val fecha: Long = System.currentTimeMillis()
)
