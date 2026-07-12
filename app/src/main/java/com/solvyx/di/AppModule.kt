package com.solvyx.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.dao.EjercicioDao
import com.solvyx.backend.data.local.dao.GuiaExtendidaDao
import com.solvyx.backend.data.local.dao.JournalingDao
import com.solvyx.backend.data.local.dao.LeccionDao
import com.solvyx.backend.data.local.dao.LeccionProgresoDao
import com.solvyx.backend.data.local.dao.LogroDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.PromptJournalingDao
import com.solvyx.backend.data.local.dao.ResultadoAssistDao
import com.solvyx.backend.data.local.dao.RutinaDao
import com.solvyx.backend.data.local.dao.RutinaProgresoDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.database.AppDatabase
import com.solvyx.backend.data.local.migrations.MIGRATION_2_3
import com.solvyx.backend.data.local.migrations.MIGRATION_3_4
import com.solvyx.backend.data.local.preferences.SeedPreferencesRepository
import com.solvyx.backend.insights.repository.InsightsDebounceRepository
import com.solvyx.backend.insights.repository.InsightsDebounceRepositoryImpl
import com.solvyx.solvyxDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Proveedor del [AppDatabase].
     *
     * Política de migración:
     * - Cada salto de versión DEBE tener su `Migration` registrada con
     *   `addMigrations(...)`. Esto es obligatorio y lo exige la regla
     *   "Nunca destruyas datos de usuario" del data architect.
     * - `fallbackToDestructiveMigration()` se ha ELIMINADO del builder: si
     *   en el futuro alguien sube la versión sin escribir migración, Room
     *   lanzará una excepción explícita al abrir la DB, en lugar de borrar
     *   la bitácora del usuario silenciosamente.
     * - `fallbackToDestructiveMigrationOnDowngrade()` se mantiene como red
     *   de seguridad SOLO para el caso raro de instalar un APK más antiguo
     *   sobre una DB más nueva (ej. downgrade por sideload). En ese caso no
     *   hay una "migración hacia atrás" razonable: lo correcto es reconstruir.
     */
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "solvyx_database")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigrationOnDowngrade()
            .addCallback(AppDatabase.SEED_CALLBACK)
            .build()

    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideContactoSosDao(db: AppDatabase): ContactoSosDao = db.contactoSosDao()

    @Provides @Singleton
    fun provideResultadoAssistDao(db: AppDatabase): ResultadoAssistDao = db.resultadoAssistDao()

    @Provides @Singleton
    fun provideBitacoraDao(db: AppDatabase): BitacoraDao = db.bitacoraDao()

    @Provides @Singleton
    fun providePlanDao(db: AppDatabase): PlanDao = db.planDao()

    @Provides @Singleton
    fun provideLogroDao(db: AppDatabase): LogroDao = db.logroDao()

    @Provides @Singleton
    fun provideSosEventDao(db: AppDatabase): SosEventDao = db.sosEventDao()

    // --- DAOs del módulo offline (v3) ---

    @Provides @Singleton
    fun provideEjercicioDao(db: AppDatabase): EjercicioDao = db.ejercicioDao()

    @Provides @Singleton
    fun provideGuiaExtendidaDao(db: AppDatabase): GuiaExtendidaDao = db.guiaExtendidaDao()

    @Provides @Singleton
    fun provideLeccionDao(db: AppDatabase): LeccionDao = db.leccionDao()

    @Provides @Singleton
    fun provideRutinaDao(db: AppDatabase): RutinaDao = db.rutinaDao()

    @Provides @Singleton
    fun providePromptJournalingDao(db: AppDatabase): PromptJournalingDao = db.promptJournalingDao()

    @Provides @Singleton
    fun provideJournalingDao(db: AppDatabase): JournalingDao = db.journalingDao()

    // --- DAOs de progreso del usuario (v4) ---

    @Provides @Singleton
    fun provideLeccionProgresoDao(db: AppDatabase): LeccionProgresoDao = db.leccionProgresoDao()

    @Provides @Singleton
    fun provideRutinaProgresoDao(db: AppDatabase): RutinaProgresoDao = db.rutinaProgresoDao()

    // --- DataStore para preferencias de seed (solvyx_prefs) ---

    @Provides @Singleton
    fun provideSeedPreferencesRepository(
        @ApplicationContext context: Context
    ): SeedPreferencesRepository = SeedPreferencesRepository(context)

    // --- DataStore: alias compartido para inyección ---

    /**
     * Expone el `DataStore<Preferences>` global de Solvyx como dependencia
     * inyectable. Lo usa [InsightsDebounceRepository] para leer/escribir
     * el timestamp del último insight mostrado.
     */
    @Provides @Singleton
    fun provideSolvyxDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.solvyxDataStore

    // --- Motor de insights ---

    /**
     * Repositorio de debouncing del motor de insights.
     * Lo proveemos como `@Singleton` para que Hilt lo inyecte en
     * `InsightsEngine` sin necesidad de binding explícito.
     */
    @Provides @Singleton
    fun provideInsightsDebounceRepository(
        dataStore: DataStore<Preferences>
    ): InsightsDebounceRepository = InsightsDebounceRepositoryImpl(dataStore)
}
