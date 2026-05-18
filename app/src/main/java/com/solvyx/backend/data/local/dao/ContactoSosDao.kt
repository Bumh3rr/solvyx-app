package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoSosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(contactos: List<ContactoSosEntity>)

    @Query("DELETE FROM contactos_sos")
    suspend fun deleteAll()

    @Query("SELECT * FROM contactos_sos ORDER BY orden ASC")
    fun observar(): Flow<List<ContactoSosEntity>>
}
