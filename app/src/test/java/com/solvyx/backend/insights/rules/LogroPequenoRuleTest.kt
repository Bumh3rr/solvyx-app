package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LogroPequenoRuleTest {

    private val regla = LogroPequenoRule()

    @Test
    fun `regla emite cuando ultima entrada llega tras gap de 5 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 5).build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(LogroPequenoRule.ID, insight!!.id)
        assertEquals(TipoInsight.RECONOCIMIENTO, insight.tipo)
        assertEquals(Severidad.BAJA, insight.severidad)
        assertEquals(5L, insight.datos["dias_de_ausencia"])
    }

    @Test
    fun `regla emite con gap de 10 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 10).build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(10L, insight!!.datos["dias_de_ausencia"])
    }

    @Test
    fun `regla no emite cuando la ultima entrada es del dia siguiente`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con menos de 2 entradas`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con lista vacia`() = runTest {
        assertNull(regla.evaluate(emptyList()))
    }

    @Test
    fun `regla funciona con entradas desordenadas`() = runTest {
        val now = System.currentTimeMillis()
        // Pasamos la lista desordenada a propósito.
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 5).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build()
        )

        // El segundo y tercer dato están a 1 día. El primero a 5 del más
        // reciente. El gap entre los dos más recientes consecutivos es 1,
        // no 5: la regla NO debe disparar.
        assertNull(regla.evaluate(entries))
    }
}