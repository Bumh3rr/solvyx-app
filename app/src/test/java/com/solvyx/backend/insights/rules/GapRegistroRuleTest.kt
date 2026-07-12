package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GapRegistroRuleTest {

    private val regla = GapRegistroRule()

    @Test
    fun `regla emite cuando han pasado 5 dias sin registro`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 5).build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(GapRegistroRule.ID, insight!!.id)
        assertEquals(TipoInsight.OBSERVACION, insight.tipo)
        assertEquals(Severidad.BAJA, insight.severidad)
        // días_sin_registrar ≥ 5
        val dias = insight.datos["dias_sin_registrar"] as Long
        assert(dias >= 5L) { "dias_sin_registrar debería ser ≥ 5, fue $dias" }
    }

    @Test
    fun `regla emite cuando han pasado 10 dias sin registro`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 10).build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        val dias = insight!!.datos["dias_sin_registrar"] as Long
        assert(dias >= 10L)
    }

    @Test
    fun `regla no emite si la ultima entrada es de hace 4 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 4).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite si la ultima entrada es de hoy`() = runTest {
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
    fun `regla considera la entrada mas reciente incluso si hay otras mas antiguas`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 30).build()
        )

        // La más reciente es hoy: no hay gap.
        assertNull(regla.evaluate(entries))
    }
}