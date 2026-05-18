package com.solvyx.backend.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BitacoraRepository @Inject constructor(private val dao: BitacoraDao) {
    fun observar(): Flow<List<BitacoraEntity>> = dao.observar()
    suspend fun guardar(entry: BitacoraEntity) = dao.insertar(entry)

    @RequiresApi(Build.VERSION_CODES.O)
    fun observarFechas(): Flow<Set<LocalDate>> =
        dao.observarFechas().map { millis ->
            millis.map {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }.toSet()
        }
}
