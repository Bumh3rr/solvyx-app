package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Rutina diaria sugerida (ej. matutina, nocturna) con sus pasos asociados.
 *
 * Dominio: catálogo de rutinas que el usuario puede activar/desactivar y seguir
 * paso a paso. La cabecera [RutinaEntity] define el qué y el cuándo (vía
 * [horaSugerida]); los pasos individuales viven en [RutinaPasoEntity].
 *
 * Relación 1-N con `RutinaPasoEntity` (FK CASCADE en la tabla hija).
 *
 * [horaSugerida] es la hora del día 0-23 en zona horaria local del usuario
 * en que se sugiere la rutina. Es solo sugerencia; no dispara notificaciones
 * desde esta capa.
 */
@Entity(
    tableName = "rutinas",
    indices = [
        Index(value = ["slug"], unique = true)
    ]
)
data class RutinaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val slug: String,
    val nombre: String,
    val descripcion: String,
    val horaSugerida: Int = 0,
    val iconAsset: String? = null,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)