package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logros")
data class LogroEntity(
    @PrimaryKey val id: String,
    val unlocked: Boolean = false,
    val fechaUnlock: Long? = null
)
