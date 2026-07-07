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
import com.solvyx.backend.repository.BitacoraRepository
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
    private val bitacoraRepository: BitacoraRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var apodo by mutableStateOf("Usuario")
        private set

    var racha by mutableIntStateOf(0)
        private set

    var assistCompletado by mutableStateOf(true)
        private set

    var esAnonimo by mutableStateOf(false)
        private set

    val fechaHoy: String = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            userRepository.observar().collect { user ->
                apodo = user?.apodo?.ifBlank { "Usuario" } ?: "Usuario"
                esAnonimo = user?.esAnonimo ?: false
            }
        }
        viewModelScope.launch {
            val user = authRepository.usuarioActual()
            assistCompletado = when {
                user == null -> true
                user.isAnonymous -> false
                else -> authRepository.assistCompletado(user.uid)
            }
        }
        viewModelScope.launch {
            bitacoraRepository.observar().collect { entries ->
                val today = LocalDate.now(zone)
                val entryMap = entries.groupBy {
                    Instant.ofEpochMilli(it.fecha).atZone(zone).toLocalDate()
                }
                var streak = 0
                var day = today
                while (true) {
                    val dayEntries = entryMap[day]
                    if (dayEntries == null || dayEntries.any { it.consumio }) break
                    streak++
                    day = day.minusDays(1)
                }
                racha = streak
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
    }
}
