package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.EjercicioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para el catálogo de [EjercicioEntity].
 *
 * Convenciones:
 * - Lecturas observables devuelven `Flow` para que la UI se actualice sola
 *   cuando el seed termine de cargar o se actualice un ejercicio.
 * - `upsert` y `softDelete` son `suspend` para ejecutarse en coroutine scope.
 * - El borrado es lógico (`activo = 0`) para preservar historial de uso y
 *   permitir re-seed idempotente.
 */
@Dao
interface EjercicioDao {

    /** Catálogo completo de ejercicios activos, ordenado para la UI. */
    @Query("SELECT * FROM ejercicios WHERE activo = 1 ORDER BY tipo ASC, orden ASC")
    fun observeActivos(): Flow<List<EjercicioEntity>>

    /** Búsqueda puntual por slug (usado por deep-link y navegación). */
    @Query("SELECT * FROM ejercicios WHERE slug = :slug LIMIT 1")
    suspend fun findBySlug(slug: String): EjercicioEntity?

    /** Flujo reactivo de ejercicios filtrados por tipo. */
    @Query("SELECT * FROM ejercicios WHERE activo = 1 AND tipo = :tipo ORDER BY orden ASC")
    fun observeByTipo(tipo: String): Flow<List<EjercicioEntity>>

    /**
     * Inserta o reemplaza un ejercicio.
     *
     * Estrategia REPLACE: si el `id` coincide, sobrescribe; si no, inserta.
     * Útil para el seed inicial y para re-sincronizaciones.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ejercicio: EjercicioEntity)

    /** Variante batch del upsert (para cargar un seed completo). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(ejercicios: List<EjercicioEntity>)

    /**
     * Borrado lógico: marca como inactivo y actualiza `updatedAt`.
     * NO elimina la fila para no romper historial ni claves foráneas.
     */
    @Query("UPDATE ejercicios SET activo = 0, updatedAt = :now WHERE slug = :slug")
    suspend fun softDelete(slug: String, now: Long = System.currentTimeMillis())
}