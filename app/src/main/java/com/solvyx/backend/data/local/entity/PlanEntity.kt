package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * OBSOLETA: el plan de consejos fijos se reemplazó por las metas de Firestore (`metas`). La tabla
 * se queda solo para no cambiar el esquema de Room: con fallbackToDestructiveMigration un cambio
 * borraría todas las tablas, incluidos los contactos SOS que solo viven en el teléfono.
 */
@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey val id: Int = 1,
    val goalIndex: Int = 0,
    val goalAchievedToday: Boolean = false,
    val date: Long = System.currentTimeMillis()
)
