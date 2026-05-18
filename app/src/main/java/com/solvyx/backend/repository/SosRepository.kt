package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepository @Inject constructor(
    private val contactoSosDao: ContactoSosDao,
    private val sosEventDao: SosEventDao
) {
    fun observarContactos(): Flow<List<ContactoSosEntity>> = contactoSosDao.observar()
    suspend fun registrarEvento(telefonos: List<String>) =
        sosEventDao.insertar(SosEventEntity(telefonosEnviados = telefonos.joinToString("|||")))
}
