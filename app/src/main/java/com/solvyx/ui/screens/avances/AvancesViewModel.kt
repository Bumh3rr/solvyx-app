package com.solvyx.ui.screens.avances

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solvyx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AvancesViewModel @Inject constructor() : ViewModel() {

    var selectedTab by mutableStateOf(0)
        private set   // 0 = Semana, 1 = Mes

    val racha = 12
    val mejorRacha = 18
    val proximoLogro = 15
    val milestoneDays = listOf(7, 15, 30)
    val milestoneProgress = 0.80f  // 80% toward 15-day milestone

    fun selectTab(index: Int) {
        selectedTab = index
    }

    // Feelings chart data: 7 data points for the week
    // bienestar: 0-10 scale, ansiedad: 0-10 scale
    val feelingsDataSemana = listOf(
        Pair(6f, 4f),  // Lun
        Pair(7f, 3f),  // Mar
        Pair(5f, 6f),  // Mie
        Pair(6f, 5f),  // Jue
        Pair(8f, 2f),  // Vie
        Pair(9f, 1f),  // Sab
        Pair(7f, 3f)   // Dom
    )

    val feelingsDataMes = listOf(
        Pair(5f, 5f), Pair(6f, 4f), Pair(7f, 4f), Pair(6f, 5f),
        Pair(8f, 3f), Pair(7f, 3f), Pair(9f, 2f), Pair(8f, 3f),
        Pair(7f, 4f), Pair(6f, 5f), Pair(5f, 6f), Pair(7f, 4f),
        Pair(8f, 2f), Pair(9f, 1f), Pair(8f, 2f), Pair(7f, 3f),
        Pair(6f, 4f), Pair(5f, 5f), Pair(7f, 3f), Pair(8f, 2f),
        Pair(9f, 1f), Pair(8f, 2f), Pair(7f, 3f), Pair(6f, 4f),
        Pair(5f, 5f), Pair(6f, 4f), Pair(7f, 3f), Pair(8f, 2f)
    )

    // Consumption chart: days with consumption (0 = no use, higher = more)
    val consumoSemana = listOf(0f, 1f, 0f, 2f, 0f, 0f, 1f)  // Lun..Dom
    val consumoMes = listOf(
        0f, 1f, 0f, 0f, 2f, 0f, 0f, 1f,
        0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f,
        0f, 2f, 0f, 0f, 0f, 1f, 0f, 0f,
        1f, 0f, 0f, 0f
    )

    // Achievements: 5 total, 3 unlocked
    data class Logro(
        val icon: Int,
        val titulo: String,
        val descripcion: String,
        val unlocked: Boolean
    )

    val logros get() = listOf(
        Logro(R.drawable.ic_trophy,      "Primera semana",   "7 días consecutivos",    true),
        Logro(R.drawable.ic_flame,       "Racha de fuego",   "12 días sin consumo",    true),
        Logro(R.drawable.ic_brain,       "Mente clara",      "Técnicas usadas 5 veces",true),
        Logro(R.drawable.ic_flag,        "2 semanas",        "14 días consecutivos",   false),
        Logro(R.drawable.ic_gem,         "Un mes",           "30 días consecutivos",   false)
    )

    val labelsSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    val labelsMes = (1..28).map { it.toString() }
}
