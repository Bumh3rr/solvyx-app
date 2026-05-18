package com.solvyx.ui.screens.directorio

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

enum class TipoDirectorio(val label: String) {
    CIJ("CIJ"),
    CLINICA("Clínicas"),
    PSICOLOGO("Psicólogos"),
    LINEA("Líneas de apoyo")
}

data class EntradaDirectorio(
    val id: String,
    val nombre: String,
    val tipo: TipoDirectorio,
    val descripcion: String,
    val direccion: String? = null,
    val telefono: String,
    val horario: String? = null,
    val especialidad: String? = null,
    val tipoCita: String? = null,
    val tipoCosto: String? = null,
    val tags: List<String> = emptyList(),
    val verificado: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null,
    val rating: Double? = null,
    val mapEmbedUrl: String? = null
)

private val DIRECTORIO_DATA = listOf(

    // ── CIJ ──────────────────────────────────────────
    EntradaDirectorio(
        id = "cij-chilpancingo",
        nombre = "CIJ Chilpancingo",
        tipo = TipoDirectorio.CIJ,
        descripcion = "Centro de Integración Juvenil con prevención y tratamiento " +
                "de adicciones, atención psicológica, orientación familiar " +
                "y programas comunitarios y educativos.",
        direccion = "Salubridad, C.P. 39096, Chilpancingo de los Bravo, Gro.",
        telefono = "7474949445",
        horario = "Lunes a viernes · 8:30 a 19:30 hrs",
        tags = listOf("Adicciones", "Atención psicológica",
            "Orientación familiar", "Sin costo"),
        verificado = true,
        lat = 17.5174, lng = -99.4994,
        rating = 4.6,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d529627.8938302608!2d-99.97737629998296!3d17.517351706891905!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x85cbec6580fb5deb%3A0x3550e7b1a5132e20!2sCentros%20de%20Integraci%C3%B3n%20Juvenil%2C%20A.C.%20Chilpancingo.!5e0!3m2!1ses-419!2smx!4v1778991364485!5m2!1ses-419!2smx"
    ),

    // ── CLÍNICAS ──────────────────────────────────────
    EntradaDirectorio(
        id = "clinica-salud-emocional",
        nombre = "Clínica de Salud Emocional",
        tipo = TipoDirectorio.CLINICA,
        descripcion = "Dependencia de la Secretaría de Salud de Guerrero. " +
                "Atención psicológica y emocional para población abierta, " +
                "sin costo.",
        direccion = "Eje Central, Col. Burócratas, C.P. 39090, Chilpancingo",
        telefono = "7474800457",
        horario = "Lunes a domingo · 8:00 a 20:00 hrs",
        tags = listOf("Salud mental", "Atención psicológica", "Sin costo"),
        verificado = true,
        lat = 17.5325, lng = -99.4907,
        rating = 4.4,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d15217.903142302286!2d-99.4907147050583!3d17.53252010210735!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x85cbebe73e6d7767%3A0xe25dd478e369e83b!2sCl%C3%ADnica%20de%20Salud%20Emocional!5e0!3m2!1ses-419!2smx!4v1778990124258!5m2!1ses-419!2smx"
    ),
    EntradaDirectorio(
        id = "cecosama",
        nombre = "CECOSAMA",
        tipo = TipoDirectorio.CLINICA,
        descripcion = "Centro de Salud Mental y Adicciones — UNEME Centro Nueva Vida. " +
                "Tratamiento especializado en salud mental y dependencias, " +
                "sin costo.",
        direccion = "Venustiano Carranza #18, Col. 20 de Noviembre, " +
                "C.P. 39096, Chilpancingo",
        telefono = "7474949883",
        horario = "Lunes a viernes · 8:00 a 16:00 hrs",
        tags = listOf("Adicciones", "Salud mental", "Sin costo"),
        verificado = true,
        lat = 17.5374, lng = -99.5208,
        rating = 4.3,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d35382.34589915387!2d-99.52080276214065!3d17.53737146164908!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x85cbeb8e63cb23c1%3A0x71a3ad5b9bbf1dd3!2sUneme%20Centro%20Nueva%20Vida!5e0!3m2!1ses-419!2smx!4v1778990264763!5m2!1ses-419!2smx"
    ),

    // ── PSICÓLOGOS ────────────────────────────────────
    EntradaDirectorio(
        id = "psy-frida",
        nombre = "Mtra. Frida Sianet Vázquez Lucas",
        tipo = TipoDirectorio.PSICOLOGO,
        descripcion = "Consultorio Psicológico Resiliencia al Cambio. " +
                "Especialista en intervención en crisis.",
        telefono = "7471123344",
        horario = "Con cita previa",
        especialidad = "Ansiedad · Depresión · Adolescentes · Pareja",
        tipoCita = "Con cita previa",
        tipoCosto = "Sin especificar",
        verificado = true,
        lat = 17.5417, lng = -99.5042,
        rating = 4.7,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m17!1m12!1m3!1d3804.2840387276606!2d-99.5042343!3d17.541658399999996!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1m1!2zMTfCsDMyJzMwLjAiTiA5OcKwMzAnMTUuMiJX!5e0!3m2!1ses-419!2smx!4v1778991904619!5m2!1ses-419!2smx"
    ),
    EntradaDirectorio(
        id = "psy-edgar",
        nombre = "Lic. Edgar Medina Miguel",
        tipo = TipoDirectorio.PSICOLOGO,
        descripcion = "Consultorio Guerrero. Control de adicciones " +
                "y trastornos de ansiedad.",
        direccion = "Paseo Alejandro Cervantes Delgado, " +
                "Zona Col. 29, C.P. 39000, Chilpancingo",
        telefono = "7471188821",
        horario = "Con cita previa",
        especialidad = "Trastornos de ansiedad · Control de adicciones",
        tipoCita = "Con cita previa",
        tipoCosto = "$600 por sesión",
        verificado = true,
        lat = 17.5446, lng = -99.5020,
        rating = 4.5,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m17!1m12!1m3!1d3804.2223455868016!2d-99.50196079999999!3d17.5445976!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1m1!2zMTfCsDMyJzQwLjYiTiA5OcKwMzAnMDcuMSJX!5e0!3m2!1ses-419!2smx!4v1778992034632!5m2!1ses-419!2smx"
    ),
    EntradaDirectorio(
        id = "psy-jorge",
        nombre = "Lic. Jorge Peña Díaz",
        tipo = TipoDirectorio.PSICOLOGO,
        descripcion = "Psicoterapia, tanatología y coaching. " +
                "Atención en intervención en crisis y acompañamiento.",
        direccion = "Prosperidad No. 4, Col. Universal, Chilpancingo",
        telefono = "7471347760",
        horario = "Con cita previa",
        especialidad = "Psicoterapia · Tanatología · Coaching",
        tipoCita = "Con cita previa",
        tipoCosto = "$500 por sesión",
        verificado = true,
        lat = 17.5428, lng = -99.4992,
        rating = 4.8,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m17!1m12!1m3!1d3804.259418924326!2d-99.49919129999999!3d17.5428314!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1m1!2zMTfCsDMyJzM0LjIiTiA5OcKwMjknNTcuMSJX!5e0!3m2!1ses-419!2smx!4v1778992119772!5m2!1ses-419!2smx"
    ),
    EntradaDirectorio(
        id = "psy-raquel",
        nombre = "Lic. Raquel Cepeda Salazar",
        tipo = TipoDirectorio.PSICOLOGO,
        descripcion = "Psicoterapia especializada e intervención en crisis. " +
                "Atención individualizada y confidencial.",
        direccion = "Torre Médica Siglo XXI, Chilpancingo",
        telefono = "7471562290",
        horario = "Con cita previa",
        especialidad = "Psicoterapia especializada · Crisis",
        tipoCita = "Con cita previa",
        tipoCosto = "Sin especificar",
        verificado = false,
        lat = 17.5408, lng = -99.5009,
        rating = 4.6,
        mapEmbedUrl = "https://www.google.com/maps/embed?pb=!1m17!1m12!1m3!1d3804.3021321121523!2d-99.50085449999999!3d17.5407963!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1m1!2zMTfCsDMyJzI2LjkiTiA5OcKwMzAnMDMuMSJX!5e0!3m2!1ses-419!2smx!4v1778992171451!5m2!1ses-419!2smx"
    ),

    // ── LÍNEAS DE APOYO ───────────────────────────────
    EntradaDirectorio(
        id = "linea-vida",
        nombre = "Línea de la Vida — CONADIC",
        tipo = TipoDirectorio.LINEA,
        descripcion = "Atención psicológica gratuita y confidencial " +
                "las 24 horas del día, los 365 días del año.",
        telefono = "8009112000",
        horario = "24 horas · 365 días",
        tags = listOf("Gratuita", "Nacional", "Confidencial", "24/7")
    ),
    EntradaDirectorio(
        id = "dif-guerrero",
        nombre = "DIF Guerrero — Chilpancingo",
        tipo = TipoDirectorio.LINEA,
        descripcion = "Atención psicológica gratuita, presencial y telefónica, " +
                "para personas y familias en situación de vulnerabilidad.",
        direccion = "Blvd. René Juárez Cisneros #62, Chilpancingo",
        telefono = "7474718490",
        horario = "Lunes a viernes · Horario hábil",
        tags = listOf("Gratuita", "Presencial", "Familias")
    ),
    EntradaDirectorio(
        id = "cjm",
        nombre = "Centro de Justicia para las Mujeres",
        tipo = TipoDirectorio.LINEA,
        descripcion = "Apoyo psicológico y jurídico gratuito para mujeres " +
                "en situación de riesgo o violencia.",
        telefono = "7474719997",
        horario = "Lunes a viernes · Horario hábil",
        tags = listOf("Mujeres", "Apoyo jurídico", "Gratuita")
    ),
    EntradaDirectorio(
        id = "consejo-ciudadano",
        nombre = "Consejo Ciudadano CDMX",
        tipo = TipoDirectorio.LINEA,
        descripcion = "Apoyo emocional por teléfono. Atiende llamadas " +
                "de todo el país, incluido Guerrero.",
        telefono = "5555335533",
        horario = "24 horas · 365 días",
        tags = listOf("Gratuita", "Nacional", "24/7")
    ),
)

@HiltViewModel
class DirectorioViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    var filtroActivo by mutableStateOf<TipoDirectorio?>(null)
        private set

    val resultados: List<EntradaDirectorio>
        get() = DIRECTORIO_DATA.filter { entrada ->
            val matchQuery = query.isBlank() ||
                entrada.nombre.contains(query, ignoreCase = true) ||
                entrada.descripcion.contains(query, ignoreCase = true) ||
                entrada.especialidad?.contains(query, ignoreCase = true) == true
            val matchFiltro = filtroActivo == null || entrada.tipo == filtroActivo
            matchQuery && matchFiltro
        }

    var hayInternet by mutableStateOf(false)
        private set

    init {
        hayInternet = checkConnectivity()
    }

    private fun checkConnectivity(): Boolean = try {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (_: Exception) {
        false
    }

    fun onQueryChange(newQuery: String) { query = newQuery }

    fun onFiltroChange(tipo: TipoDirectorio?) {
        filtroActivo = if (filtroActivo == tipo) null else tipo
    }
}
