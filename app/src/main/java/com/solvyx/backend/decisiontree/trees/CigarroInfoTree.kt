package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType

val cigarroInfoTree = DecisionTree(
    id = "cigarro_info",
    nombre = "Información Cigarro",
    nodoInicialId = "inicio",
    nodos = mapOf(

        // =========================
        // INICIO
        // =========================
        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿Conoces los compuestos químicos y los efectos reales del tabaco en tu salud?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, los conozco",
                    siguienteNodoId = "riesgo"
                ),
                DecisionOption(
                    texto = "No del todo / Tengo dudas",
                    siguienteNodoId = "efectos"
                )
            )
        ),

        // =========================
        // EFECTOS Y MITOS
        // =========================
        "efectos" to DecisionNode(
            id = "efectos",
            texto = "El humo del cigarro contiene más de 7,000 sustancias químicas, de las cuales al menos 70 causan cáncer:",
            mensaje =
                """
                • Alquitrán: Una sustancia densa que se adhiere a los pulmones obstruyendo los alveolos.
                • Monóxido de carbono: Un gas tóxico que desplaza al oxígeno en la sangre, forzando al corazón.
                • El mito de la relajación: El cigarro no relaja; solo calma temporalmente la ansiedad de la abstinencia que el mismo cigarro provocó.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "impacto"
                )
            )
        ),

        // =========================
        // IMPACTO EN LA VIDA
        // =========================
        "impacto" to DecisionNode(
            id = "impacto",
            texto = "¿Sabías que el tabaquismo causa un impacto directo e inmediato en tus actividades diarias y economía?",
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
            texto = "Consecuencias cotidianas del consumo de tabaco:",
            mensaje =
                """
                • Pérdida notable de la condición física, capacidad pulmonar y resistencia al hacer ejercicio.
                • Daño estético visible: coloración amarillenta en dientes y dedos, además de mal aliento crónico.
                • Impacto financiero severo debido al costo acumulado de las cajetillas semanales.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "abstinencia"
                )
            )
        ),

        // =========================
        // SÍNDROME DE ABSTINENCIA
        // =========================
        "abstinencia" to DecisionNode(
            id = "abstinencia",
            texto = "¿Sabías que la nicotina es una de las sustancias más adictivas y que su abstinencia genera malestares físicos?",
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
            texto = "Cuando dejas de fumar, tu cuerpo experimenta un proceso de desintoxicación que incluye:",
            mensaje =
                """
                • Fuertes deseos de fumar (craving) repetitivos durante el día.
                • Dolores de cabeza constantes debido a la regulación del flujo sanguíneo.
                • Aumento temporal de la tos (tus pulmones se están limpiando y expulsando flemas).
                • Dificultad para dormir o despertarse a mitad de la noche.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "final"
                )
            )
        ),

        // =========================
        // SEÑALES DE RIESGO
        // =========================
        "riesgo" to DecisionNode(
            id = "riesgo",
            texto = "¿Te interesa aprender a identificar las señales que indican una dependencia severa al tabaco?",
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
            texto = "Señales críticas de dependencia al cigarro:",
            mensaje =
                """
                • Necesidad imperiosa de fumar el primer cigarro del día dentro de los primeros 30 minutos tras despertar.
                • Dificultad extrema para abstenerse en lugares prohibidos (hospitales, escuelas, cines).
                • Fumar incluso cuando estás tan enfermo que debes guardar cama.
                • Consumir más de media cajetilla al día para sentir el mismo efecto.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // FINAL
        // =========================
        "final" to DecisionNode(
            id = "final",
            texto = "Tomar la decisión de cuidar tu salud es un acto de valentía. Sigue usando Solvyx para mantener tus metas claras y tus pulmones limpios.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)