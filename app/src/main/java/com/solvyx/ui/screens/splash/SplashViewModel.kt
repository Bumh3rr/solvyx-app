package com.solvyx.ui.screens.splash

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.solvyxDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class Destination {
        object Loading : Destination()
        object Onboarding : Destination()
        object Login : Destination()
    }

    private val _destination = MutableStateFlow<Destination>(Destination.Loading)
    val destination = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = context.solvyxDataStore.data.first()
            //val done = prefs[ONBOARDING_DONE] ?: false
            // TODO: Implementar lógica real para determinar si el onboarding ya se completó
            val done =  false

            _destination.value = if (done) Destination.Login else Destination.Onboarding
        }
    }
}
