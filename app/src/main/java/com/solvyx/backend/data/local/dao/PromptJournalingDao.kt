package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.PromptJournalingEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para el banco de [PromptJournalingEntity] (preguntas disparadoras).
 *
 * Los prompts son contenido curado: se cargan desde el seed y rara vez
 * cambian. Por eso las lecturas son `Flow` (para reflejar reactividad ante
 * eventuales updates remotos) pero no hay endpoint de modificación en UI.
 */
@Dao
interface PromptJournalingDao {

    /** Todos los prompts activos, ordenados por categoría y luego por orden. */
    @Query("SELECT * FROM prompts_journaling WHERE activo = 1 ORDER BY categoria ASC, orden ASC")
    fun observeActivos(): Flow<List<PromptJournalingEntity>>

    /** Flujo de prompts filtrados por categoría (para sesiones temáticas). */
    @Query("SELECT * FROM prompts_journaling WHERE activo = 1 AND categoria = :categoria ORDER BY orden ASC")
    fun observeByCategoria(categoria: String): Flow<List<PromptJournalingEntity>>

    /**
     * Snapshot de un prompt aleatorio de una categoría (para selección
     * durante una sesión de journaling).
     */
    @Query("SELECT * FROM prompts_journaling WHERE activo = 1 AND categoria = :categoria ORDER BY RANDOM() LIMIT 1")
    suspend fun findRandomByCategoria(categoria: String): PromptJournalingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(prompt: PromptJournalingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(prompts: List<PromptJournalingEntity>)
}