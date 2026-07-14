package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.data.remote.model.AssessmentResultRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssessmentRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveResult(uid: String, dto: AssessmentResultRemoteDto) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(AssessmentResultRemoteDto.ASSESSMENT_RESULTS).add(
                hashMapOf(
                    AssessmentResultRemoteDto.SUBSTANCE to dto.substance,
                    AssessmentResultRemoteDto.P2_FREQUENCY to dto.p2Frequency,
                    AssessmentResultRemoteDto.P3_CRAVING to dto.p3Craving,
                    AssessmentResultRemoteDto.P4_PROBLEMS to dto.p4Problems,
                    AssessmentResultRemoteDto.P5_OBLIGATIONS to dto.p5Obligations,
                    AssessmentResultRemoteDto.P6_CONCERN to dto.p6Concern,
                    AssessmentResultRemoteDto.P7_ATTEMPTS to dto.p7Attempts,
                    AssessmentResultRemoteDto.P8_INJECTED to dto.p8Injected,
                    AssessmentResultRemoteDto.TOTAL_SCORE to dto.totalScore,
                    AssessmentResultRemoteDto.RISK_LEVEL to dto.riskLevel,
                    AssessmentResultRemoteDto.RECOMMENDATION to dto.recommendation,
                    AssessmentResultRemoteDto.DATE to FieldValue.serverTimestamp()
                )
            ).await()
    }

    suspend fun markAssistCompleted(uid: String) {
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .set(mapOf(UserRemoteDto.ASSIST_COMPLETED to true), SetOptions.merge())
            .await()
    }

    suspend fun getHistory(uid: String): List<AssessmentResultRemoteDto> {
        return firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(AssessmentResultRemoteDto.ASSESSMENT_RESULTS)
            .orderBy(AssessmentResultRemoteDto.DATE, Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toAssessmentResultRemoteDto() }
    }

    private fun DocumentSnapshot.toAssessmentResultRemoteDto(): AssessmentResultRemoteDto? {
        val substance = getString(AssessmentResultRemoteDto.SUBSTANCE) ?: return null
        return AssessmentResultRemoteDto(
            substance = substance,
            p2Frequency = (getLong(AssessmentResultRemoteDto.P2_FREQUENCY) ?: 0).toInt(),
            p3Craving = (getLong(AssessmentResultRemoteDto.P3_CRAVING) ?: 0).toInt(),
            p4Problems = (getLong(AssessmentResultRemoteDto.P4_PROBLEMS) ?: 0).toInt(),
            p5Obligations = (getLong(AssessmentResultRemoteDto.P5_OBLIGATIONS) ?: 0).toInt(),
            p6Concern = (getLong(AssessmentResultRemoteDto.P6_CONCERN) ?: 0).toInt(),
            p7Attempts = (getLong(AssessmentResultRemoteDto.P7_ATTEMPTS) ?: 0).toInt(),
            p8Injected = getLong(AssessmentResultRemoteDto.P8_INJECTED)?.toInt(),
            totalScore = (getLong(AssessmentResultRemoteDto.TOTAL_SCORE) ?: 0).toInt(),
            riskLevel = getString(AssessmentResultRemoteDto.RISK_LEVEL) ?: "BAJO",
            recommendation = getString(AssessmentResultRemoteDto.RECOMMENDATION) ?: "",
            date = getTimestamp(AssessmentResultRemoteDto.DATE)?.toDate()?.time ?: 0L
        )
    }
}
