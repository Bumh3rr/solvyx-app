package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.solvyx.backend.data.remote.model.AchievementRemoteDto
import com.solvyx.backend.data.remote.model.AssessmentResultRemoteDto
import com.solvyx.backend.data.remote.model.GoalRemoteDto
import com.solvyx.backend.data.remote.model.JournalRemoteDto
import com.solvyx.backend.data.remote.model.SosEventRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Firestore rejects write batches with more than 500 operations.
private const val MAX_BATCH_SIZE = 500

/**
 * Deletes everything a user has in Firestore. Deleting `users/{uid}` does not delete its
 * subcollections, so each one is emptied first; the profile document goes last.
 */
@Singleton
class AccountRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val subcollections = listOf(
        JournalRemoteDto.JOURNAL,
        AssessmentResultRemoteDto.ASSESSMENT_RESULTS,
        AchievementRemoteDto.ACHIEVEMENTS,
        SosEventRemoteDto.SOS_EVENTS,
        GoalRemoteDto.METAS
    )

    suspend fun deleteAllData(uid: String) {
        val userDoc = firestore.collection(UserRemoteDto.USERS).document(uid)
        subcollections.forEach { deleteCollection(userDoc.collection(it)) }
        userDoc.delete().await()
    }

    private suspend fun deleteCollection(collection: CollectionReference) {
        while (true) {
            val page = collection.limit(MAX_BATCH_SIZE.toLong()).get().await()
            if (page.isEmpty) return
            val batch = firestore.batch()
            page.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }
}
