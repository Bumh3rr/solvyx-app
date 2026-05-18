package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType

val vapeCravingTree = DecisionTree(
    id = "vape_craving",
    nombre = "Craving Vapeo",
    nodoInicialId = "inicio",
    nodos = mapOf(

        // =========================
        // INICIO
        // =========================
        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿En este momento sientes ganas intensas de usar el vapeador?",
            mensaje =
                """
                Estoy contigo. Vamos paso a paso y sin juzgarte.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "intensidad"
                ),
                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "prevencion"
                )
            )
        ),

        // =========================
        // PREVENCIÓN (Si el usuario dice que NO tiene antojo)
        // =========================
        "prevencion" to DecisionNode(
            id = "prevencion",
            texto = "Que bien. Mantenerte firme protege tus pulmones y tu energia.",
            mensaje = "¿Te late revisar estrategias para mantenerte sin vapear?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "Estrategias recomendadas para evitar el vapeo:",
            mensaje =
                """
                • Guarda o desecha el vapeador si puedes.
                • Sustituye el gesto (chicle, popote, menta).
                • Identifica lugares o personas que te disparan el impulso.
                • Toma agua fria cuando sientas un antojo leve.
                • Descansa bien: el cansancio hace mas dificil resistir.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin_prevencion" to DecisionNode(
            id = "fin_prevencion",
            texto = "Sigue asi. Cada dia sin vapear es una decision que te fortalece.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // INTENSIDAD
        // =========================
        "intensidad" to DecisionNode(
            id = "intensidad",
            texto = "¿Qué tan fuerte es el deseo de usar el vapeador en este momento?",
            mensaje =
                """
                Los picos suelen bajar en minutos si no los alimentas.
                """.trimIndent(),
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
            texto = "¿Te animas a posponer el vapeo 15 minutos y distraerte?",
            mensaje =
                """
                Retrasar el impulso debilita el habito automatico.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "Prueba estas estrategias para romper el impulso automatico:",
            mensaje =
                """
                • Toma un vaso grande de agua fria.
                • Mastica chicle o una menta fuerte.
                • Respira lento: inhala 4, exhala 6, repite 6 veces.
                • Cambia de lugar por unos minutos.
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
            texto = "¿Sientes que las ansias o el deseo por vapear disminuyeron?",
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
            texto = "¡Bien hecho! Lograste surfear la ola del craving y mantener el control.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // MODERADO
        // =========================
        "moderado" to DecisionNode(
            id = "moderado",
            texto = "¿Has usado el vapeador el dia de hoy?",
            mensaje =
                """
                Una sola bocanada puede reactivar el impulso. No es un fracaso, es informacion.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "El uso reciente suele intensificar el deseo por nicotina.",
            mensaje =
                """
                Vamos a protegerte del siguiente impulso.
                """.trimIndent(),
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
            texto = "¿Estás acompañado por alguien de confianza en este momento?",
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
            texto = "Vamos a romper el entorno que te incita a vapear:",
            mensaje =
                """
                • Si estas con gente que vapea, muévete de lugar unos minutos.
                • Manda un mensaje a alguien de confianza diciendo como te sientes.
                • Recuerda que no necesitas traer el dispositivo contigo.
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
            texto = "¿Tienes dolor de cabeza, irritabilidad intensa o una fijacion muy alta por vapear?",
            mensaje =
                """
                Son sintomas comunes de abstinencia de nicotina y suelen bajar con el tiempo.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "Lograste estabilizar este episodio. Mantén tus manos ocupadas y recuerda tu por que.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // ALTO
        // =========================
        "alto" to DecisionNode(
            id = "alto",
            texto = "El impulso es muy fuerte, pero no es permanente.",
            mensaje = "¿Sientes que estas a punto de perder el control y usar el vapeador?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "Detente un momento. Vamos con pasos concretos:",
            mensaje =
                """
                • Considera usar el boton SOS si tu app lo permite.
                • Aleja o entrega el vapeador a alguien de confianza.
                • Sal del lugar y respira lento por 2 minutos.
                • No te quedes solo con el pensamiento.
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
            texto = "¿Presentas sudoracion, temblores leves, palpitaciones o ansiedad dificil de contener?",
            mensaje =
                """
                Si los sintomas son intensos, pedir ayuda es valido.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "Buscar apoyo profesional puede hacer el proceso mas llevadero.",
            mensaje =
                """
                En Mexico, la Linea de la Vida atiende 24/7 al 800 911 2000.
                Si hay una urgencia, llama al 911.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "alto_controlado" to DecisionNode(
            id = "alto_controlado",
            texto = "El pico del deseo baja en minutos. Si te sostienes un poco mas, el impulso pierde fuerza.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)