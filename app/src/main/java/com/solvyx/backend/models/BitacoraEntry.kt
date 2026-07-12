package com.solvyx.backend.models

/**
 * Entrada de bitácora del usuario, vista de dominio.
 *
 * Esta clase es la "cara pública" de la bitácora. A diferencia de la
 * entity, **todos los campos son expuestos con tipos de Kotlin limpios**
 * (no `Int?` para booleanos nullable, no strings vacíos como "ausencia").
 * La capa de UI puede consumir este modelo sin traducciones adicionales.
 *
 * Decisiones:
 * - Los campos extendidos son `null` cuando el usuario no los llenó; el
 *   dominio no usa sentinels.
 * - [nivelAnsiedad] va de 0 a 10 (escala auto-reportada).
 * - [suenoCalidad] y [calidadComida] van de 1 a 5.
 * - [notaPrivada] se expone tal cual desde la entity; el "modo privado"
 *   (cifrado/oculto en pantallas compartidas) es responsabilidad de la
 *   capa de UI.
 */
data class BitacoraEntry(
    val id: Int,
    val fecha: Long,
    val estadoAnimo: String,
    val consumio: Boolean,
    val sustancia: String?,
    val nota: String?,
    val suenoHoras: Int?,
    val suenoCalidad: Int?,
    val comio: Boolean?,
    val calidadComida: Int?,
    val actividadFisica: String?,
    val contextoSocial: String?,
    val detonantePrincipal: String?,
    val nivelAnsiedad: Int?,
    val tuvoCraving: Boolean?,
    val ejercicioFisico: Boolean?,
    val notaPrivada: String?,
    val updatedAt: Long
)
