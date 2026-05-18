package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultadoAssistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(resultado: ResultadoAssistEntity)

    @Query("SELECT * FROM resultados_assist ORDER BY fecha DESC")
    fun observar(): Flow<List<ResultadoAssistEntity>>
}
