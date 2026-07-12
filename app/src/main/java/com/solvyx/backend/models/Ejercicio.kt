package com.solvyx.backend.models

/**
 * Ejercicio de regulación emocional (respiración, grounding, body-scan, etc.).
 *
 * Modelo de **dominio**, desacoplado de la entity de Room. Los repositorios
 * son responsables de mapear entre `EjercicioEntity` y esta clase.
 *
 * Decisiones de modelado:
 * - [pasos] es una lista de strings (no un único string con JSON-encoded).
 *   El parseo del JSON de la entity lo hace el repositorio.
 * - [ttsText] es un mapa inmutable `langCode → texto`. Los códigos siguen
 *   BCP-47 (`es-MX`, `en-US`). En la v1 el seed solo trae claves sueltas
 *   (`intro`, `paso1`, ... `cierre`) en lugar de localizaciones, así que
 *   en el parser se conservan tales claves y la UI las usa como índices
 *   del recorrido guiado.
 * - [iconAsset] es un nombre lógico (`ic_wind`) que la capa de UI mapea a
 *   un `painterResource(id)`. No es una URL ni un path.
 */
data class Ejercicio(
    val id: Int,
    val slug: String,
    val nombre: String,
    val tipo: String,
    val duracionMinutos: Int,
    val descripcionCorta: String,
    val pasos: List<String>,
    val ttsText: Map<String, String>,
    val iconAsset: String?,
    val orden: Int,
    val activo: Boolean
)
