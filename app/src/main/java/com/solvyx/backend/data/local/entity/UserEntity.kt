package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val serverId: String? = null,
    val apodo: String = "",
    val email: String? = null,
    val esAnonimo: Boolean = false,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaNacimiento: String = "",
    val sustanciasJson: String = ""
)
