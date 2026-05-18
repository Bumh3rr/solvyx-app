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
            texto = "¿Quieres que te cuente, sin juicio, los efectos del vape en tu cuerpo y tu mente?",
            mensaje =
                """
                Te lo explico claro y sin reganos.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si, quiero entenderlo bien",
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
            texto = "El aerosol del vape no es vapor de agua; puede contener sustancias irritantes y toxicas:",
            mensaje =
                """
                • Nicotina (muchos liquidos la contienen y genera dependencia).
                • Metales que pueden desprenderse de la resistencia al calentarse.
                • Compuestos organicos volatiles que irritan vias respiratorias.
                • Saborizantes que, al inhalarse, pueden danar el tejido pulmonar.
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
            texto = "¿Sabias que el uso frecuente puede afectar tu rendimiento, tu condicion fisica y tus relaciones?",
            mensaje =
                """
                No solo es el momento de vapear: tambien cambia rutinas, energia y decisiones.
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
                )
            )
        ),

        "problemas_sociales" to DecisionNode(
            id = "problemas_sociales",
            texto = "El vapeo frecuente puede meterse en tu rutina sin que lo notes:",
            mensaje =
                """
                • Aislarte para vapear a escondidas.
                • Gasto constante que se acumula.
                • Menos aire al hacer ejercicio o subir escaleras.
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
            texto = "¿Sabias que pasar unas horas sin nicotina puede cambiar tu estado de animo?",
            mensaje =
                """
                No significa debilidad; es el cuerpo ajustandose.
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
                )
            )
        ),

        "info_abstinencia" to DecisionNode(
            id = "info_abstinencia",
            texto = "La abstinencia de nicotina puede incluir:",
            mensaje =
                """
                • Ansiedad o inquietud que sube y baja.
                • Irritabilidad o cambios de humor.
                • Dolor de cabeza y dificultad para concentrarte.
                • Problemas para dormir los primeros dias.
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
            texto = "¿Te gustaria identificar cuando el vapeo se vuelve de riesgo?",
            mensaje =
                """
                Saberlo ayuda a pedir apoyo a tiempo.
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
                • Necesidad de vapear poco despues de despertar.
                • Usarlo en lugares donde no se permite.
                • Seguir vapeando aunque ya tengas tos, irritacion o resequedad.
                • Ansiedad intensa si no tienes bateria o liquido.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // FINAL
        // =========================
        "final" to DecisionNode(
            id = "final",
            texto = "Tu salud es primero. Si quieres apoyo, aqui estoy.",
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