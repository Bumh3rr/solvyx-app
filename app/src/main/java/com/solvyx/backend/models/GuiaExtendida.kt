package com.solvyx.backend.models

/**
 * Guía extendida para contextos de apoyo (crisis, craving, post-consumo, etc.).
 *
 * Modelo de dominio. El JSON almacenado en la entity se deserializa a
 * [ContenidoGuia] en el repositorio.
 */
data class GuiaExtendida(
    val id: Int,
    val slug: String,
    val titulo: String,
    val categoria: String,
    val descripcionCorta: String,
    val contenido: ContenidoGuia,
    val iconAsset: String?,
    val orden: Int,
    val activo: Boolean
)
