package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.JournalingEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para entradas escritas por el usuario ([JournalingEntryEntity]).
 *
 * A diferencia de las tablas de catálogo, aquí las escrituras son
 * "del usuario" y los borrados son físicos (no soft) porque la entrada
 * es contenido privado del usuario.
 */
@Dao
interface JournalingDao {

    /** Flujo de todas las entradas, ordenadas por fecha descendente. */
    @Query("SELECT * FROM journaling_entries ORDER BY fecha DESC")
    fun observeEntries(): Flow<List<JournalingEntryEntity>>

    /**
     * Entradas en un día concreto (rango `[desde, hasta)` en epoch millis).
     * La UI arma el rango del día con `LocalDate.atStartOfDay` y
     * `plusDays(1).atStartOfDay` antes de llamar.
     */
    @Query("SELECT * FROM journaling_entries WHERE fecha >= :desde AND fecha < :hasta ORDER BY fecha DESC")
    fun observeEntriesByFecha(desde: Long, hasta: Long): Flow<List<JournalingEntryEntity>>

    /** Entradas asociadas a un prompt concreto (analítica "qué prompts mueven más"). */
    @Query("SELECT * FROM journaling_entries WHERE promptId = :promptId ORDER BY fecha DESC")
    fun observeEntriesByPrompt(promptId: Int): Flow<List<JournalingEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entry: JournalingEntryEntity): Long

    /**
     * Borrado físico de una entrada.
     *
     * Se permite porque la entrada es contenido privado del usuario y debe
     * poder eliminarse (GDPR, derecho al olvido, etc.). Las relaciones con
     * `prompts_journaling` son lógicas y no se ven afectadas.
     */
    @Delete
    suspend fun eliminar(entry: JournalingEntryEntity)

    @Query("DELETE FROM journaling_entries WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}