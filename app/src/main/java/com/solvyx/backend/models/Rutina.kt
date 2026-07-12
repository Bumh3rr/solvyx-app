package com.solvyx.backend.models

/**
 * Rutina diaria sugerida (matutina, nocturna, etc.).
 *
 * El modelo de dominio incluye [pasos] ya cargados para evitar que la UI
 * tenga que orquestar dos queries. El repositorio los carga al mapear.
 */
data class Rutina(
    val id: Int,
    val slug: String,
    val nombre: String,
    val descripcion: String,
    val horaSugerida: Int,
    val pasos: List<RutinaPaso>,
    val iconAsset: String?,
    val activo: Boolean
)

/**
 * Paso individual dentro de una [Rutina].
 *
 * `id` es `Int` para coincidir con la entity
 * [com.solvyx.backend.data.local.entity.RutinaPasoEntity] y con el FK
 * usado en `RutinaProgresoEntity.rutinaPasoId`.
 */
data class RutinaPaso(
    val id: Int,
    val rutinaId: Int,
    val orden: Int,
    val titulo: String,
    val descripcion: String,
    val duracionSegundos: Int,
    val iconAsset: String?
)

/**
 * Estado de una rutina con su progreso del día.
 *
 * - [rutina]: la rutina tal cual del catálogo.
 * - [pasosCompletadosHoy]: set de `pasoId` con check-in en el día actual
 *   (zona horaria del usuario). La UI lo cruza con `rutina.pasos`.
 */
data class RutinaConProgreso(
    val rutina: Rutina,
    val pasosCompletadosHoy: Set<Int>
)
