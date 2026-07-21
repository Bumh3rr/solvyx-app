package com.solvyx.ui.screens.plan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: PlanRepository
) : ViewModel() {

    var metaIndex: Int by mutableStateOf(0)
        private set

    var metaLogradaHoy: Boolean by mutableStateOf(false)
        private set

    var showSosDialog: Boolean by mutableStateOf(false)
        private set

    val metasList = listOf(
        "Antes de consumir, toma agua y come algo primero.",
        "Si sientes ganas de consumir, espera 15 minutos antes de decidir.",
        "Habla con alguien de confianza antes de consumir.",
        "Reduce la dosis a la mitad respecto a la última vez."
    )

    val metaActual get() = metasList[metaIndex]

    init {
        viewModelScope.launch {
            repository.observe().collect { plan -> if (plan != null) metaIndex = plan.goalIndex }
        }
        viewModelScope.launch { metaLogradaHoy = repository.isMetaLogradaToday() }
    }

    fun toggleMetaLograda() {
        metaLogradaHoy = !metaLogradaHoy
        viewModelScope.launch { repository.setMetaLogradaToday(metaLogradaHoy) }
    }

    fun siguienteMeta() {
        metaIndex = (metaIndex + 1) % metasList.size
        metaLogradaHoy = false
        viewModelScope.launch { repository.saveGoalIndex(metaIndex) }
    }

    fun abrirSosDialog() {
        showSosDialog = true
    }

    fun cerrarSosDialog() {
        showSosDialog = false
    }
}
