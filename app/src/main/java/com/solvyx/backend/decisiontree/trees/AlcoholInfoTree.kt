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
                "¿Consumes alcohol actualmente o lo has hecho en los ultimos 30 dias?",

            mensaje =
                """
                No es un examen ni un juicio. Solo quiero entender tu situacion para darte info util.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
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
                "Informacion preventiva sobre alcohol.",

            mensaje =
                """
                Tomar alcohol no es una obligacion social. Tu salud y tu seguridad van primero.

                Señales de riesgo cuando se bebe en exceso:
                • Alteracion del juicio y la coordinacion
                • Mayor probabilidad de accidentes y violencia
                • Problemas de sueno y estado de animo
                • Dano al higado, corazon y sistema nervioso con el tiempo

                Si decides beber alguna vez, hacerlo con moderacion y con compania responsable reduce riesgos.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "consumo" to DecisionNode(

            id = "consumo",

            texto =
                "¿Alguna vez consumiste mas de lo planeado o te costo parar?",

            mensaje =
                """
                A muchas personas les pasa. Preguntar esto ayuda a identificar si el alcohol esta ganando demasiado espacio.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
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
                "Consumir mas de lo previsto puede ser una señal de consumo problematico.",

            mensaje =
                """
                No significa que tengas un trastorno, pero si es una alerta temprana.
                Vale la pena observar si se repite o si hay culpa, lagunas o conflictos.
                """.trimIndent(),

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
                "¿El alcohol ha afectado escuela, trabajo o relaciones?",

            mensaje =
                """
                Lo importante no es la cantidad, sino el impacto en tu vida diaria.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
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

            mensaje =
                """
                Ejemplos comunes: discusiones, faltas al trabajo, bajo rendimiento, o alejarse de personas queridas.
                Reconocerlo a tiempo ayuda a prevenir danos mayores.
                """.trimIndent(),

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
                "¿Has sentido ansiedad, temblores, sudoracion o insomnio al dejar de beber?",

            mensaje =
                """
                Estos sintomas pueden aparecer cuando el cuerpo se acostumbra al alcohol.
                Si son intensos o se acompanian de confusion o convulsiones, es una urgencia medica.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
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
                "Estos sintomas pueden relacionarse con abstinencia.",

            mensaje =
                """
                Si te esta pasando, considera buscar ayuda profesional.
                Un medico o terapeuta puede orientar un plan seguro para reducir o dejar el consumo.
                """.trimIndent(),

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
                "Informacion importante sobre alcohol:",

            mensaje =
                """
                Efectos frecuentes del consumo:
                • Habla pastosa e incoordinacion
                • Alteracion del juicio y la memoria
                • Mayor riesgo de accidentes y violencia
                • Mal sueno, ansiedad o irritabilidad al dia siguiente

                Si sientes que el alcohol te esta quitando control o paz, no estas solo.
                Buscar apoyo es un paso valiente y efectivo.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)