package com.solvyx.backend.models

/**
 * Contenido estructurado de una lección educativa.
 *
 * - [introduccion]: párrafo de apertura, sin secciones.
 * - [secciones]: bloques temáticos con título y cuerpo. La UI los
 *   renderiza como tarjetas apiladas.
 * - [conclusion]: párrafo de cierre, sin secciones.
 */
data class ContenidoLeccion(
    val introduccion: String,
    val secciones: List<SeccionLeccion>,
    val conclusion: String
)

data class SeccionLeccion(
    val titulo: String,
    val texto: String
)
