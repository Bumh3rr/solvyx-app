package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val mood: String,
    val consumed: Boolean,
    val substance: String? = null,
    val note: String? = null
)
