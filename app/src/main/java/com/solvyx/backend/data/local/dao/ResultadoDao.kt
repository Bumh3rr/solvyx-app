package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.ResultadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarResultado(
        resultado: ResultadoEntity
    )

    @Query("SELECT * FROM resultados ORDER BY fecha DESC")
    fun obtenerResultados():
            Flow<List<ResultadoEntity>>
}