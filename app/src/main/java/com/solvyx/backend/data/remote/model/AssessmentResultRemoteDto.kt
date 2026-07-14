package com.solvyx.backend.data.remote.model

data class AssessmentResultRemoteDto(
    val substance: String,
    val p2Frequency: Int,
    val p3Craving: Int,
    val p4Problems: Int,
    val p5Obligations: Int,
    val p6Concern: Int,
    val p7Attempts: Int,
    val p8Injected: Int?,
    val totalScore: Int,
    val riskLevel: String,
    val recommendation: String,
    val date: Long
){
    companion object {
        const val ASSESSMENT_RESULTS = "assist_results"
        const val SUBSTANCE = "substance"
        const val P2_FREQUENCY = "p2_frequency"
        const val P3_CRAVING = "p3_craving"
        const val P4_PROBLEMS = "p4_problems"
        const val P5_OBLIGATIONS = "p5_obligations"
        const val P6_CONCERN = "p6_concern"
        const val P7_ATTEMPTS = "p7_attempts"
        const val P8_INJECTED = "p8_injected"
        const val TOTAL_SCORE = "total_score"
        const val RISK_LEVEL = "risk_level"
        const val RECOMMENDATION = "recommendation"
        const val DATE = "date"
    }
}
