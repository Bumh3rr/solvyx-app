package com.solvyx.backend.insights.rules

import com.solvyx.backend.insights.AccionInsight
import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.InsightRule
import com.solvyx.backend.insights.Severidad
import com.solvyx.backend.insights.TipoAccion
import com.solvyx.backend.insights.TipoInsight
import com.solvyx.backend.models.BitacoraEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Regla: `consumo_reciente`.
 *
 * **Disparador**: hay al menos una entrada con `consumio = true`
 * en las últimas [VENTANA_HORAS] horas.
 *
 * **Severidad**: MEDIA, tipo OBSERVACION. No es una alarma pero
 * sí una invitación a la reflexión.
 *
 * **Texto validado por RD**: el copy de este insight es sensible.
 * `backend-content-curator` debe coordinar con el equipo de
 * Reducción de Daños para validar el tono. La regla solo aporta
 * el `id` y los `datos` (sustancia, fecha); el copy lo diseña
 * otro agente.
 *
 * **Acción sugerida**: `VER_BITACORA`. El usuario puede querer
 * revisar/editar el registro o ver la entrada completa.
 *
 * **Por qué 24h**: ventana suficiente para que el insight sea
 * oportunamente relevante sin ser intrusivo. Más allá de 24h el
 * copy pierde sentido ("ayer registraste..." deja de aplicar).
 */
class ConsumoRecienteRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            val ventanaMs = TimeUnit.HOURS.toMillis(VENTANA_HORAS.toLong())
            val cutoff = System.currentTimeMillis() - ventanaMs

            val masReciente = entries.asSequence()
                .filter { it.consumio && it.fecha >= cutoff }
                .maxByOrNull { it.fecha }

                ?: return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.OBSERVACION,
                severidad = Severidad.MEDIA,
                ventanaTexto = "últimas 24 horas",
                datos = mapOf(
                    "sustancia" to (masReciente.sustancia ?: "no especificada"),
                    "fecha" to masReciente.fecha
                ),
                accion = AccionInsight(tipo = TipoAccion.VER_BITACORA)
            )
        }

    companion object {
        const val ID = "consumo_reciente"
        const val VENTANA_HORAS = 24
    }
}