package com.solvyx.ui.screens.directory.data

import com.solvyx.ui.components.common.HelpLine
import com.solvyx.ui.screens.directory.model.DirectoryCategory
import com.solvyx.ui.screens.directory.model.DirectoryEntry

/**
 * Real providers in Chilpancingo, Guerrero. Hardcoded by product decision — this is not mock data
 * and must not move to Firestore. "Sin costo" / "24/7" live in [DirectoryEntry.isFree] and
 * [DirectoryEntry.available24h] (so they can be filtered), not in the tags.
 */
object DirectoryData {

    val entries: List<DirectoryEntry> = listOf(

        // ── Líneas de apoyo ──────────────────────────────
        DirectoryEntry(
            id = "linea-vida",
            name = "Línea de la Vida — CONADIC",
            category = DirectoryCategory.HELPLINE,
            description = "Atención psicológica gratuita y confidencial las 24 horas del día, " +
                "los 365 días del año.",
            phone = HelpLine.LINEA_DE_LA_VIDA.dialNumber,
            schedule = "24 horas · 365 días",
            isFree = true,
            available24h = true,
            tags = listOf("Nacional", "Confidencial")
        ),
        DirectoryEntry(
            id = "consejo-ciudadano",
            name = "Consejo Ciudadano CDMX",
            category = DirectoryCategory.HELPLINE,
            description = "Apoyo emocional por teléfono. Atiende llamadas de todo el país, " +
                "incluido Guerrero.",
            phone = "5555335533",
            schedule = "24 horas · 365 días",
            isFree = true,
            available24h = true,
            tags = listOf("Nacional")
        ),
        DirectoryEntry(
            id = "dif-guerrero",
            name = "DIF Guerrero — Chilpancingo",
            category = DirectoryCategory.HELPLINE,
            description = "Atención psicológica gratuita, presencial y telefónica, para personas " +
                "y familias en situación de vulnerabilidad.",
            phone = "7474718490",
            address = "Blvd. René Juárez Cisneros #62, Chilpancingo",
            schedule = "Lunes a viernes · Horario hábil",
            isFree = true,
            tags = listOf("Presencial", "Familias")
        ),
        DirectoryEntry(
            id = "cjm",
            name = "Centro de Justicia para las Mujeres",
            category = DirectoryCategory.HELPLINE,
            description = "Apoyo psicológico y jurídico gratuito para mujeres en situación de " +
                "riesgo o violencia.",
            phone = "7474719997",
            schedule = "Lunes a viernes · Horario hábil",
            isFree = true,
            tags = listOf("Mujeres", "Apoyo jurídico")
        ),

        // ── Centros de atención ──────────────────────────
        DirectoryEntry(
            id = "cij-chilpancingo",
            name = "CIJ Chilpancingo",
            category = DirectoryCategory.CENTER,
            description = "Centro de Integración Juvenil con prevención y tratamiento de " +
                "adicciones, atención psicológica, orientación familiar y programas " +
                "comunitarios y educativos.",
            phone = "7474949445",
            address = "Salubridad, C.P. 39096, Chilpancingo de los Bravo, Gro.",
            schedule = "Lunes a viernes · 8:30 a 19:30 hrs",
            isFree = true,
            isVerified = true,
            latitude = 17.5174,
            longitude = -99.4994,
            tags = listOf("Adicciones", "Atención psicológica", "Orientación familiar")
        ),
        DirectoryEntry(
            id = "clinica-salud-emocional",
            name = "Clínica de Salud Emocional",
            category = DirectoryCategory.CENTER,
            description = "Dependencia de la Secretaría de Salud de Guerrero. Atención " +
                "psicológica y emocional para población abierta.",
            phone = "7474800457",
            address = "Eje Central, Col. Burócratas, C.P. 39090, Chilpancingo",
            schedule = "Lunes a domingo · 8:00 a 20:00 hrs",
            isFree = true,
            isVerified = true,
            latitude = 17.5325,
            longitude = -99.4907,
            tags = listOf("Salud mental", "Atención psicológica")
        ),
        DirectoryEntry(
            id = "cecosama",
            name = "CECOSAMA",
            category = DirectoryCategory.CENTER,
            description = "Centro de Salud Mental y Adicciones — UNEME Centro Nueva Vida. " +
                "Tratamiento especializado en salud mental y dependencias.",
            phone = "7474949883",
            address = "Venustiano Carranza #18, Col. 20 de Noviembre, C.P. 39096, Chilpancingo",
            schedule = "Lunes a viernes · 8:00 a 16:00 hrs",
            isFree = true,
            isVerified = true,
            latitude = 17.5374,
            longitude = -99.5208,
            tags = listOf("Adicciones", "Salud mental")
        ),

        // ── Psicólogos ───────────────────────────────────
        DirectoryEntry(
            id = "psy-frida",
            name = "Mtra. Frida Sianet Vázquez Lucas",
            category = DirectoryCategory.PSYCHOLOGIST,
            description = "Consultorio Psicológico Resiliencia al Cambio. Especialista en " +
                "intervención en crisis.",
            phone = "7471123344",
            specialty = "Ansiedad · Depresión · Adolescentes · Pareja",
            appointment = "Con cita previa",
            isVerified = true,
            latitude = 17.5417,
            longitude = -99.5042
        ),
        DirectoryEntry(
            id = "psy-edgar",
            name = "Lic. Edgar Medina Miguel",
            category = DirectoryCategory.PSYCHOLOGIST,
            description = "Consultorio Guerrero. Control de adicciones y trastornos de ansiedad.",
            phone = "7471188821",
            address = "Paseo Alejandro Cervantes Delgado, Zona Col. 29, C.P. 39000, Chilpancingo",
            specialty = "Trastornos de ansiedad · Control de adicciones",
            appointment = "Con cita previa",
            cost = "$600 por sesión",
            isVerified = true,
            latitude = 17.5446,
            longitude = -99.5020
        ),
        DirectoryEntry(
            id = "psy-jorge",
            name = "Lic. Jorge Peña Díaz",
            category = DirectoryCategory.PSYCHOLOGIST,
            description = "Psicoterapia, tanatología y coaching. Atención en intervención en " +
                "crisis y acompañamiento.",
            phone = "7471347760",
            address = "Prosperidad No. 4, Col. Universal, Chilpancingo",
            specialty = "Psicoterapia · Tanatología · Coaching",
            appointment = "Con cita previa",
            cost = "$500 por sesión",
            isVerified = true,
            latitude = 17.5428,
            longitude = -99.4992
        ),
        DirectoryEntry(
            id = "psy-raquel",
            name = "Lic. Raquel Cepeda Salazar",
            category = DirectoryCategory.PSYCHOLOGIST,
            description = "Psicoterapia especializada e intervención en crisis. Atención " +
                "individualizada y confidencial.",
            phone = "7471562290",
            address = "Torre Médica Siglo XXI, Chilpancingo",
            specialty = "Psicoterapia especializada · Crisis",
            appointment = "Con cita previa",
            latitude = 17.5408,
            longitude = -99.5009
        )
    )

    fun findById(id: String): DirectoryEntry? = entries.firstOrNull { it.id == id }
}
