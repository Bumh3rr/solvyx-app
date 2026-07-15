package com.solvyx.backend.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.JournalDao
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.backend.data.remote.datasource.JournalRemoteDataSource
import com.solvyx.backend.data.remote.model.JournalRemoteDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val dao: JournalDao,
    private val firebaseAuth: FirebaseAuth,
    private val remoteDataSource: JournalRemoteDataSource
) {
    fun observe(): Flow<List<JournalEntity>> = dao.observe()

    suspend fun save(entry: JournalEntity) {
        val localId = dao.insert(entry)
        syncToRemote(localId, entry)
    }

    private suspend fun syncToRemote(localId: Long, entry: JournalEntity) {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            val serverId = remoteDataSource.saveEntry(
                user.uid,
                JournalRemoteDto(
                    date = entry.date,
                    mood = entry.mood,
                    consumed = entry.consumed,
                    substance = entry.substance,
                    note = entry.note
                )
            )
            dao.setServerId(localId.toInt(), serverId)
        } catch (e: Exception) {
            // best-effort: la entrada ya está en Room. Si esto falla, la entrada queda local-only
            // (nunca se reintenta el push — hydrateFromServer solo trae datos, no reenvía)
        }
    }

    suspend fun hydrateFromServer() {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            val existingServerIds = dao.getSyncedServerIds().toSet()
            remoteDataSource.getAll(user.uid).forEach { (docId, dto) ->
                if (docId in existingServerIds) return@forEach
                dao.insert(
                    JournalEntity(
                        date = dto.date,
                        mood = dto.mood,
                        consumed = dto.consumed,
                        substance = dto.substance,
                        note = dto.note,
                        serverId = docId
                    )
                )
            }
        } catch (e: Exception) {
            // best-effort
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeDates(): Flow<Set<LocalDate>> =
        dao.observeDates().map { millis ->
            millis.map {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }.toSet()
        }
}
