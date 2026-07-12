package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

// Auditado por psicologo-solvyx 2026-07-12 v2 (conversacional + RD)

val vapeInfoTree = DecisionTree(
    id = "vape_info",
    nombre = "Información Vapeo",
    nodoInicialId = "inicio",
    nodos = mapOf(

        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿Quieres saber, sin rollo, los efectos del vape en tu cuerpo y mente?",
            mensaje = "Te lo cuento claro y sin regañar.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, quiero entenderlo",
                    siguienteNodoId = "efectos_vape"
                ),
                DecisionOption(
                    texto = "Tengo dudas / No del todo",
                    siguienteNodoId = "efectos_vape"
                )
            )
        ),

        "efectos_vape" to DecisionNode(
            id = "efectos_vape",
            texto = "El aerosol del vape no es vapor de agua. Puede contener sustancias irritantes:",
            mensaje = "Nicotina, que muchos líquidos traen y genera dependencia. Metales que pueden desprenderse al calentarse la resistencia. Compuestos orgánicos volátiles que irritan vías respiratorias. Saborizantes que, al inhalarse, pueden dañar el tejido pulmonar.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir explorando",
                    siguienteNodoId = "impacto_vida"
                )
            )
        ),

        "impacto_vida" to DecisionNode(
            id = "impacto_vida",
            texto = "¿Te interesa saber cómo impacta tu día a día?",
            mensaje = "Tos, falta de aire con esfuerzo cotidiano, irritación de garganta, mal aliento y manchas en dientes. Gasto que se acumula. Aerosol de segunda mano que afecta a quien vive contigo.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "abstinencia_vape"
                ),
                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "abstinencia_vape"
                )
            )
        ),

        "abstinencia_vape" to DecisionNode(
            id = "abstinencia_vape",
            texto = "¿Sabías que al vapear con frecuencia aparece tolerancia y al dejarlo hay malestar?",
            mensaje = "No significa que sea imposible dejarlo. Tu cuerpo se está ajustando.",
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
            texto = "La abstinencia de vapeo puede incluir:",
            mensaje = "Craving que sube y baja, irritabilidad o ansiedad leve, dificultad para dormir los primeros días y antojo cuando ves a alguien vapeando.",
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
            texto = "¿Quieres saber qué señales te avisan de que el vapeo ya está ganando espacio?",
            mensaje = "Te ayuda a decidir si necesitas apoyo extra antes de que escale.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "senales_enganche"
                ),
                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "final"
                )
            )
        ),

        "senales_enganche" to DecisionNode(
            id = "senales_enganche",
            texto = "Señales frecuentes de enganche al vapeo:",
            mensaje = "Vapear al despertar es lo primero que haces. Te cuesta no vapear en lugares donde no se permite. Sientes craving cuando ves el dispositivo. Necesitas más caladas para sentir lo mismo.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "final" to DecisionNode(
            id = "final",
            texto = "Cuidar tu salud es una decisión valiente. ¿Quieres tips iniciales para empezar?",
            mensaje = "Si prefieres cerrar aquí, está bien. Si quieres apoyo, también.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, dame tips",
                    siguienteNodoId = "tips_iniciales"
                ),
                DecisionOption(
                    texto = "No, cerrar",
                    siguienteNodoId = "fin"
                )
            )
        ),

        "tips_iniciales" to DecisionNode(
            id = "tips_iniciales",
            texto = "Tips iniciales para dejar o reducir el vapeo:",
            mensaje = "Elige una fecha cercana y avisa a alguien de confianza. Deja el dispositivo fuera de tu alcance. Cambia rutinas que disparen el antojo. Sustitutos: agua, chicle, palillo o caminar 5 minutos. Si sube el craving, espera 10 minutos y respira lento.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin" to DecisionNode(
            id = "fin",
            texto = "Cuidar tu salud es una decisión valiente. Aquí estoy cuando lo necesites.",
            mensaje = "En México, Línea de la Vida 800 911 2000 (24/7). Si hay urgencia, 911.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)
