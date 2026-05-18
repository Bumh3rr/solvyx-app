package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val apodo: String = "",
    val email: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaNacimiento: String = "",
    val sustanciasJson: String = ""
)
