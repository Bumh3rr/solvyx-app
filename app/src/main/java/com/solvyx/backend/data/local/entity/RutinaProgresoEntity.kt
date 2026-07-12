package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Marca de "paso X de la rutina Y fue completado en la fecha Z".
 *
 * Se usa para mostrar el progreso del día en la pantalla de detalle de
 * una rutina: cada paso se considera "hecho hoy" si existe al menos
 * una fila con `fecha` dentro del día actual (zona horaria del usuario).
 *
 * Decisiones de diseño:
 * - PK autogenerada: una misma combinación (paso, fecha) puede aparecer
 *   varias veces si el usuario abre y cierra la pantalla; no interesa
 *   deduplicar a nivel de tabla, se hace en la query (`WHERE fecha BETWEEN ...`).
 * - `rutinaPasoId` es el id de la fila de `rutina_pasos` (no el slug de la
 *   rutina, porque la rutina puede tener pasos con el mismo `orden` si se
 *   re-seed-ea). Se resuelve en el repositorio.
 * - `fecha` es epoch millis del momento del check, NO normalizado al día:
 *   el filtrado "pasos hechos hoy" se hace en SQL o en Kotlin con un
 *   `LocalDate.atStartOfDay()` antes/después.
 *
 * Esta tabla NO tiene FK real a `rutina_pasos` porque:
 * - Permite re-seed de pasos sin perder progreso.
 * - El progreso "huérfano" simplemente se filtra en la UI (no hay paso
 *   correspondiente → no se muestra). Se podría hacer limpieza batch en
 *   un job de mantenimiento más adelante.
 */
@Entity(
    tableName = "rutina_progreso",
    indices = [
        Index(value = ["rutinaPasoId"], name = "idx_rutina_progreso_paso"),
        Index(value = ["fecha"], name = "idx_rutina_progreso_fecha")
    ]
)
data class RutinaProgresoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutinaPasoId: Int,
    val fecha: Long
)
