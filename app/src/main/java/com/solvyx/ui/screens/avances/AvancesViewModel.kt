package com.solvyx.ui.screens.avances

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.solvyx.R
import com.solvyx.backend.common.streak.StreakCalculator
import com.solvyx.backend.data.local.entity.AchievementEntity
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/** Mínimo de días registrados antes de afirmar cualquier patrón en "Berto dice". */
private const val MIN_DIAS_PARA_PATRON = 5

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AvancesViewModel @Inject constructor(
    private val repository: ProgressRepository,
    private val streakCalculator: StreakCalculator,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val isAnonymous: Boolean get() = firebaseAuth.currentUser?.isAnonymous == true

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
    var bertoInsight by mutableStateOf("")
        private set

    var selectedDay by mutableStateOf<JournalEntry?>(null)
        private set

    // Días visibles por período y lookup fecha->entrada, para el detalle del día.
    private var daysSemana: List<LocalDate> = emptyList()
    private var daysMes: List<LocalDate> = emptyList()
    private var entriesByDate: Map<LocalDate, JournalEntry> = emptyMap()

    val milestoneDays = AchievementEntity.MILESTONE_DAYS
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
                val entryMap = bitacora.groupBy { it.date }

                val stats = streakCalculator.compute(bitacora, today)
                racha = stats.current
                mejorRacha = stats.best
                proximoLogro = stats.nextMilestone
                milestoneProgress = stats.progress

                // Chart data
                val semanaDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val mesDays = (27 downTo 0).map { today.minusDays(it.toLong()) }
                daysSemana = semanaDays
                daysMes = mesDays
                entriesByDate = bitacora.associateBy { it.date }
                feelingsDataSemana = semanaDays.map { d ->
                    entryMap[d]?.firstOrNull()?.mood?.let { m -> moodScale[m] } ?: 0f
                }
                consumoSemana = semanaDays.map { d ->
                    if (entryMap[d]?.any { it.consumed == true } == true) 1f else 0f
                }
                feelingsDataMes = mesDays.map { d ->
                    entryMap[d]?.firstOrNull()?.mood?.let { m -> moodScale[m] } ?: 0f
                }
                consumoMes = mesDays.map { d ->
                    if (entryMap[d]?.any { it.consumed == true } == true) 1f else 0f
                }

                // Insight de Berto — calculado desde la bitácora real, nunca inventado
                bertoInsight = buildInsight(entryMap)

                // Logros
                uiLogros = logros.map { mapLogro(it) }
                autoUnlock(logros, stats.current)
            }
        }
    }

    /**
     * Texto de la tarjeta "Berto dice". Se deriva únicamente de la bitácora real: si no hay
     * datos suficientes lo dice en vez de afirmar un patrón que no existe.
     */
    private fun buildInsight(entryMap: Map<LocalDate, List<JournalEntry>>): String {
        val totalDias = entryMap.size
        if (totalDias < MIN_DIAS_PARA_PATRON) {
            return "Aún no tengo suficientes registros para ver patrones. " +
                "Registra unos días más y aquí te muestro lo que encuentre."
        }

        val diasConConsumo = entryMap.filterValues { dia -> dia.any { it.consumed == true } }.keys
        if (diasConConsumo.isEmpty()) {
            return "En $totalDias días registrados no reportaste consumo. " +
                "Ese es un patrón que vale la pena sostener."
        }

        val porDiaSemana = diasConConsumo.groupingBy { it.dayOfWeek }.eachCount()
        val (diaTop, veces) = porDiaSemana.maxByOrNull { it.value }!!
        if (veces >= 2) {
            val nombreDia = diaTop.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
            return "Los $nombreDia concentran tu mayor consumo registrado " +
                "($veces de ${diasConConsumo.size} días con consumo). " +
                "Planear algo distinto ese día puede ayudarte."
        }

        return "Llevas ${diasConConsumo.size} de $totalDias días registrados con consumo, " +
            "sin repetirse en un mismo día de la semana. Sigue registrando para ver tus patrones."
    }

    private fun autoUnlock(logros: List<AchievementEntity>, currentRacha: Int) {
        logros.filter { !it.unlocked }.forEach { logro ->
            val threshold = AchievementEntity.STREAK_THRESHOLDS[logro.id] ?: return@forEach
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

    /** Abre el detalle del día tocado en la gráfica. Ignora días sin registro. */
    fun onChartPointSelected(index: Int) {
        val days = if (selectedTab == 0) daysSemana else daysMes
        val date = dateForChartIndex(index, days) ?: return
        entriesByDate[date]?.let { selectedDay = it }
    }

    fun dismissDayDetail() { selectedDay = null }
}
