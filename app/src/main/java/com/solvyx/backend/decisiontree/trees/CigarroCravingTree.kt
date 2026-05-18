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
            mensaje =
                """
                Estoy contigo. Vamos paso a paso, sin juzgarte.
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
        // PREVENCIÓN
        // =========================
        "prevencion" to DecisionNode(
            id = "prevencion",
            texto = "¡Que buena noticia! Mantenerte libre del humo protege tu salud y tu energia.",
            mensaje = "¿Te late revisar pautas simples para prevenir una recaida?",
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
            texto = "Estrategias de prevencion recomendadas:",
            mensaje =
                """
                • Retira ceniceros, encendedores y cajetillas de tu casa o auto.
                • Identifica tus detonantes (cafe, salir de clases, estres, alcohol).
                • Cambia rutas si pasas por donde solias comprar cigarros.
                • Ten sustitutos conductuales: palillos, popotes, chicle o agua.
                • Duerme bien y come a tiempo: el cansancio aumenta el impulso.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin_prevencion" to DecisionNode(
            id = "fin_prevencion",
            texto = "Sigue firme. Cada dia sin fumar es una decision que te fortalece.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // INTENSIDAD
        // =========================
        "intensidad" to DecisionNode(
            id = "intensidad",
            texto = "¿Que tan fuerte es la necesidad de fumar justo ahora?",
            mensaje =
                """
                Los picos de ansias suelen durar pocos minutos y luego bajan.
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
            texto = "¿Te animas a retrasar el cigarro 15 minutos haciendo otra cosa?",
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
            texto = "La ansiedad baja si mantienes manos y boca ocupadas. Prueba esto:",
            mensaje =
                """
                • Sostene un boligrafo o un objeto pequeno para calmar la costumbre manual.
                • Toma agua despacio, saboreando cada trago.
                • Mastica chicle o una pastilla de menta.
                • Haz 5 respiraciones lentas: inhala por la nariz, exhala por la boca.
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
            texto = "¿Sientes que las ganas de fumar disminuyeron tras la distraccion?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "¡Excelente! Rompiste el impulso automatico. Ese es un gran avance.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // MODERADO
        // =========================
        "moderado" to DecisionNode(
            id = "moderado",
            texto = "¿Has fumado aunque sea una parte de un cigarro hoy?",
            mensaje =
                """
                Si fumas, el cerebro vuelve a pedir nicotina con mas fuerza. Reconocerlo ayuda a cortar el ciclo.
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
            texto = "Fumar hoy reactiva el habito y puede hacer que el impulso se repita.",
            mensaje =
                """
                No es un fracaso. Es informacion para decidir tu siguiente paso.
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
            texto = "¿Estas acompanado por alguien de tu confianza en este momento?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Si",
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
            texto = "Vamos a cuidar tu entorno inmediato:",
            mensaje =
                """
                • Alejate de areas de fumadores o de personas que esten fumando.
                • Manda un mensaje a alguien de confianza: "Traigo antojo de fumar, ayudame a distraerme".
                • Muevete a un espacio libre de humo.
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
            texto = "¿Notas irritabilidad, tension muscular, ansiedad o sudoracion?",
            mensaje =
                """
                Son sintomas comunes de abstinencia de nicotina y suelen mejorar con el tiempo.
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
            texto = "Superaste un episodio moderado. El malestar pasa y tus pulmones agradecen cada dia sin humo.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // ALTO
        // =========================
        "alto" to DecisionNode(
            id = "alto",
            texto = "El impulso es fuerte, pero no es permanente.",
            mensaje = "¿Sientes que estas a punto de ceder y encender un cigarro?",
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
            texto = "Detente un momento. Vamos con pasos concretos para salir de este pico:",
            mensaje =
                """
                • Desecha el cigarro o cualquier cajetilla que tengas a la mano.
                • Lavar la cara con agua fria o cepillarte los dientes cambia el estimulo sensorial.
                • Respira lento durante 2 minutos. El cuerpo se calma antes que la mente.
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
            texto = "¿Presentas dolor de cabeza, opresion en el pecho o una ansiedad dificil de contener?",
            mensaje =
                """
                Si aparecen sintomas intensos o te sientes en riesgo, pedir ayuda es valido.
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
            texto = "Pedir apoyo profesional puede hacer el proceso mas llevadero.",
            mensaje =
                """
                Puedes acudir a un medico, psicologo o a un programa para dejar de fumar.
                En Mexico, la Linea de la Vida atiende 24/7 al 800 911 2000.
                Si sientes riesgo inmediato, llama al 911.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "alto_controlado" to DecisionNode(
            id = "alto_controlado",
            texto = "El pico del deseo suele bajar en minutos. Si te sostienes un poco mas, el impulso pierde fuerza.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)