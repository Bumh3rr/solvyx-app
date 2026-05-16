package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

val cristalInfoTree = DecisionTree(

    id = "cristal_info",

    nombre = "Información Cristal",

    nodoInicialId = "inicio",

    nodos = mapOf(

        "inicio" to DecisionNode(

            id = "inicio",

            texto =
                "¿Conoces los efectos del cristal en cuerpo y mente?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "riesgo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "efectos"
                )
            )
        ),

        "efectos" to DecisionNode(

            id = "efectos",

            texto =
                "El cristal puede provocar:",

            mensaje =
                """
                • Ansiedad
                • Agitación
                • Taquicardia
                • Conductas impulsivas
                • Alteración del juicio
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "impacto"
                )
            )
        ),

        "impacto" to DecisionNode(

            id = "impacto",

            texto =
                "¿Sabías que puede afectar escuela, trabajo y relaciones?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "abstinencia"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "problemas_sociales"
                )
            )
        ),

        "problemas_sociales" to DecisionNode(

            id = "problemas_sociales",

            texto =
                "El consumo problemático puede provocar abandono de actividades importantes.",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "abstinencia"
                )
            )
        ),

        "abstinencia" to DecisionNode(

            id = "abstinencia",

            texto =
                "¿Sabías que la abstinencia puede provocar fatiga y alteraciones del sueño?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "final"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "info_abstinencia"
                )
            )
        ),

        "info_abstinencia" to DecisionNode(

            id = "info_abstinencia",

            texto =
                "La abstinencia puede incluir fatiga intensa y cambios emocionales.",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "final"
                )
            )
        ),

        "riesgo" to DecisionNode(

            id = "riesgo",

            texto =
                "¿Quieres aprender a identificar señales de riesgo?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "senales"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "final"
                )
            )
        ),

        "senales" to DecisionNode(

            id = "senales",

            texto =
                "Señales importantes de riesgo:",

            mensaje =
                """
                • Craving intenso
                • Consumo impulsivo
                • Aislamiento social
                • Ansiedad
                • Conductas de riesgo
                • Continuar consumiendo pese a consecuencias negativas
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "final" to DecisionNode(

            id = "final",

            texto =
                "Continúa reforzando hábitos saludables y apoyo emocional.",

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)