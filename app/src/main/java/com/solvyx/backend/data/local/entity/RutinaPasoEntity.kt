package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Paso individual dentro de una [RutinaEntity].
 *
 * Relación N-1 con `RutinaEntity`. Al borrar la rutina padre se borran sus
 * pasos (CASCADE), evitando huérfanos en SQLite.
 *
 * Ordenamiento:
 * - El índice compuesto `(rutinaId, orden)` (ver [indices]) hace eficiente la
 *   query típica "dame los pasos de la rutina X ordenados".
 * - El usuario no debe reordenar pasos manualmente desde UI; el orden viene
 *   del seed y es estable.
 *
 * [duracionSegundos] se usa para mostrar al usuario el tiempo estimado del
 * paso y opcionalmente alimentar un temporizador en UI (no implementado en
 * esta capa).
 */
@Entity(
    tableName = "rutina_pasos",
    foreignKeys = [
        ForeignKey(
            entity = RutinaEntity::class,
            parentColumns = ["id"],
            childColumns = ["rutinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["rutinaId"], name = "idx_rutina_pasos_rutinaId"),
        Index(value = ["rutinaId", "orden"], name = "idx_rutina_pasos_orden")
    ]
)
data class RutinaPasoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rutinaId: Int,
    val orden: Int,
    val titulo: String,
    val descripcion: String,
    val duracionSegundos: Int = 0,
    val iconAsset: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)