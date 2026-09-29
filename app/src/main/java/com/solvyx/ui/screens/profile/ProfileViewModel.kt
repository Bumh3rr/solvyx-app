package com.solvyx.ui.screens.profile

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.common.formatter.DateFormatter
import com.solvyx.backend.common.streak.MilestoneProgress
import com.solvyx.backend.common.streak.milestoneProgress
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.AssistRepository
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.SosContactRepository
import com.solvyx.backend.repository.UserRepository
import com.solvyx.backend.validation.Validadores
import com.solvyx.ui.components.common.substanceLabel
import com.solvyx.ui.components.common.AssistRiskLevel
import com.solvyx.ui.screens.profile.model.AssistSummary
import com.solvyx.ui.screens.profile.model.SafetyItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_NICKNAME = "Usuario"
const val NICKNAME_MAX_LENGTH = 30
private const val MEMBER_SINCE_PATTERN = "MMMM yyyy"
private const val ASSIST_DATE_PATTERN = "d 'de' MMMM yyyy"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    assistRepository: AssistRepository,
    sosContactRepository: SosContactRepository,
    private val authRepository: AuthRepository,
    private val dateFormatter: DateFormatter
) : ViewModel() {

    // ── Profile data ──
    var nickname by mutableStateOf("")
        private set
    var memberSince by mutableStateOf("")
        private set
    var streak by mutableIntStateOf(0)
        private set
    var bestStreak by mutableIntStateOf(0)
        private set
    var completedAssessments by mutableIntStateOf(0)
        private set
    var assist by mutableStateOf<AssistSummary?>(null)
        private set
    var selectedSubstances by mutableStateOf(setOf<String>())
        private set
    var contactCount by mutableIntStateOf(0)
        private set
    var isAnonymous by mutableStateOf(false)
        private set

    /** Bumped after each substance change is saved, so the UI can flash "Guardado". */
    var substancesSavedTick by mutableIntStateOf(0)
        private set

    val milestone: MilestoneProgress by derivedStateOf { milestoneProgress(streak) }

    val safetyItems: List<SafetyItem> by derivedStateOf {
        safetyItems(
            contactCount = contactCount,
            hasAssessment = assist != null,
            hasSubstances = selectedSubstances.isNotEmpty(),
            isAnonymous = isAnonymous
        )
    }

    // ── Edit profile sheet ──
    var isEditingProfile by mutableStateOf(false)
        private set
    var editingNickname by mutableStateOf("")
        private set
    var editingBirthDate by mutableStateOf("")
        private set
    var isSavingProfile by mutableStateOf(false)
        private set
    /** Shown inside the edit sheet: a snackbar would sit hidden underneath it. */
    var profileSaveFailed by mutableStateOf(false)
        private set
    val canSaveProfile: Boolean get() = !isSavingProfile && Validadores.esNombreValido(editingNickname)

    // ── Dialogs & feedback ──
    var showLogoutDialog by mutableStateOf(false)
        private set
    // ── Delete account ──
    var showDeleteAccountDialog by mutableStateOf(false)
        private set
    var deletePassword by mutableStateOf("")
        private set
    var isDeletingAccount by mutableStateOf(false)
        private set
    var deleteAccountError by mutableStateOf<String?>(null)
        private set

    /** One-shot: the screen shows a confirmation snackbar and then calls [onProfileSavedShown]. */
    var showProfileSaved by mutableStateOf(false)
        private set

    private var birthDate = ""
    private var cachedUser: UserEntity? = null

    init {
        viewModelScope.launch {
            userRepository.observe().collect { user ->
                cachedUser = user
                isAnonymous = user?.isAnonymous ?: false
                selectedSubstances = user?.substancesJson
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.toSet()
                    .orEmpty()
            }
        }
        viewModelScope.launch {
            val profile = authRepository.getProfile()
            nickname = profile?.nickname?.ifBlank { null } ?: DEFAULT_NICKNAME
            birthDate = profile?.birthDate.orEmpty()
            memberSince = profile?.let { dateFormatter.format(it.createdAt, MEMBER_SINCE_PATTERN) }.orEmpty()
            streak = profile?.currentStreak ?: 0
            bestStreak = profile?.bestStreak ?: 0
        }
        viewModelScope.launch {
            assistRepository.observeLast().collect { last ->
                completedAssessments = last?.totalCompleted ?: 0
                assist = last?.let { entity ->
                    AssistRiskLevel.fromStored(entity.level)?.let { level ->
                        AssistSummary(
                            level = level,
                            score = entity.score,
                            substanceLabel = substanceLabel(entity.substanceId),
                            date = dateFormatter.format(entity.date, ASSIST_DATE_PATTERN)
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            sosContactRepository.observe().collect { contacts ->
                contactCount = contacts.count { it.name.isNotBlank() }
            }
        }
    }

    // ── Edit profile ──
    fun startEditingProfile() {
        editingNickname = nickname
        editingBirthDate = birthDate
        profileSaveFailed = false
        isEditingProfile = true
    }

    fun dismissEditProfile() {
        if (!isSavingProfile) isEditingProfile = false
    }

    fun onNicknameChange(value: String) {
        if (value.length <= NICKNAME_MAX_LENGTH) {
            editingNickname = value
            profileSaveFailed = false
        }
    }

    fun onBirthDateChange(value: String) { editingBirthDate = value }

    /** Keeps the sheet open until the save result is known, then reports it either way. */
    fun saveProfile() {
        if (!canSaveProfile) return
        isSavingProfile = true
        profileSaveFailed = false
        viewModelScope.launch {
            val newNickname = editingNickname.trim()
            val result = authRepository.updateProfile(newNickname, editingBirthDate)
            isSavingProfile = false
            if (result.isSuccess) {
                nickname = newNickname
                birthDate = editingBirthDate
                isEditingProfile = false
                showProfileSaved = true
            } else {
                profileSaveFailed = true
            }
        }
    }

    // ── Substances ──
    fun toggleSubstance(id: String) {
        val updated = if (id in selectedSubstances) selectedSubstances - id else selectedSubstances + id
        selectedSubstances = updated
        viewModelScope.launch {
            val current = cachedUser ?: UserEntity()
            userRepository.save(current.copy(substancesJson = updated.joinToString(",")))
            authRepository.updateSubstances(updated)
            substancesSavedTick++
        }
    }

    // ── Session ──
    fun requestLogout() { showLogoutDialog = true }
    fun dismissLogout() { showLogoutDialog = false }

    /** Navigates away only once sign-out (and the local wipe) has finished. */
    fun logout(onLoggedOut: () -> Unit) {
        showLogoutDialog = false
        viewModelScope.launch {
            authRepository.signOut()
            onLoggedOut()
        }
    }

    fun onProfileSavedShown() { showProfileSaved = false }

    fun requestDeleteAccount() {
        deletePassword = ""
        deleteAccountError = null
        showDeleteAccountDialog = true
    }

    fun dismissDeleteAccount() {
        if (!isDeletingAccount) showDeleteAccountDialog = false
    }

    fun onDeletePasswordChange(value: String) {
        deletePassword = value
        deleteAccountError = null
    }

    /** Leaves the screen only once everything is gone; on failure the dialog stays with the reason. */
    fun deleteAccount(onDeleted: () -> Unit) {
        if (isDeletingAccount || deletePassword.isBlank()) return
        isDeletingAccount = true
        viewModelScope.launch {
            authRepository.deleteAccount(deletePassword)
                .onSuccess {
                    showDeleteAccountDialog = false
                    onDeleted()
                }
                .onFailure { deleteAccountError = it.message }
            isDeletingAccount = false
        }
    }
}
