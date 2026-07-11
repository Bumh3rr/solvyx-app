package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.data.local.dao.UltimoAssistDao
import com.solvyx.backend.data.local.entity.UltimoAssistEntity
import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.backend.models.ResultadoDiagnostico
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val ultimoAssistDao: UltimoAssistDao
) {
    suspend fun guardarResultado(resultado: ResultadoDiagnostico) {
        val actual = ultimoAssistDao.observar().first()
        ultimoAssistDao.upsert(
            UltimoAssistEntity(
                sustanciaId = resultado.sustanciaId,
                puntaje = resultado.puntaje,
                nivel = resultado.nivel.name,
                fecha = resultado.fecha,
                totalCompletados = (actual?.totalCompletados ?: 0) + 1
            )
        )
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        try {
            firestore.collection("users").document(user.uid)
                .collection("assist_resultados").add(
                    hashMapOf(
                        "sustancia" to resultado.sustanciaId,
                        "p2_frecuencia" to resultado.p2Frecuencia,
                        "p3_craving" to resultado.p3Craving,
                        "p4_problemas" to resultado.p4Problemas,
                        "p5_obligaciones" to resultado.p5Obligaciones,
                        "p6_preocupacion" to resultado.p6Preocupacion,
                        "p7_intentos" to resultado.p7Intentos,
                        "p8_inyectado" to resultado.p8Inyectado,
                        "puntaje_total" to resultado.puntaje,
                        "nivel_riesgo" to resultado.nivel.name,
                        "recomendacion" to resultado.recomendacion,
                        "fecha" to FieldValue.serverTimestamp()
                    )
                ).await()
            firestore.collection("users").document(user.uid)
                .set(mapOf("assist_completado" to true), SetOptions.merge()).await()
        } catch (e: Exception) {
            // Best-effort: Room ya quedó actualizado arriba; una falla de red
            // aquí no debe interrumpir el flujo de ASSIST para el usuario.
        }
    }

    fun observarUltimo(): Flow<UltimoAssistEntity?> = ultimoAssistDao.observar()

    suspend fun obtenerHistorial(): List<ResultadoDiagnostico> {
        val uid = firebaseAuth.currentUser?.uid ?: return emptyList()
        return try {
            firestore.collection("users").document(uid)
                .collection("assist_resultados")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
                .documents.mapNotNull { it.toResultadoDiagnostico() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun hidratarDesdeServidor() {
        val user = firebaseAuth.currentUser ?: return
        if (user.isAnonymous) return
        val historial = obtenerHistorial()
        val masReciente = historial.firstOrNull() ?: return
        val actual = ultimoAssistDao.observar().first()
        if ((actual?.totalCompletados ?: 0) >= historial.size) return
        ultimoAssistDao.upsert(
            UltimoAssistEntity(
                sustanciaId = masReciente.sustanciaId,
                puntaje = masReciente.puntaje,
                nivel = masReciente.nivel.name,
                fecha = masReciente.fecha,
                totalCompletados = historial.size
            )
        )
    }
}

private fun DocumentSnapshot.toResultadoDiagnostico(): ResultadoDiagnostico? {
    val sustancia = getString("sustancia") ?: return null
    return ResultadoDiagnostico(
        sustanciaId = sustancia,
        p2Frecuencia = (getLong("p2_frecuencia") ?: 0).toInt(),
        p3Craving = (getLong("p3_craving") ?: 0).toInt(),
        p4Problemas = (getLong("p4_problemas") ?: 0).toInt(),
        p5Obligaciones = (getLong("p5_obligaciones") ?: 0).toInt(),
        p6Preocupacion = (getLong("p6_preocupacion") ?: 0).toInt(),
        p7Intentos = (getLong("p7_intentos") ?: 0).toInt(),
        p8Inyectado = getLong("p8_inyectado")?.toInt(),
        puntaje = (getLong("puntaje_total") ?: 0).toInt(),
        nivel = runCatching { NivelRiesgo.valueOf(getString("nivel_riesgo") ?: "BAJO") }.getOrDefault(NivelRiesgo.BAJO),
        recomendacion = getString("recomendacion") ?: "",
        fecha = getTimestamp("fecha")?.toDate()?.time ?: 0L
    )
}
