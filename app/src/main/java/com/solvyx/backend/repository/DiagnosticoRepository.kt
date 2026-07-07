package com.solvyx.backend.repository

import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.backend.models.FRECUENCIA_P2
import com.solvyx.backend.models.FRECUENCIA_P3_P7
import com.solvyx.backend.models.FRECUENCIA_P4
import com.solvyx.backend.models.OPCIONES_P5_P6
import com.solvyx.backend.models.OPCIONES_P8
import com.solvyx.backend.models.Pregunta
import com.solvyx.backend.models.ResultadoDiagnostico
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticoRepository @Inject constructor() {

    fun obtenerPreguntas(sustancia: String): List<Pregunta> {
        val preguntas = mutableListOf(
            Pregunta(id = 1, texto = "En los últimos 3 meses, ¿con qué frecuencia consumiste $sustancia?", opciones = FRECUENCIA_P2),
            Pregunta(id = 2, texto = "¿Con qué frecuencia sentiste deseo de consumir $sustancia?", opciones = FRECUENCIA_P3_P7),
            Pregunta(id = 3, texto = "¿El consumo de $sustancia causó problemas?", opciones = FRECUENCIA_P4),
            Pregunta(id = 4, texto = "¿Descuidaste responsabilidades por consumir $sustancia?", opciones = OPCIONES_P5_P6),
            Pregunta(id = 5, texto = "¿Alguien mostró preocupación por tu consumo?", opciones = OPCIONES_P5_P6),
            Pregunta(id = 6, texto = "¿Intentaste dejarlo y no pudiste?", opciones = OPCIONES_P5_P6)
        )
        if (sustancia.lowercase() == "cristal") {
            preguntas.add(Pregunta(id = 7, texto = "¿Has consumido cristal por vía inyectada?", opciones = OPCIONES_P8))
        }
        return preguntas
    }

    fun evaluar(sustanciaId: String, respuestas: List<Int>): ResultadoDiagnostico {
        val p2 = respuestas[0]
        val p3 = respuestas[1]
        val p4 = respuestas[2]
        val p5 = respuestas[3]
        val p6 = respuestas[4]
        val p7 = respuestas[5]
        val p8 = respuestas.getOrNull(6)
        val puntaje = p2 + p3 + p4 + p5 + p6 + p7
        val nivel = determinarNivel(puntaje)
        return ResultadoDiagnostico(
            sustanciaId = sustanciaId,
            p2Frecuencia = p2,
            p3Craving = p3,
            p4Problemas = p4,
            p5Obligaciones = p5,
            p6Preocupacion = p6,
            p7Intentos = p7,
            p8Inyectado = p8,
            puntaje = puntaje,
            nivel = nivel,
            recomendacion = generarRecomendacion(nivel)
        )
    }

    private fun determinarNivel(puntaje: Int): NivelRiesgo = when {
        puntaje <= 10 -> NivelRiesgo.BAJO
        puntaje <= 26 -> NivelRiesgo.MODERADO
        else -> NivelRiesgo.ALTO
    }

    private fun generarRecomendacion(nivel: NivelRiesgo): String = when (nivel) {
        NivelRiesgo.BAJO -> "Riesgo bajo."
        NivelRiesgo.MODERADO -> "Se recomienda seguimiento."
        NivelRiesgo.ALTO -> "Se recomienda atención profesional."
    }
}
