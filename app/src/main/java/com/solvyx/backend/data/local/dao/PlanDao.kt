package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

/** OBSOLETO, ver [PlanEntity]. Nadie lo usa; se queda porque AppDatabase lo declara. */
@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: PlanEntity)

    @Query("SELECT * FROM plan WHERE id = 1")
    fun observe(): Flow<PlanEntity?>
}
