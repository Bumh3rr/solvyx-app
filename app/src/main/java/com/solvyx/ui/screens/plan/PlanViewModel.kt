package com.solvyx.ui.screens.plan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.common.goals.GoalSuggestion
import com.solvyx.backend.common.goals.GoalSuggestions
import com.solvyx.backend.data.model.Goal
import com.solvyx.backend.data.model.GoalType
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.backend.repository.CreateGoalResult
import com.solvyx.backend.repository.GoalRepository
import com.solvyx.backend.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface PlanGoalsState {
    data object Loading : PlanGoalsState

    /** Anonymous: goals live in Firestore, so they need an account. */
    data object AccountRequired : PlanGoalsState

    data class Content(
        val active: List<GoalCardUi>,
        val suggestions: List<GoalSuggestion>,
        val suggestProfessional: Boolean,
        val completed: List<Goal>
    ) : PlanGoalsState
}

/** Where the substances and ASSIST levels for the suggestions come from (loaded once per visit). */
private data class SuggestionInputs(
    val substances: List<String> = emptyList(),
    val riskLevels: Map<String, NivelRiesgo> = emptyMap()
)

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    var goalsState by mutableStateOf<PlanGoalsState>(PlanGoalsState.Loading)
        private set

    var bertoLine by mutableStateOf(DefaultBertoLine)
        private set

    /** Goal whose detail sheet is open (kept fresh on every snapshot). */
    var selectedGoal by mutableStateOf<GoalCardUi?>(null)
        private set

    private var selectedGoalId: String? = null

    /** Goals completed during this visit, celebrated full screen one at a time. */
    var celebrations by mutableStateOf<List<Goal>>(emptyList())
        private set

    fun celebrationDone() {
        celebrations = celebrations.drop(1)
    }

    /** The "Crear mi propia meta" sheet; null while closed. */
    var createDraft by mutableStateOf<CreateGoalDraft?>(null)
        private set

    /** Journal of the last snapshot, for the day-by-day / week-by-week detail. */
    var journal by mutableStateOf<List<JournalEntry>>(emptyList())
        private set

    private var event: PlanEvent? = null
    private var answeredToday = false
    private var adding = false
    private val suggestionInputs = MutableStateFlow(SuggestionInputs())

    init {
        if (goalRepository.requiresAccount) {
            goalsState = PlanGoalsState.AccountRequired
            bertoLine = bertoLine(true, emptyList(), false, false, null)
        } else {
            viewModelScope.launch {
                // Room (substances) works offline; the ASSIST history may take a moment without network.
                suggestionInputs.value = SuggestionInputs(goalRepository.trackedSubstances(), goalRepository.riskLevels())
            }
            viewModelScope.launch {
                combine(
                    goalRepository.observeGoals(),
                    journalRepository.observeAll(),
                    suggestionInputs
                ) { goals, journal, inputs -> Triple(goals, journal, inputs) }
                    .catch { emit(Triple(emptyList(), emptyList(), SuggestionInputs())) }
                    .collect { (goals, journal, inputs) -> onSnapshot(goals, journal, inputs) }
            }
        }
    }

    private fun onSnapshot(goals: List<Goal>, journal: List<JournalEntry>, inputs: SuggestionInputs) {
        val today = LocalDate.now()
        // Goals that reach their target now (e.g. a reduce week that just ended) are completed here.
        // They are celebrated here unless the check-in that completed them already did.
        val justCompleted = goalRepository.syncProgress(goals, journal, today)
        val toCelebrate = justCompleted.filter { goalRepository.claimCelebration(it.id) }
        toCelebrate.firstOrNull()?.let { event = PlanEvent.GoalCompleted(it.title) }
        celebrations = celebrations + toCelebrate
        val completedNow = justCompleted.associateBy { it.id }
        val current = goals.map { completedNow[it.id] ?: it }

        val active = current.filter { it.active && !it.completed }
        val cards = active.map { goalCard(it, journal, today) }
        val suggestions = GoalSuggestions.suggest(inputs.substances, inputs.riskLevels, journal, active, today)
        val showSuggestions = active.size < Goal.MAX_ACTIVE

        this.journal = journal
        answeredToday = answeredToday(journal, today)
        goalsState = PlanGoalsState.Content(
            active = cards,
            suggestions = if (showSuggestions) suggestions else emptyList(),
            suggestProfessional = showSuggestions && GoalSuggestions.shouldSuggestProfessional(inputs.riskLevels),
            completed = completedGoals(current)
        )
        selectedGoal = cards.firstOrNull { it.goal.id == selectedGoalId }
        if (selectedGoal == null) selectedGoalId = null
        refreshBerto()
    }

    fun acceptSuggestion(suggestion: GoalSuggestion) = addGoal(suggestion.toGoal(LocalDate.now()))

    private fun addGoal(goal: Goal, onCreated: () -> Unit = {}) {
        // A quick double tap would otherwise create the same goal twice.
        if (adding) return
        adding = true
        viewModelScope.launch {
            try {
                val result = goalRepository.create(goal)
                if (result is CreateGoalResult.Created) {
                    event = PlanEvent.GoalAdded
                    refreshBerto()
                    onCreated()
                }
            } finally {
                adding = false
            }
        }
    }

    // ── Crear mi propia meta ─────────────────────────────────────────────────

    private val activeGoals: List<Goal>
        get() = (goalsState as? PlanGoalsState.Content)?.active?.map { it.goal }.orEmpty()

    val canCreateGoal: Boolean get() = activeGoals.size < Goal.MAX_ACTIVE

    fun openCreateGoal() {
        if (canCreateGoal) createDraft = CreateGoalDraft()
    }

    fun closeCreateGoal() {
        createDraft = null
    }

    fun createGoalBack() {
        createDraft = createDraft?.back()
    }

    fun selectGoalType(type: GoalType) {
        createDraft = createDraft?.selectType(type)
    }

    fun selectGoalSubstance(substance: String?) {
        val draft = createDraft ?: return
        val inputs = suggestionInputs.value
        val defaultLimit = substance?.let {
            GoalSuggestions.defaultWeeklyLimit(it, inputs.riskLevels[it], journal, LocalDate.now())
        } ?: GoalSuggestions.MIN_WEEKLY_LIMIT
        createDraft = draft.selectSubstance(substance, defaultLimit)
    }

    fun setGoalDays(value: Int) {
        createDraft = createDraft?.withDays(value)
    }

    fun setGoalWeeklyLimit(value: Int) {
        createDraft = createDraft?.withWeeklyLimit(value)
    }

    fun setGoalWeeks(value: Int) {
        createDraft = createDraft?.withWeeks(value)
    }

    fun substanceChoicesFor(type: GoalType): List<SubstanceChoice> =
        substanceChoices(type, suggestionInputs.value.substances, activeGoals)

    /** Alcohol safety: warns (never blocks) before quitting alcohol at once with a high ASSIST level. */
    fun needsWithdrawalWarning(draft: CreateGoalDraft): Boolean {
        val type = draft.type ?: return false
        return draft.hasSubstance &&
            GoalSuggestions.needsWithdrawalWarning(type, draft.substance, suggestionInputs.value.riskLevels)
    }

    fun confirmCreateGoal() {
        val goal = createDraft?.toGoal(LocalDate.now()) ?: return
        addGoal(goal, onCreated = { createDraft = null })
    }

    fun openGoal(goalId: String) {
        selectedGoalId = goalId
        selectedGoal = (goalsState as? PlanGoalsState.Content)?.active?.firstOrNull { it.goal.id == goalId }
    }

    fun closeGoal() {
        selectedGoalId = null
        selectedGoal = null
    }

    fun archiveGoal(goalId: String) {
        closeGoal()
        // "¡Hecho! Empezamos hoy." no longer fits once a goal is archived.
        if (event == PlanEvent.GoalAdded) event = null
        goalRepository.archive(goalId)
    }

    private fun refreshBerto() {
        val content = goalsState as? PlanGoalsState.Content ?: return
        bertoLine = bertoLine(
            requiresAccount = false,
            activeGoals = content.active,
            hasSuggestions = content.suggestions.isNotEmpty(),
            answeredToday = answeredToday,
            event = event
        )
    }
}
