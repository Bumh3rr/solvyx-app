package com.solvyx.backend.repository

import com.solvyx.backend.assets.AssetsSeeder
import com.solvyx.backend.data.local.dao.GuiaExtendidaDao
import com.solvyx.backend.data.local.entity.GuiaExtendidaEntity
import com.solvyx.backend.models.ContenidoGuia
import com.solvyx.backend.models.GuiaExtendida
import com.solvyx.backend.models.LineaAyuda
import com.solvyx.backend.models.PasoGuia
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de [GuiaExtendida] (guías contextuales: crisis, craving, etc.).
 */
interface GuiasExtendidasRepository {
    fun observeGuias(): Flow<List<GuiaExtendida>>
    fun observeByCategoria(categoria: String): Flow<List<GuiaExtendida>>
    suspend fun findBySlug(slug: String): GuiaExtendida?
    suspend fun refresh()
}

@Singleton
class GuiasExtendidasRepositoryImpl @Inject constructor(
    private val dao: GuiaExtendidaDao,
    private val seeder: AssetsSeeder
) : GuiasExtendidasRepository {

    override fun observeGuias(): Flow<List<GuiaExtendida>> =
        dao.observeActivas().map { it.map(::toDomain) }

    override fun observeByCategoria(categoria: String): Flow<List<GuiaExtendida>> =
        dao.observeByCategoria(categoria).map { it.map(::toDomain) }

    override suspend fun findBySlug(slug: String): GuiaExtendida? =
        dao.findBySlug(slug)?.let(::toDomain)

    override suspend fun refresh() {
        seeder.ensureLoaded()
    }

    // ---------------------------------------------------------------
    // Mapper
    // ---------------------------------------------------------------

    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val pasoListType = object : TypeToken<List<PasoGuia>>() {}.type
    private val lineaListType = object : TypeToken<List<LineaAyuda>>() {}.type

    private fun toDomain(entity: GuiaExtendidaEntity): GuiaExtendida {
        val contenido = parseContenido(entity.contenido)
        return GuiaExtendida(
            id = entity.id,
            slug = entity.slug,
            titulo = entity.titulo,
            categoria = entity.categoria,
            descripcionCorta = entity.descripcionCorta,
            contenido = contenido,
            iconAsset = entity.iconAsset,
            orden = entity.orden,
            activo = entity.activo
        )
    }

    /**
     * Parsea el JSON-encoded `contenido` de la entity a [ContenidoGuia].
     *
     * Si el JSON está corrupto, devuelve un [ContenidoGuia] con campos
     * vacíos (en lugar de lanzar). Esto evita que un dato mal formado
     * rompa la pantalla entera; la UI verá una guía con solo el
     * `titulo` y un mensaje de fallback.
     */
    private fun parseContenido(raw: String): ContenidoGuia {
        val obj = runCatching { gson.fromJson(raw, JsonObject::class.java) }.getOrNull()
            ?: return ContenidoGuia("", emptyList(), emptyList(), emptyList(), emptyList())

        val introduccion = obj.get("introduccion")?.asString.orEmpty()
        val pasos: List<PasoGuia> = runCatching {
            gson.fromJson<List<PasoGuia>>(obj.get("pasos"), pasoListType)
        }.getOrNull() ?: emptyList()
        val senales: List<String> = runCatching {
            gson.fromJson<List<String>>(obj.get("senalesAlerta"), stringListType)
        }.getOrNull() ?: emptyList()
        val cuando911: List<String> = runCatching {
            gson.fromJson<List<String>>(obj.get("cuandoLlamar911"), stringListType)
        }.getOrNull() ?: emptyList()
        val lineas: List<LineaAyuda> = runCatching {
            gson.fromJson<List<LineaAyuda>>(obj.get("lineasAyuda"), lineaListType)
        }.getOrNull() ?: emptyList()

        return ContenidoGuia(
            introduccion = introduccion,
            pasos = pasos,
            senalesAlerta = senales,
            cuandoLlamar911 = cuando911,
            lineasAyuda = lineas
        )
    }
}
