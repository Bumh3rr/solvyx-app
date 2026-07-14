package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val serverId: String? = null,
    val isAnonymous: Boolean = false,
    val substancesJson: String = ""
)
