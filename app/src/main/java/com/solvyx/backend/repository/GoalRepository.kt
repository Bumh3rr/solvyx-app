package com.solvyx.backend.repository

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.common.goals.CheckInGoalOutcome
import com.solvyx.backend.common.goals.GoalDays
import com.solvyx.backend.common.goals.GoalProgressCalculator
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.data.remote.datasource.GoalRemoteDataSource
import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CreateGoalResult {
    data class Created(val goalId: String) : CreateGoalResult
    /** Ya hay [Goal.MAX_ACTIVE] metas activas. */
    data object LimitReached : CreateGoalResult
    /** Las metas viven en Firestore: los usuarios anónimos no las tienen (docs: solo offline). */
    data object AccountRequired : CreateGoalResult
}

/**
 * Metas de reducción de daños. Solo para usuarios con cuenta.
 *
 * Las escrituras van en [appScope] y no se esperan: sin red quedan en la cola de Firestore y la
 * lista observada ya las refleja desde la caché, así que la UI nunca se queda esperando la red.
 */
@Singleton
class GoalRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val remote: GoalRemoteDataSource,
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository,
    private val journalRepository: JournalRepository,
    @ApplicationScope private val appScope: CoroutineScope
) {
    private val uid: String?
        get() = firebaseAuth.currentUser?.takeUnless { it.isAnonymous }?.uid

    val requiresAccount: Boolean get() = uid == null

    /** Todas las metas (activas e historial). Vacío si anónimo o sin sesión. */
    fun observeGoals(): Flow<List<Goal>> {
        val uid = uid ?: return flowOf(emptyList())
        return remote.observeAll(uid)
    }

    suspend fun create(goal: Goal): CreateGoalResult {
        val uid = uid ?: return CreateGoalResult.AccountRequired
        if (observeGoals().first().count { it.active } >= Goal.MAX_ACTIVE) return CreateGoalResult.LimitReached
        val goalId = remote.newGoalId(uid)
        appScope.launch { runCatching { remote.create(uid, goalId, goal) } }
        return CreateGoalResult.Created(goalId)
    }

    fun archive(goalId: String) {
        val uid = uid ?: return
        appScope.launch { runCatching { remote.archive(uid, goalId) } }
    }

    /**
     * Cada meta cumplida se celebra una sola vez aunque la detecten a la vez Plan (al abrirse) y
     * el registro diario (al guardar). El primero que la reclama la celebra.
     */
    private val celebrated: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun claimCelebration(goalId: String): Boolean = celebrated.add(goalId)

    /**
     * Metas tras guardar el registro de hoy: cuáles avanzaron y cuáles se cumplieron. Lee metas y
     * bitácora de la caché (no espera a la red) y suma [entry] aunque su escritura aún no llegue.
     * Llamarla ANTES de escribir el registro: así Plan no puede adelantarse a celebrar.
     */
    suspend fun afterCheckIn(entry: JournalEntry): CheckInGoalOutcome {
        if (uid == null) return CheckInGoalOutcome()
        val goals = observeGoals().first()
        val journal = journalRepository.observeAll().first().filterNot { it.date == entry.date } + entry
        val completed = syncProgress(goals, journal, entry.date).filter { claimCelebration(it.id) }
        val advanced = GoalDays.advancedOn(goals.filter { it.active && !it.completed }, journal, entry.date)
        return CheckInGoalOutcome(advanced = advanced, completed = completed)
    }

    /**
     * Recalcula el progreso de las metas activas desde la bitácora y lo guarda si cambió.
     * Devuelve las que se acaban de completar; para celebrarlas, pasar cada una por [claimCelebration].
     */
    fun syncProgress(goals: List<Goal>, journal: List<JournalEntry>, today: LocalDate): List<Goal> {
        val uid = uid ?: return emptyList()
        val justCompleted = mutableListOf<Goal>()
        goals.filter { it.active && !it.completed }.forEach { goal ->
            val progress = GoalProgressCalculator.progress(goal, journal, today)
            when {
                progress >= goal.target -> {
                    justCompleted += goal.copy(progress = progress, completed = true, active = false)
                    appScope.launch { runCatching { remote.markCompleted(uid, goal.id, progress) } }
                }
                progress != goal.progress ->
                    appScope.launch { runCatching { remote.updateProgress(uid, goal.id, progress) } }
            }
        }
        return justCompleted
    }

    /** Sustancias del perfil (Room, funciona sin red). */
    suspend fun trackedSubstances(): List<String> =
        userRepository.observe().first()?.substancesJson
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            .orEmpty()

    /**
     * Nivel ASSIST más reciente por sustancia. Con red sale del historial de Firestore; sin red,
     * del último resultado guardado en Room (solo una sustancia).
     */
    suspend fun riskLevels(): Map<String, NivelRiesgo> {
        val history = assistRepository.getHistory()
        if (history.isNotEmpty()) {
            return history.groupBy { it.sustanciaId }
                .mapValues { (_, results) -> results.maxBy { it.fecha }.nivel }
        }
        val last = assistRepository.observeLast().first() ?: return emptyMap()
        val level = runCatching { NivelRiesgo.valueOf(last.level) }.getOrNull() ?: return emptyMap()
        return mapOf(last.substanceId to level)
    }
}
