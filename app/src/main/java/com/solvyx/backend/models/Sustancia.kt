package com.solvyx.backend.models

data class Sustancia(
    val id: String,
    val nombre: String
)

val SUSTANCIAS = listOf(
    Sustancia("alcohol", "Alcohol"),
    Sustancia("cigarro", "Cigarro"),
    Sustancia("vape", "Vape"),
    Sustancia("cristal", "Cristal")
)