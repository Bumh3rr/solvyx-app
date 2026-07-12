package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solvyx.backend.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para [BitacoraEntity].
 *
 * Convenciones:
 * - Lecturas observables devuelven `Flow` para alimentar pantallas reactivas.
 * - Escrituras son `suspend` (operan en coroutine scope, no bloquean main).
 * - El esquema soporta v2 (campos base) y v3 (campos extendidos). Los métodos
 *   `observar*` devuelven todas las columnas, así que el código de UI no
 *   cambia al pasar de v2 a v3.
 */
@Dao
interface BitacoraDao {

    /**
     * Insertar o reemplazar una entrada de bitácora.
     *
     * REPLACE: si el `id` ya existe, se sobrescribe. Útil cuando la UI edita
     * una entrada existente y la vuelve a guardar con el mismo `id`.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entry: BitacoraEntity)

    /**
     * Actualización explícita de una entrada existente (preserva el `id`).
     * Alternativa más semántica que `insertar` cuando la fila ya existe.
     */
    @Update
    suspend fun actualizar(entry: BitacoraEntity)

    /** Todas las entradas, ordenadas por fecha descendente. */
    @Query("SELECT * FROM bitacora ORDER BY fecha DESC")
    fun observar(): Flow<List<BitacoraEntity>>

    /** Solo los timestamps de las entradas (para pintar el calendario). */
    @Query("SELECT fecha FROM bitacora")
    fun observarFechas(): Flow<List<Long>>

    /**
     * Entradas en un rango de fechas (rango `[desde, hasta)` en epoch millis).
     *
     * Pensada para que el motor de insights y la UI de "esta semana" pidan
     * exactamente la ventana que necesitan, sin escanear toda la tabla.
     */
    @Query("SELECT * FROM bitacora WHERE fecha >= :desde AND fecha < :hasta ORDER BY fecha DESC")
    fun observarPorRango(desde: Long, hasta: Long): Flow<List<BitacoraEntity>>

    /**
     * Última entrada registrada (la más reciente).
     * Usada por el dashboard para mostrar el "check-in de hoy" sin enumerar
     * todas las filas.
     */
    @Query("SELECT * FROM bitacora ORDER BY fecha DESC LIMIT 1")
    suspend fun ultima(): BitacoraEntity?

    /**
     * Búsqueda puntual por id. Usada por el repositorio extendido para
     * actualizar solo los campos opcionales sin reescribir los básicos.
     */
    @Query("SELECT * FROM bitacora WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): BitacoraEntity?

    /**
     * Entradas donde el usuario marcó que consumió.
     * Alimenta la vista "historial de consumo" y la métrica "días limpios".
     */
    @Query("SELECT * FROM bitacora WHERE consumio = 1 ORDER BY fecha DESC")
    fun observarConConsumo(): Flow<List<BitacoraEntity>>

    /** Entradas donde el usuario reportó craving (campo extendido v3). */
    @Query("SELECT * FROM bitacora WHERE tuvoCraving = 1 ORDER BY fecha DESC")
    fun observarConCraving(): Flow<List<BitacoraEntity>>

    /**
     * Borrado físico de una entrada.
     *
     * A diferencia del catálogo, las entradas de bitácora son contenido del
     * usuario, así que se permite el borrado real para respetar el derecho
     * de supresión (GDPR y análogos).
     */
    @Delete
    suspend fun eliminar(entry: BitacoraEntity)

    @Query("DELETE FROM bitacora WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}