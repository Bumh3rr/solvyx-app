package com.solvyx.backend.insights

import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.insights.repository.InsightsDebounceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del motor. Nos centramos en la lógica de debouncing y la
 * correcta invocación de reglas; las reglas individuales ya tienen
 * sus propios tests.
 *
 * Para evitar levantar Hilt ni Room ni agregar dependencias de mockeo,
 * usamos **fakes hand-written**: implementaciones controladas del DAO
 * y del repo de debouncing. Más simple y sin coste de build.
 */
class InsightsEngineTest {

    @Test
    fun `shouldShowBasedOnDebounce retorna true cuando nunca se mostro nada`() {
        val engine = makeEngine(entries = emptyList(), lastShown = 0L)
        assertTrue(engine.shouldShowBasedOnDebounce(0L, System.currentTimeMillis(), false))
    }

    @Test
    fun `shouldShowBasedOnDebounce bloquea cuando el ultimo fue hace menos de 72h`() {
        val engine = makeEngine(entries = emptyList(), lastShown = 0L)
        val now = 1_000_000_000_000L
        val lastShown = now - 24L * 60L * 60L * 1000L // 24h atrás
        assertFalse(engine.shouldShowBasedOnDebounce(lastShown, now, userAcceptsMore = false))
    }

    @Test
    fun `shouldShowBasedOnDebounce permite cuando el ultimo fue hace 73h o mas`() {
        val engine = makeEngine(entries = emptyList(), lastShown = 0L)
        val now = 1_000_000_000_000L
        val lastShown = now - 73L * 60L * 60L * 1000L // 73h atrás
        assertTrue(engine.shouldShowBasedOnDebounce(lastShown, now, userAcceptsMore = false))
    }

    @Test
    fun `shouldShowBasedOnDebounce permite con 25h si userAcceptsMore es true`() {
        val engine = makeEngine(entries = emptyList(), lastShown = 0L)
        val now = 1_000_000_000_000L
        val lastShown = now - 25L * 60L * 60L * 1000L // 25h atrás
        assertTrue(engine.shouldShowBasedOnDebounce(lastShown, now, userAcceptsMore = true))
    }

    @Test
    fun `shouldShowBasedOnDebounce bloquea con 23h si userAcceptsMore es true`() {
        val engine = makeEngine(entries = emptyList(), lastShown = 0L)
        val now = 1_000_000_000_000L
        val lastShown = now - 23L * 60L * 60L * 1000L // 23h atrás
        assertFalse(engine.shouldShowBasedOnDebounce(lastShown, now, userAcceptsMore = true))
    }

    @Test
    fun `evaluateNow devuelve lista vacia cuando debounce bloquea`() = runBlocking {
        val now = 1_000_000_000_000L
        val lastShown = now - 10L * 60L * 60L * 1000L // 10h atrás
        val engine = makeEngine(entries = emptyList(), lastShown = lastShown)

        val insights = engine.evaluateNow()
        assertEquals(emptyList<Insight>(), insights)
    }

    @Test
    fun `evaluateNow ejecuta reglas y devuelve insights cuando debounce permite`() = runBlocking {
        val now = System.currentTimeMillis()
        val entity = BitacoraEntity(
            id = 1,
            fecha = now,
            estadoAnimo = "neutral",
            consumio = true,
            sustancia = "alcohol"
        )
        val engine = makeEngine(entries = listOf(entity), lastShown = 0L)

        val insights = engine.evaluateNow()
        assertNotNull(insights)
        assertTrue(
            "Debe haber al menos un insight de consumo_reciente",
            insights.any { it.id == "consumo_reciente" }
        )
    }

    @Test
    fun `evaluateNow retorna lista ordenada por severidad descendente`() = runBlocking {
        val now = System.currentTimeMillis()
        val entity = BitacoraEntity(
            id = 1,
            fecha = now,
            estadoAnimo = "neutral",
            consumio = true,
            sustancia = "alcohol"
        )
        val engine = makeEngine(entries = listOf(entity), lastShown = 0L)

        val insights = engine.evaluateNow()
        val pesos = insights.map { it.severidad.peso }
        assertEquals(pesos.sortedDescending(), pesos)
    }

    @Test
    fun `evaluateNow actualiza timestamp cuando hay insights`() = runBlocking {
        val now = System.currentTimeMillis()
        val entity = BitacoraEntity(
            id = 1,
            fecha = now,
            estadoAnimo = "neutral",
            consumio = true,
            sustancia = "alcohol"
        )
        val debounceRepo = RecordingDebounceRepository()
        val engine = InsightsEngine(
            bitacoraDao = FakeBitacoraDao(listOf(entity)),
            debounceRepo = debounceRepo,
            context = null
        )

        engine.evaluateNow()

        assertTrue(
            "setLastShownTimestamp debió llamarse cuando hay insights",
            debounceRepo.setCount > 0
        )
    }

    @Test
    fun `evaluateNow NO actualiza timestamp cuando no hay insights`() = runBlocking {
        val debounceRepo = RecordingDebounceRepository()
        val engine = InsightsEngine(
            bitacoraDao = FakeBitacoraDao(emptyList()),
            debounceRepo = debounceRepo,
            context = null
        )

        engine.evaluateNow()

        assertEquals(
            "setLastShownTimestamp NO debió llamarse sin insights",
            0,
            debounceRepo.setCount
        )
    }

    @Test
    fun `evaluateNow filtra entradas fuera de la ventana de 60 dias`() = runBlocking {
        val now = System.currentTimeMillis()
        // Entrada de hace 70 días: el motor NO debe verla.
        val entity = BitacoraEntity(
            id = 1,
            fecha = now - 70L * 24L * 60L * 60L * 1000L,
            estadoAnimo = "neutral",
            consumio = true,
            sustancia = "alcohol"
        )
        val engine = makeEngine(entries = listOf(entity), lastShown = 0L)

        val insights = engine.evaluateNow()
        // No debe haber consumo_reciente porque la fecha está fuera de la ventana.
        assertFalse(
            "Una entrada de hace 70 días no debe disparar consumo_reciente",
            insights.any { it.id == "consumo_reciente" }
        )
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun makeEngine(
        entries: List<BitacoraEntity>,
        lastShown: Long
    ): InsightsEngine = InsightsEngine(
        bitacoraDao = FakeBitacoraDao(entries),
        debounceRepo = StaticDebounceRepository(lastShown),
        context = null
    )
}

/**
 * Fake minimalista de [BitacoraDao] que solo implementa el método
 * que el motor consume: `observar()`. El resto lanza
 * `NotImplementedError` porque el motor no los invoca.
 */
private class FakeBitacoraDao(
    private val data: List<BitacoraEntity>
) : BitacoraDao {
    override fun observar(): Flow<List<BitacoraEntity>> =
        MutableStateFlow(data).asStateFlow()

    private fun nope(): Nothing = throw NotImplementedError(
        "FakeBitacoraDao: método no implementado en este test"
    )

    override suspend fun insertar(entry: BitacoraEntity) = nope()
    override suspend fun actualizar(entry: BitacoraEntity) = nope()
    override fun observarFechas(): Flow<List<Long>> = nope()
    override fun observarPorRango(desde: Long, hasta: Long): Flow<List<BitacoraEntity>> = nope()
    override suspend fun ultima(): BitacoraEntity? = nope()
    override suspend fun findById(id: Int): BitacoraEntity? = nope()
    override fun observarConConsumo(): Flow<List<BitacoraEntity>> = nope()
    override fun observarConCraving(): Flow<List<BitacoraEntity>> = nope()
    override suspend fun eliminar(entry: BitacoraEntity) = nope()
    override suspend fun eliminarPorId(id: Int) = nope()
}

/**
 * Fake de [InsightsDebounceRepository] que devuelve siempre el mismo
 * `lastShown` y descarta los `set`.
 */
private class StaticDebounceRepository(
    private val lastShown: Long
) : InsightsDebounceRepository {
    override suspend fun getLastShownTimestamp(): Long = lastShown
    override suspend fun setLastShownTimestamp(timestamp: Long) = Unit
    override fun observe(): Flow<Long> = MutableStateFlow(lastShown).asStateFlow()
}

/**
 * Igual que [StaticDebounceRepository] pero cuenta cuántas veces se
 * llamó a `setLastShownTimestamp`, útil para validar que el motor
 * persistió el timestamp cuando correspondía.
 */
private class RecordingDebounceRepository : InsightsDebounceRepository {
    var setCount: Int = 0
        private set

    override suspend fun getLastShownTimestamp(): Long = 0L
    override suspend fun setLastShownTimestamp(timestamp: Long) {
        setCount++
    }
    override fun observe(): Flow<Long> = MutableStateFlow(0L).asStateFlow()
}

/**
 * Stub del [Context] de Android. Como el motor no usa el contexto
 * actualmente, los tests pasan `null` directamente al constructor
 * (que ahora acepta `Context?`). Si en el futuro una regla necesitase
 * el contexto, este fake debe ser reemplazado por Robolectric o un
 * Context real.
 */