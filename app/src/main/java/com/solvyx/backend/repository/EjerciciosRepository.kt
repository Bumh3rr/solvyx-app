package com.solvyx.backend.repository

import com.solvyx.backend.assets.AssetsSeeder
import com.solvyx.backend.data.local.dao.EjercicioDao
import com.solvyx.backend.data.local.entity.EjercicioEntity
import com.solvyx.backend.models.Ejercicio
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio del catálogo de [Ejercicio] (respiración, body scan, etc.).
 *
 * Contrato:
 * - `observe*` devuelven `Flow` reactivo (se actualiza ante cualquier
 *   cambio en la tabla).
 * - `find*` devuelven un snapshot suspend (un único valor, sin observación).
 * - `refresh` delega en el [AssetsSeeder] para recargar desde assets.
 */
interface EjerciciosRepository {
    fun observeEjercicios(): Flow<List<Ejercicio>>
    fun observeByTipo(tipo: String): Flow<List<Ejercicio>>
    suspend fun findBySlug(slug: String): Ejercicio?
    suspend fun refresh()
}

@Singleton
class EjerciciosRepositoryImpl @Inject constructor(
    private val dao: EjercicioDao,
    private val seeder: AssetsSeeder
) : EjerciciosRepository {

    override fun observeEjercicios(): Flow<List<Ejercicio>> =
        dao.observeActivos().map { it.map(::toDomain) }

    override fun observeByTipo(tipo: String): Flow<List<Ejercicio>> =
        dao.observeByTipo(tipo).map { it.map(::toDomain) }

    override suspend fun findBySlug(slug: String): Ejercicio? =
        dao.findBySlug(slug)?.let(::toDomain)

    override suspend fun refresh() {
        seeder.ensureLoaded()
    }

    // ---------------------------------------------------------------
    // Mapper entity → domain
    // ---------------------------------------------------------------

    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    private fun toDomain(entity: EjercicioEntity): Ejercicio {
        // Decodificación segura: si el JSON está corrupto o vacío, devolvemos
        // valores por defecto en lugar de lanzar y romper la UI.
        val pasos: List<String> = runCatching { gson.fromJson<List<String>>(entity.pasos, listType) }
            .getOrNull() ?: emptyList()
        val ttsText: Map<String, String> = runCatching { gson.fromJson<Map<String, String>>(entity.ttsText, mapType) }
            .getOrNull() ?: emptyMap()

        return Ejercicio(
            id = entity.id,
            slug = entity.slug,
            nombre = entity.nombre,
            tipo = entity.tipo,
            duracionMinutos = entity.duracionMinutos,
            descripcionCorta = entity.descripcionCorta,
            pasos = pasos,
            ttsText = ttsText,
            iconAsset = entity.iconAsset,
            orden = entity.orden,
            activo = entity.activo
        )
    }
}
