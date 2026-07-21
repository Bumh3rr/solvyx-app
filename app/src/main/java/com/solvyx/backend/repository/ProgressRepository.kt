package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.AchievementDao
import com.solvyx.backend.data.local.entity.AchievementEntity
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.data.remote.datasource.AchievementRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val journalRepository: JournalRepository,
    private val achievementDao: AchievementDao,
    private val firebaseAuth: FirebaseAuth,
    private val achievementRemoteDataSource: AchievementRemoteDataSource
) {
    fun observeJournal(): Flow<List<JournalEntry>> = journalRepository.observeAll()
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

    /**
     * Repone las filas base de logros. Idempotente: `insertAll` usa `OnConflictStrategy.IGNORE`,
     * así que nunca re-bloquea un logro ya conseguido.
     *
     * Necesario porque `AuthRepository.signOut()` llama a `clearAllTables()`, que vacía la tabla,
     * y `AppDatabase.SEED_CALLBACK` solo corre al **crear** el archivo de la DB — no vuelve a
     * dispararse. Sin esto, la tabla queda vacía para siempre tras el primer cierre de sesión y
     * `autoUnlock` no tiene filas que desbloquear.
     *
     * Va aparte de [hydrateAchievements] a propósito: ese método salta a los usuarios anónimos
     * (no tocan Firestore), pero un anónimo también necesita sus logros locales.
     */
    suspend fun ensureAchievementsSeeded() {
        achievementDao.insertAll(
            AchievementEntity.BASE_IDS.map { AchievementEntity(id = it) }
        )
    }

    suspend fun hydrateAchievements() {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
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
