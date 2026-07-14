package com.solvyx.backend.data.remote.model

data class PlanRemoteDto(
    val id: Int = 1,
    val goalIndex: Int = 0,
    val goalAchievedToday: Boolean = false,
    val date: Long = System.currentTimeMillis()
)