package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.LeccionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para el catálogo de [LeccionEntity] (lecciones por sustancia y tema).
 *
 * Modelo de consulta típico:
 * 1. El usuario entra a "Educación → Alcohol".
 * 2. La UI lista las sustancias disponibles ([observeActivas]).
 * 3. Al elegir una sustancia, la UI pide los temas disponibles
 *    ([observeBySustancia]) y dentro de cada tema las lecciones ordenadas
 *    (también cubierto por [observeBySustancia]).
 * 4. Para deep-link desde una notificación, [findBySlug] resuelve el slug.
 */
@Dao
interface LeccionDao {

    /** Todas las lecciones activas. */
    @Query("SELECT * FROM lecciones WHERE activo = 1 ORDER BY sustancia ASC, tema ASC, orden ASC")
    fun observeActivas(): Flow<List<LeccionEntity>>

    /** Búsqueda puntual por slug (deep-links, navegación programática). */
    @Query("SELECT * FROM lecciones WHERE slug = :slug LIMIT 1")
    suspend fun findBySlug(slug: String): LeccionEntity?

    /**
     * Lecciones de una sustancia concreta (sin filtrar tema).
     * Devuelve el set completo para que la UI agrupe por tema en memoria.
     */
    @Query("SELECT * FROM lecciones WHERE activo = 1 AND sustancia = :sustancia ORDER BY tema ASC, orden ASC")
    fun observeBySustancia(sustancia: String): Flow<List<LeccionEntity>>

    /**
     * Lecciones de un par (sustancia, tema) concreto, en orden de aprendizaje.
     * Es la query más usada en la ruta de aprendizaje.
     */
    @Query("SELECT * FROM lecciones WHERE activo = 1 AND sustancia = :sustancia AND tema = :tema ORDER BY orden ASC")
    fun observeBySustanciaYTema(sustancia: String, tema: String): Flow<List<LeccionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(leccion: LeccionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lecciones: List<LeccionEntity>)

    @Query("UPDATE lecciones SET activo = 0, updatedAt = :now WHERE slug = :slug")
    suspend fun softDelete(slug: String, now: Long = System.currentTimeMillis())
}