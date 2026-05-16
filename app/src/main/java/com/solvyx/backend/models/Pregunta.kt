package com.solvyx.backend.models

data class Pregunta(
    val id: Int,
    val texto: String,
    val opciones: List<Opcion>
)

data class Opcion(
    val texto: String,
    val puntaje: Int
)