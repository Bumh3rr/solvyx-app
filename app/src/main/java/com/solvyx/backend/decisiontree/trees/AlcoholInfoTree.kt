package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

val alcoholInfoTree = DecisionTree(

    id = "alcohol_info",

    nombre = "Información Alcohol",

    nodoInicialId = "inicio",

    nodos = mapOf(

        "inicio" to DecisionNode(

            id = "inicio",

            texto =
                "¿Consumes alcohol actualmente?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "consumo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "prevencion"
                )
            )
        ),

        "prevencion" to DecisionNode(

            id = "prevencion",

            texto =
                "Información preventiva sobre alcohol.",

            mensaje =
                """
                • Riesgos del consumo excesivo
                • Alteración del juicio
                • Riesgo físico y emocional
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "consumo" to DecisionNode(

            id = "consumo",

            texto =
                "¿Alguna vez consumiste más de lo planeado?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "riesgo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "afectacion"
                )
            )
        ),

        "riesgo" to DecisionNode(

            id = "riesgo",

            texto =
                "Consumir más de lo previsto puede ser señal de consumo problemático.",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "afectacion"
                )
            )
        ),

        "afectacion" to DecisionNode(

            id = "afectacion",

            texto =
                "¿El alcohol afectó escuela, trabajo o relaciones?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "impacto_social"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "abstinencia"
                )
            )
        ),

        "impacto_social" to DecisionNode(

            id = "impacto_social",

            texto =
                "El alcohol puede provocar deterioro funcional y social.",

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
                "¿Has sentido ansiedad, temblores o insomnio al dejar de beber?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "sintomas"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "final"
                )
            )
        ),

        "sintomas" to DecisionNode(

            id = "sintomas",

            texto =
                "Estos síntomas pueden relacionarse con abstinencia.",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "final"
                )
            )
        ),

        "final" to DecisionNode(

            id = "final",

            texto =
                "Información importante sobre alcohol.\n\n" +
                "• Habla pastosa\n" +
                "• Incoordinación\n" +
                "• Alteración del juicio\n" +
                "• Riesgo físico",

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)