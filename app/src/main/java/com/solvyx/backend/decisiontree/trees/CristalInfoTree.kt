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
                "¿Quieres saber, sin juicio, que efectos puede tener el cristal en cuerpo y mente?",

            mensaje =
                """
                Estoy aqui para explicartelo claro y sin reganos.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "riesgo"
                ),

                DecisionOption(
                    texto = "No, quiero empezar por lo basico",
                    siguienteNodoId = "efectos"
                ),

                DecisionOption(
                    texto = "Quiero mitos y realidades",
                    siguienteNodoId = "mitos"
                )
            )
        ),

        "efectos" to DecisionNode(

            id = "efectos",

            texto =
                "El cristal puede provocar efectos intensos en el cuerpo y la mente:",

            mensaje =
                """
                • Ansiedad, inquietud o paranoia
                • Taquicardia y sudoracion
                • Insomnio y falta de apetito
                • Conductas impulsivas y alteracion del juicio

                Estos efectos pueden variar segun la persona y la cantidad.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "impacto"
                ),

                DecisionOption(
                    texto = "Ver mitos y realidades",
                    siguienteNodoId = "mitos"
                )
            )
        ),

        "mitos" to DecisionNode(

            id = "mitos",

            texto =
                "Mitos y realidades sobre el cristal:",

            mensaje =
                """
                • Mito: "Me ayuda a rendir". Realidad: puede dar sensacion de energia, pero aumenta errores, impulsividad y desgaste.
                • Mito: "Solo uso cuando quiero". Realidad: la dependencia puede aparecer aun con consumo esporadico.
                • Mito: "Si duermo despues, se me pasa". Realidad: el insomnio y la ansiedad pueden durar dias.
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
                "¿Sabias que el consumo puede afectar escuela, trabajo y relaciones?",

            mensaje =
                """
                No es solo el momento del uso; tambien impacta rutinas, animo y decisiones.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "abstinencia"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "problemas_sociales"
                ),

                DecisionOption(
                    texto = "Quiero saber el impacto a largo plazo",
                    siguienteNodoId = "impacto_largo_plazo"
                )
            )
        ),

        "impacto_largo_plazo" to DecisionNode(

            id = "impacto_largo_plazo",

            texto =
                "Impacto posible a largo plazo:",

            mensaje =
                """
                • Mayor riesgo de ansiedad, depresion o paranoia persistente
                • Problemas de sueno y memoria
                • Deterioro en relaciones y estabilidad laboral

                No a todas las personas les pasa igual, pero el riesgo aumenta con el tiempo y la frecuencia.
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
                "¿Sabias que al dejar de consumir pueden aparecer molestias?",

            mensaje =
                """
                Es una reaccion comun del cuerpo mientras se ajusta.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "final"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "info_abstinencia"
                ),

                DecisionOption(
                    texto = "Quiero saber cuando es urgencia",
                    siguienteNodoId = "senales_alarma"
                )
            )
        ),

        "info_abstinencia" to DecisionNode(

            id = "info_abstinencia",

            texto =
                "La abstinencia puede incluir sintomas como:",

            mensaje =
                """
                • Cansancio intenso o sueno excesivo
                • Estado de animo bajo o irritabilidad
                • Ansiedad y dificultad para concentrarse
                • Cambios en el apetito y el sueno
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "final"
                )
            )
        ),

        "senales_alarma" to DecisionNode(

            id = "senales_alarma",

            texto =
                "Señales de alerta para buscar ayuda urgente:",

            mensaje =
                """
                • Dolor en el pecho, dificultad para respirar o desmayo
                • Confusion intensa, paranoia extrema o alucinaciones
                • Agitacion que no baja o riesgo de hacerse dano

                En Mexico, llama al 911 si hay riesgo inmediato.
                """.trimIndent(),

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

            mensaje =
                """
                Te ayuda a saber cuando pedir apoyo extra.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "senales"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "final"
                ),

                DecisionOption(
                    texto = "Tambien quiero mitos y realidades",
                    siguienteNodoId = "mitos"
                )
            )
        ),

        "senales" to DecisionNode(

            id = "senales",

            texto =
                "Señales importantes de riesgo:",

            mensaje =
                """
                • Craving intenso y frecuente
                • Consumo impulsivo o compulsivo
                • Aislamiento social y perdida de rutinas
                • Ansiedad, irritabilidad o paranoia
                • Continuar consumiendo pese a consecuencias negativas
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "final" to DecisionNode(

            id = "final",

            texto =
                "Cuidar tu salud es una decision valiente. ¿Quieres pasos simples para pedir apoyo?",

            mensaje =
                """
                Si prefieres terminar aqui, esta bien.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si, quiero pasos",
                    siguienteNodoId = "pasos_apoyo"
                ),

                DecisionOption(
                    texto = "No, terminar",
                    siguienteNodoId = "fin"
                )
            )
        ),

        "pasos_apoyo" to DecisionNode(

            id = "pasos_apoyo",

            texto =
                "Pasos simples para pedir apoyo:",

            mensaje =
                """
                • Elige a alguien de confianza y dile: "Necesito apoyo, no quiero consumir".
                • Si puedes, aléjate del lugar donde estas y ve a un espacio seguro.
                • Considera hablar con un profesional; no tienes que hacerlo solo.

                En Mexico, la Linea de la Vida atiende 24/7 al 800 911 2000.
                Si hay una urgencia, llama al 911.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "fin" to DecisionNode(

            id = "fin",

            texto =
                "Gracias por cuidar de ti. Aqui estoy para apoyarte.",

            mensaje =
                """
                En Mexico, la Linea de la Vida atiende 24/7 al 800 911 2000.
                Si hay una urgencia, llama al 911.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)