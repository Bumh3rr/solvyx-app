package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.remote.datasource.JournalRemoteDataSource
import com.solvyx.backend.data.remote.datasource.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(
    private val dao: PlanDao,
    private val firebaseAuth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val journalRemoteDataSource: JournalRemoteDataSource
) {
    fun observe(): Flow<PlanEntity?> = dao.observe()

    suspend fun saveLocalOnly(plan: PlanEntity) = dao.upsert(plan)

    /** Persiste qué sugerencia está seleccionada (sin el flag de lograda). */
    suspend fun saveGoalIndex(goalIndex: Int) {
        dao.upsert(PlanEntity(goalIndex = goalIndex))
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            userRemoteDataSource.updatePlan(
                uid = user.uid,
                goalIndex = goalIndex,
                goalAchievedToday = false,   // el flag ya no vive aquí; ver setMetaLogradaToday
                date = System.currentTimeMillis()
            )
        } catch (e: Exception) { /* best-effort */ }
    }

    /** "Lo logré hoy": merge de meta_lograda sobre el doc de bitácora de hoy. */
    suspend fun setMetaLogradaToday(value: Boolean) {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            journalRemoteDataSource.setMetaLograda(user.uid, LocalDate.now(), value)
        } catch (e: Exception) { /* best-effort */ }
    }

    /** Estado de la meta de hoy, leído del doc de bitácora. */
    suspend fun isMetaLogradaToday(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        if (user.isAnonymous) return false
        return journalRemoteDataSource.getEntry(user.uid, LocalDate.now())?.metaLograda == true
    }
}
