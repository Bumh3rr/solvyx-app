package com.solvyx.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.solvyx.backend.data.local.dao.SosContactDao
import com.solvyx.backend.data.local.dao.AchievementDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.LastAssistDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.database.AppDatabase
import com.solvyx.backend.common.formatter.DateFormatter
import com.solvyx.backend.common.formatter.DateFormatterImpl
import com.solvyx.backend.common.streak.StreakCalculator
import com.solvyx.backend.common.streak.StreakCalculatorImpl
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
    fun provideSosContactDao(db: AppDatabase): SosContactDao = db.sosContactDao()

    @Provides @Singleton
    fun provideLastAssistDao(db: AppDatabase): LastAssistDao = db.lastAssistDao()

    @Provides @Singleton
    fun providePlanDao(db: AppDatabase): PlanDao = db.planDao()

    @Provides @Singleton
    fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()

    @Provides @Singleton
    fun provideSosEventDao(db: AppDatabase): SosEventDao = db.sosEventDao()

    @Provides @Singleton
    fun provideDateFormatter(impl: DateFormatterImpl): DateFormatter = impl

    @Provides @Singleton
    fun provideStreakCalculator(impl: StreakCalculatorImpl): StreakCalculator = impl
}
