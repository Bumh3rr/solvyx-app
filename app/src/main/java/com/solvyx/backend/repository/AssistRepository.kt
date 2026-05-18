package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ResultadoAssistDao
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistRepository @Inject constructor(private val dao: ResultadoAssistDao) {
    fun observar(): Flow<List<ResultadoAssistEntity>> = dao.observar()
    suspend fun guardar(resultado: ResultadoAssistEntity) = dao.insertar(resultado)
}
