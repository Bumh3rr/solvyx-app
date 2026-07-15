package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.data.remote.model.AchievementRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUnlock(uid: String, achievementId: String, unlockDate: Long) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(AchievementRemoteDto.ACHIEVEMENTS).document(achievementId)
            .set(
                mapOf(
                    AchievementRemoteDto.UNLOCKED to true,
                    AchievementRemoteDto.UNLOCK_DATE to unlockDate
                ),
                SetOptions.merge()
            ).await()
    }

    suspend fun getUnlocked(uid: String): List<AchievementRemoteDto> {
        return firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(AchievementRemoteDto.ACHIEVEMENTS)
            .get().await()
            .documents.map { doc ->
                AchievementRemoteDto(
                    id = doc.id,
                    unlocked = true,
                    unlockDate = doc.getLong(AchievementRemoteDto.UNLOCK_DATE)
                )
            }
    }
}
