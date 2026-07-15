package com.solvyx.backend.data.remote.model

data class AchievementRemoteDto(
    val id: String,
    val unlocked: Boolean = false,
    val unlockDate: Long? = null
) {
    companion object {
        const val ACHIEVEMENTS = "achievements"
        const val UNLOCKED = "unlocked"
        const val UNLOCK_DATE = "unlock_date"
    }
}