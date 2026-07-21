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
import com.solvyx.backend.repository.SosContactRepository
import com.solvyx.backend.repository.UserRepository
import com.solvyx.backend.validation.Validadores
import com.solvyx.ui.theme.TextMuted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository,
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
            val profile = authRepository.getProfile()
            rachaActual = profile?.currentStreak ?: 0
            mejorRacha = profile?.bestStreak ?: 0
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

    /**
     * `false` mientras no exista un resultado ASSIST en Room: usuario que aún no lo completó, o
     * cuenta convertida de anónimo cuyo detalle nunca se subió (gap documentado en Pendientes).
     * La UI **no** debe pintar un nivel de riesgo en ese caso: sin dato no hay riesgo que afirmar.
     */
    val hasAssistData: Boolean get() = riskLevel.isNotBlank()

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
        // Sin datos: neutro. Antes caía en el verde de "BAJO", insinuando riesgo bajo sin evaluar.
        else       -> TextMuted
    }
    fun bgColorNivel(): androidx.compose.ui.graphics.Color = when (riskLevel) {
        "BAJO"     -> androidx.compose.ui.graphics.Color(0xFFD1FAE5)
        "MODERADO" -> androidx.compose.ui.graphics.Color(0xFFfef9c3)
        "ALTO"     -> androidx.compose.ui.graphics.Color(0xFFfde8e8)
        else       -> TextMuted.copy(alpha = 0.15f)
    }
}
