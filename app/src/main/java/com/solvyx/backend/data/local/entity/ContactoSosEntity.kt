package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos_sos")
data class ContactoSosEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String = "",
    val telefono: String = "",
    val orden: Int = 0
)
