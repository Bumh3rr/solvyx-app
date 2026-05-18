package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.dao.LogroDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.data.local.entity.LogroEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvancesRepository @Inject constructor(
    private val bitacoraDao: BitacoraDao,
    private val logroDao: LogroDao
) {
    fun observarBitacora(): Flow<List<BitacoraEntity>> = bitacoraDao.observar()
    fun observarLogros(): Flow<List<LogroEntity>> = logroDao.observar()
    suspend fun desbloquearLogro(id: String) =
        logroDao.actualizar(
            LogroEntity(
                id = id,
                unlocked = true,
                fechaUnlock = System.currentTimeMillis()
            )
        )
}
