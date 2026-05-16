package com.solvyx.di

import android.content.Context
import androidx.room.Room
import com.solvyx.backend.data.local.dao.ResultadoDao
import com.solvyx.backend.data.local.database.AppDatabase
import com.solvyx.backend.repository.DiagnosticoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Room Database
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "solvyx_database"
        ).build()
    }

    // DAO
    @Provides
    @Singleton
    fun provideResultadoDao(
        database: AppDatabase
    ): ResultadoDao {

        return database.resultadoDao()
    }

    // Repository
    @Provides
    @Singleton
    fun provideDiagnosticoRepository(
        dao: ResultadoDao
    ): DiagnosticoRepository {

        return DiagnosticoRepository(dao)
    }
}