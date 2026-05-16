package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ResultadoDao
import com.solvyx.backend.data.local.entity.ResultadoEntity
import com.solvyx.backend.models.*
import kotlinx.coroutines.flow.Flow

class DiagnosticoRepository(

    private val resultadoDao: ResultadoDao

) {

    // Obtener preguntas
    fun obtenerPreguntas(
        sustancia: String
    ): List<Pregunta> {

        val preguntas = mutableListOf(

            Pregunta(
                id = 1,
                texto = "¿Has consumido $sustancia alguna vez en tu vida?",
                opciones = listOf(
                    Opcion("Sí", 1),
                    Opcion("No", 0)
                )
            ),

            Pregunta(
                id = 2,
                texto = "En los últimos 3 meses, ¿con qué frecuencia consumiste $sustancia?",
                opciones = FRECUENCIA_P2
            ),

            Pregunta(
                id = 3,
                texto = "¿Con qué frecuencia sentiste deseo de consumir $sustancia?",
                opciones = FRECUENCIA_P3_P7
            ),

            Pregunta(
                id = 4,
                texto = "¿El consumo de $sustancia causó problemas?",
                opciones = FRECUENCIA_P4
            ),

            Pregunta(
                id = 5,
                texto = "¿Descuidaste responsabilidades por consumir $sustancia?",
                opciones = OPCIONES_P5_P6
            ),

            Pregunta(
                id = 6,
                texto = "¿Alguien mostró preocupación por tu consumo?",
                opciones = OPCIONES_P5_P6
            ),

            Pregunta(
                id = 7,
                texto = "¿Intentaste dejarlo y no pudiste?",
                opciones = FRECUENCIA_P3_P7
            )
        )

        // P8 solo cristal
        if (sustancia.lowercase() == "cristal") {

            preguntas.add(

                Pregunta(
                    id = 8,
                    texto = "¿Has consumido cristal por vía inyectada?",
                    opciones = OPCIONES_P8
                )
            )
        }

        return preguntas
    }

    // Evaluar y guardar
    suspend fun evaluarYGuardar(
        sustanciaId: String,
        respuestas: List<Int>
    ): ResultadoDiagnostico {

        val puntajeTotal = respuestas.sum()

        val nivel = determinarNivel(puntajeTotal)

        val recomendacion =
            generarRecomendacion(nivel)

        val resultado = ResultadoDiagnostico(
            sustanciaId = sustanciaId,
            puntaje = puntajeTotal,
            nivel = nivel,
            recomendacion = recomendacion
        )

        // Guardar en Room
        resultadoDao.guardarResultado(

            ResultadoEntity(
                sustanciaId = resultado.sustanciaId,
                puntaje = resultado.puntaje,
                nivel = resultado.nivel.name,
                recomendacion = resultado.recomendacion
            )
        )

        return resultado
    }

    // Obtener historial
    fun obtenerHistorial():
            Flow<List<ResultadoEntity>> {

        return resultadoDao.obtenerResultados()
    }

    // Nivel de riesgo
    private fun determinarNivel(
        puntaje: Int
    ): NivelRiesgo {

        return when {

            puntaje <= 10 ->
                NivelRiesgo.BAJO

            puntaje <= 26 ->
                NivelRiesgo.MODERADO

            else ->
                NivelRiesgo.ALTO
        }
    }

    // Recomendación
    private fun generarRecomendacion(
        nivel: NivelRiesgo
    ): String {

        return when(nivel) {

            NivelRiesgo.BAJO ->
                "Riesgo bajo."

            NivelRiesgo.MODERADO ->
                "Se recomienda seguimiento."

            NivelRiesgo.ALTO ->
                "Se recomienda atención profesional."
        }
    }
}