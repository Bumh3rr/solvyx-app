package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.solvyx.backend.data.remote.model.SosEventRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Firestore access for the SOS event audit log: `users/{uid}/sos_events/{autoId}`. */
@Singleton
class SosEventRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Appends one SOS event. Auto-generated doc id — each activation is its own document. Only the
     * date and how many contacts were notified leave the phone: their numbers stay local (privacy).
     */
    suspend fun saveEvent(uid: String, date: Long, contactCount: Int) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(SosEventRemoteDto.SOS_EVENTS)
            .add(
                mapOf(
                    SosEventRemoteDto.DATE to date,
                    SosEventRemoteDto.CONTACT_COUNT to contactCount,
                    SosEventRemoteDto.CREATED_AT to FieldValue.serverTimestamp()
                )
            ).await()
    }
}
