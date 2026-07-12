---
description: Patrón Repository para Solvyx. Interfaz pública + implementación @Singleton, fuentes de datos, threading, testing.
---

# Skill: Repository Pattern

Esta skill te entrega las convenciones para implementar el patrón Repository en Solvyx. Aplícala al crear o modificar la capa de repositories.

## Principios

1. **Interfaz pública + implementación `Impl`.** El VM y otros consumers dependen de la interfaz.
2. **Un repository por dominio o agregado.** No "un repository gigante con todo".
3. **`@Singleton`** — vive todo el ciclo de la app.
4. **Inyección por constructor** de DAOs, data sources, mappers.
5. **Threading gestionado** con `Dispatchers.IO` o `withContext` interno.
6. **Errores como resultado tipado**, no excepciones escapadas.

## Estructura

```kotlin
// Interfaz pública
interface EjerciciosRepository {
    fun observeEjercios(): Flow<List<Ejercicio>>
    fun observeByTipo(tipo: TipoEjercicio): Flow<List<Ejercicio>>
    suspend fun findBySlug(slug: String): Ejercicio?
    suspend fun refresh()
}

// Implementación
@Singleton
class EjerciciosRepositoryImpl @Inject constructor(
    private val dao: EjercicioDao,
    private val assetsLoader: AssetsLoader,
    private val seeder: EjerciciosSeeder,
) : EjerciciosRepository {
    
    override fun observeEjercios(): Flow<List<EjercicioEntity>> = dao.observeActivos()
    
    override fun observeByTipo(tipo: TipoEjercicio): Flow<List<EjercicioEntity>> =
        dao.observeByTipo(tipo.name)
    
    override suspend fun findBySlug(slug: String): EjercicioEntity? = withContext(Dispatchers.IO) {
        dao.findBySlug(slug)
    }
    
    override suspend fun refresh() {
        seeder.seedIfNeeded()
    }
}
```

## Binding en Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindEjerciciosRepository(
        impl: EjerciciosRepositoryImpl
    ): EjerciciosRepository
}
```

## Sources de datos

Un repository puede combinar múltiples fuentes:

```kotlin
@Singleton
class BitacoraRepositoryImpl @Inject constructor(
    private val localDao: BitacoraDao,
    private val remoteApi: SolvyxApi,        // futuro
    private val preferences: UserPreferencesRepository
) : BitacoraRepository {
    
    override fun observeEntries(): Flow<List<BitacoraEntry>> =
        localDao.observeAll().map { list -> list.map { it.toDomain() } }
    
    override suspend fun guardar(entry: BitacoraEntry): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            localDao.upsert(entry.toEntity())
        }
    }
    
    override suspend fun sync(): Result<Unit> = runCatching {
        // Si online: enviar a remoto
        if (preferences.isOnlineSyncEnabled()) {
            val remote = remoteApi.uploadEntries(localDao.getUnsynced())
            // ...
        }
    }
}
```

## Mappers Entity ↔ Domain

```kotlin
fun BitacoraEntryEntity.toDomain(): BitacoraEntry = BitacoraEntry(
    id = id,
    fecha = fecha,
    animo = animo,
    consumo = consumo == 1,
    sustancia = sustancia,
    nota = nota,
    suenoHoras = suenoHoras,
    comida = comida,
    detonantePrincipal = detonantePrincipal,
    nivelAnsiedad = nivelAnsiedad
)

fun BitacoraEntry.toEntity(): BitacoraEntryEntity = BitacoraEntryEntity(
    id = id,
    fecha = fecha,
    animo = animo,
    consumo = if (consumo) 1 else 0,
    sustancia = sustancia,
    nota = nota,
    suenoHoras = suenoHoras,
    comida = comida,
    detonantePrincipal = detonantePrincipal,
    nivelAnsiedad = nivelAnsiedad
)
```

## Result tipado (alternativa a throw)

Para errores esperados (validación, conflictos):

```kotlin
sealed class GuardarBitacoraResult {
    object Success : GuardarBitacoraResult()
    data class Error(val tipo: ErrorTipo) : GuardarBitacoraResult()
    
    enum class ErrorTipo {
        FECHA_INVALIDA,
        NOTA_MUY_LARGA,
        DB_ERROR
    }
}

override suspend fun guardar(entry: BitacoraEntry): GuardarBitacoraResult {
    if (entry.fecha <= 0) return GuardarBitacoraResult.Error(FECHA_INVALIDA)
    if ((entry.nota?.length ?: 0) > 100) return GuardarBitacoraResult.Error(NOTA_MUY_LARGA)
    
    return runCatching {
        withContext(Dispatchers.IO) { localDao.upsert(entry.toEntity()) }
    }.fold(
        onSuccess = { GuardarBitacoraResult.Success },
        onFailure = { GuardarBitacoraResult.Error(ErrorTipo.DB_ERROR) }
    )
}
```

## Threading

- **DAO operations** ya son asíncronas vía Room. No necesitas `withContext(Dispatchers.IO)`.
- **Network** debe ir en `Dispatchers.IO`.
- **Parsing JSON** en `Dispatchers.Default` (CPU-intensive).
- **Operaciones combinadas** en `withContext(Dispatchers.IO)` para asegurar orden.

```kotlin
override suspend fun refreshAll() = withContext(Dispatchers.IO) {
    val seedJson = assetsLoader.read("seed/v1/ejercicios.json")
    val parsed = withContext(Dispatchers.Default) { parseSeed(seedJson) }
    dao.upsertAll(parsed)
}
```

## Errores

| Caso | Estrategia |
|---|---|
| Validación de entrada | Validar en el repo, retornar `Result.Error`. |
| DB constraint violation | `runCatching` + mapear a `Error`. |
| Network error | `runCatching` + retry logic si aplica. |
| Parsing JSON malformed | Capturar `JsonDecodingException`, log + fallback a versión anterior. |

## Testing

```kotlin
class EjerciciosRepositoryTest {
    
    private val dao: EjercicioDao = mockk()
    private val seeder: EjerciciosSeeder = mockk(relaxed = true)
    private lateinit var repo: EjerciciosRepositoryImpl
    
    @Before
    fun setup() {
        repo = EjerciciosRepositoryImpl(dao, seeder)
    }
    
    @Test
    fun `findBySlug returns domain model when found`() = runTest {
        coEvery { dao.findBySlug("respiracion-4-7-8") } returns entityTest
        
        val result = repo.findBySlug("respiracion-4-7-8")
        
        assertEquals("Respiración 4-7-8", result?.nombre)
    }
    
    @Test
    fun `findBySlug returns null when not found`() = runTest {
        coEvery { dao.findBySlug("nope") } returns null
        
        val result = repo.findBySlug("nope")
        
        assertNull(result)
    }
}
```

## Cuándo usar Result vs Sealed Result vs Throw

| Estrategia | Cuándo |
|---|---|
| `runCatching` con `Result<T>` | Errores recuperables donde no importa el tipo. |
| `sealed class XResult` | Cuando el VM necesita saber el tipo de error para mostrar UI diferente. |
| Throw | Solo errores irrecuperables o bugs (DB corrupta, OOM). |

## Anti-patrones prohibidos

1. **Repository sin interfaz.** Difícil de testear.
2. **Repository con dependencias de UI** (Context sin qualifier, Activity, View).
3. **Repository como "God class".** Si tiene 30 métodos, divídelo.
4. **Lógica de negocio en el DAO.** El DAO solo persiste.
5. **Mezclar sources sin lógica de merge clara.** Si hay remoto y local, define la política (last-write-wins, etc.).
6. **`@Singleton` con estado mutable** que no es thread-safe.
7. **Repository que retorna Entities al VM.** El VM debe recibir domain models.