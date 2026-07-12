package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.JournalingDao
import com.solvyx.backend.data.local.entity.JournalingEntryEntity
import com.solvyx.backend.models.JournalingEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de entradas de journaling escritas por el usuario.
 *
 * Las entradas son contenido privado del usuario; el borrado es físico
 * y la inserción devuelve el id generado para confirmar al usuario.
 *
 * El error se modela como [Result] para no propagar excepciones
 * específicas de SQLite a la capa de presentación.
 */
interface JournalingRepository {
    fun observeEntries(): Flow<List<JournalingEntry>>

    /**
     * Entradas en el día de [desde, hasta) en epoch millis. La UI arma
     * el rango con la zona horaria del usuario.
     */
    fun observeByFecha(desde: Long, hasta: Long): Flow<List<JournalingEntry>>

    suspend fun insertar(entry: JournalingEntry): Result<Long>
    suspend fun eliminar(entry: JournalingEntry): Result<Unit>
}

@Singleton
class JournalingRepositoryImpl @Inject constructor(
    private val dao: JournalingDao
) : JournalingRepository {

    override fun observeEntries(): Flow<List<JournalingEntry>> =
        dao.observeEntries().map { it.map(::toDomain) }

    override fun observeByFecha(desde: Long, hasta: Long): Flow<List<JournalingEntry>> =
        dao.observeEntriesByFecha(desde, hasta).map { it.map(::toDomain) }

    override suspend fun insertar(entry: JournalingEntry): Result<Long> = runCatching {
        dao.insertar(entry.toEntity())
    }.recoverCatching { e ->
        throw JournalingException("No pudimos guardar tu entrada. Inténtalo de nuevo.", e)
    }

    override suspend fun eliminar(entry: JournalingEntry): Result<Unit> = runCatching {
        dao.eliminar(entry.toEntity())
    }.recoverCatching { e ->
        throw JournalingException("No pudimos borrar tu entrada. Inténtalo de nuevo.", e)
    }

    // ---------------------------------------------------------------
    // Mappers
    // ---------------------------------------------------------------

    private fun toDomain(entity: JournalingEntryEntity): JournalingEntry = JournalingEntry(
        id = entity.id.toLong(),
        fecha = entity.fecha,
        promptId = entity.promptId,
        promptTexto = entity.promptTexto,
        contenido = entity.contenido,
        createdAt = entity.createdAt
    )

    private fun JournalingEntry.toEntity(): JournalingEntryEntity = JournalingEntryEntity(
        id = id.toInt(),
        fecha = fecha,
        promptId = promptId,
        promptTexto = promptTexto,
        contenido = contenido,
        createdAt = createdAt
    )
}

/** Excepción tipada del repositorio, con mensaje user-friendly. */
class JournalingException(message: String, cause: Throwable? = null) : Exception(message, cause)
