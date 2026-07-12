package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.GuiaExtendidaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para el catálogo de [GuiaExtendidaEntity] (guías contextuales).
 *
 * Las guías se agrupan por [GuiaExtendidaEntity.categoria]
 * (`crisis`, `craving`, `post_consumo`, ...) para que el árbol de decisión
 * pueda resolver la guía apropiada según el contexto detectado.
 */
@Dao
interface GuiaExtendidaDao {

    /** Todas las guías activas, ordenadas por categoría y orden interno. */
    @Query("SELECT * FROM guias_extendidas WHERE activo = 1 ORDER BY categoria ASC, orden ASC")
    fun observeActivas(): Flow<List<GuiaExtendidaEntity>>

    /** Búsqueda puntual por slug. */
    @Query("SELECT * FROM guias_extendidas WHERE slug = :slug LIMIT 1")
    suspend fun findBySlug(slug: String): GuiaExtendidaEntity?

    /** Flujo de guías filtradas por categoría (ej. pantalla "Guías de craving"). */
    @Query("SELECT * FROM guias_extendidas WHERE activo = 1 AND categoria = :categoria ORDER BY orden ASC")
    fun observeByCategoria(categoria: String): Flow<List<GuiaExtendidaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(guia: GuiaExtendidaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(guias: List<GuiaExtendidaEntity>)

    @Query("UPDATE guias_extendidas SET activo = 0, updatedAt = :now WHERE slug = :slug")
    suspend fun softDelete(slug: String, now: Long = System.currentTimeMillis())
}