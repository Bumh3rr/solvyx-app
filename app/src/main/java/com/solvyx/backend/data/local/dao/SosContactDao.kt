package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.SosContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(contacts: List<SosContactEntity>)

    @Query("DELETE FROM sos_contacts")
    suspend fun deleteAll()

    @Query("SELECT * FROM sos_contacts ORDER BY position ASC")
    fun observe(): Flow<List<SosContactEntity>>
}
