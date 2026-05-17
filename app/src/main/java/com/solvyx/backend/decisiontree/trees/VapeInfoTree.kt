package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

val vapeInfoTree = DecisionTree(
    id = "vape_info",
    nombre = "Información Vapeo",
    nodoInicialId = "inicio",
    nodos = mapOf(

        // =========================
        // INICIO
        // =========================
        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿Conoces los efectos reales que tienen los vapeadores en tu cuerpo y cerebro?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, los conozco",
                    siguienteNodoId = "evaluar_riesgo"
                ),
                DecisionOption(
                    texto = "No del todo / Tengo dudas",
                    siguienteNodoId = "efectos_vape"
                )
            )
        ),

        "efectos_vape" to DecisionNode(
            id = "efectos_vape",
            texto = "El aerosol del vapeador NO es vapor de agua; contiene sustancias altamente tóxicas:",
            mensaje =
                """
                • Sales de nicotina ultra concentradas (generan adicción más rápido que el cigarro común).
                • Metales pesados (níquel, estaño y plomo) que se desprenden de la resistencia al calentarse.
                • Compuestos orgánicos volátiles que dañan el tejido pulmonar profundamente.
                • Químicos saborizantes asociados a enfermedades pulmonares graves (como el diacetilo).
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "impacto_vida"
                )
            )
        ),

        "impacto_vida" to DecisionNode(
            id = "impacto_vida",
            texto = "¿Sabías que el uso constante del vapeador puede afectar tu rendimiento escolar, tu condición física y tus relaciones?",
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
            texto = "El vapeo frecuente genera una dependencia invisible que altera tu rutina:",
            mensaje =
                """
                • Aislamiento para poder vapear a escondidas en baños o salones.
                • Gasto económico constante que afecta tus finanzas personales o familiares.
                • Disminución del rendimiento físico y fatiga prematura al hacer ejercicio.
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
            texto = "¿Sabías que la falta de nicotina por pasar unas horas sin vapear provoca cambios muy rápidos en tu estado de ánimo?",
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
            texto = "Al ser una sustancia de acción rápida, la abstinencia de la nicotina en vapeadores incluye:",
            mensaje =
                """
                • Ansiedad y desesperación intensa a los pocos minutos o horas de dejarlo.
                • Irritabilidad severa y cambios drásticos de humor inexplicables.
                • Dolores de cabeza y dificultad severa para concentrarte en tus clases.
                • Problemas para conciliar el sueño o insomnio.
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
        // EVALUACIÓN DE RIESGO
        // =========================
        "evaluar_riesgo" to DecisionNode(
            id = "evaluar_riesgo",
            texto = "¿Te gustaría aprender a identificar cuándo el uso del vapeador se convierte en un comportamiento de riesgo?",
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

        // =========================
        // SEÑALES DE ALERTA
        // =========================
        "senales" to DecisionNode(
            id = "senales",
            texto = "Señales de alerta de dependencia al vapeo:",
            mensaje =
                """
                • Sentir una necesidad incontrolable de vapear apenas te despiertas por la mañana.
                • Esconder el dispositivo o usarlo en espacios prohibidos (salones de clase, baños, cines).
                • Continuar vapeando a pesar de presentar tos crónica, dolor de pecho o resequedad severa.
                • Experimentar desesperación o ansiedad intensa si te quedas sin batería o líquido.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // FINAL
        // =========================
        "final" to DecisionNode(
            id = "final",
            texto = "Recuerda que tú tienes el control absoluto de tus decisiones. Continúa fortaleciendo tus hábitos saludables y protegiendo tu capacidad pulmonar.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)