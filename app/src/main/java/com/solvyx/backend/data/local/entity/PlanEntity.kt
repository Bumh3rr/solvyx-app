package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey val id: Int = 1,
    val metaIndex: Int = 0,
    val metaLogradaHoy: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
