package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.InsightRule
import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.backend.models.BitacoraEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Regla: `racha_registro`.
 *
 * **Disparador**: hay al menos [MIN_DIAS_RACHA] días *consecutivos*
 * con al menos una entrada registrada.
 *
 * **Severidad**: BAJA, tipo RECONOCIMIENTO. Es un refuerzo
 * positivo, nunca combinado con severidad ALTA.
 *
 * **Tolerancia a gaps**: la racha se rompe si hay un día sin
 * entradas. Si el usuario registró ayer y anteayer, pero no hace 3
 * días, la racha actual es 2 días.
 *
 * El cálculo recorre las fechas únicas (un día = un día calendario),
 * ordenadas desc, y cuenta el tramo inicial consecutivo. O(n) sobre
 * la ventana.
 */
class RachaRegistroRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            if (entries.isEmpty()) return@withContext null

            val now = System.currentTimeMillis()
            val ventanaMs = TimeUnit.DAYS.toMillis(VENTANA_DIAS.toLong())
            val cutoff = now - ventanaMs

            // Días únicos (en epoch días) con al menos una entrada dentro
            // de la ventana. `it / MS_PER_DAY` trunca a día calendario en
            // UTC; sirve para la lógica de racha sin importar la zona.
            val diasConEntrada: Set<Long> = entries.asSequence()
                .filter { it.fecha >= cutoff }
                .map { it.fecha / MS_PER_DAY }
                .toSet()

            if (diasConEntrada.isEmpty()) return@withContext null

            val hoy = now / MS_PER_DAY

            // Contar cuántos días consecutivos hacia atrás tienen entrada,
            // empezando por hoy (o ayer, si el usuario aún no registró
            // hoy: la racha no se rompe por no haber escrito hoy todavía).
            var cursor = hoy
            if (cursor !in diasConEntrada) {
                // Si hoy no hay entrada pero ayer sí, la racha sigue
                // contando desde ayer; si ayer tampoco, racha = 0.
                val ayer = hoy - 1
                if (ayer !in diasConEntrada) return@withContext null
                cursor = ayer
            }

            var racha = 0
            while (cursor in diasConEntrada) {
                racha++
                cursor--
            }

            if (racha < MIN_DIAS_RACHA) return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.RECONOCIMIENTO,
                severidad = Severidad.BAJA,
                ventanaTexto = "últimos $racha días",
                datos = mapOf("dias" to racha)
            )
        }

    companion object {
        const val ID = "racha_registro"
        const val MIN_DIAS_RACHA = 5
        const val VENTANA_DIAS = 30
        private const val MS_PER_DAY = 86_400_000L
    }
}