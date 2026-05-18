package com.solvyx.ui.screens.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor() : ViewModel() {

    var apodo by mutableStateOf("Alex")
        private set
    var fechaRegistro by mutableStateOf("mayo 2026")
        private set
    var fechaNacimiento by mutableStateOf("")
        private set

    var rachaActual by mutableStateOf(12)
        private set
    var mejorRacha by mutableStateOf(18)
        private set
    var diagnosticosCompletados by mutableStateOf(3)
        private set

    // "alcohol", "vape", "cristal", "tabaco"
    var sustanciasSeleccionadas by mutableStateOf(setOf("alcohol", "vape", "tabaco"))
        private set

    var nivelRiesgo by mutableStateOf("BAJO")
        private set
    var puntajeAssist by mutableStateOf(8)
        private set
    var fechaUltimoAssist by mutableStateOf("15 de mayo 2026")
        private set

    var notificacionesActivas by mutableStateOf(true)
        private set
    var cantidadContactos by mutableStateOf(3)
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

    fun abrirEditarPerfil() {
        apodoEditando = apodo
        fechaNacimientoEditando = fechaNacimiento
        showEditarPerfil = true
    }
    fun cerrarEditarPerfil() { showEditarPerfil = false }
    fun onApodoChange(v: String) { if (v.length <= 30) apodoEditando = v }
    fun onFechaNacimientoChange(v: String) { fechaNacimientoEditando = v }
    fun guardarPerfil() {
        if (apodoEditando.isNotBlank()) apodo = apodoEditando.trim()
        fechaNacimiento = fechaNacimientoEditando
        showEditarPerfil = false
    }

    fun toggleSustancia(id: String) {
        sustanciasSeleccionadas = if (sustanciasSeleccionadas.contains(id))
            sustanciasSeleccionadas - id
        else
            sustanciasSeleccionadas + id
    }
    fun abrirEditarSustancias() { showEditarSustancias = true }
    fun cerrarEditarSustancias() { showEditarSustancias = false }

    fun toggleNotificaciones() { notificacionesActivas = !notificacionesActivas }

    fun abrirLogoutDialog() { showLogoutDialog = true }
    fun cerrarLogoutDialog() { showLogoutDialog = false }

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
