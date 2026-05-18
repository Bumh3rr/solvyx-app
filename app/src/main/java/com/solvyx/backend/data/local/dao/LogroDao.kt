package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solvyx.backend.data.local.entity.LogroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogroDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(logros: List<LogroEntity>)

    @Update
    suspend fun actualizar(logro: LogroEntity)

    @Query("SELECT * FROM logros ORDER BY id ASC")
    fun observar(): Flow<List<LogroEntity>>
}
