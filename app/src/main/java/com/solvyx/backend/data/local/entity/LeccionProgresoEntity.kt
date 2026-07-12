package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Progreso de lectura de una [LeccionEntity] del catálogo educativo.
 *
 * Esta tabla es **independiente** del catálogo de lecciones: si una lección
 * se elimina o se actualiza su `slug`, conservamos el progreso del usuario
 * porque la PK es el `slug` (que es estable a nivel conceptual aunque la
 * fila de la lección cambie).
 *
 * Reglas:
 * - PK = `slug` (string, no autogenerado). Si la lección nunca existió,
 *   la fila sigue siendo válida y se filtra en la UI al cruzar con la
 *   lista de lecciones activas.
 * - `leida` es la bandera de "completada".
 * - `fechaLectura` (epoch millis) es la primera vez que se marcó como
 *   leída. Se usa para ordenar el historial y para alimentar futuras
 *   estadísticas de "qué tanto has aprendido".
 */
@Entity(tableName = "leccion_progreso")
data class LeccionProgresoEntity(
    @PrimaryKey val slug: String,
    val leida: Boolean = false,
    val fechaLectura: Long? = null
)
