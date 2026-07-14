package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_assist")
data class LastAssistEntity(
    @PrimaryKey val id: Int = 1,
    val substanceId: String,
    val score: Int,
    val level: String,
    val date: Long,
    val totalCompleted: Int = 0
)
