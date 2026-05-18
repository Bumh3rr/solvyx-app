package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactoSosRepository @Inject constructor(private val dao: ContactoSosDao) {
    fun observar(): Flow<List<ContactoSosEntity>> = dao.observar()

    suspend fun guardarTodos(contactos: List<ContactoSosEntity>) {
        dao.deleteAll()
        dao.upsertAll(contactos.mapIndexed { i, c -> c.copy(orden = i) })
    }
}
