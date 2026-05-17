package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType

val cigarroCravingTree = DecisionTree(
    id = "cigarro_craving",
    nombre = "Craving Cigarro",
    nodoInicialId = "inicio",
    nodos = mapOf(

        // =========================
        // INICIO
        // =========================
        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿En este momento sientes ganas intensas de fumar un cigarro?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
                    siguienteNodoId = "intensidad"
                ),
                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "prevencion"
                )
            )
        ),

        // =========================
        // PREVENCIÓN
        // =========================
        "prevencion" to DecisionNode(
            id = "prevencion",
            texto = "¡Qué gran noticia! Mantenerte libre del humo de tabaco protege tu sistema cardiovascular.",
            mensaje = "¿Te gustaría revisar algunas pautas para prevenir una recaída?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
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
            texto = "Estrategias de prevención recomendadas:",
            mensaje =
                """
                • Deshazte de ceniceros, encendedores y cajetillas que tengas en casa o el auto.
                • Identifica tus disparadores (café, salir de clases, momentos de estrés).
                • Cambia tus rutas habituales si pasas cerca de donde solías comprar cigarros.
                • Mantén a la mano sustitutos conductuales (palillos de madera, popotes o dulces sanos).
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin_prevencion" to DecisionNode(
            id = "fin_prevencion",
            texto = "Continúa firme. Cada día sin fumar es un paso hacia una vida más saludable.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // INTENSIDAD
        // =========================
        "intensidad" to DecisionNode(
            id = "intensidad",
            texto = "¿Qué tan fuerte es la necesidad de fumar justo ahora?",
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
            texto = "¿Puedes comprometerte a retrasar el cigarro durante 15 minutos realizando otra actividad?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
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
            texto = "La ansiedad del tabaco baja si mantienes tus manos y boca ocupadas. Intenta esto:",
            mensaje =
                """
                • Sostén un bolígrafo o un objeto pequeño entre tus dedos para calmar la costumbre manual.
                • Toma un vaso de agua despacio, saboreando cada trago.
                • Mastica una goma de mascar o una pastilla de menta.
                • Realiza 5 respiraciones profundas inhalando por la nariz y exhalando lentamente por la boca.
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
            texto = "¿Sientes que las ganas de fumar disminuyeron tras la distracción?",
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
            texto = "¡Excelente! Lograste romper el eslabón automático del antojo. Estás ganando control.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // MODERADO
        // =========================
        "moderado" to DecisionNode(
            id = "moderado",
            texto = "¿Has fumado aunque sea una parte de un cigarro el día de hoy?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
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
            texto = "Fumar hoy mantiene activos los receptores de nicotina en tu cerebro, haciendo que el impulso se repita con más fuerza.",
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
            texto = "¿Te encuentras acompañado de personas de tu confianza en este momento?",
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
            texto = "Es momento de modificar tu entorno inmediato para protegerte:",
            mensaje =
                """
                • Si estás en el 'área de fumadores' o con amigos que fuman, aléjate inmediatamente.
                • Llama o escribe a alguien de tu red de apoyo y dile: 'Tengo antojo de fumar, ayúdame a platicar de otra cosa'.
                • Camina hacia un espacio cerrado o público libre de humo.
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
            texto = "¿Experimentas fuerte irritabilidad, tensión muscular, cambios de humor o sudoración en las manos?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
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
            texto = "Superaste un episodio moderado. Recuerda que la ansiedad pasará, pero los beneficios en tus pulmones se quedan.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // ALTO
        // =========================
        "alto" to DecisionNode(
            id = "alto",
            texto = "Tu nivel de urgencia por fumar es elevado y la dependencia conductual está presionando al máximo.",
            mensaje = "¿Sientes que estás a punto de ceder y encender un cigarro?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
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
            texto = "¡Detente! Estás en una zona de alto riesgo de recaída. Recuerda las herramientas de Solvyx:",
            mensaje =
                """
                • Tienes a tu disposición el sistema de Alertas SOS vía SMS en la app para pedir ayuda sin usar datos.
                • Desecha o destruye el cigarro que tengas en la mano justo ahora. No te lo guardes.
                • Lávate los dientes o la cara con agua muy fría para cambiar el estímulo sensorial.
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
            texto = "¿Presentas opresión leve en el pecho, dolores de cabeza punzantes o una desesperación difícil de contener?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí",
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
            texto = "La abstinencia física severa de la nicotina combustionada genera malestares reales en el cuerpo.",
            mensaje = "No estás solo en esto. Te sugerimos acudir al módulo de Canalización Profesional de Solvyx para recibir asesoría especializada y anónima de las instituciones de salud en Guerrero.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "alto_controlado" to DecisionNode(
            id = "alto_controlado",
            texto = "El pico del deseo severo dura solo unos minutos. Si logras sostenerte un poco más, la química cerebral volverá a equilibrarse. Mantente firme.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)