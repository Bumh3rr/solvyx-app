package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.remote.datasource.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(
    private val dao: PlanDao,
    private val firebaseAuth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource
) {
    fun observe(): Flow<PlanEntity?> = dao.observe()

    suspend fun save(plan: PlanEntity) {
        dao.upsert(plan)
        syncToRemote(plan)
    }

    suspend fun saveLocalOnly(plan: PlanEntity) = dao.upsert(plan)

    private suspend fun syncToRemote(plan: PlanEntity) {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            userRemoteDataSource.updatePlan(
                uid = user.uid,
                goalIndex = plan.goalIndex,
                goalAchievedToday = plan.goalAchievedToday,
                date = plan.date
            )
        } catch (e: Exception) {
            // best-effort
        }
    }
}
