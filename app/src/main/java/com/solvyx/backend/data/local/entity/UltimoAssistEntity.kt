package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ultimo_assist")
data class UltimoAssistEntity(
    @PrimaryKey val id: Int = 1,
    val sustanciaId: String,
    val puntaje: Int,
    val nivel: String,
    val fecha: Long,
    val totalCompletados: Int = 0
)
