package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RachaRegistroRuleTest {

    private val regla = RachaRegistroRule()

    @Test
    fun `regla emite reconocimiento con 5 dias consecutivos`() = runTest {
        val now = System.currentTimeMillis()
        val entries = (0..4).map { i ->
            BitacoraEntryFixture(now).conFecha(diasAtras = i).build()
        }

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(RachaRegistroRule.ID, insight!!.id)
        assertEquals(TipoInsight.RECONOCIMIENTO, insight.tipo)
        assertEquals(Severidad.BAJA, insight.severidad)
        assertEquals(5, insight.datos["dias"])
    }

    @Test
    fun `regla emite reconocimiento con 7 dias consecutivos`() = runTest {
        val now = System.currentTimeMillis()
        val entries = (0..6).map { i ->
            BitacoraEntryFixture(now).conFecha(diasAtras = i).build()
        }

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(7, insight!!.datos["dias"])
    }

    @Test
    fun `regla no emite con 4 dias consecutivos`() = runTest {
        val now = System.currentTimeMillis()
        val entries = (0..3).map { i ->
            BitacoraEntryFixture(now).conFecha(diasAtras = i).build()
        }

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con lista vacia`() = runTest {
        assertNull(regla.evaluate(emptyList()))
    }

    @Test
    fun `regla no emite cuando hay gap en dias recientes`() = runTest {
        val now = System.currentTimeMillis()
        // 5 entradas pero con un hueco de 2 días.
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            // gap: días 1 y 2 sin entrada
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 4).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 5).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 6).build()
        )

        // La racha desde hoy se rompe en el día 1. La regla considera
        // la racha actual empezando por hoy (o ayer si hoy no hay entrada).
        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla cuenta multiples entradas en el mismo dia como un solo dia`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            // Hoy: 2 entradas (mañana y noche)
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 4).build()
        )

        val insight = regla.evaluate(entries)
        assertNotNull(insight)
        // 5 días únicos: hoy, -1, -2, -3, -4.
        assertEquals(5, insight!!.datos["dias"])
    }

    @Test
    fun `regla permite que hoy no tenga entrada pero ayer si sin romper racha`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            // Hoy sin entrada
            // Ayer y 4 días atrás consecutivos.
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 4).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 5).build()
        )

        val insight = regla.evaluate(entries)
        assertNotNull("La racha debe contar desde ayer si hoy no hay entrada", insight)
        assertEquals(5, insight!!.datos["dias"])
    }
}