package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.InsightRule
import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.backend.models.BitacoraEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Regla: `craving_dia_semana`.
 *
 * **Disparador**: en los últimos 30 días, un día de la semana
 * (lunes, martes, ...) concentra ≥ 60% del total de cravings,
 * siempre que ese día tenga ≥ [MIN_CRAVINGS_DIA] cravings registrados.
 *
 * **Severidad**: BAJA, tipo OBSERVACION. Es un dato, no una alerta.
 *
 * **Por qué 60% y no mayoría absoluta**: si el usuario tiene 3
 * cravings el martes y 2 el jueves, el martes ya es 60% (3/5) y
 * vale la pena mencionarlo. Un umbral más alto exigiría muchos
 * cravings para activarse.
 *
 * **Cálculo**: agrupa por día de la semana (Calendar.DAY_OF_WEEK)
 * usando la zona horaria del dispositivo. Esto refleja cuándo
 * SIENTE el craving, no cuándo se guardó el timestamp UTC.
 *
 * **Tie-break**: si dos días empatan, gana el de la entrada más
 * reciente (ordenamos por `maxByOrNull`).
 */
class CravingsAgrupadosPorDiaRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            val ventanaMs = TimeUnit.DAYS.toMillis(VENTANA_DIAS.toLong())
            val cutoff = System.currentTimeMillis() - ventanaMs

            val cravings = entries.asSequence()
                .filter { it.fecha >= cutoff && it.tuvoCraving == true }
                .toList()

            if (cravings.size < MIN_CRAVINGS_TOTAL) return@withContext null

            // Agrupar por día de la semana (Calendar.DAY_OF_WEEK -> nombre).
            val porDia: Map<Int, List<BitacoraEntry>> = cravings
                .groupBy { entry ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = entry.fecha
                    }
                    cal.get(Calendar.DAY_OF_WEEK)
                }

            val total = cravings.size

            // Buscar el primer día que cumpla umbral y mínimo absoluto.
            // Iteramos en orden de DAY_OF_WEEK (1=domingo .. 7=sábado)
            // para determinismo.
            val candidato = porDia.entries
                .asSequence()
                .filter { (_, lista) ->
                    lista.size >= MIN_CRAVINGS_DIA &&
                        lista.size.toDouble() / total >= UMBRAL_CONCENTRACION
                }
                .maxWithOrNull(compareBy<Map.Entry<Int, List<BitacoraEntry>>> { it.value.size }
                    .thenBy { it.value.maxOf { entry -> entry.fecha } })

            val (diaSemana, lista) = candidato ?: return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.OBSERVACION,
                severidad = Severidad.BAJA,
                ventanaTexto = "último mes",
                datos = mapOf(
                    "dia" to NOMBRES_DIAS[diaSemana]!!,
                    "cravings_dia" to lista.size,
                    "cravings_total" to total
                )
            )
        }

    companion object {
        const val ID = "craving_dia_semana"
        const val MIN_CRAVINGS_TOTAL = 4
        const val MIN_CRAVINGS_DIA = 2
        const val UMBRAL_CONCENTRACION = 0.60
        const val VENTANA_DIAS = 30

        /** Nombres de los días de la semana indexados por Calendar.DAY_OF_WEEK. */
        val NOMBRES_DIAS = mapOf(
            Calendar.SUNDAY to "domingo",
            Calendar.MONDAY to "lunes",
            Calendar.TUESDAY to "martes",
            Calendar.WEDNESDAY to "miércoles",
            Calendar.THURSDAY to "jueves",
            Calendar.FRIDAY to "viernes",
            Calendar.SATURDAY to "sábado"
        )
    }
}