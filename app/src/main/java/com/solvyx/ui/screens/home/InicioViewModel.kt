package com.solvyx.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.common.streak.StreakCalculator
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.JournalRepository
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class InicioViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val journalRepository: JournalRepository,
    private val authRepository: AuthRepository,
    private val streakCalculator: StreakCalculator
) : ViewModel() {

    var nickname by mutableStateOf("Usuario")
        private set

    var streak by mutableIntStateOf(0)
        private set

    var bestStreak by mutableIntStateOf(0)
        private set

    var nextMilestone by mutableIntStateOf(3)
        private set

    var milestoneProgress by mutableFloatStateOf(0f)
        private set

    var moodToday by mutableStateOf<String?>(null)
        private set

    var isAssistCompleted by mutableStateOf(true)
        private set

    var isAnonymous by mutableStateOf(false)
        private set

    val fechaHoy: String = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            userRepository.observe().collect { user ->
                isAnonymous = user?.isAnonymous ?: false
            }
        }
        viewModelScope.launch {
            nickname = authRepository.getProfile()?.nickname?.ifBlank { "Usuario" } ?: "Usuario"
        }
        viewModelScope.launch {
            val user = authRepository.currentUser
            isAssistCompleted = when {
                user == null -> true
                user.isAnonymous -> false
                else -> authRepository.isAssistCompleted(user.uid)
            }
        }
        viewModelScope.launch {
            journalRepository.observe().collect { entries ->
                val today = LocalDate.now(zone)

                val stats = streakCalculator.compute(entries, today)
                streak = stats.current
                bestStreak = stats.best
                nextMilestone = stats.nextMilestone
                milestoneProgress = stats.progress

                moodToday = entries
                    .filter { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() == today }
                    .maxByOrNull { it.id }
                    ?.mood
            }
        }
    }

    fun logMood(mood: String) {
        viewModelScope.launch {
            journalRepository.save(
                JournalEntity(
                    date = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli(),
                    mood = mood,
                    consumed = false
                )
            )
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
