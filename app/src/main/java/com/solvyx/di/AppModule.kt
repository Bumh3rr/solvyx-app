package com.solvyx.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.dao.LogroDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.UltimoAssistDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "solvyx_database")
            .fallbackToDestructiveMigration()
            .addCallback(AppDatabase.SEED_CALLBACK)
            .build()

    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideContactoSosDao(db: AppDatabase): ContactoSosDao = db.contactoSosDao()

    @Provides @Singleton
    fun provideUltimoAssistDao(db: AppDatabase): UltimoAssistDao = db.ultimoAssistDao()

    @Provides @Singleton
    fun provideBitacoraDao(db: AppDatabase): BitacoraDao = db.bitacoraDao()

    @Provides @Singleton
    fun providePlanDao(db: AppDatabase): PlanDao = db.planDao()

    @Provides @Singleton
    fun provideLogroDao(db: AppDatabase): LogroDao = db.logroDao()

    @Provides @Singleton
    fun provideSosEventDao(db: AppDatabase): SosEventDao = db.sosEventDao()
}
