package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class CravingsAgrupadosPorDiaRuleTest {

    private val regla = CravingsAgrupadosPorDiaRule()

    /**
     * Helper: devuelve una fecha que cae en el día de la semana pedido.
     * Como `Calendar` se inicializa con `now`, retrocedemos hasta el
     * día objetivo dentro de los últimos 30 días.
     */
    private fun fechaEnDiaSemana(
        nowMs: Long,
        targetDow: Int, // Calendar.MONDAY, etc.
        diasAtras: Int
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs - diasAtras * 24L * 60L * 60L * 1000L
        }
        val diff = cal.get(Calendar.DAY_OF_WEEK) - targetDow
        cal.add(Calendar.DAY_OF_MONTH, -diff)
        return cal.timeInMillis
    }

    @Test
    fun `regla emite insight cuando un dia concentra 60 por ciento o mas`() = runTest {
        val now = System.currentTimeMillis()
        // Simulamos 4 cravings: 3 lunes + 1 martes.
        val entries = listOf(
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 0))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 7))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 14))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.TUESDAY, 3))
                .conTuvoCraving(true).build()
        )

        val insight = regla.evaluate(entries)

        assertNotNull(insight)
        assertEquals(CravingsAgrupadosPorDiaRule.ID, insight!!.id)
        assertEquals("lunes", insight.datos["dia"])
        assertEquals(3, insight.datos["cravings_dia"])
        assertEquals(4, insight.datos["cravings_total"])
        assertEquals(TipoInsight.OBSERVACION, insight.tipo)
        assertEquals(Severidad.BAJA, insight.severidad)
    }

    @Test
    fun `regla no emite cuando los cravings estan repartidos`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 0))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.TUESDAY, 0))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.WEDNESDAY, 0))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.THURSDAY, 0))
                .conTuvoCraving(true).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con menos de 4 cravings totales`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 0))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 7))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 14))
                .conTuvoCraving(true).build()
        )

        // 3/3 = 100% pero < MIN_CRAVINGS_TOTAL=4
        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite sin cravings`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conTuvoCraving(false).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conTuvoCraving(null).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conTuvoCraving(false).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla ignora cravings fuera de ventana de 30 dias`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            // Hace 40 días: 3 lunes con craving (fuera de ventana).
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 40))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 45))
                .conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFechaMs(fechaEnDiaSemana(now, Calendar.MONDAY, 50))
                .conTuvoCraving(true).build(),
            // Esta semana: solo 1 craving
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conTuvoCraving(true).build()
        )

        assertNull(regla.evaluate(entries))
    }

    @Test
    fun `regla no emite con un solo dia con cravings aunque concentre 100 por ciento`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            BitacoraEntryFixture(now).conFecha(diasAtras = 0).conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 1).conTuvoCraving(true).build(),
            BitacoraEntryFixture(now).conFecha(diasAtras = 2).conTuvoCraving(true).build()
        )

        // 3 cravings el mismo día (100%), pero < MIN_CRAVINGS_TOTAL=4.
        assertNull(regla.evaluate(entries))
    }
}