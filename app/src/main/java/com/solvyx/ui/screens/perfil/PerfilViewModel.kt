package com.solvyx.ui.screens.perfil

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.AssistRepository
import com.solvyx.backend.repository.AuthRepository
import com.solvyx.backend.repository.BitacoraRepository
import com.solvyx.backend.repository.ContactoSosRepository
import com.solvyx.backend.repository.UserRepository
import com.solvyx.backend.validation.Validadores
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
class PerfilViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository,
    private val bitacoraRepository: BitacoraRepository,
    private val contactoRepository: ContactoSosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var apodo by mutableStateOf("")
        private set
    var fechaRegistro by mutableStateOf("")
        private set
    var fechaNacimiento by mutableStateOf("")
        private set
    var rachaActual by mutableStateOf(0)
        private set
    var mejorRacha by mutableStateOf(0)
        private set
    var diagnosticosCompletados by mutableStateOf(0)
        private set
    var sustanciasSeleccionadas by mutableStateOf(setOf<String>())
        private set
    var nivelRiesgo by mutableStateOf("BAJO")
        private set
    var puntajeAssist by mutableStateOf(0)
        private set
    var fechaUltimoAssist by mutableStateOf("")
        private set
    var cantidadContactos by mutableStateOf(0)
        private set
    var notificacionesActivas by mutableStateOf(true)
        private set
    var showEditarPerfil by mutableStateOf(false)
        private set
    var showLogoutDialog by mutableStateOf(false)
        private set
    var showEditarSustancias by mutableStateOf(false)
        private set
    var apodoEditando by mutableStateOf("")
        private set
    var fechaNacimientoEditando by mutableStateOf("")
        private set

    private var cachedUser: UserEntity? = null
    private val zone = ZoneId.systemDefault()
    private val fmtRegistro = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
    private val fmtAssist = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "MX"))

    init {
        viewModelScope.launch {
            userRepository.observar().collect { user ->
                cachedUser = user
                apodo = user?.apodo?.ifBlank { "Usuario" } ?: "Usuario"
                fechaRegistro = user?.let { fmtRegistro.format(Date(it.fechaRegistro)) } ?: ""
                fechaNacimiento = user?.fechaNacimiento ?: ""
                sustanciasSeleccionadas = user?.sustanciasJson
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.toSet() ?: emptySet()
            }
        }
        viewModelScope.launch {
            assistRepository.observarUltimo().collect { ultimo ->
                diagnosticosCompletados = ultimo?.totalCompletados ?: 0
                if (ultimo != null) {
                    nivelRiesgo = ultimo.nivel
                    puntajeAssist = ultimo.puntaje
                    fechaUltimoAssist = fmtAssist.format(Date(ultimo.fecha))
                        .replaceFirstChar { it.uppercase() }
                } else {
                    nivelRiesgo = "BAJO"
                    puntajeAssist = 0
                    fechaUltimoAssist = ""
                }
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
                rachaActual = streak
                var best = 0
                var current = 0
                val sortedDates = entryMap.keys.sorted()
                for (i in sortedDates.indices) {
                    val d = sortedDates[i]
                    val hasConsumption = entryMap[d]!!.any { it.consumio }
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
            contactoRepository.observar().collect { contactos ->
                cantidadContactos = contactos.count { it.nombre.isNotBlank() }
            }
        }
    }

    fun abrirEditarPerfil() {
        apodoEditando = apodo
        fechaNacimientoEditando = fechaNacimiento
        showEditarPerfil = true
    }
    fun cerrarEditarPerfil() { showEditarPerfil = false }
    fun onApodoChange(v: String) { if (v.length <= 30) apodoEditando = v }
    fun onFechaNacimientoChange(v: String) { fechaNacimientoEditando = v }
    fun guardarPerfil() {
        if (!Validadores.esNombreValido(apodoEditando)) return
        viewModelScope.launch {
            val current = cachedUser ?: UserEntity()
            userRepository.guardar(
                current.copy(
                    apodo = apodoEditando.trim(),
                    fechaNacimiento = fechaNacimientoEditando,
                    sustanciasJson = sustanciasSeleccionadas.joinToString(",")
                )
            )
            authRepository.actualizarPerfil(apodoEditando.trim(), fechaNacimientoEditando)
        }
        showEditarPerfil = false
    }

    fun toggleSustancia(id: String) {
        val nuevas = if (sustanciasSeleccionadas.contains(id))
            sustanciasSeleccionadas - id
        else
            sustanciasSeleccionadas + id
        sustanciasSeleccionadas = nuevas
        viewModelScope.launch {
            val current = cachedUser ?: UserEntity()
            userRepository.guardar(current.copy(sustanciasJson = nuevas.joinToString(",")))
            authRepository.actualizarSustancias(nuevas)
        }
    }
    fun abrirEditarSustancias() { showEditarSustancias = true }
    fun cerrarEditarSustancias() { showEditarSustancias = false }

    fun toggleNotificaciones() { notificacionesActivas = !notificacionesActivas }
    fun abrirLogoutDialog() { showLogoutDialog = true }
    fun cerrarLogoutDialog() { showLogoutDialog = false }
    fun confirmarLogout() {
        authRepository.cerrarSesion()
        cerrarLogoutDialog()
    }

    fun progresoRiesgo(): Float = when (nivelRiesgo) {
        "BAJO"     -> puntajeAssist / 27f * 0.40f
        "MODERADO" -> puntajeAssist / 27f * 0.75f
        "ALTO"     -> 1f
        else       -> 0f
    }

    fun colorNivel(): androidx.compose.ui.graphics.Color = when (nivelRiesgo) {
        "BAJO"     -> androidx.compose.ui.graphics.Color(0xFF065F46)
        "MODERADO" -> androidx.compose.ui.graphics.Color(0xFFd97706)
        "ALTO"     -> androidx.compose.ui.graphics.Color(0xFFE24B4A)
        else       -> androidx.compose.ui.graphics.Color(0xFF065F46)
    }

    fun bgColorNivel(): androidx.compose.ui.graphics.Color = when (nivelRiesgo) {
        "BAJO"     -> androidx.compose.ui.graphics.Color(0xFFD1FAE5)
        "MODERADO" -> androidx.compose.ui.graphics.Color(0xFFfef9c3)
        "ALTO"     -> androidx.compose.ui.graphics.Color(0xFFfde8e8)
        else       -> androidx.compose.ui.graphics.Color(0xFFD1FAE5)
    }
}
