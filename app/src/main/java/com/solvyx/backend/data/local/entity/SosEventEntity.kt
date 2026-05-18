package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val telefonosEnviados: String = ""
)
