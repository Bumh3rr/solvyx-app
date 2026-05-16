package com.solvyx.backend.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var database: AppDatabase? = null

    fun getDatabase(
        context: Context
    ): AppDatabase {

        return database ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "solvyx_database"
            ).build()

            database = instance

            instance
        }
    }
}