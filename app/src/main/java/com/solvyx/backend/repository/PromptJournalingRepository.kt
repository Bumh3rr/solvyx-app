package com.solvyx.backend.repository

import com.solvyx.backend.assets.AssetsSeeder
import com.solvyx.backend.data.local.dao.PromptJournalingDao
import com.solvyx.backend.data.local.entity.PromptJournalingEntity
import com.solvyx.backend.models.PromptJournaling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio del banco de [PromptJournaling].
 */
interface PromptJournalingRepository {
    fun observePrompts(): Flow<List<PromptJournaling>>
    fun observeByCategoria(categoria: String): Flow<List<PromptJournaling>>
    suspend fun refresh()
}

@Singleton
class PromptJournalingRepositoryImpl @Inject constructor(
    private val dao: PromptJournalingDao,
    private val seeder: AssetsSeeder
) : PromptJournalingRepository {

    override fun observePrompts(): Flow<List<PromptJournaling>> =
        dao.observeActivos().map { it.map(::toDomain) }

    override fun observeByCategoria(categoria: String): Flow<List<PromptJournaling>> =
        dao.observeByCategoria(categoria).map { it.map(::toDomain) }

    override suspend fun refresh() {
        seeder.ensureLoaded()
    }

    // ---------------------------------------------------------------
    // Mapper
    // ---------------------------------------------------------------

    /**
     * El `slug` se reconstruye a partir de `categoria + orden` siguiendo
     * la convención del seed (`gratitud-001`, `dificultad-005`, etc.).
     * Esto evita añadir una columna a la entity y mantiene la API
     * uniforme: el dominio siempre tiene `slug`, venga de donde venga.
     */
    private fun toDomain(entity: PromptJournalingEntity): PromptJournaling = PromptJournaling(
        id = entity.id,
        slug = "${entity.categoria}-${entity.orden.toString().padStart(3, '0')}",
        categoria = entity.categoria,
        texto = entity.texto,
        orden = entity.orden,
        activo = entity.activo
    )
}
