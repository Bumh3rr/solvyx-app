package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

// Auditado por psicologo-solvyx 2026-07-12 v2 (conversacional + RD)

val cristalInfoTree = DecisionTree(
    id = "cristal_info",
    nombre = "Información Cristal",
    nodoInicialId = "inicio",
    nodos = mapOf(

        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿Quieres saber, sin rollo, cómo te puede afectar el cristal?",
            mensaje = "Te lo cuento claro y sin juzgarte. Tú decides qué hacer con la información.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, cuéntame",
                    siguienteNodoId = "efectos"
                ),
                DecisionOption(
                    texto = "Mejor dime mitos y realidades",
                    siguienteNodoId = "mitos"
                )
            )
        ),

        "efectos" to DecisionNode(
            id = "efectos",
            texto = "El cristal puede provocar efectos intensos en cuerpo y mente.",
            mensaje = "Ansiedad, inquietud o paranoia. Taquicardia y sudoración. Insomnio y pérdida de apetito. Conductas impulsivas y alteración del juicio. Cada persona lo vive distinto.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir explorando",
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
            texto = "Algunos mitos frecuentes sobre el cristal:",
            mensaje = "Mito: me ayuda a rendir. Realidad: da sensación de energía, pero sube los errores y el desgaste. Mito: solo lo uso cuando quiero. Realidad: la dependencia puede aparecer incluso con consumo esporádico. Mito: si duermo después, se pasa. Realidad: el insomnio y la ansiedad pueden durar días.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir explorando",
                    siguienteNodoId = "impacto"
                )
            )
        ),

        "impacto" to DecisionNode(
            id = "impacto",
            texto = "¿Sabías que el consumo puede afectar la escuela, el trabajo y las relaciones?",
            mensaje = "No es solo el momento del uso: también impacta rutinas, ánimo y decisiones. A veces uno se entera por terceros, no por uno mismo.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, me ha pasado",
                    siguienteNodoId = "impacto_largo_plazo"
                ),
                DecisionOption(
                    texto = "No de momento",
                    siguienteNodoId = "abstinencia"
                ),
                DecisionOption(
                    texto = "Quiero ver el panorama completo",
                    siguienteNodoId = "impacto_largo_plazo"
                )
            )
        ),

        "impacto_largo_plazo" to DecisionNode(
            id = "impacto_largo_plazo",
            texto = "Mirando a meses y años, el consumo puede dejar huella:",
            mensaje = "Mayor riesgo de ansiedad, depresión o paranoia persistente. Problemas de sueño y memoria. Desgaste en relaciones y estabilidad. No a todas las personas les pasa igual, pero el riesgo sube con el tiempo y la frecuencia.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "abstinencia"
                )
            )
        ),

        "abstinencia" to DecisionNode(
            id = "abstinencia",
            texto = "Cuando se deja de consumir pueden aparecer molestias. ¿Sabías cuáles?",
            mensaje = "Es una reacción común del cuerpo mientras se ajusta. No es para siempre.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, dime las señales",
                    siguienteNodoId = "info_abstinencia"
                ),
                DecisionOption(
                    texto = "No, cuéntame",
                    siguienteNodoId = "info_abstinencia"
                ),
                DecisionOption(
                    texto = "¿Cuándo es urgencia?",
                    siguienteNodoId = "senales_alarma"
                )
            )
        ),

        "info_abstinencia" to DecisionNode(
            id = "info_abstinencia",
            texto = "La abstinencia puede incluir:",
            mensaje = "Cansancio intenso o sueño excesivo. Ánimo bajo o irritabilidad. Ansiedad y dificultad para concentrarte. Cambios en el apetito y el sueño. Suelen ir bajando con el tiempo.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "final"
                )
            )
        ),

        "senales_alarma" to DecisionNode(
            id = "senales_alarma",
            texto = "Señales de alerta para buscar ayuda urgente:",
            mensaje = "Dolor en el pecho, dificultad para respirar o desmayo. Confusión intensa, paranoia extrema o alucinaciones. Agitación que no baja o riesgo de hacerte daño. En México, 911 si hay riesgo inmediato.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "final"
                )
            )
        ),

        "riesgo" to DecisionNode(
            id = "riesgo",
            texto = "¿Quieres aprender a identificar señales de riesgo de enganche?",
            mensaje = "Te ayuda a saber cuándo pedir apoyo extra, antes de que escale.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "senales_enganche"
                ),
                DecisionOption(
                    texto = "No, mejor ir al final",
                    siguienteNodoId = "final"
                ),
                DecisionOption(
                    texto = "También mitos y realidades",
                    siguienteNodoId = "mitos"
                )
            )
        ),

        "senales_enganche" to DecisionNode(
            id = "senales_enganche",
            texto = "Señales frecuentes de enganche al cristal:",
            mensaje = "Craving intenso y recurrente. Consumo impulsivo o compulsivo. Aislamiento social y pérdida de rutinas. Ansiedad, irritabilidad o paranoia. Seguir consumiendo aunque ya haya consecuencias negativas.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "final" to DecisionNode(
            id = "final",
            texto = "Cuidar tu salud es una decisión valiente. ¿Quieres unos pasos simples para pedir apoyo?",
            mensaje = "Si prefieres cerrar aquí, está bien. Si quieres apoyo, también.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, dame pasos",
                    siguienteNodoId = "pasos_apoyo"
                ),
                DecisionOption(
                    texto = "No, cerrar",
                    siguienteNodoId = "fin"
                )
            )
        ),

        "pasos_apoyo" to DecisionNode(
            id = "pasos_apoyo",
            texto = "Pasos simples para pedir apoyo:",
            mensaje = "Elige a alguien de tu confianza y dile: necesito apoyo, no quiero consumir. Si puedes, aléjate del lugar y ve a un espacio seguro. Considera hablar con un profesional, no tienes que hacerlo solo. En México, Línea de la Vida 800 911 2000 (24/7). Si hay urgencia, 911.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin" to DecisionNode(
            id = "fin",
            texto = "Gracias por cuidar de ti. Aquí estoy cuando quieras.",
            mensaje = "En México, Línea de la Vida 800 911 2000 (24/7). Si hay urgencia, 911.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)
