package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.solvyx.backend.data.remote.model.JournalRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveEntry(uid: String, dto: JournalRemoteDto): String {
        val ref = firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(JournalRemoteDto.JOURNAL).add(
                hashMapOf(
                    JournalRemoteDto.DATE to dto.date,
                    JournalRemoteDto.MOOD to dto.mood,
                    JournalRemoteDto.CONSUMED to dto.consumed,
                    JournalRemoteDto.SUBSTANCE to dto.substance,
                    JournalRemoteDto.NOTE to dto.note,
                    JournalRemoteDto.CREATED_AT to FieldValue.serverTimestamp()
                )
            ).await()
        return ref.id
    }

    suspend fun getAll(uid: String): List<Pair<String, JournalRemoteDto>> {
        return firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(JournalRemoteDto.JOURNAL)
            .get().await()
            .documents.mapNotNull { doc -> doc.toJournalRemoteDto()?.let { doc.id to it } }
    }

    private fun DocumentSnapshot.toJournalRemoteDto(): JournalRemoteDto? {
        val mood = getString(JournalRemoteDto.MOOD) ?: return null
        val date = getLong(JournalRemoteDto.DATE) ?: return null
        return JournalRemoteDto(
            date = date,
            mood = mood,
            consumed = getBoolean(JournalRemoteDto.CONSUMED) ?: false,
            substance = getString(JournalRemoteDto.SUBSTANCE),
            note = getString(JournalRemoteDto.NOTE)
        )
    }
}
