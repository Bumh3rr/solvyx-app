package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.RutinaProgresoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para [RutinaProgresoEntity] (check-ins de pasos de rutina).
 *
 * Modelo de uso:
 * 1. Usuario abre la rutina "Matutina".
 * 2. La UI pide los pasos y pregunta "¿cuáles están hechos hoy?".
 * 3. `observePasosCompletadosEnRango(desde, hasta)` devuelve el set de
 *    `rutinaPasoId` con check-in en ese rango. La UI lo cruza con la
 *    lista de pasos.
 * 4. Al marcar un paso, la UI llama a `marcarCompletado(pasoId, fecha)`.
 *    El registro se inserta; si se vuelve a llamar el mismo día, se
 *    inserta OTRA fila (no se deduplica) — la deduplicación se hace
 *    en la lectura con el filtro de rango.
 */
@Dao
interface RutinaProgresoDao {

    /**
     * Inserta un check-in de un paso en una fecha concreta (epoch millis).
     *
     * No es upsert: una llamada siempre crea una fila. La semántica de
     * "ya estaba hecho" se evalúa en lectura (existe al menos una fila
     * en el rango del día).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun marcarCompletado(progreso: RutinaProgresoEntity)

    /**
     * IDs de pasos con al menos un check-in en el rango `[desde, hasta)`.
     * El caller (repositorio) construye el rango con la zona horaria local.
     */
    @Query(
        "SELECT DISTINCT rutinaPasoId FROM rutina_progreso " +
            "WHERE fecha >= :desde AND fecha < :hasta"
    )
    fun observePasosCompletadosEnRango(desde: Long, hasta: Long): Flow<List<Int>>

    @Query(
        "SELECT DISTINCT rutinaPasoId FROM rutina_progreso " +
            "WHERE fecha >= :desde AND fecha < :hasta"
    )
    suspend fun findPasosCompletadosEnRango(desde: Long, hasta: Long): List<Int>

    /** Borrado físico del check-in (por si el usuario deshace). */
    @Query("DELETE FROM rutina_progreso WHERE rutinaPasoId = :pasoId AND fecha >= :desde AND fecha < :hasta")
    suspend fun eliminarCheckinsEnRango(pasoId: Int, desde: Long, hasta: Long)
}
