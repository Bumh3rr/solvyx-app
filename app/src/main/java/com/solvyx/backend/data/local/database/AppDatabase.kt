package com.solvyx.backend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.solvyx.backend.data.local.dao.ResultadoDao
import com.solvyx.backend.data.local.entity.ResultadoEntity


@Database(
    entities = [ResultadoEntity::class],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun resultadoDao(): ResultadoDao
}