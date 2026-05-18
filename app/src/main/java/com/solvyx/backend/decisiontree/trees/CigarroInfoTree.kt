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
            texto = "¿Quieres saber, sin rollo, que le hace el tabaco a tu cuerpo?",
            mensaje =
                """
                Estoy aqui para contartelo claro y sin juzgarte.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si, quiero entenderlo bien",
                    siguienteNodoId = "efectos"
                ),
                DecisionOption(
                    texto = "Ya se algo, pero tengo dudas",
                    siguienteNodoId = "impacto"
                )
            )
        ),

        // =========================
        // EFECTOS Y MITOS
        // =========================
        "efectos" to DecisionNode(
            id = "efectos",
            texto = "El humo del cigarro contiene mas de 7,000 sustancias quimicas; al menos 70 se asocian con cancer:",
            mensaje =
                """
                • Alquitran: se pega a los pulmones y reduce la capacidad de oxigeno.
                • Monoxido de carbono: desplaza al oxigeno en la sangre y fuerza al corazon.
                • Nicotina: genera dependencia y mantiene el ciclo de ansiedad.
                • El mito de la relajacion: el cigarro no relaja, solo calma la ansiedad de abstinencia que el mismo provoca.
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
            texto = "¿Sabias que el tabaquismo afecta lo que haces diario y tambien tu bolsillo?",
            mensaje =
                """
                No solo es salud futura; hay efectos que se notan en semanas.
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
            texto = "Consecuencias cotidianas del consumo de tabaco:",
            mensaje =
                """
                • Menos condicion fisica y mas falta de aire al subir escaleras.
                • Mal aliento y manchas en dientes y dedos.
                • Gasto acumulado que puede ser alto al mes.
                • Humo de segunda mano: afecta a quienes conviven contigo.
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
            texto = "¿Sabias que la nicotina genera dependencia y que al dejarla pueden aparecer molestias?",
            mensaje =
                """
                No significa que sea imposible; significa que tu cuerpo se esta ajustando.
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
            texto = "Cuando dejas de fumar, pueden aparecer sintomas comunes como:",
            mensaje =
                """
                • Deseos intensos (craving) que aparecen y bajan con el tiempo.
                • Irritabilidad o ansiedad leve.
                • Dificultad para dormir los primeros dias.
                • Tos temporal mientras los pulmones se limpian.
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
            texto = "¿Te interesa identificar senales de dependencia al tabaco?",
            mensaje =
                """
                Saber esto ayuda a decidir si necesitas apoyo extra.
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

        "senales" to DecisionNode(
            id = "senales",
            texto = "Senales frecuentes de dependencia al cigarro:",
            mensaje =
                """
                • Fumar el primer cigarro dentro de los 30 minutos de despertar.
                • Dificultad para no fumar en lugares prohibidos.
                • Fumar incluso cuando estas enfermo.
                • Necesitar cada vez mas cigarros para sentir el mismo efecto.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // FINAL
        // =========================
        "final" to DecisionNode(
            id = "final",
            texto = "Cuidar tu salud es una decision valiente. ¿Quieres ver tips iniciales para empezar?",
            mensaje =
                """
                Si quieres cerrar aqui, esta bien. Y si quieres apoyo, tambien.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si, dame tips",
                    siguienteNodoId = "tips_iniciales"
                ),
                DecisionOption(
                    texto = "No, terminar",
                    siguienteNodoId = "fin"
                )
            )
        ),

        "tips_iniciales" to DecisionNode(
            id = "tips_iniciales",
            texto = "Tips iniciales para dejar de fumar:",
            mensaje =
                """
                • Elige una fecha cercana para empezar y avisa a alguien de confianza.
                • Quita cajetillas, encendedores y ceniceros de tu entorno.
                • Cambia rutinas que te disparen el antojo (cafe, alcohol, estres).
                • Ten sustitutos a la mano: agua, chicle, palillos o caminar 5 minutos.
                • Si un antojo sube, espera 10 minutos y respira lento: baja mas rapido de lo que parece.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin" to DecisionNode(
            id = "fin",
            texto = "Cuidar tu salud es una decision valiente. Aqui estoy cuando lo necesites.",
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