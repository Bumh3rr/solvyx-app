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
 * Regla: `emocion_recurrente`.
 *
 * **Disparador**: en los últimos 7 días, alguna emoción aparece
 * en ≥ [MIN_FRECUENCIA] entradas distintas.
 *
 * **Severidad**: MEDIA, tipo OBSERVACION. La regla NO etiqueta
 * la emoción como "mala"; el copy de `backend-content-curator`
 * decide el tono (ej. "ansioso" se trata con neutralidad,
 * "triste" puede invitar a hablar con alguien).
 *
 * **Por qué ventana móvil de 7 días**: es la unidad que el
 * usuario entiende como "esta semana" y la que mejor refleja
 * estados emocionales sostenidos. Una ventana mayor enterraría
 * variaciones recientes; una menor dispararía con muy pocos datos.
 *
 * **Tie-break**: si dos emociones empatan en frecuencia, gana
 * la más reciente (la última entrada). Esto hace la regla
 * determinística y evita cambios de orden entre ejecuciones.
 */
class EmocionRecurrenteRule : InsightRule {

    override suspend fun evaluate(entries: List<BitacoraEntry>): Insight? =
        withContext(Dispatchers.Default) {
            val ventanaMs = TimeUnit.DAYS.toMillis(VENTANA_DIAS.toLong())
            val cutoff = System.currentTimeMillis() - ventanaMs

            // Filtrar entradas dentro de la ventana y con emoción no vacía.
            val recientes = entries.asSequence()
                .filter { it.fecha >= cutoff && it.estadoAnimo.isNotBlank() }
                .toList()

            if (recientes.isEmpty()) return@withContext null

            // Frecuencia por emoción.
            val frecuencias: Map<String, Int> = recientes
                .groupingBy { it.estadoAnimo }
                .eachCount()

            // Emoción más frecuente que supere el umbral. Si hay empate,
            // gana la de la entrada más reciente.
            val candidata = frecuencias
                .asSequence()
                .filter { it.value >= MIN_FRECUENCIA }
                .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }
                    .thenBy { entry ->
                        recientes.first { it.estadoAnimo == entry.key }.fecha
                    })

            val (emocion, frecuencia) = candidata ?: return@withContext null

            Insight(
                id = ID,
                tipo = TipoInsight.OBSERVACION,
                severidad = Severidad.MEDIA,
                ventanaTexto = "esta semana",
                datos = mapOf(
                    "emocion" to emocion,
                    "frecuencia" to frecuencia
                )
            )
        }

    companion object {
        const val ID = "emocion_recurrente"
        const val MIN_FRECUENCIA = 3
        const val VENTANA_DIAS = 7
    }
}