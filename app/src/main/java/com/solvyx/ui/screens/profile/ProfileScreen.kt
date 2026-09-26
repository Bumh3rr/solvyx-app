package com.solvyx.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.components.common.SolvyxMenuButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.profile.components.AssistRiskCard
import com.solvyx.ui.screens.profile.components.EditProfileSheet
import com.solvyx.ui.screens.profile.components.LogoutConfirmDialog
import com.solvyx.ui.screens.profile.components.ProfileAccountActions
import com.solvyx.ui.screens.profile.components.ProfileHeader
import com.solvyx.ui.screens.profile.components.ProfileSettingsCard
import com.solvyx.ui.screens.profile.components.ProfileStatsRow
import com.solvyx.ui.screens.profile.components.SafetyChecklistCard
import com.solvyx.ui.screens.profile.components.StaggeredEntrance
import com.solvyx.ui.screens.profile.components.SubstanceTrackingCard
import com.solvyx.ui.screens.profile.components.staggeredEntranceDuration
import com.solvyx.ui.screens.profile.model.SafetyStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SectionCount = 6
private const val ProfileSavedText = "Perfil actualizado"

/**
 * Mi perfil. Orchestrates the sections (header, stats, safety checklist, substances, ASSIST,
 * settings, account actions); each one lives in its own file under `components/`. Sections
 * assemble top to bottom the first time the screen opens, not when coming back from a sub-screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    onOpenDrawer: () -> Unit,
    onOpenJourney: () -> Unit,
    onOpenAssessment: () -> Unit,
    onOpenSupportNetwork: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTerms: () -> Unit,
    onCreateAccount: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var entranceDone by rememberSaveable { mutableStateOf(false) }
    val animateEntrance = !entranceDone
    LaunchedEffect(Unit) {
        delay(staggeredEntranceDuration(SectionCount))
        entranceDone = true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.showProfileSaved) {
        if (viewModel.showProfileSaved) {
            snackbarHostState.showSnackbar(ProfileSavedText)
            viewModel.onProfileSavedShown()
        }
    }

    val scope = rememberCoroutineScope()
    val substancesRequester = remember { BringIntoViewRequester() }
    val onSafetyStep: (SafetyStep) -> Unit = { step ->
        when (step) {
            SafetyStep.SUPPORT_CONTACTS -> onOpenSupportNetwork()
            SafetyStep.ASSESSMENT -> onOpenAssessment()
            SafetyStep.SUBSTANCES -> scope.launch { substancesRequester.bringIntoView() }
            SafetyStep.ACCOUNT -> onCreateAccount()
        }
    }

    ProfileDialogs(viewModel = viewModel, onLoggedOut = onLoggedOut)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            SolvyxTopBar(
                title = "Mi perfil",
                navigationButton = { SolvyxMenuButton(onClick = onOpenDrawer) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileHeader(
                    nickname = viewModel.nickname,
                    memberSince = viewModel.memberSince,
                    streak = viewModel.streak,
                    milestone = viewModel.milestone,
                    onEditProfile = viewModel::startEditingProfile
                )
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StaggeredEntrance(index = 0, animate = animateEntrance) {
                        ProfileStatsRow(
                            streak = viewModel.streak,
                            bestStreak = viewModel.bestStreak,
                            completedAssessments = viewModel.completedAssessments,
                            onOpenJourney = onOpenJourney,
                            onOpenAssessment = onOpenAssessment
                        )
                    }
                    StaggeredEntrance(index = 1, animate = animateEntrance) {
                        SafetyChecklistCard(items = viewModel.safetyItems, onStepClick = onSafetyStep)
                    }
                    StaggeredEntrance(index = 2, animate = animateEntrance) {
                        SubstanceTrackingCard(
                            selected = viewModel.selectedSubstances,
                            savedTick = viewModel.substancesSavedTick,
                            onToggle = viewModel::toggleSubstance,
                            modifier = Modifier.bringIntoViewRequester(substancesRequester)
                        )
                    }
                    StaggeredEntrance(index = 3, animate = animateEntrance) {
                        AssistRiskCard(assist = viewModel.assist, onOpenAssessment = onOpenAssessment)
                    }
                    StaggeredEntrance(index = 4, animate = animateEntrance) {
                        ProfileSettingsCard(
                            contactCount = viewModel.contactCount,
                            onOpenSupportNetwork = onOpenSupportNetwork,
                            onOpenPrivacy = onOpenPrivacy,
                            onOpenAbout = onOpenAbout,
                            onOpenTerms = onOpenTerms
                        )
                    }
                    StaggeredEntrance(index = 5, animate = animateEntrance) {
                        ProfileAccountActions(
                            isAnonymous = viewModel.isAnonymous,
                            onCreateAccount = onCreateAccount,
                            onLogout = viewModel::requestLogout
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

@Composable
private fun ProfileDialogs(viewModel: ProfileViewModel, onLoggedOut: () -> Unit) {
    if (viewModel.isEditingProfile) {
        EditProfileSheet(
            nickname = viewModel.editingNickname,
            birthDate = viewModel.editingBirthDate,
            isSaving = viewModel.isSavingProfile,
            saveFailed = viewModel.profileSaveFailed,
            canSave = viewModel.canSaveProfile,
            onNicknameChange = viewModel::onNicknameChange,
            onBirthDateChange = viewModel::onBirthDateChange,
            onSave = viewModel::saveProfile,
            onDismiss = viewModel::dismissEditProfile
        )
    }
    if (viewModel.showLogoutDialog) {
        LogoutConfirmDialog(
            isAnonymous = viewModel.isAnonymous,
            onConfirm = { viewModel.logout(onLoggedOut) },
            onDismiss = viewModel::dismissLogout
        )
    }
}
