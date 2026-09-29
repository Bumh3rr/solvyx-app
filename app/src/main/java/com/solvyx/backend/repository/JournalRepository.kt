package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.common.streak.StreakCalculator
import com.solvyx.backend.common.streak.StreakStats
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.data.remote.datasource.JournalRemoteDataSource
import com.solvyx.backend.data.remote.datasource.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val remoteDataSource: JournalRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val streakCalculator: StreakCalculator
) {
    /** Todas las entradas del usuario en tiempo real. Vacío si anónimo o sin sesión. */
    fun observeAll(): Flow<List<JournalEntry>> {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) return flowOf(emptyList())
        return remoteDataSource.observeAll(user.uid)
    }

    suspend fun getToday(): JournalEntry? {
        val user = firebaseAuth.currentUser ?: return null
        if (user.isAnonymous) return null
        return remoteDataSource.getEntry(user.uid, LocalDate.now())
    }

    /**
     * Escribe el registro y recalcula/persiste la racha del usuario. Devuelve la racha recalculada,
     * o null si no se pudo calcular (el registro igual queda). No-op (null) para anónimos.
     */
    suspend fun save(entry: JournalEntry): StreakStats? {
        val user = firebaseAuth.currentUser ?: return null
        if (user.isAnonymous) return null
        remoteDataSource.saveEntry(user.uid, entry)
        return try {
            val all = remoteDataSource.observeAll(user.uid).first()
            streakCalculator.compute(all, LocalDate.now()).also { stats ->
                userRemoteDataSource.updateStreak(user.uid, stats.current, stats.best)
            }
        } catch (e: Exception) {
            // best-effort: el registro ya quedó; la racha se recalcula en el próximo save/lectura.
            null
        }
    }
}
