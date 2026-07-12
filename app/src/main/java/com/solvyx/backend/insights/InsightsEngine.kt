package com.solvyx.backend.insights

import android.content.Context
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.insights.repository.InsightsDebounceRepository
import com.solvyx.backend.insights.rules.ConsumoRecienteRule
import com.solvyx.backend.insights.rules.CravingsAgrupadosPorDiaRule
import com.solvyx.backend.insights.rules.EmocionRecurrenteRule
import com.solvyx.backend.insights.rules.GapRegistroRule
import com.solvyx.backend.insights.rules.LogroPequenoRule
import com.solvyx.backend.insights.rules.RachaRegistroRule
import com.solvyx.backend.insights.rules.SuelnoBajoEstaSemanaRule
import com.solvyx.backend.models.BitacoraEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de insights offline de Solvyx.
 *
 * **Responsabilidad**: evaluar reglas determinísticas sobre la bitácora
 * local del usuario y devolver una lista de [Insight]s ordenada por
 * relevancia (severidad descendente).
 *
 * **Lo que NO hace**:
 * - No redacta copy. El texto lo diseña `backend-content-curator` con
 *   revisión de `psicologo-solvyx`. Esta capa solo expone la estructura.
 * - No usa IA. Cada regla es una función pura sobre `List<BitacoraEntry>`.
 * - No persiste los insights generados; solo el timestamp del último
 *   mostrado (debouncing).
 * - No envía datos fuera del dispositivo.
 *
 * **Flujo de [evaluateNow]**:
 * 1. Comprobar debouncing (no emitir si el último fue reciente).
 * 2. Cargar entradas de los últimos 60 días.
 * 3. Evaluar todas las reglas en paralelo sobre `Dispatchers.Default`.
 * 4. Filtrar `null`s, ordenar por `severidad.peso` desc.
 * 5. Si hay insights, actualizar timestamp (para próximo debounce).
 *
 * **Por qué ventana de 60 días**: cubre el caso de uso "último mes"
 * (cravings por día de semana) y "última semana" (sueño, emoción)
 * sin escanear tablas históricas enormes. El DAO indexa por fecha.
 *
 * **Por qué `Context` como dependencia**: hoy no se usa, pero se
 * reserva para futuras reglas que necesiten acceso a `Resources`
 * (ej. umbral adaptado al locale). Marcarlo `@ApplicationContext`
 * garantiza que no se filtre el Activity.
 */
@Singleton
class InsightsEngine @Inject constructor(
    private val bitacoraDao: BitacoraDao,
    private val debounceRepo: InsightsDebounceRepository,
    @ApplicationContext private val context: Context?
) {

    /**
     * Lista inmutable de reglas activas. Se evalúan todas en paralelo.
     * Para añadir/quitar reglas, basta con tocar esta lista (la lista
     * está abierta a extensión vía futuras reglas; cerrada a
     * modificación por ser `val`).
     */
    private val rules: List<InsightRule> = listOf(
        SuelnoBajoEstaSemanaRule(),
        RachaRegistroRule(),
        EmocionRecurrenteRule(),
        CravingsAgrupadosPorDiaRule(),
        GapRegistroRule(),
        LogroPequenoRule(),
        ConsumoRecienteRule()
    )

    /**
     * Evalúa todas las reglas y devuelve los insights generados.
     *
     * @param userAcceptsMore si `true`, reduce el debounce a
     *   [InsightsDebounceRepository.ACCEPT_MORE_DEBOUNCE_HOURS]h
     *   (configuración de "más insights" en Mi Perfil). Por defecto
     *   `false` (debounce estándar de 72h).
     * @return lista ordenada por severidad desc. Vacía si el debounce
     *   bloquea la emisión o si ninguna regla aplica.
     */
    suspend fun evaluateNow(userAcceptsMore: Boolean = false): List<Insight> =
        withContext(Dispatchers.Default) {
            val now = System.currentTimeMillis()
            val lastShown = debounceRepo.getLastShownTimestamp()

            if (!shouldShowBasedOnDebounce(lastShown, now, userAcceptsMore)) {
                return@withContext emptyList()
            }

            val entries = loadRecentEntries(now)

            // Evaluar todas las reglas en paralelo. `runCatching` aísla
            // fallos: una regla rota NO debe tumbar el motor entero.
            val resultados = rules.map { regla ->
                async {
                    runCatching { regla.evaluate(entries) }.getOrNull()
                }
            }.awaitAll()

            val insights = resultados
                .filterNotNull()
                .sortedByDescending { it.severidad.peso }

            if (insights.isNotEmpty()) {
                debounceRepo.setLastShownTimestamp(now)
            }

            insights
        }

    /**
     * Carga las entradas de los últimos [VENTANA_DIAS] días desde el DAO.
     *
     * Se usa `dao.observar().first()` para tomar el primer valor emitido
     * (la lista actual). Esto evita mantener un Flow vivo dentro del motor.
     *
     * Para mejorar la performance, en una iteración futura se podría pedir
     * a `backend-data-architect` que añada un método
     * `observarDesdeFecha(cutoff: Long)` que haga el filtrado en SQL.
     * Por ahora el filtro es en memoria; sigue siendo O(n) sobre 60 días.
     */
    private suspend fun loadRecentEntries(now: Long): List<BitacoraEntry> {
        val cutoff = now - VENTANA_DIAS * MILLIS_PER_DAY
        return bitacoraDao
            .observar()
            .first()
            .map { e -> BitacoraEntityMapper.toDomain(e) }
            .filter { it.fecha >= cutoff }
    }

    /**
     * Decide si el debounce permite mostrar un nuevo insight.
     *
     * Política:
     * - Si nunca se mostró nada (`lastShown == 0L`), sí.
     * - Si el usuario acepta más, el intervalo es 24h; si no, 72h.
     * - Si han pasado más horas que el intervalo desde `lastShown`, sí.
     *
     * Función `internal` para que pueda ser testeada directamente.
     */
    internal fun shouldShowBasedOnDebounce(
        lastShown: Long,
        now: Long,
        userAcceptsMore: Boolean
    ): Boolean {
        if (lastShown == 0L) return true
        val intervaloHoras = if (userAcceptsMore) {
            InsightsDebounceRepository.ACCEPT_MORE_DEBOUNCE_HOURS
        } else {
            InsightsDebounceRepository.DEFAULT_DEBOUNCE_HOURS
        }
        val elapsedHours = (now - lastShown) / MILLIS_PER_HOUR
        return elapsedHours >= intervaloHoras
    }

    companion object {
        const val VENTANA_DIAS = 60
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
        private const val MILLIS_PER_HOUR = 60L * 60L * 1000L
    }
}

/**
 * Mapper de [BitacoraEntity] a [BitacoraEntry]. Existe como object
 * para no acoplar el motor a un mapper concreto del repositorio
 * extendido.
 */
private object BitacoraEntityMapper {
    fun toDomain(entity: BitacoraEntity): BitacoraEntry =
        BitacoraEntry(
            id = entity.id,
            fecha = entity.fecha,
            estadoAnimo = entity.estadoAnimo,
            consumio = entity.consumio,
            sustancia = entity.sustancia,
            nota = entity.nota,
            suenoHoras = entity.suenoHoras,
            suenoCalidad = entity.suenoCalidad,
            comio = entity.comio,
            calidadComida = entity.calidadComida,
            actividadFisica = entity.actividadFisica,
            contextoSocial = entity.contextoSocial,
            detonantePrincipal = entity.detonantePrincipal,
            nivelAnsiedad = entity.nivelAnsiedad,
            tuvoCraving = entity.tuvoCraving,
            ejercicioFisico = entity.ejercicioFisico,
            notaPrivada = entity.notaPrivada,
            updatedAt = entity.updatedAt
        )
}