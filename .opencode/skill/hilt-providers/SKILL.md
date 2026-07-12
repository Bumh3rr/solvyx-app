---
description: Convenciones de Hilt para Solvyx: @Inject, @Provides, @Singleton, @HiltViewModel y registro en AppModule.
---

# Skill: Hilt Providers

Esta skill te entrega las convenciones del proyecto Solvyx para inyección de dependencias con Hilt. Aplícala al registrar DAOs, repositorios, configuración de Room, DataStore y cualquier dependencia de larga vida.

## Principios

1. **Inyección por constructor siempre que sea posible.** Es más testeable y declarativo.
2. **`@Provides` solo cuando no puedes poner `@Inject` en el constructor** (interfaces, clases de terceros, configuración con builder).
3. **`@Singleton` para dependencias de larga vida** (Database, DAOs, Repositories, Retrofit, OkHttp).
4. **`@ActivityRetainedScoped` o `@ViewModelScoped` para ViewModels.** Aunque `@HiltViewModel` lo cubre.
5. **Módulos pequeños y cohesivos.** Un módulo por dominio o tipo de dependencia.

## Estructura de módulos

```
app/src/main/java/com/solvyx/di/
├── AppModule.kt          # Database, DAOs, Context
├── RepositoryModule.kt   # Repositories (bindings)
├── NetworkModule.kt      # HttpClient, ApiService
└── WorkerModule.kt       # Hilt WorkerFactory
```

## Plantilla de AppModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "solvyx.db"
    )
    .addMigrations(*AppDatabase.MIGRATIONS)
    .build()

    @Provides
    fun provideEjercicioDao(db: AppDatabase): EjercicioDao = db.ejercicioDao()

    @Provides
    fun provideGuiaDao(db: AppDatabase): GuiaDao = db.guiaDao()
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("solvyx_prefs") }
        )
}
```

## Repository Module (binding)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEjerciciosRepository(
        impl: EjerciciosRepositoryImpl
    ): EjerciciosRepository

    @Binds
    @Singleton
    abstract fun bindInsightsRepository(
        impl: InsightsRepositoryImpl
    ): InsightsRepository
}
```

## Inyección en clases propias

```kotlin
class EjerciciosRepositoryImpl @Inject constructor(
    private val dao: EjercicioDao,
    private val assetsLoader: AssetsLoader,
) : EjerciciosRepository {
    // ...
}
```

## ViewModels

```kotlin
@HiltViewModel
class EjerciciosViewModel @Inject constructor(
    private val repository: EjerciciosRepository
) : ViewModel() {
    // ...
}
```

## Workers (Hilt Worker)

```kotlin
@HiltWorker
class BitacoraReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: BitacoraRepository,
    private val notifier: Notifier,
) : CoroutineWorker(context, params) {
    // ...
}
```

Hilt requiere configurar `HiltWorkerFactory` en `Application.onCreate()`:

```kotlin
@HiltAndroidApp
class SolvyxApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

## DataStore con Proto (opcional)

Si la app usa Proto DataStore en lugar de Preferences:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = UserPreferencesSerializer,
        produceFile = { context.dataStoreFile("user_prefs.pb") }
    )
}
```

## Naming y organización

1. **Un módulo por tipo de dependencia.** No todo en `AppModule.kt`.
2. **Métodos `provide*` para `@Provides`.**
3. **Métodos `bind*` para `@Binds`.**
4. **`@Singleton` explícito en todo lo que sea de larga vida.** No `@Reusable` salvo justificación.
5. **`@ApplicationContext` y `@ActivityContext`** cuando necesites inyectar Context.

## Errores comunes y cómo evitarlos

| Error | Solución |
|---|---|
| "MissingBinding" para X | Crear `@Provides` o `@Binds` en el módulo correcto con scope correcto. |
| "Cannot be provided without an @Inject constructor" | Agregar `@Inject constructor` o usar `@Provides`. |
| Inyectar `Context` sin qualifier | Usar `@ApplicationContext` o `@ActivityContext`. |
| ViewModel sin `@HiltViewModel` | Agregar la anotación; Hilt lo genera. |
| Worker no se inyecta | Verificar que `HiltWorkerFactory` esté configurado en `Application.onCreate()`. |
| Doble `@Singleton` en jerarquía | Solo el más externo lleva `@Singleton`. |

## Testing

Para tests unitarios que no requieren Android:

```kotlin
@UninstallModules(AppModule::class)
@HiltAndroidTest
class MyTest {
    @BindValue val mockRepo: MyRepository = mockk()
}
```

Para tests de integración con Android:

```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class IntegrationTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    @Inject lateinit var database: AppDatabase
    
    @Before fun setup() = hiltRule.inject()
}
```

## Anti-patrones prohibidos

1. **Service Locator manual** (`MyClass.repository = ...`).
2. **Inyectar `Application` o `Activity` directamente.** Usa los qualifiers.
3. **`@Singleton` sin justificación real.** Solo para cosas que vivan todo el ciclo de la app.
4. **Mezclar `@Provides` y `@Binds` en el mismo módulo object/abstract class.** Separa.
5. **Pasar dependencias a través de constructores de UI.** El UI recibe ViewModels, no DAOs ni Repositories.
