package com.solvyx.backend.validation

object Validadores {
    private val CARACTERES_INVALIDOS_NOMBRE = Regex("[^A-Za-zÀ-ÿñÑ\\s'-]")
    fun filtrarNombre(texto: String): String = CARACTERES_INVALIDOS_NOMBRE.replace(texto, "")
    fun esNombreValido(texto: String): Boolean = texto.trim().length >= 2

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    fun esEmailValido(texto: String): Boolean = EMAIL_REGEX.matches(texto.trim())

    fun filtrarTelefono(texto: String): String = texto.filter { it.isDigit() }.take(10)
    fun esTelefonoValido(texto: String): Boolean = texto.trim().length == 10
}
