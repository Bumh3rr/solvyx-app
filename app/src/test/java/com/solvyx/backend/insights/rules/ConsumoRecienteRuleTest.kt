package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoAccion
import com.solvyx.backend.insights.TipoInsight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConsumoRecienteRuleTest {

    private val regla = ConsumoRecienteRule()

    @Test
    fun `regla emite insight cuando hubo consumo en las ultimas 24h`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now)
                .conFecha(diasAtras = 0)
                .conConsumio(true, sustancia = "alcohol")
                .build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(ConsumoRecienteRule.ID, insight!!.id)
        assertEquals(TipoInsight.OBSERVACION, insight.tipo)
        assertEquals(Severidad.MEDIA, insight.severidad)
        assertEquals("alcohol", insight.datos["sustancia"])
        assertEquals("últimas 24 horas", insight.ventanaTexto)
        assertNotNull(insight.accion)
        assertEquals(TipoAccion.VER_BITACORA, insight.accion!!.tipo)
    }

    @Test
    fun `regla emite con sustancia null como no especificada`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now)
                .conFecha(diasAtras = 0)
                .conConsumio(true, sustancia = null)
                .build()
        )

        val insight = regla.evaluate(entries)
        assertNotNull(insight)
        assertEquals("no especificada", insight!!.datos["sustancia"])
    }

    @Test
    fun `regla usa la entrada mas reciente si hay varias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0)
                .conConsumio(true, sustancia = "alcohol").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1)
                .conConsumio(true, sustancia = "tabaco").build()
        )

        val insight = regla.evaluate(entries)
        assertNotNull(insight)
        assertEquals("alcohol", insight!!.datos["sustancia"])
    }

    @Test
    fun `regla no emite cuando no hubo consumo reciente`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conConsumio(false).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conConsumio(false).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite si el consumo fue hace mas de 24h`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conConsumio(true).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con lista vacia`() = runTest {
        assertNull(regla.evaluate(emptyList()))
    }
}