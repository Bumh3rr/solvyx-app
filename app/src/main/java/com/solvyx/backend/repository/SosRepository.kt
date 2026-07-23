package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.SosContactDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import com.solvyx.backend.data.remote.datasource.SosEventRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepository @Inject constructor(
    private val sosContactDao: SosContactDao,
    private val sosEventDao: SosEventDao,
    private val firebaseAuth: FirebaseAuth,
    private val sosEventRemoteDataSource: SosEventRemoteDataSource
) {
    fun observeContacts(): Flow<List<SosContactEntity>> = sosContactDao.observe()

    /** Logs the SOS activation locally (audit trail), then best-effort mirrors it to Firestore. */
    suspend fun registerEvent(phones: List<String>) {
        val date = System.currentTimeMillis()
        sosEventDao.insert(SosEventEntity(date = date, notifiedPhones = phones.joinToString("|||")))

        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            sosEventRemoteDataSource.saveEvent(user.uid, date, phones)
        } catch (e: Exception) {
            // best-effort: the local audit row already exists.
        }
    }
}
