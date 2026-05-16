package com.solvyx.ui.screens.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.solvyxDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun markDone() {
        viewModelScope.launch {
            context.solvyxDataStore.edit { it[ONBOARDING_DONE] = true }
        }
    }
}
