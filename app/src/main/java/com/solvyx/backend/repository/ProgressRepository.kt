package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.JournalDao
import com.solvyx.backend.data.local.dao.AchievementDao
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.backend.data.local.entity.AchievementEntity
import com.solvyx.backend.data.remote.datasource.AchievementRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val achievementDao: AchievementDao,
    private val firebaseAuth: FirebaseAuth,
    private val achievementRemoteDataSource: AchievementRemoteDataSource
) {
    fun observeJournal(): Flow<List<JournalEntity>> = journalDao.observe()
    fun observeAchievements(): Flow<List<AchievementEntity>> = achievementDao.observe()

    suspend fun unlockAchievement(id: String) {
        val unlockDate = System.currentTimeMillis()
        achievementDao.update(
            AchievementEntity(id = id, unlocked = true, unlockDate = unlockDate)
        )
        syncUnlockToRemote(id, unlockDate)
    }

    private suspend fun syncUnlockToRemote(id: String, unlockDate: Long) {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            achievementRemoteDataSource.saveUnlock(user.uid, id, unlockDate)
        } catch (e: Exception) {
            // best-effort
        }
    }

    suspend fun hydrateAchievements() {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            achievementDao.insertAll(
                listOf("racha_3", "racha_7", "racha_10", "racha_15", "racha_30").map {
                    AchievementEntity(id = it, unlocked = false, unlockDate = null)
                }
            )
            val localUnlockedIds = achievementDao.observe().first()
                .filter { it.unlocked }.map { it.id }.toSet()
            achievementRemoteDataSource.getUnlocked(user.uid).forEach { dto ->
                if (dto.id in localUnlockedIds) return@forEach
                achievementDao.update(
                    AchievementEntity(
                        id = dto.id,
                        unlocked = true,
                        unlockDate = dto.unlockDate ?: System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            // best-effort
        }
    }
}
