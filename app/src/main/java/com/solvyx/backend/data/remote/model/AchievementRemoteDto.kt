package com.solvyx.backend.data.remote.model

data class AchievementRemoteDto(
    val id: String,
    val unlocked: Boolean = false,
    val unlockDate: Long? = null
)