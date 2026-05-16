package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType

val alcoholCravingTree = DecisionTree(

    id = "alcohol_craving",

    nombre = "Craving Alcohol",

    nodoInicialId = "inicio",

    nodos = mapOf(

        // =========================
        // INICIO
        // =========================

        "inicio" to DecisionNode(

            id = "inicio",

            texto =
                "¿En este momento sientes ganas intensas de consumir alcohol?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "intensidad"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "prevencion"
                )
            )
        ),

        // =========================
        // PREVENCIÓN
        // =========================

        "prevencion" to DecisionNode(

            id = "prevencion",

            texto =
                "Excelente. Mantenerte consciente ayuda a prevenir recaídas.",

            mensaje =
                "¿Quieres revisar estrategias de prevención?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "estrategias"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "fin_prevencion"
                )
            )
        ),

        "estrategias" to DecisionNode(

            id = "estrategias",

            texto =
                "Estrategias recomendadas:",

            mensaje =
                """
                • Evitar lugares asociados al consumo
                • Mantener horarios saludables
                • Buscar actividades recreativas
                • Hablar con personas de confianza
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "fin_prevencion" to DecisionNode(

            id = "fin_prevencion",

            texto =
                "Continúa reforzando hábitos saludables.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        // =========================
        // INTENSIDAD
        // =========================

        "intensidad" to DecisionNode(

            id = "intensidad",

            texto =
                "¿Qué tan fuerte es el deseo de consumir alcohol?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Leve",
                    siguienteNodoId = "leve"
                ),

                DecisionOption(
                    texto = "Moderado",
                    siguienteNodoId = "moderado"
                ),

                DecisionOption(
                    texto = "Muy fuerte",
                    siguienteNodoId = "alto"
                )
            )
        ),

        // =========================
        // LEVE
        // =========================

        "leve" to DecisionNode(

            id = "leve",

            texto =
                "¿Puedes distraerte durante 15 minutos?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "distraccion"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "moderado"
                )
            )
        ),

        "distraccion" to DecisionNode(

            id = "distraccion",

            texto =
                "Prueba estas estrategias:",

            mensaje =
                """
                • Camina
                • Escucha música
                • Toma agua
                • Respira profundamente
                • Llama a alguien
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "deseo_disminuyo"
                )
            )
        ),

        "deseo_disminuyo" to DecisionNode(

            id = "deseo_disminuyo",

            texto =
                "¿El deseo disminuyó?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "leve_controlado"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "moderado"
                )
            )
        ),

        "leve_controlado" to DecisionNode(

            id = "leve_controlado",

            texto =
                "Lograste manejar el craving sin consumir.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        // =========================
        // MODERADO
        // =========================

        "moderado" to DecisionNode(

            id = "moderado",

            texto =
                "¿Has consumido alcohol hoy?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "consumo_hoy"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "acompanado"
                )
            )
        ),

        "consumo_hoy" to DecisionNode(

            id = "consumo_hoy",

            texto =
                "El consumo puede aumentar el deseo de seguir bebiendo.",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "acompanado"
                )
            )
        ),

        "acompanado" to DecisionNode(

            id = "acompanado",

            texto =
                "¿Estás acompañado por alguien de confianza?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "sintomas"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "buscar_apoyo"
                )
            )
        ),

        "buscar_apoyo" to DecisionNode(

            id = "buscar_apoyo",

            texto =
                "Busca apoyo y aléjate de estímulos relacionados al consumo.",

            mensaje =
                """
                • Contactar familiar o amigo
                • Salir del lugar relacionado con alcohol
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "sintomas"
                )
            )
        ),

        "sintomas" to DecisionNode(

            id = "sintomas",

            texto =
                "¿Presentas ansiedad, temblores o dificultad para controlar el impulso?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "alto"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "moderado_controlado"
                )
            )
        ),

        "moderado_controlado" to DecisionNode(

            id = "moderado_controlado",

            texto =
                "Practica respiración, hidratación y evita estímulos de consumo.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        // =========================
        // ALTO
        // =========================

        "alto" to DecisionNode(

            id = "alto",

            texto =
                "Tu nivel de craving puede representar riesgo de recaída.",

            mensaje =
                "¿Sientes que podrías perder el control y consumir?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "alto_riesgo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "sintomas_abstinencia"
                )
            )
        ),

        "alto_riesgo" to DecisionNode(

            id = "alto_riesgo",

            texto =
                "Busca apoyo inmediato.",

            mensaje =
                """
                • Evita permanecer solo
                • Aleja el alcohol
                • Contacta ayuda profesional
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "sintomas_abstinencia"
                )
            )
        ),

        "sintomas_abstinencia" to DecisionNode(

            id = "sintomas_abstinencia",

            texto =
                "¿Presentas temblores, náuseas, sudoración o ansiedad intensa?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "buscar_profesional"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "alto_controlado"
                )
            )
        ),

        "buscar_profesional" to DecisionNode(

            id = "buscar_profesional",

            texto =
                "Estos síntomas pueden relacionarse con abstinencia.",

            mensaje =
                "Busca atención profesional lo antes posible.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "alto_controlado" to DecisionNode(

            id = "alto_controlado",

            texto =
                "El craving intenso puede disminuir con apoyo emocional.",

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)