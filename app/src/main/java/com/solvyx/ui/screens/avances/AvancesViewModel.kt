package com.solvyx.ui.screens.avances

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.R
import com.solvyx.backend.data.local.entity.AchievementEntity
import com.solvyx.backend.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AvancesViewModel @Inject constructor(
    private val repository: ProgressRepository
) : ViewModel() {

    data class UiLogro(
        val icon: Int,
        val titulo: String,
        val descripcion: String,
        val unlocked: Boolean
    )

    var selectedTab by mutableStateOf(0)
        private set
    var racha by mutableStateOf(0)
        private set
    var mejorRacha by mutableStateOf(0)
        private set
    var proximoLogro by mutableStateOf(3)
        private set
    var milestoneProgress by mutableStateOf(0f)
        private set
    var feelingsDataSemana by mutableStateOf(List(7) { 0f })
        private set
    var feelingsDataMes by mutableStateOf(List(28) { 0f })
        private set
    var consumoSemana by mutableStateOf(List(7) { 0f })
        private set
    var consumoMes by mutableStateOf(List(28) { 0f })
        private set
    var uiLogros by mutableStateOf(listOf<UiLogro>())
        private set

    val milestoneDays = listOf(3, 7, 10, 15, 30)
    val labelsSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    val labelsMes = (1..28).map { it.toString() }

    private val moodScale = mapOf(
        "triste" to 1f, "ansioso" to 3f, "neutral" to 5f, "bien" to 7f, "euforico" to 10f
    )
    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            combine(
                repository.observeJournal(),
                repository.observeAchievements()
            ) { bitacora, logros -> Pair(bitacora, logros) }
            .collect { (bitacora, logros) ->
                val today = LocalDate.now(zone)
                val entryMap = bitacora.groupBy {
                    Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
                }

                // Current racha
                var streak = 0
                var day = today
                while (true) {
                    val dayEntries = entryMap[day]
                    if (dayEntries == null || dayEntries.any { it.consumed }) break
                    streak++
                    day = day.minusDays(1)
                }
                racha = streak

                // Best racha across all data
                var best = 0
                var current = 0
                val sortedDates = entryMap.keys.sorted()
                for (i in sortedDates.indices) {
                    val d = sortedDates[i]
                    val hasConsumption = entryMap[d]!!.any { it.consumed }
                    if (!hasConsumption) {
                        current = if (i > 0 && sortedDates[i - 1] == d.minusDays(1)) current + 1 else 1
                        if (current > best) best = current
                    } else {
                        current = 0
                    }
                }
                mejorRacha = maxOf(best, streak)

                // Next milestone
                val next = milestoneDays.firstOrNull { it > streak }
                proximoLogro = next ?: milestoneDays.last()
                milestoneProgress = if (next != null) {
                    val prev = milestoneDays.lastOrNull { it <= streak } ?: 0
                    (streak - prev).toFloat() / (next - prev).coerceAtLeast(1)
                } else 1f

                // Chart data
                val semanaDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val mesDays = (27 downTo 0).map { today.minusDays(it.toLong()) }
                feelingsDataSemana = semanaDays.map { d ->
                    entryMap[d]?.firstOrNull()?.let { moodScale[it.mood] } ?: 0f
                }
                consumoSemana = semanaDays.map { d ->
                    if (entryMap[d]?.any { it.consumed } == true) 1f else 0f
                }
                feelingsDataMes = mesDays.map { d ->
                    entryMap[d]?.firstOrNull()?.let { moodScale[it.mood] } ?: 0f
                }
                consumoMes = mesDays.map { d ->
                    if (entryMap[d]?.any { it.consumed } == true) 1f else 0f
                }

                // Logros
                uiLogros = logros.map { mapLogro(it) }
                autoUnlock(logros, streak)
            }
        }
    }

    private fun autoUnlock(logros: List<AchievementEntity>, currentRacha: Int) {
        val thresholds = mapOf("racha_3" to 3, "racha_7" to 7, "racha_10" to 10, "racha_15" to 15, "racha_30" to 30)
        logros.filter { !it.unlocked }.forEach { logro ->
            val threshold = thresholds[logro.id] ?: return@forEach
            if (currentRacha >= threshold) {
                viewModelScope.launch { repository.unlockAchievement(logro.id) }
            }
        }
    }

    private fun mapLogro(entity: AchievementEntity): UiLogro {
        val (icon, titulo, descripcion) = when (entity.id) {
            "racha_3"  -> Triple(R.drawable.ic_trophy, "Primeros pasos", "3 días consecutivos")
            "racha_7"  -> Triple(R.drawable.ic_flame,  "Primera semana", "7 días sin consumo")
            "racha_10" -> Triple(R.drawable.ic_brain,  "Mente clara",    "10 días consecutivos")
            "racha_15" -> Triple(R.drawable.ic_flag,   "2 semanas",      "15 días consecutivos")
            "racha_30" -> Triple(R.drawable.ic_gem,    "Un mes",         "30 días consecutivos")
            else       -> Triple(R.drawable.ic_trophy, entity.id,        "")
        }
        return UiLogro(icon, titulo, descripcion, entity.unlocked)
    }

    fun selectTab(index: Int) { selectedTab = index }
}
