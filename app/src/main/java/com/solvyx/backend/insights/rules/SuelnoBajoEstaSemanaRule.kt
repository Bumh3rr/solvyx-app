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
 * Regla: `sueño_bajo_esta_semana`.
 *
 * **Disparador**: en los últimos 7 días hay al menos 3 entradas con
 * dato de sueño y el promedio de horas dormidas es `< 6.0`.
 *
 * **Severidad**: MEDIA (observación, no diagnóstico).
 *
 * **Por qué ≥ 3 días con dato**: con 1 o 2 muestras el promedio es
 * muy sensible a outliers y dispara falsos positivos. La regla
 * respeta el principio "no castigar al sub-reporte": si el usuario
 * no está registrando sueño, no le decimos que duerme mal.
 *
 * **Por qué ventana de 7 días**: es la ventana "natural" que el
 * usuario percibe como "esta semana" y la que el copy de
 * `backend-content-curator` referencia.
 */
class SuelnoBajoEstaSemanaRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            val ventanaMs = TimeUnit.DAYS.toMillis(7)
            val cutoff = System.currentTimeMillis() - ventanaMs

            val conSueno = entries
                .asSequence()
                .filter { it.fecha >= cutoff }
                .mapNotNull { it.suenoHoras }
                .toList()

            // Datos insuficientes: no emitir.
            if (conSueno.size < MIN_DIAS_CON_DATO) return@withContext null

            val promedio = conSueno.average()

            // Si el promedio es ≥ 6h, no hay patrón de sueño bajo.
            if (promedio >= UMBRAL_HORAS_SUENO_BAJO) return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.OBSERVACION,
                severidad = Severidad.MEDIA,
                ventanaTexto = "esta semana",
                datos = mapOf(
                    "promedio" to promedio,
                    "dias_con_datos" to conSueno.size
                )
            )
        }

    companion object {
        const val ID = "sueno_bajo_esta_semana"
        const val MIN_DIAS_CON_DATO = 3
        const val UMBRAL_HORAS_SUENO_BAJO = 6.0
    }
}