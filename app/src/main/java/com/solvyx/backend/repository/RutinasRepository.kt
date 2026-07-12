package com.solvyx.backend.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.solvyx.backend.assets.AssetsSeeder
import com.solvyx.backend.data.local.dao.RutinaDao
import com.solvyx.backend.data.local.dao.RutinaProgresoDao
import com.solvyx.backend.data.local.entity.RutinaEntity
import com.solvyx.backend.data.local.entity.RutinaPasoEntity
import com.solvyx.backend.models.Rutina
import com.solvyx.backend.models.RutinaPaso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de [Rutina] (rutinas diarias: matutina, nocturna) + progreso.
 */
interface RutinasRepository {
    /** Catálogo de rutinas activas. */
    fun observeRutinas(): Flow<List<Rutina>>

    /** Snapshot de los pasos de una rutina (en orden). */
    suspend fun findPasos(rutinaId: Int): List<RutinaPaso>

    suspend fun findRutinaBySlug(slug: String): Rutina?

    /** Marca un paso como completado. */
    suspend fun marcarPasoCompletado(rutinaPasoId: Int, fecha: Long = System.currentTimeMillis())

    /**
     * Flujo del set de `pasoId` con check-in en el día de [fecha]
     * (zona horaria del usuario).
     */
    fun observeProgresoDelDia(fecha: LocalDate = LocalDate.now()): Flow<Set<Int>>

    /**
     * Rutinas con el set de pasos completados HOY incrustado. Útil para
     * pintar el catálogo con un check al lado de cada paso.
     */
    fun observeRutinasConProgreso(): Flow<List<Pair<Rutina, Set<Int>>>>

    suspend fun refresh()
}

@Singleton
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RutinasRepositoryImpl @Inject constructor(
    private val dao: RutinaDao,
    private val progresoDao: RutinaProgresoDao,
    private val seeder: AssetsSeeder
) : RutinasRepository {

    override fun observeRutinas(): Flow<List<Rutina>> =
        dao.observeActivas().map { list ->
            list.map { entity ->
                entity.toDomain(emptyList()) // sin pasos para la lista
            }
        }

    override suspend fun findPasos(rutinaId: Int): List<RutinaPaso> =
        dao.findPasosByRutinaId(rutinaId).map { it.toDomain() }

    override suspend fun findRutinaBySlug(slug: String): Rutina? {
        val entity = dao.findBySlug(slug) ?: return null
        val pasos = dao.findPasosByRutinaId(entity.id).map { it.toDomain() }
        return entity.toDomain(pasos)
    }

    override suspend fun marcarPasoCompletado(rutinaPasoId: Int, fecha: Long) {
        progresoDao.marcarCompletado(
            com.solvyx.backend.data.local.entity.RutinaProgresoEntity(
                rutinaPasoId = rutinaPasoId,
                fecha = fecha
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeProgresoDelDia(fecha: LocalDate): Flow<Set<Int>> {
        val zone = ZoneId.systemDefault()
        val desde = fecha.atStartOfDay(zone).toInstant().toEpochMilli()
        val hasta = fecha.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return progresoDao.observePasosCompletadosEnRango(desde, hasta)
            .map { it.toSet() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeRutinasConProgreso(): Flow<List<Pair<Rutina, Set<Int>>>> {
        return dao.observeActivas().flatMapLatest { rutinas ->
            val zone = ZoneId.systemDefault()
            val hoy = LocalDate.now()
            val desde = hoy.atStartOfDay(zone).toInstant().toEpochMilli()
            val hasta = hoy.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            progresoDao.observePasosCompletadosEnRango(desde, hasta).map { pasosIds ->
                val setPasos = pasosIds.toSet()
                rutinas.map { entity ->
                    // Cargamos los pasos en una sola query; en catálogos
                    // pequeños (~2 rutinas × 4 pasos) es negligible.
                    val pasos = dao.findPasosByRutinaId(entity.id).map { it.toDomain() }
                    entity.toDomain(pasos) to setPasos
                }
            }
        }
    }

    override suspend fun refresh() {
        seeder.ensureLoaded()
    }

    // ---------------------------------------------------------------
    // Mappers
    // ---------------------------------------------------------------

    private fun RutinaEntity.toDomain(pasos: List<RutinaPaso>): Rutina = Rutina(
        id = id,
        slug = slug,
        nombre = nombre,
        descripcion = descripcion,
        horaSugerida = horaSugerida,
        pasos = pasos,
        iconAsset = iconAsset,
        activo = activo
    )

    private fun RutinaPasoEntity.toDomain(): RutinaPaso = RutinaPaso(
        id = id,
        rutinaId = rutinaId,
        orden = orden,
        titulo = titulo,
        descripcion = descripcion,
        duracionSegundos = duracionSegundos,
        iconAsset = iconAsset
    )
}
