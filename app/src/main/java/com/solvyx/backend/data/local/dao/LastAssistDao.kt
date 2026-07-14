package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.LastAssistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LastAssistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LastAssistEntity)

    @Query("SELECT * FROM last_assist WHERE id = 1")
    fun observe(): Flow<LastAssistEntity?>
}
