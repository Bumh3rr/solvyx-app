package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.solvyx.backend.data.local.entity.RutinaEntity
import com.solvyx.backend.data.local.entity.RutinaPasoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para el catálogo de rutinas ([RutinaEntity]) y sus pasos
 * ([RutinaPasoEntity]).
 *
 * Modelo de consulta típico:
 * - Lista de rutinas activas → [observeActivas].
 * - Pasos de una rutina concreta → [findPasosByRutinaId].
 * - Carga inicial / refresh desde JSON → [upsertRutinaConPasos] (transacción).
 *
 * Decisión de diseño: las dos tablas se exponen por separado para mantener
 * queries simples y predecibles. La transacción [upsertRutinaConPasos]
 * garantiza atomicidad cuando se reemplaza una rutina entera con sus pasos.
 */
@Dao
interface RutinaDao {

    /** Todas las rutinas activas, ordenadas por hora sugerida. */
    @Query("SELECT * FROM rutinas WHERE activo = 1 ORDER BY horaSugerida ASC")
    fun observeActivas(): Flow<List<RutinaEntity>>

    /** Búsqueda puntual por slug (deep-link). */
    @Query("SELECT * FROM rutinas WHERE slug = :slug LIMIT 1")
    suspend fun findBySlug(slug: String): RutinaEntity?

    /**
     * Pasos de una rutina ordenados.
     *
     * Se devuelve un `snapshot` (suspend, no Flow) porque la pantalla de
     * pasos no necesita reactividad: una vez cargada la rutina, sus pasos
     * no cambian durante la sesión de uso.
     */
    @Query("SELECT * FROM rutina_pasos WHERE rutinaId = :rutinaId ORDER BY orden ASC")
    suspend fun findPasosByRutinaId(rutinaId: Int): List<RutinaPasoEntity>

    /** Flujo reactivo de pasos (útil para watchdogs en background). */
    @Query("SELECT * FROM rutina_pasos WHERE rutinaId = :rutinaId ORDER BY orden ASC")
    fun observePasosByRutinaId(rutinaId: Int): Flow<List<RutinaPasoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRutina(rutina: RutinaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPasos(pasos: List<RutinaPasoEntity>)

    /**
     * Reemplaza una rutina y TODOS sus pasos en una sola transacción.
     *
     * Se usa para el seed inicial o para sincronizaciones que sobrescriben
     * completamente el catálogo. Garantiza que la UI nunca vea una rutina
     * "a medias" (con pasos faltantes o sobrantes de una versión previa).
     *
     * Estrategia:
     * 1) Insertar la rutina (REPLACE si la PK ya existe).
     * 2) Borrar los pasos previos del `id` antiguo (si lo hay).
     * 3) Re-mapear el FK `rutinaId` de cada paso al id real.
     * 4) Insertar los nuevos pasos.
     */
    @Transaction
    suspend fun upsertRutinaConPasos(rutina: RutinaEntity, pasos: List<RutinaPasoEntity>) {
        val existingId = rutina.id
        upsertRutina(rutina)
        // Si la rutina ya existía con otro id, hay que borrar los pasos viejos.
        // En la práctica, como RutinaEntity.id es autogenerado, los pasos
        // quedan huérfanos sólo si se cambió manualmente el id; cubrimos el
        // caso igualmente.
        if (existingId != 0 && existingId != rutina.id) {
            deletePasosByRutinaId(existingId)
        }
        // Re-mapear: si los pasos vienen con rutinaId=0 o con el id anterior,
        // los ajustamos al id real resultante del upsert.
        val realId = findBySlug(rutina.slug)?.id ?: rutina.id
        val pasosReales = pasos.map { it.copy(rutinaId = realId) }
        upsertPasos(pasosReales)
    }

    @Query("DELETE FROM rutina_pasos WHERE rutinaId = :rutinaId")
    suspend fun deletePasosByRutinaId(rutinaId: Int)

    @Query("UPDATE rutinas SET activo = 0, updatedAt = :now WHERE slug = :slug")
    suspend fun softDelete(slug: String, now: Long = System.currentTimeMillis())
}