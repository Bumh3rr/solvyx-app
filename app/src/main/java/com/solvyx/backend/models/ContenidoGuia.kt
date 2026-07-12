package com.solvyx.backend.models

/**
 * Contenido estructurado de una guía extendida.
 *
 * Lo definimos como modelo de **dominio** (no entity) para que la UI
 * pueda pintar la guía con tipos seguros sin manejar JSON strings.
 *
 * Decisiones:
 * - `introduccion` es un único bloque de texto (puede tener saltos de línea).
 * - `pasos` es la lista numerada de acciones sugeridas.
 * - `senalesAlerta` y `cuandoLlamar911` son dos categorías DISTINTAS porque
 *   en UX se muestran separadas: las primeras son "considera pedir ayuda"
 *   y las segundas son "llama ya al 911". Mezclarlas sería riesgoso.
 * - `lineasAyuda` es una lista porque cada región/país tiene su propio
 *   set; en la v1 el seed trae 2-4 líneas por guía.
 */
data class ContenidoGuia(
    val introduccion: String,
    val pasos: List<PasoGuia>,
    val senalesAlerta: List<String>,
    val cuandoLlamar911: List<String>,
    val lineasAyuda: List<LineaAyuda>
)

data class PasoGuia(
    val titulo: String,
    val descripcion: String
)

data class LineaAyuda(
    val nombre: String,
    val telefono: String,
    val horario: String
)
