package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.models.BitacoraEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio "extendido" de la bitácora.
 *
 * Vive **al lado** de [BitacoraRepository] (no lo reemplaza) para no
 * romper el `RegistroViewModel` existente en `ui/screens/bitacora/`. La
 * idea es:
 * - [BitacoraRepository] sigue siendo el punto de entrada del flujo
 *   emocional básico (estado de ánimo, consumo, sustancia, nota) que ya
 *   está en producción.
 * - [BitacoraExtendidaRepository] se usa desde el `BitacoraExtendidaViewModel`
 *   para escribir/consultar el registro **completo** (básicos + campos
 *   opcionales extendidos).
 *
 * Los dos repositorios comparten la misma entity y la misma tabla
 * (`bitacora`); este expone un modelo de dominio [BitacoraEntry] con
 * todos los campos.
 */
interface BitacoraExtendidaRepository {
    /** Flujo de todas las entradas, ordenadas por fecha desc. */
    fun observar(): Flow<List<BitacoraEntry>>

    /** Snapshot de una entrada por id. */
    suspend fun findById(id: Int): BitacoraEntry?

    /**
     * Inserta (o reemplaza si `id != 0`) una entrada completa con
     * todos los campos, incluyendo los extendidos. Devuelve el `id`
     * de la fila afectada.
     *
     * El caller (típicamente la VM) construye el [BitacoraEntry] desde
     * su UiState y llama aquí; el repositorio se encarga de mapear
     * a la entity y delegar al DAO.
     */
    suspend fun guardar(entry: BitacoraEntry): Result<Int>

    /**
     * Actualiza SOLO los campos extendidos de una entrada, sin tocar
     * los básicos (estado de ánimo, consumo, sustancia, nota).
     *
     * Implementación: lee la fila, copia los nuevos valores, hace update
     * del row completo. Refleja `updatedAt` al now.
     */
    suspend fun actualizarCamposExtendidos(
        id: Int,
        suenoHoras: Int? = null,
        suenoCalidad: Int? = null,
        comio: Boolean? = null,
        calidadComida: Int? = null,
        actividadFisica: String? = null,
        contextoSocial: String? = null,
        detonantePrincipal: String? = null,
        nivelAnsiedad: Int? = null,
        tuvoCraving: Boolean? = null,
        ejercicioFisico: Boolean? = null,
        notaPrivada: String? = null
    ): Result<Unit>
}

@Singleton
class BitacoraExtendidaRepositoryImpl @Inject constructor(
    private val dao: BitacoraDao
) : BitacoraExtendidaRepository {

    override fun observar(): Flow<List<BitacoraEntry>> =
        dao.observar().map { it.map(::toDomain) }

    override suspend fun findById(id: Int): BitacoraEntry? =
        dao.findById(id)?.let(::toDomain)

    override suspend fun guardar(entry: BitacoraEntry): Result<Int> = runCatching {
        dao.insertar(entry.toEntity())
        // `insertar` con REPLACE devuelve Unit; el id que ya tenía la
        // entry es el id real. Si era 0, fue autogenerado por Room y la
        // próxima query (findById) lo recuperaría; para simplificar,
        // devolvemos el id entrante cuando no es 0 y leemos el último
        // cuando es 0.
        if (entry.id == 0) dao.ultima()?.id ?: 0 else entry.id
    }.recoverCatching { e ->
        throw BitacoraExtendidaException(
            "No pudimos guardar tu registro. Inténtalo de nuevo.",
            e
        )
    }

    override suspend fun actualizarCamposExtendidos(
        id: Int,
        suenoHoras: Int?,
        suenoCalidad: Int?,
        comio: Boolean?,
        calidadComida: Int?,
        actividadFisica: String?,
        contextoSocial: String?,
        detonantePrincipal: String?,
        nivelAnsiedad: Int?,
        tuvoCraving: Boolean?,
        ejercicioFisico: Boolean?,
        notaPrivada: String?
    ): Result<Unit> = runCatching {
        val actual = dao.findById(id) ?: throw BitacoraExtendidaException(
            "No encontramos el registro que querías actualizar."
        )

        // Mantener los básicos intactos; aplicar los extendidos que lleguen
        // no nulos (los null = "no se quiere cambiar este campo").
        val actualizado = actual.copy(
            suenoHoras = suenoHoras ?: actual.suenoHoras,
            suenoCalidad = suenoCalidad ?: actual.suenoCalidad,
            comio = comio ?: actual.comio,
            calidadComida = calidadComida ?: actual.calidadComida,
            actividadFisica = actividadFisica ?: actual.actividadFisica,
            contextoSocial = contextoSocial ?: actual.contextoSocial,
            detonantePrincipal = detonantePrincipal ?: actual.detonantePrincipal,
            nivelAnsiedad = nivelAnsiedad ?: actual.nivelAnsiedad,
            tuvoCraving = tuvoCraving ?: actual.tuvoCraving,
            ejercicioFisico = ejercicioFisico ?: actual.ejercicioFisico,
            notaPrivada = notaPrivada ?: actual.notaPrivada,
            updatedAt = System.currentTimeMillis()
        )
        dao.actualizar(actualizado)
    }.recoverCatching { e ->
        if (e is BitacoraExtendidaException) throw e
        throw BitacoraExtendidaException(
            "No pudimos guardar los datos extendidos. Inténtalo de nuevo.",
            e
        )
    }

    // ---------------------------------------------------------------
    // Mappers
    // ---------------------------------------------------------------

    private fun toDomain(entity: BitacoraEntity): BitacoraEntry = BitacoraEntry(
        id = entity.id,
        fecha = entity.fecha,
        estadoAnimo = entity.estadoAnimo,
        consumio = entity.consumio,
        sustancia = entity.sustancia,
        nota = entity.nota,
        suenoHoras = entity.suenoHoras,
        suenoCalidad = entity.suenoCalidad,
        comio = entity.comio,
        calidadComida = entity.calidadComida,
        actividadFisica = entity.actividadFisica,
        contextoSocial = entity.contextoSocial,
        detonantePrincipal = entity.detonantePrincipal,
        nivelAnsiedad = entity.nivelAnsiedad,
        tuvoCraving = entity.tuvoCraving,
        ejercicioFisico = entity.ejercicioFisico,
        notaPrivada = entity.notaPrivada,
        updatedAt = entity.updatedAt
    )

    private fun BitacoraEntry.toEntity(): BitacoraEntity = BitacoraEntity(
        id = id,
        fecha = fecha,
        estadoAnimo = estadoAnimo,
        consumio = consumio,
        sustancia = sustancia,
        nota = nota,
        suenoHoras = suenoHoras,
        suenoCalidad = suenoCalidad,
        comio = comio,
        calidadComida = calidadComida,
        actividadFisica = actividadFisica,
        contextoSocial = contextoSocial,
        detonantePrincipal = detonantePrincipal,
        nivelAnsiedad = nivelAnsiedad,
        tuvoCraving = tuvoCraving,
        ejercicioFisico = ejercicioFisico,
        notaPrivada = notaPrivada,
        updatedAt = updatedAt
    )
}

/** Excepción tipada del repositorio, con mensaje user-friendly. */
class BitacoraExtendidaException(message: String, cause: Throwable? = null) : Exception(message, cause)
