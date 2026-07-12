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
 * Regla: `logro_pequeno`.
 *
 * **Disparador**: la entrada más reciente fue registrada después
 * de un gap de ≥ [MIN_DIAS_AUSENCIA] días sin entradas.
 *
 * **Severidad**: BAJA, tipo RECONOCIMIENTO. Refuerza
 * positivamente la decisión de volver a registrar.
 *
 * **Por qué importa**: para personas en proceso de cambio, la
 * constancia del registro es predictora de mejor pronóstico. Un
 * "volviste a registrar, eso también cuenta" tiene valor clínico
 * sin ser condescendiente.
 *
 * **Dependencia entre reglas**: esta regla y [GapRegistroRule]
 * son mutuamente excluyentes para el mismo intervalo temporal.
 * El motor emite ambas porque el copy es distinto: una invita a
 * la reflexión ("hace N días que no registras"), la otra celebra
 * el regreso. El ordenamiento por severidad (BAJA ambas) no
 * resuelve esto; depende del copy resolver qué mostrar primero.
 */
class LogroPequenoRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            if (entries.size < 2) return@withContext null

            // Asumimos entries ordenadas desc. Calculamos la fecha de la
            // entrada más reciente y la de la SEGUNDA más reciente. Si
            // la diferencia entre ambas es ≥ umbral, hubo un gap antes
            // del último registro y el usuario volvió.
            val fechasOrdenadas = entries.map { it.fecha }.sortedDescending()
            val ultima = fechasOrdenadas[0]
            val previa = fechasOrdenadas[1]

            val diasDeAusencia = TimeUnit.MILLISECONDS.toDays(ultima - previa)

            if (diasDeAusencia < MIN_DIAS_AUSENCIA) return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.RECONOCIMIENTO,
                severidad = Severidad.BAJA,
                ventanaTexto = "volviste a registrar",
                datos = mapOf("dias_de_ausencia" to diasDeAusencia)
            )
        }

    companion object {
        const val ID = "logro_pequeno"
        const val MIN_DIAS_AUSENCIA = 5L
    }
}