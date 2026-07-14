package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_session")
data class ChatSessionEntity(
    @PrimaryKey val id: Int,
    val treeId: String,
    val currentNodeId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)