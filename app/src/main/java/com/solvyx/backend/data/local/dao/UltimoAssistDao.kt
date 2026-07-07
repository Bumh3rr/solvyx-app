package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.UltimoAssistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UltimoAssistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entidad: UltimoAssistEntity)

    @Query("SELECT * FROM ultimo_assist WHERE id = 1")
    fun observar(): Flow<UltimoAssistEntity?>
}
