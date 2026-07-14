package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.SosContactDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepository @Inject constructor(
    private val sosContactDao: SosContactDao,
    private val sosEventDao: SosEventDao
) {
    fun observeContacts(): Flow<List<SosContactEntity>> = sosContactDao.observe()
    suspend fun registerEvent(phones: List<String>) =
        sosEventDao.insert(SosEventEntity(notifiedPhones = phones.joinToString("|||")))
}
