package com.solvyx.backend.data.remote.model

data class UserRemoteDto(
    val nickname: String? = null,
    val email: String? = null,
    val birthDate: String? = null,
    val selectedSubstances: List<String> = emptyList(),
    val assistCompleted: Boolean = false,
    val isAnonymous: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: Long? = null
){
    companion object {
        const val USERS = "users"
        const val NICKNAME = "nickname"
        const val EMAIL = "email"
        const val BIRTH_DATE = "birth_date"
        const val SELECTED_SUBSTANCES = "selected_substances"
        const val ASSIST_COMPLETED = "assist_completed"
        const val IS_ANONYMOUS = "is_anonymous"
        const val CURRENT_STREAK = "current_streak"
        const val BEST_STREAK = "best_streak"
        const val CREATED_AT = "created_at"
    }
}
