package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EmocionRecurrenteRuleTest {

    private val regla = EmocionRecurrenteRule()

    @Test
    fun `regla emite insight cuando emocion aparece 3 veces en 7 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).conEstadoAnimo("feliz").build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(EmocionRecurrenteRule.ID, insight!!.id)
        assertEquals("ansioso", insight.datos["emocion"])
        assertEquals(3, insight.datos["frecuencia"])
        assertEquals(TipoInsight.OBSERVACION, insight.tipo)
        assertEquals(Severidad.MEDIA, insight.severidad)
    }

    @Test
    fun `regla emite insight con emocion triste recurrente`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conEstadoAnimo("triste").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conEstadoAnimo("triste").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 5).conEstadoAnimo("triste").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 6).conEstadoAnimo("neutral").build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals("triste", insight!!.datos["emocion"])
        assertEquals(3, insight.datos["frecuencia"])
    }

    @Test
    fun `regla no emite cuando ninguna emocion llega a 3 repeticiones`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conEstadoAnimo("feliz").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).conEstadoAnimo("feliz").build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla ignora entradas fuera de ventana de 7 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            // Hace 10 días: 3 veces "ansioso" (NO debe contar).
            BitacoraEntryFixture(now).conFecha(diasAtras = 10).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 11).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 12).conEstadoAnimo("ansioso").build(),
            // Últimos 7 días: solo 1 vez.
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conEstadoAnimo("ansioso").build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con lista vacia`() = runTest {
        assertNull(regla.evaluate(emptyList()))
    }

    @Test
    fun `regla ignora entradas con estadoAnimo vacio`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conEstadoAnimo("").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conEstadoAnimo("   ").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conEstadoAnimo("feliz").build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla toma la emocion mas frecuente cuando hay varias candidatas`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conEstadoAnimo("feliz").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conEstadoAnimo("feliz").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).conEstadoAnimo("ansioso").build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 4).conEstadoAnimo("ansioso").build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals("ansioso", insight!!.datos["emocion"])
        assertEquals(3, insight.datos["frecuencia"])
    }
}