package com.solvyx.backend.models

/**
 * Entrada escrita por el usuario en respuesta a un prompt (o libre).
 *
 * - [fecha] es epoch millis (no LocalDate) para conservar la hora exacta
 *   y poder ordenar con precisión en la UI.
 * - [promptId] y [promptTexto] son snapshots. Si el prompt se borra, la
 *   entrada sigue siendo legible. La entity lo modela igual.
 * - [contenido] puede ser largo; no se trunca aquí.
 */
data class JournalingEntry(
    val id: Long,
    val fecha: Long,
    val promptId: Int?,
    val promptTexto: String?,
    val contenido: String,
    val createdAt: Long
)
