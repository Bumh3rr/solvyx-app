package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(private val dao: PlanDao) {
    fun observe(): Flow<PlanEntity?> = dao.observe()
    suspend fun save(plan: PlanEntity) = dao.upsert(plan)
}
