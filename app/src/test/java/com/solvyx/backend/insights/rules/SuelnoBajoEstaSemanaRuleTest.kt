package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.backend.models.BitacoraEntry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SuelnoBajoEstaSemanaRuleTest {

    private val regla = SuelnoBajoEstaSemanaRule()

    // ── Caso positivo ────────────────────────────────────────────────────

    @Test
    fun `regla emite insight cuando promedio de sueno es menor a 6h con 3 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conSuenoHoras(4).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conSuenoHoras(5).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conSuenoHoras(4).build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull("Debe emitir insight con 3 días < 6h", insight)
        assertEquals(SuelnoBajoEstaSemanaRule.ID, insight!!.id)
        assertEquals(Severidad.MEDIA, insight.severidad)
        assertEquals(TipoInsight.OBSERVACION, insight.tipo)
        assertEquals("esta semana", insight.ventanaTexto)
        val promedio = insight.datos["promedio"] as Double
        assertEquals(4.33, promedio, 0.01)
        assertEquals(3, insight.datos["dias_con_datos"])
    }

    @Test
    fun `regla emite insight con mas dias con sueno bajo`() = runTest {
        val now = System.currentTimeMillis()
        val entries = (0..6).map { offset ->
            BitacoraEntryFixture(now)
                .conFecha(diasAtras = offset)
                .conSuenoHoras(5) // promedio 5.0 < 6.0
                .build()
        }

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(7, insight!!.datos["dias_con_datos"])
    }

    // ── Caso negativo ────────────────────────────────────────────────────

    @Test
    fun `regla no emite cuando promedio es 6h o mas`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conSuenoHoras(7).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conSuenoHoras(6).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conSuenoHoras(8).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite cuando promedio es exactamente 6h`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conSuenoHoras(6).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conSuenoHoras(6).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conSuenoHoras(6).build()
        )

        // promedio = 6.0; el comparador es estricto (>= 6.0 → no emitir).
        assertNull(regla.evaluate(entries))
    }

    // ── Datos insuficientes ──────────────────────────────────────────────

    @Test
    fun `regla no emite con menos de 3 dias con dato`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conSuenoHoras(4).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conSuenoHoras(5).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con entradas sin dato de sueno`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con lista vacia`() = runTest {
        assertNull(regla.evaluate(emptyList()))
    }

    // ── Tolerancia a gaps ────────────────────────────────────────────────

    @Test
    fun `regla ignora entradas fuera de la ventana de 7 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            // Hace 10 días: dormir mal (NO debería contar).
            BitacoraEntryFixture(now).conFecha(diasAtras = 10).conSuenoHoras(3).build(),
            // Hace 30 días: dormir mal.
            BitacoraEntryFixture(now).conFecha(diasAtras = 30).conSuenoHoras(2).build(),
            // Últimos 7 días: dormir bien.
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conSuenoHoras(7).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conSuenoHoras(8).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conSuenoHoras(7).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla cuenta solo entradas con sueno no nulo`() = runTest {
        val now = System.currentTimeMillis()
        val entries: List<BitacoraEntry> = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conSuenoHoras(4).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).build(), // sin sueno
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conSuenoHoras(5).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 3).build(), // sin sueno
            BitacoraEntryFixture(now).conFecha(diasAtras = 4).conSuenoHoras(4).build()
        )

        val insight = regla.evaluate(entries)
        assertNotNull(insight)
        // promedio de 4+5+4 = 13/3 = 4.33
        val promedio = insight!!.datos["promedio"] as Double
        assertEquals(4.33, promedio, 0.01)
        assertEquals(3, insight.datos["dias_con_datos"])
        assertTrue(insight.severidad == Severidad.MEDIA)
    }
}