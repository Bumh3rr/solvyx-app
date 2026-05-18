package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entry: BitacoraEntity)

    @Query("SELECT * FROM bitacora ORDER BY fecha DESC")
    fun observar(): Flow<List<BitacoraEntity>>

    @Query("SELECT fecha FROM bitacora")
    fun observarFechas(): Flow<List<Long>>
}
