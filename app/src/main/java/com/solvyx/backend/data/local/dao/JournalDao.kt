package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntity): Long

    @Query("UPDATE journal SET serverId = :serverId WHERE id = :id")
    suspend fun setServerId(id: Int, serverId: String)

    @Query("SELECT serverId FROM journal WHERE serverId IS NOT NULL")
    suspend fun getSyncedServerIds(): List<String>

    @Query("SELECT * FROM journal ORDER BY date DESC")
    fun observe(): Flow<List<JournalEntity>>

    @Query("SELECT date FROM journal")
    fun observeDates(): Flow<List<Long>>
}
