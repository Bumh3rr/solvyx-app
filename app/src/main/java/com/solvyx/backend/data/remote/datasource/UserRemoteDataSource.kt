package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.common.formatter.DateFormatter
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val birthDateFormatter: DateFormatter
) {
    suspend fun getProfile(uid: String): UserRemoteDto? = try {
        val doc = firestore.collection(UserRemoteDto.USERS).document(uid).get().await()
        if (!doc.exists()) {
            null
        } else {
            @Suppress("UNCHECKED_CAST")
            val substances = doc.get(UserRemoteDto.SELECTED_SUBSTANCES) as? List<String> ?: emptyList()
            val birthDate = doc.getTimestamp(UserRemoteDto.BIRTH_DATE)?.let { birthDateFormatter.format(it.toDate()) }
            UserRemoteDto(
                nickname = doc.getString(UserRemoteDto.NICKNAME),
                email = doc.getString(UserRemoteDto.EMAIL),
                birthDate = birthDate,
                selectedSubstances = substances,
                assistCompleted = doc.getBoolean(UserRemoteDto.ASSIST_COMPLETED) ?: false,
                isAnonymous = doc.getBoolean(UserRemoteDto.IS_ANONYMOUS) ?: false,
                createdAt = doc.getTimestamp(UserRemoteDto.CREATED_AT)?.toDate()?.time,
                planGoalIndex = doc.getLong(UserRemoteDto.PLAN_GOAL_INDEX)?.toInt(),
                planGoalAchievedToday = doc.getBoolean(UserRemoteDto.PLAN_GOAL_ACHIEVED_TODAY),
                planDate = doc.getLong(UserRemoteDto.PLAN_DATE),
                currentStreak = (doc.getLong(UserRemoteDto.CURRENT_STREAK) ?: 0L).toInt(),
                bestStreak = (doc.getLong(UserRemoteDto.BEST_STREAK) ?: 0L).toInt()
            )
        }
    } catch (e: Exception) {
        null
    }

    suspend fun createProfile(uid: String, dto: UserRemoteDto) {
        firestore.collection(UserRemoteDto.USERS).document(uid).set(
            hashMapOf(
                UserRemoteDto.NICKNAME to dto.nickname,
                UserRemoteDto.EMAIL to dto.email,
                UserRemoteDto.BIRTH_DATE to birthDateFormatter.parse(dto.birthDate),
                UserRemoteDto.SELECTED_SUBSTANCES to dto.selectedSubstances,
                UserRemoteDto.ASSIST_COMPLETED to dto.assistCompleted,
                UserRemoteDto.IS_ANONYMOUS to dto.isAnonymous,
                UserRemoteDto.CURRENT_STREAK to dto.currentStreak,
                UserRemoteDto.BEST_STREAK to dto.bestStreak,
                UserRemoteDto.CREATED_AT to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun updateProfile(uid: String, nickname: String, birthDate: String) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .set(
                mapOf(
                    UserRemoteDto.NICKNAME to nickname,
                    UserRemoteDto.BIRTH_DATE to birthDateFormatter.parse(birthDate)
                ),
                SetOptions.merge()
            ).await()
    }

    suspend fun updateSubstances(uid: String, substances: Set<String>) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .set(mapOf(UserRemoteDto.SELECTED_SUBSTANCES to substances.toList()), SetOptions.merge())
            .await()
    }

    suspend fun isAssistCompleted(uid: String): Boolean = try {
        firestore.collection(UserRemoteDto.USERS).document(uid).get().await()
            .getBoolean(UserRemoteDto.ASSIST_COMPLETED) ?: false
    } catch (e: Exception) {
        false
    }

    suspend fun updatePlan(uid: String, goalIndex: Int, goalAchievedToday: Boolean, date: Long) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .set(
                mapOf(
                    UserRemoteDto.PLAN_GOAL_INDEX to goalIndex,
                    UserRemoteDto.PLAN_GOAL_ACHIEVED_TODAY to goalAchievedToday,
                    UserRemoteDto.PLAN_DATE to date
                ),
                SetOptions.merge()
            ).await()
    }

    suspend fun updateStreak(uid: String, current: Int, best: Int) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .set(
                mapOf(
                    UserRemoteDto.CURRENT_STREAK to current,
                    UserRemoteDto.BEST_STREAK to best
                ),
                SetOptions.merge()
            ).await()
    }
}
