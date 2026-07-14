package com.solvyx.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val authRepository: AuthRepository
) : ViewModel() {

    var nickname by mutableStateOf("Usuario")
        private set

    var streak by mutableIntStateOf(0)
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
                val entryMap = entries.groupBy {
                    Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
                }
                var streakCount = 0
                var day = today
                while (true) {
                    val dayEntries = entryMap[day]
                    if (dayEntries == null || dayEntries.any { it.consumed }) break
                    streakCount++
                    day = day.minusDays(1)
                }
                streak = streakCount
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
