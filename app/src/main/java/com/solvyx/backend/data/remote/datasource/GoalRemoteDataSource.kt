package com.solvyx.backend.data.remote.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalOrigin
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.remote.model.GoalRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `users/{uid}/metas`. Las escrituras sin red quedan en la cola de Firestore y se sincronizan al
 * volver la conexión; las lecturas salen de su caché.
 */
@Singleton
class GoalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val zone = ZoneId.systemDefault()

    private fun goalsCol(uid: String) =
        firestore.collection(UserRemoteDto.USERS).document(uid).collection(GoalRemoteDto.METAS)

    /** Todas las metas (activas e historial) en tiempo real. */
    fun observeAll(uid: String): Flow<List<Goal>> = callbackFlow {
        val registration = goalsCol(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Firestore deja de reintentar errores permanentes (permiso denegado, sesión
                // vencida); sin esto el Flow nunca volvería a emitir.
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) trySend(snapshot.documents.mapNotNull { it.toGoal() })
        }
        awaitClose { registration.remove() }
    }

    /** Id para una meta nueva, generado localmente (funciona sin red). */
    fun newGoalId(uid: String): String = goalsCol(uid).document().id

    /**
     * Sin red la escritura queda en la cola de Firestore (la lista observada ya la muestra) y
     * este `await` termina al sincronizar: nadie en la UI debe esperarlo.
     */
    suspend fun create(uid: String, goalId: String, goal: Goal) {
        val data = buildMap<String, Any?> {
            put(GoalRemoteDto.TIPO, goal.type.firestoreValue)
            put(GoalRemoteDto.ORIGEN, goal.origin.firestoreValue)
            put(GoalRemoteDto.SUSTANCIA, goal.substance)
            put(GoalRemoteDto.TITULO, goal.title)
            put(GoalRemoteDto.OBJETIVO, goal.target)
            put(GoalRemoteDto.PROGRESO_ACTUAL, goal.progress)
            put(GoalRemoteDto.UNIDAD, goal.unit)
            goal.weeklyLimit?.let { put(GoalRemoteDto.LIMITE_SEMANAL, it) }
            put(GoalRemoteDto.FECHA_INICIO, goal.startDate.toTimestamp())
            put(GoalRemoteDto.FECHA_LIMITE, null)
            put(GoalRemoteDto.ACTIVA, true)
            put(GoalRemoteDto.COMPLETADA, false)
            put(GoalRemoteDto.CREADO_EN, FieldValue.serverTimestamp())
        }
        goalsCol(uid).document(goalId).set(data).await()
    }

    suspend fun updateProgress(uid: String, goalId: String, progress: Int) {
        goalsCol(uid).document(goalId).update(GoalRemoteDto.PROGRESO_ACTUAL, progress).await()
    }

    /** Completada: sale de las activas y pasa al historial. */
    suspend fun markCompleted(uid: String, goalId: String, progress: Int) {
        goalsCol(uid).document(goalId).update(
            mapOf(
                GoalRemoteDto.PROGRESO_ACTUAL to progress,
                GoalRemoteDto.COMPLETADA to true,
                GoalRemoteDto.ACTIVA to false,
                GoalRemoteDto.COMPLETADA_EN to FieldValue.serverTimestamp()
            )
        ).await()
    }

    /** Archivada por el usuario: deja de estar activa sin contar como completada. */
    suspend fun archive(uid: String, goalId: String) {
        goalsCol(uid).document(goalId).update(GoalRemoteDto.ACTIVA, false).await()
    }

    private fun LocalDate.toTimestamp(): Timestamp = Timestamp(Date.from(atStartOfDay(zone).toInstant()))

    private fun Timestamp.toLocalDate(): LocalDate = toDate().toInstant().atZone(zone).toLocalDate()

    private fun DocumentSnapshot.toGoal(): Goal? {
        val type = GoalType.fromFirestore(getString(GoalRemoteDto.TIPO)) ?: return null
        return Goal(
            id = id,
            type = type,
            origin = GoalOrigin.fromFirestore(getString(GoalRemoteDto.ORIGEN)),
            substance = getString(GoalRemoteDto.SUSTANCIA),
            title = getString(GoalRemoteDto.TITULO) ?: return null,
            target = getLong(GoalRemoteDto.OBJETIVO)?.toInt() ?: return null,
            weeklyLimit = getLong(GoalRemoteDto.LIMITE_SEMANAL)?.toInt(),
            progress = getLong(GoalRemoteDto.PROGRESO_ACTUAL)?.toInt() ?: 0,
            startDate = getTimestamp(GoalRemoteDto.FECHA_INICIO)?.toLocalDate() ?: return null,
            active = getBoolean(GoalRemoteDto.ACTIVA) ?: false,
            completed = getBoolean(GoalRemoteDto.COMPLETADA) ?: false,
            // ESTIMATE: sin red el serverTimestamp aún está pendiente; así "Metas cumplidas" ya
            // tiene fecha para mostrar y ordenar.
            completedAt = getTimestamp(GoalRemoteDto.COMPLETADA_EN, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toDate()?.time
        )
    }
}
