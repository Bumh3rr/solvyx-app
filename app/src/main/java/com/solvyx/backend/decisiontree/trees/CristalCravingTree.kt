package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

val cristalCravingTree = DecisionTree(

    id = "cristal_craving",

    nombre = "Craving Cristal",

    nodoInicialId = "inicio",

    nodos = mapOf(

        "inicio" to DecisionNode(

            id = "inicio",

            texto =
                "¿Tienes ganas intensas de consumir cristal en este momento?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "intensidad"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "entorno"
                )
            )
        ),

        "entorno" to DecisionNode(

            id = "entorno",

            texto =
                "¿Te encuentras en un lugar o con personas relacionadas al consumo?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "evitar_estimulos"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "habitos_saludables"
                )
            )
        ),

        "evitar_estimulos" to DecisionNode(

            id = "evitar_estimulos",

            texto =
                "Los estímulos relacionados pueden aumentar el craving.",

            mensaje =
                """
                • Cambiar de lugar
                • Buscar compañía segura
                • Evitar situaciones de riesgo
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "habitos_saludables" to DecisionNode(

            id = "habitos_saludables",

            texto =
                "Continúa reforzando hábitos saludables y espacios seguros.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "intensidad" to DecisionNode(

            id = "intensidad",

            texto =
                "¿Qué tan fuerte es el deseo?",

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

        "leve" to DecisionNode(

            id = "leve",

            texto =
                "¿Puedes realizar otra actividad durante 20 minutos?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "estrategias"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "moderado"
                )
            )
        ),

        "estrategias" to DecisionNode(

            id = "estrategias",

            texto =
                "Prueba estas estrategias:",

            mensaje =
                """
                • Caminar
                • Escuchar música
                • Ejercicio breve
                • Tomar agua
                • Alejarte del estímulo
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
                "Lograste manejar el craving.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "moderado" to DecisionNode(

            id = "moderado",

            texto =
                "¿Presentas ansiedad, tensión o agitación?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "respiracion"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "solo"
                )
            )
        ),

        "respiracion" to DecisionNode(

            id = "respiracion",

            texto =
                "Busca regulación emocional.",

            mensaje =
                """
                • Respiración guiada
                • Buscar espacio tranquilo
                • Contactar apoyo
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "solo"
                )
            )
        ),

        "solo" to DecisionNode(

            id = "solo",

            texto =
                "¿Estás solo?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "riesgo_solo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "control_impulso"
                )
            )
        ),

        "riesgo_solo" to DecisionNode(

            id = "riesgo_solo",

            texto =
                "Permanecer solo puede aumentar el riesgo de consumo.",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "control_impulso"
                )
            )
        ),

        "control_impulso" to DecisionNode(

            id = "control_impulso",

            texto =
                "¿Sientes que puedes controlar el impulso?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "moderado_controlado"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "alto"
                )
            )
        ),

        "moderado_controlado" to DecisionNode(

            id = "moderado_controlado",

            texto =
                "Continúa usando estrategias de regulación.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "alto" to DecisionNode(

            id = "alto",

            texto =
                "El craving intenso puede aumentar el riesgo de consumo impulsivo.",

            mensaje =
                "¿Presentas síntomas físicos importantes?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "apoyo_inmediato"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "contactar_apoyo"
                )
            )
        ),

        "apoyo_inmediato" to DecisionNode(

            id = "apoyo_inmediato",

            texto =
                "Busca apoyo inmediato.",

            mensaje =
                """
                • Taquicardia
                • Sudoración
                • Agitación
                • Ansiedad intensa
                • Dolor en el pecho
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "contactar_apoyo" to DecisionNode(

            id = "contactar_apoyo",

            texto =
                "¿Puedes contactar a alguien de confianza ahora mismo?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "llamar_apoyo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "emergencias"
                )
            )
        ),

        "llamar_apoyo" to DecisionNode(

            id = "llamar_apoyo",

            texto =
                "Contacta inmediatamente a una persona de confianza.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "emergencias" to DecisionNode(

            id = "emergencias",

            texto =
                "Busca apoyo profesional o línea de emergencia.",

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)