package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey val id: Int = 1,
    val goalIndex: Int = 0,
    val goalAchievedToday: Boolean = false,
    val date: Long = System.currentTimeMillis()
)
