package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val dao: UserDao) {
    fun observe(): Flow<UserEntity?> = dao.observe()
    suspend fun save(user: UserEntity) = dao.upsert(user)
}
