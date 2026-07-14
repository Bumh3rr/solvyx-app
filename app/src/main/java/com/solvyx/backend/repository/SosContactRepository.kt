package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.SosContactDao
import com.solvyx.backend.data.local.entity.SosContactEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosContactRepository @Inject constructor(private val dao: SosContactDao) {
    fun observe(): Flow<List<SosContactEntity>> = dao.observe()

    suspend fun saveAll(contacts: List<SosContactEntity>) {
        dao.deleteAll()
        dao.upsertAll(contacts.mapIndexed { i, c -> c.copy(position = i) })
    }
}
