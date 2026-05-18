package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bitacora")
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,
    val estadoAnimo: String,
    val consumio: Boolean,
    val sustancia: String? = null,
    val nota: String? = null
)
