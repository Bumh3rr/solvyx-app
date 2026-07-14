package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.solvyx.backend.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosEventDao {
    @Insert
    suspend fun insert(event: SosEventEntity)

    @Query("SELECT * FROM sos_events ORDER BY date DESC")
    fun observe(): Flow<List<SosEventEntity>>
}
