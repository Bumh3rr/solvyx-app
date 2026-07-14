package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.JournalDao
import com.solvyx.backend.data.local.dao.AchievementDao
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.backend.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val achievementDao: AchievementDao
) {
    fun observeJournal(): Flow<List<JournalEntity>> = journalDao.observe()
    fun observeAchievements(): Flow<List<AchievementEntity>> = achievementDao.observe()
    suspend fun unlockAchievement(id: String) =
        achievementDao.update(
            AchievementEntity(
                id = id,
                unlocked = true,
                unlockDate = System.currentTimeMillis()
            )
        )
}
