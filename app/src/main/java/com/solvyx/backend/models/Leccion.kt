package com.solvyx.backend.models

/**
 * Lección educativa por sustancia y tema.
 */
data class Leccion(
    val id: Int,
    val slug: String,
    val sustancia: String,
    val tema: String,
    val titulo: String,
    val contenido: ContenidoLeccion,
    val duracionLecturaMinutos: Int,
    val orden: Int,
    val activo: Boolean
)

/**
 * Estado de lectura de una lección desde el punto de vista del usuario.
 *
 * Es un envoltorio del modelo de dominio + el flag "ya la leí". Permite
 * a la UI pintar la insignia "leída" sin cruzar dos Flows.
 */
data class LeccionConProgreso(
    val leccion: Leccion,
    val leida: Boolean,
    val fechaLectura: Long?
)
