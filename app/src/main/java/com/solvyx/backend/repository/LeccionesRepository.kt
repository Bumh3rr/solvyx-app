package com.solvyx.backend.repository

import com.solvyx.backend.assets.AssetsSeeder
import com.solvyx.backend.data.local.dao.LeccionDao
import com.solvyx.backend.data.local.dao.LeccionProgresoDao
import com.solvyx.backend.data.local.entity.LeccionEntity
import com.solvyx.backend.data.local.entity.LeccionProgresoEntity
import com.solvyx.backend.models.ContenidoLeccion
import com.solvyx.backend.models.Leccion
import com.solvyx.backend.models.SeccionLeccion
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de [Leccion] (lecciones educativas por sustancia y tema)
 * + progreso de lectura del usuario.
 */
interface LeccionesRepository {
    /** Flujo de lecciones activas. */
    fun observeLecciones(): Flow<List<Leccion>>

    /** Flujo de lecciones de una sustancia concreta. */
    fun observeBySustancia(sustancia: String): Flow<List<Leccion>>

    suspend fun findBySlug(slug: String): Leccion?

    /** Marca una lección como leída. Idempotente. */
    suspend fun marcarComoLeida(slug: String)

    /** Slugs de lecciones que el usuario marcó como leídas. */
    fun observeLeidas(): Flow<List<String>>

    /**
     * Flujo de lecciones de una sustancia, ya cruzadas con el flag `leida`.
     * La UI lo prefiere sobre [observeBySustancia] cuando quiere mostrar
     * la insignia "leída".
     */
    fun observeBySustanciaConProgreso(sustancia: String): Flow<List<Pair<Leccion, Boolean>>>

    suspend fun refresh()
}

@Singleton
class LeccionesRepositoryImpl @Inject constructor(
    private val dao: LeccionDao,
    private val progresoDao: LeccionProgresoDao,
    private val seeder: AssetsSeeder
) : LeccionesRepository {

    override fun observeLecciones(): Flow<List<Leccion>> =
        dao.observeActivas().map { it.map(::toDomain) }

    override fun observeBySustancia(sustancia: String): Flow<List<Leccion>> =
        dao.observeBySustancia(sustancia).map { it.map(::toDomain) }

    override suspend fun findBySlug(slug: String): Leccion? =
        dao.findBySlug(slug)?.let(::toDomain)

    override suspend fun marcarComoLeida(slug: String) {
        progresoDao.marcarLeida(slug, System.currentTimeMillis())
    }

    override fun observeLeidas(): Flow<List<String>> = progresoDao.observeLeidas()

    override fun observeBySustanciaConProgreso(sustancia: String): Flow<List<Pair<Leccion, Boolean>>> =
        combine(
            dao.observeBySustancia(sustancia),
            progresoDao.observeLeidas()
        ) { entidades, slugsLeidas ->
            val setLeidas = slugsLeidas.toSet()
            entidades.map { toDomain(it) to (it.slug in setLeidas) }
        }

    override suspend fun refresh() {
        seeder.ensureLoaded()
    }

    // ---------------------------------------------------------------
    // Mapper
    // ---------------------------------------------------------------

    private val gson = Gson()
    private val seccionesType = object : TypeToken<List<SeccionLeccion>>() {}.type

    private fun toDomain(entity: LeccionEntity): Leccion = Leccion(
        id = entity.id,
        slug = entity.slug,
        sustancia = entity.sustancia,
        tema = entity.tema,
        titulo = entity.titulo,
        contenido = parseContenido(entity.contenido),
        duracionLecturaMinutos = entity.duracionLecturaMinutos,
        orden = entity.orden,
        activo = entity.activo
    )

    private fun parseContenido(raw: String): ContenidoLeccion {
        val obj = runCatching { gson.fromJson(raw, JsonObject::class.java) }.getOrNull()
            ?: return ContenidoLeccion("", emptyList(), "")
        val introduccion = obj.get("introduccion")?.asString.orEmpty()
        val secciones: List<SeccionLeccion> = runCatching {
            gson.fromJson<List<SeccionLeccion>>(obj.get("secciones"), seccionesType)
        }.getOrNull() ?: emptyList()
        val conclusion = obj.get("conclusion")?.asString.orEmpty()
        return ContenidoLeccion(introduccion, secciones, conclusion)
    }
}
