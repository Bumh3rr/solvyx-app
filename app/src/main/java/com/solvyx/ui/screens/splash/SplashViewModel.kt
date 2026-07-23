package com.solvyx.ui.screens.splash

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.router.Destino
import com.solvyx.backend.router.PostAuthRouter
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
    @ApplicationContext private val context: Context,
    private val postAuthRouter: PostAuthRouter
) : ViewModel() {

    sealed class Destination {
        object Loading : Destination()
        object Onboarding : Destination()
        data class PostAuth(val destino: Destino) : Destination()
    }

    private val _destination = MutableStateFlow<Destination>(Destination.Loading)
    val destination = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingDone = try {
                context.solvyxDataStore.data.first()[ONBOARDING_DONE] ?: false
            } catch (e: Exception) {
                // DataStore documenta IOException en lecturas fallidas; sin este catch,
                // _destination se quedaba en Loading para siempre y la app no abría.
                // Asumir "ya se hizo" es lo menos disruptivo: la mayoría de los arranques
                // no son el primero.
                true
            }
            _destination.value = if (!onboardingDone) {
                Destination.Onboarding
            } else {
                Destination.PostAuth(postAuthRouter.resolver())
            }
        }
    }
}
