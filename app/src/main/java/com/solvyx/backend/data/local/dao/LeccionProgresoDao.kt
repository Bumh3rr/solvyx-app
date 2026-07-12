package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.LeccionProgresoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para [LeccionProgresoEntity] (progreso de lecciones).
 *
 * Convenciones:
 * - La PK es `slug`, por lo que `upsert` resuelve inserción/actualización
 *   en una sola operación.
 * - Las observaciones devuelven `Flow` para que la UI reaccione al
 *   instante de marcar una lección como leída.
 */
@Dao
interface LeccionProgresoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progreso: LeccionProgresoEntity)

    /** Slug → progreso (suspend para resolver "ya fue leída?" en una sola query). */
    @Query("SELECT * FROM leccion_progreso WHERE slug = :slug LIMIT 1")
    suspend fun findBySlug(slug: String): LeccionProgresoEntity?

    /**
     * Lista de slugs de lecciones marcadas como leídas.
     *
     * Es un `Flow<List<String>>` (no de entidades) para que la UI pueda
     * cruzar directamente con el catálogo de lecciones sin mapear.
     */
    @Query("SELECT slug FROM leccion_progreso WHERE leida = 1")
    fun observeLeidas(): Flow<List<String>>

    /**
     * Marcar como leída. Idempotente: si ya estaba marcada, solo actualiza
     * la `fechaLectura` para reflejar la última interacción.
     */
    @Query(
        """
        INSERT INTO leccion_progreso (slug, leida, fechaLectura)
        VALUES (:slug, 1, :fecha)
        ON CONFLICT(slug) DO UPDATE SET
            leida = 1,
            fechaLectura = :fecha
        """
    )
    suspend fun marcarLeida(slug: String, fecha: Long)
}
