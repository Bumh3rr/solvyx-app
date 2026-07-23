package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.model.Achievement
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.data.remote.datasource.AchievementRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val journalRepository: JournalRepository,
    private val firebaseAuth: FirebaseAuth,
    private val achievementRemoteDataSource: AchievementRemoteDataSource
) {
    fun observeJournal(): Flow<List<JournalEntry>> = journalRepository.observeAll()

    /**
     * Deriva el estado de los 5 logros combinando el catálogo estático (`Achievement.BASE_IDS`)
     * con lo que exista en la colección sparse de Firestore. Anónimo o sin sesión: catálogo
     * completo bloqueado, sin tocar Firestore — Mi camino ya bloquea esta sección para anónimos.
     */
    fun observeAchievements(): Flow<List<Achievement>> {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) {
            return flowOf(Achievement.BASE_IDS.map { Achievement(id = it) })
        }
        return achievementRemoteDataSource.observe(user.uid).map { remoteUnlocked ->
            val unlockedById = remoteUnlocked.associateBy { it.id }
            Achievement.BASE_IDS.map { id ->
                val dto = unlockedById[id]
                if (dto != null) Achievement(id = id, unlocked = true, unlockDate = dto.unlockDate)
                else Achievement(id = id)
            }
        }
    }

    suspend fun unlockAchievement(id: String) {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            achievementRemoteDataSource.saveUnlock(user.uid, id, System.currentTimeMillis())
        } catch (e: Exception) {
            // best-effort: Firestore ya encola la escritura localmente y reintenta con
            // conectividad; un fallo aquí no debe tumbar la app ni bloquear la UI.
        }
    }
}
