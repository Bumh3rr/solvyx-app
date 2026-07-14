package com.solvyx.backend.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.solvyx.backend.data.local.dao.JournalDao
import com.solvyx.backend.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(private val dao: JournalDao) {
    fun observe(): Flow<List<JournalEntity>> = dao.observe()
    suspend fun save(entry: JournalEntity) = dao.insert(entry)

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeDates(): Flow<Set<LocalDate>> =
        dao.observeDates().map { millis ->
            millis.map {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }.toSet()
        }
}
