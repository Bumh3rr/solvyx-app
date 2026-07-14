package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.local.dao.LastAssistDao
import com.solvyx.backend.data.local.entity.LastAssistEntity
import com.solvyx.backend.data.remote.datasource.AssessmentRemoteDataSource
import com.solvyx.backend.data.remote.model.AssessmentResultRemoteDto
import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.backend.models.ResultadoDiagnostico
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val assessmentRemoteDataSource: AssessmentRemoteDataSource,
    private val lastAssistDao: LastAssistDao
) {
    suspend fun saveResult(result: ResultadoDiagnostico) {
        val current = lastAssistDao.observe().first()
        lastAssistDao.upsert(
            LastAssistEntity(
                substanceId = result.sustanciaId,
                score = result.puntaje,
                level = result.nivel.name,
                date = result.fecha,
                totalCompleted = (current?.totalCompleted ?: 0) + 1
            )
        )
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            assessmentRemoteDataSource.saveResult(
                user.uid,
                AssessmentResultRemoteDto(
                    substance = result.sustanciaId,
                    p2Frequency = result.p2Frecuencia,
                    p3Craving = result.p3Craving,
                    p4Problems = result.p4Problemas,
                    p5Obligations = result.p5Obligaciones,
                    p6Concern = result.p6Preocupacion,
                    p7Attempts = result.p7Intentos,
                    p8Injected = result.p8Inyectado,
                    totalScore = result.puntaje,
                    riskLevel = result.nivel.name,
                    recommendation = result.recomendacion,
                    date = result.fecha
                )
            )
            assessmentRemoteDataSource.markAssistCompleted(user.uid)
        } catch (e: Exception) {
            // Best-effort: Room ya quedó actualizado arriba; una falla de red
            // aquí no debe interrumpir el flujo de ASSIST para el usuario.
        }
    }

    fun observeLast(): Flow<LastAssistEntity?> = lastAssistDao.observe()

    suspend fun getHistory(): List<ResultadoDiagnostico> {
        val uid = firebaseAuth.currentUser?.uid ?: return emptyList()
        return try {
            assessmentRemoteDataSource.getHistory(uid).map { it.toResultadoDiagnostico() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun hydrateFromServer() {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        val history = try {
            assessmentRemoteDataSource.getHistory(user.uid)
        } catch (e: Exception) {
            return
        }
        val mostRecent = history.firstOrNull() ?: return
        val current = lastAssistDao.observe().first()
        if ((current?.totalCompleted ?: 0) >= history.size) return
        lastAssistDao.upsert(
            LastAssistEntity(
                substanceId = mostRecent.substance,
                score = mostRecent.totalScore,
                level = mostRecent.riskLevel,
                date = mostRecent.date,
                totalCompleted = history.size
            )
        )
    }

    private fun AssessmentResultRemoteDto.toResultadoDiagnostico(): ResultadoDiagnostico =
        ResultadoDiagnostico(
            sustanciaId = substance,
            p2Frecuencia = p2Frequency,
            p3Craving = p3Craving,
            p4Problemas = p4Problems,
            p5Obligaciones = p5Obligations,
            p6Preocupacion = p6Concern,
            p7Intentos = p7Attempts,
            p8Inyectado = p8Injected,
            puntaje = totalScore,
            nivel = runCatching { NivelRiesgo.valueOf(riskLevel) }.getOrDefault(NivelRiesgo.BAJO),
            recomendacion = recommendation,
            fecha = date
        )
}
