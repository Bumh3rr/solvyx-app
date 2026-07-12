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
 * Regla: `gap_registro`.
 *
 * **Disparador**: han pasado ≥ [MIN_DIAS_SIN_REGISTRAR] días
 * desde la última entrada del usuario, sin tener en cuenta
 * entradas futuras (no hay).
 *
 * **Severidad**: BAJA, tipo OBSERVACION. Es un recordatorio
 * respetuoso, no una reprimenda.
 *
 * **Tolerancia a sub-reporte**: la regla NO castiga al usuario.
 * El copy de `backend-content-curator` debe ser neutral
 * ("aquí sigo cuando quieras"), nunca culpabilizador.
 *
 * **Orden de [entries]**: se asume que vienen ordenadas por fecha
 * desc (lo que hace el DAO). Si no, hacemos una pasada O(n) para
 * encontrar la última por seguridad.
 */
class GapRegistroRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            if (entries.isEmpty()) return@withContext null

            val ultimaFecha = entries.maxOf { it.fecha }
            val ahora = System.currentTimeMillis()
            val diasSinRegistrar = TimeUnit.MILLISECONDS.toDays(ahora - ultimaFecha)

            if (diasSinRegistrar < MIN_DIAS_SIN_REGISTRAR) return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.OBSERVACION,
                severidad = Severidad.BAJA,
                ventanaTexto = "sin registros",
                datos = mapOf("dias_sin_registrar" to diasSinRegistrar)
            )
        }

    companion object {
        const val ID = "gap_registro"
        const val MIN_DIAS_SIN_REGISTRAR = 5L
    }
}