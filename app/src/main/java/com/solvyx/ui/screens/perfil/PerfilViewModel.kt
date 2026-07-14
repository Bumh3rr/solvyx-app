package com.solvyx.ui.screens.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.common.formatter.DateFormatter
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.AssistRepository
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.JournalRepository
import com.solvyx.backend.repository.SosContactRepository
import com.solvyx.backend.repository.UserRepository
import com.solvyx.backend.validation.Validadores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository,
    private val journalRepository: JournalRepository,
    private val sosContactRepository: SosContactRepository,
    private val authRepository: AuthRepository,
    private val dateFormatter: DateFormatter
) : ViewModel() {

    var nickname by mutableStateOf("")
        private set
    var registrationDate by mutableStateOf("")
        private set
    var birthDate by mutableStateOf("")
        private set
    var rachaActual by mutableStateOf(0)
        private set
    var mejorRacha by mutableStateOf(0)
        private set
    var completedAssessments by mutableStateOf(0)
        private set
    var selectedSubstances by mutableStateOf(setOf<String>())
        private set
    var riskLevel by mutableStateOf("")
        private set
    var assistScore by mutableStateOf(0)
        private set
    var lastAssistDate by mutableStateOf("")
        private set
    var contactCount by mutableStateOf(0)
        private set
    var notificacionesActivas by mutableStateOf(true)
        private set
    var showEditarPerfil by mutableStateOf(false)
        private set
    var showLogoutDialog by mutableStateOf(false)
        private set
    var showEditarSustancias by mutableStateOf(false)
        private set
    var editingNickname by mutableStateOf("")
        private set
    var editingBirthDate by mutableStateOf("")
        private set
    var isAnonymous by mutableStateOf(false)
        private set

    private var cachedUser: UserEntity? = null
    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            userRepository.observe().collect { user ->
                cachedUser = user
                isAnonymous = user?.isAnonymous ?: false
                selectedSubstances = user?.substancesJson
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.toSet() ?: emptySet()
            }
        }
        viewModelScope.launch {
            val profile = authRepository.getProfile()
            nickname = profile?.nickname?.ifBlank { "Usuario" } ?: "Usuario"
            birthDate = profile?.birthDate ?: ""
            registrationDate = profile?.let { dateFormatter.format(it.createdAt, "MMMM yyyy") } ?: ""
        }
        viewModelScope.launch {
            assistRepository.observeLast().collect { last ->
                completedAssessments = last?.totalCompleted ?: 0
                if (last != null) {
                    riskLevel = last.level
                    assistScore = last.score
                    lastAssistDate = dateFormatter.format(last.date,"d 'de' MMMM yyyy").replaceFirstChar { it.uppercase() }
                } else {
                    riskLevel = ""
                    assistScore = 0
                    lastAssistDate = ""
                }
            }
        }
        viewModelScope.launch {
            journalRepository.observe().collect { entries ->
                val today = LocalDate.now(zone)
                val entryMap = entries.groupBy {
                    Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
                }
                var streak = 0
                var day = today
                while (true) {
                    val dayEntries = entryMap[day]
                    if (dayEntries == null || dayEntries.any { it.consumed }) break
                    streak++
                    day = day.minusDays(1)
                }
                rachaActual = streak
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
            }
        }
        viewModelScope.launch {
            sosContactRepository.observe().collect { contacts ->
                contactCount = contacts.count { it.name.isNotBlank() }
            }
        }
    }

    fun abrirEditarPerfil() {
        editingNickname = nickname
        editingBirthDate = birthDate
        showEditarPerfil = true
    }
    fun cerrarEditarPerfil() { showEditarPerfil = false }
    fun onApodoChange(v: String) { if (v.length <= 30) editingNickname = v }
    fun onFechaNacimientoChange(v: String) { editingBirthDate = v }
    fun guardarPerfil() {
        if (!Validadores.esNombreValido(editingNickname)) return
        viewModelScope.launch {
            val current = cachedUser ?: UserEntity()
            userRepository.save(
                current.copy(
                    substancesJson = selectedSubstances.joinToString(",")
                )
            )
            val result = authRepository.updateProfile(editingNickname.trim(), editingBirthDate)
            if (result.isSuccess) {
                nickname = editingNickname.trim()
                birthDate = editingBirthDate
            }
        }
        showEditarPerfil = false
    }

    fun toggleSustancia(id: String) {
        val nuevas = if (selectedSubstances.contains(id))
            selectedSubstances - id
        else
            selectedSubstances + id
        selectedSubstances = nuevas
        viewModelScope.launch {
            val current = cachedUser ?: UserEntity()
            userRepository.save(current.copy(substancesJson = nuevas.joinToString(",")))
            authRepository.updateSubstances(nuevas)
        }
    }
    fun abrirEditarSustancias() { showEditarSustancias = true }
    fun cerrarEditarSustancias() { showEditarSustancias = false }

    fun toggleNotificaciones() { notificacionesActivas = !notificacionesActivas }
    fun abrirLogoutDialog() { showLogoutDialog = true }
    fun cerrarLogoutDialog() { showLogoutDialog = false }
    fun confirmarLogout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
        cerrarLogoutDialog()
    }

    fun progresoRiesgo(): Float = when (riskLevel) {
        "BAJO"     -> assistScore / 27f * 0.40f
        "MODERADO" -> assistScore / 27f * 0.75f
        "ALTO"     -> 1f
        else       -> 0f
    }
    fun colorNivel(): androidx.compose.ui.graphics.Color = when (riskLevel) {
        "BAJO"     -> androidx.compose.ui.graphics.Color(0xFF065F46)
        "MODERADO" -> androidx.compose.ui.graphics.Color(0xFFd97706)
        "ALTO"     -> androidx.compose.ui.graphics.Color(0xFFE24B4A)
        else       -> androidx.compose.ui.graphics.Color(0xFF065F46)
    }
    fun bgColorNivel(): androidx.compose.ui.graphics.Color = when (riskLevel) {
        "BAJO"     -> androidx.compose.ui.graphics.Color(0xFFD1FAE5)
        "MODERADO" -> androidx.compose.ui.graphics.Color(0xFFfef9c3)
        "ALTO"     -> androidx.compose.ui.graphics.Color(0xFFfde8e8)
        else       -> androidx.compose.ui.graphics.Color(0xFFD1FAE5)
    }
}
