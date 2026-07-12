package com.solvyx.backend.insights

import com.solvyx.backend.models.BitacoraEntry

/**
 * Regla determinística del motor de insights.
 *
 * Contrato:
 * - Cada implementación es una **función pura** sobre la lista de
 *   entradas: el mismo input produce el mismo output.
 * - Si la regla no aplica para los datos recibidos, retorna `null`.
 * - El motor entrega a cada regla entradas de los últimos 60 días
 *   (ventana que el `InsightsEngine` filtra antes de invocar).
 * - La regla puede correr trabajo CPU: se invoca dentro de
 *   `Dispatchers.Default`. Las reglas NO deben tocar el dispatcher
 *   Main ni hacer IO bloqueante.
 *
 * Cobertura de tests objetivo: ≥ 80% por regla. Cada regla debe tener
 * tests para al menos: caso positivo, caso negativo, caso con datos
 * insuficientes.
 *
 * Las reglas NO generan copy final: devuelven la estructura [Insight]
 * con [Insight.datos] poblado. `backend-content-curator` mapea el
 * `id` del insight a un texto validado por psicología.
 */
interface InsightRule {

    /**
     * Evalúa la regla sobre [entries].
     *
     * @param entries lista de entradas de los últimos 60 días,
     *   ordenadas por fecha descendente (la más reciente primero).
     *   Puede estar vacía o contener gaps.
     * @return el insight generado, o `null` si la regla no aplica.
     */
    suspend fun evaluate(entries: List<BitacoraEntry>): Insight?
}