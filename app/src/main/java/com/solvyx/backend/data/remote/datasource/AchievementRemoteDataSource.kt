package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.data.remote.model.AchievementRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun achievementsCol(uid: String) =
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(AchievementRemoteDto.ACHIEVEMENTS)

    suspend fun saveUnlock(uid: String, achievementId: String, unlockDate: Long) {
        achievementsCol(uid).document(achievementId)
            .set(
                mapOf(
                    AchievementRemoteDto.UNLOCKED to true,
                    AchievementRemoteDto.UNLOCK_DATE to unlockDate
                ),
                SetOptions.merge()
            ).await()
    }

    /**
     * Colección sparse: solo trae los logros YA desbloqueados. Un error permanente (regla de
     * seguridad, sesión expirada) no debe dejar el Flow sin volver a emitir nunca — se traduce a
     * lista vacía, igual que `JournalRemoteDataSource.observeAll()`.
     */
    fun observe(uid: String): Flow<List<AchievementRemoteDto>> = callbackFlow {
        val registration = achievementsCol(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.map { doc ->
                    AchievementRemoteDto(
                        id = doc.id,
                        unlocked = true,
                        unlockDate = doc.getLong(AchievementRemoteDto.UNLOCK_DATE)
                    )
                })
            }
        }
        awaitClose { registration.remove() }
    }
}
