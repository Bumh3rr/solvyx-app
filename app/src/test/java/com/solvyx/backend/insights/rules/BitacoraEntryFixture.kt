package com.solvyx.backend.insights.rules

import com.solvyx.backend.models.BitacoraEntry
import java.util.concurrent.TimeUnit

/**
 * Builder fluido para construir [BitacoraEntry] en tests.
 *
 * Por defecto:
 * - `id` se asigna en orden (1, 2, 3, ...).
 * - `fecha` se calcula relativo a [nowMs] (por defecto "ahora"), restando
 *   [diasAtras] * 1 día.
 * - `estadoAnimo` = `"neutral"`, `consumio` = `false`, `sustancia` = `null`.
 * - Todos los campos extendidos son `null` salvo que se sobreescriban.
 *
 * Ejemplo:
 * ```
 * BitacoraEntryFixture(nowMs)
 *     .conFecha(diasAtras = 0)
 *     .conEstadoAnimo("ansioso")
 *     .conSuenoHoras(5)
 *     .build()
 * ```
 *
 * Pensado para tests deterministas: el `nowMs` se inyecta para evitar
 * depender del reloj del sistema y poder validar ventanas temporales
 * sin flaky tests.
 */
class BitacoraEntryFixture(
    private val nowMs: Long = System.currentTimeMillis()
) {
    private var id: Int = 0
    private var fecha: Long = nowMs
    private var estadoAnimo: String = "neutral"
    private var consumio: Boolean = false
    private var sustancia: String? = null
    private var nota: String? = null
    private var suenoHoras: Int? = null
    private var suenoCalidad: Int? = null
    private var comio: Boolean? = null
    private var calidadComida: Int? = null
    private var actividadFisica: String? = null
    private var contextoSocial: String? = null
    private var detonantePrincipal: String? = null
    private var nivelAnsiedad: Int? = null
    private var tuvoCraving: Boolean? = null
    private var ejercicioFisico: Boolean? = null
    private var notaPrivada: String? = null
    private var updatedAt: Long = nowMs

    /** Fija el id explícitamente (útil para asserts). */
    fun conId(value: Int): BitacoraEntryFixture = apply { id = value }

    /**
     * Fija la fecha a [diasAtras] días antes de `nowMs`.
     *
     * Usa [diasAtras] * 24h exactos. Esto es estable para los
     * tests porque las reglas filtran por rangos de ms.
     */
    fun conFecha(diasAtras: Int): BitacoraEntryFixture = apply {
        fecha = nowMs - TimeUnit.DAYS.toMillis(diasAtras.toLong())
    }

    /** Fija la fecha a un timestamp arbitrario (epoch millis). */
    fun conFechaMs(value: Long): BitacoraEntryFixture = apply { fecha = value }

    fun conEstadoAnimo(value: String): BitacoraEntryFixture =
        apply { estadoAnimo = value }

    fun conConsumio(value: Boolean, sustancia: String? = null): BitacoraEntryFixture =
        apply { consumio = value; this.sustancia = sustancia }

    fun conNota(value: String?): BitacoraEntryFixture =
        apply { nota = value }

    fun conSuenoHoras(value: Int?): BitacoraEntryFixture =
        apply { suenoHoras = value }

    fun conSuenoCalidad(value: Int?): BitacoraEntryFixture =
        apply { suenoCalidad = value }

    fun conComio(value: Boolean?): BitacoraEntryFixture =
        apply { comio = value }

    fun conCalidadComida(value: Int?): BitacoraEntryFixture =
        apply { calidadComida = value }

    fun conActividadFisica(value: String?): BitacoraEntryFixture =
        apply { actividadFisica = value }

    fun conContextoSocial(value: String?): BitacoraEntryFixture =
        apply { contextoSocial = value }

    fun conDetonantePrincipal(value: String?): BitacoraEntryFixture =
        apply { detonantePrincipal = value }

    fun conNivelAnsiedad(value: Int?): BitacoraEntryFixture =
        apply { nivelAnsiedad = value }

    fun conTuvoCraving(value: Boolean?): BitacoraEntryFixture =
        apply { tuvoCraving = value }

    fun conEjercicioFisico(value: Boolean?): BitacoraEntryFixture =
        apply { ejercicioFisico = value }

    fun conNotaPrivada(value: String?): BitacoraEntryFixture =
        apply { notaPrivada = value }

    fun conUpdatedAt(value: Long): BitacoraEntryFixture =
        apply { updatedAt = value }

    fun build(): BitacoraEntry = BitacoraEntry(
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

    companion object {
        /** Genera una lista de N entradas con los días de atraso dados. */
        fun buildList(
            nowMs: Long = System.currentTimeMillis(),
            count: Int,
            block: (Int, BitacoraEntryFixture) -> Unit
        ): List<BitacoraEntry> = (0 until count).map { i ->
            val fixture = BitacoraEntryFixture(nowMs).conId(i + 1)
            block(i, fixture)
            fixture.build()
        }
    }
}