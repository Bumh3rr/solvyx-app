package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType

// Auditado por psicologo-solvyx 2026-07-12 v2 (conversacional + RD)

val vapeCravingTree = DecisionTree(
    id = "vape_craving",
    nombre = "Craving Vapeo",
    nodoInicialId = "inicio",
    nodos = mapOf(

        "inicio" to DecisionNode(
            id = "inicio",
            texto = "¿Sientes ganas intensas de usar el vapeador ahorita?",
            mensaje = "Mmm, te leo. Aquí estoy contigo, sin presionar y sin culparte.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, son fuertes",
                    siguienteNodoId = "intensidad"
                ),
                DecisionOption(
                    texto = "No, solo pasaba a hablar",
                    siguienteNodoId = "prevencion"
                )
            )
        ),

        "prevencion" to DecisionNode(
            id = "prevencion",
            texto = "Va bien mantenerte sin vapear. Eso protege tus pulmones y tu energía.",
            mensaje = "¿Quieres algunos tips para seguir firme?",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, dame tips",
                    siguienteNodoId = "estrategias"
                ),
                DecisionOption(
                    texto = "No, gracias",
                    siguienteNodoId = "fin_prevencion"
                )
            )
        ),

        "estrategias" to DecisionNode(
            id = "estrategias",
            texto = "Tips para mantenerte sin vapear:",
            mensaje = "Identifica tus detonantes (estrés,社交, café, alcohol). Deja el vapeador fuera de tu alcance. Mantén alternativas a mano: chicle sin nicotina, agua, frutos secos. Cambia la rutina de las pausas: sal a caminar 5 minutos en lugar de vapear.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin_prevencion" to DecisionNode(
            id = "fin_prevencion",
            texto = "Cada día sin vapear es una decisión que te cuida.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "intensidad" to DecisionNode(
            id = "intensidad",
            texto = "¿Qué tan fuerte es la necesidad de vapear ahorita?",
            mensaje = "Los picos suelen durar poco si no los alimentas.",
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

        "leve" to DecisionNode(
            id = "leve",
            texto = "¿Te animas a esperar 10 minutos haciendo otra cosa antes de vapear?",
            mensaje = "Retrasar el impulso le baja el volumen automático.",
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
            texto = "Manos y boca ocupadas le bajan al impulso.",
            mensaje = "Sostén un bolígrafo o una moneda. Toma agua despacio. Mastica chicle o una menta. Haz 5 respiraciones lentas: inhala por nariz, exhala por boca.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "deseo_disminuyo"
                )
            )
        ),

        "deseo_disminuyo" to DecisionNode(
            id = "deseo_disminuyo",
            texto = "¿Sientes que las ganas bajaron después de la distracción?",
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
            texto = "Bien. Rompiste el reflejo automático. Eso cuenta mucho.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "moderado" to DecisionNode(
            id = "moderado",
            texto = "¿Has vapeado aunque sea una calada hoy?",
            mensaje = "Si vapeaste, el cerebro vuelve a pedir nicotina con más fuerza. Reconocerlo te ayuda a cortar el ciclo, no es un fracaso.",
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
            texto = "Vapear hoy puede reactivar el impulso, pero no te define.",
            mensaje = "Es información útil para tu siguiente paso, no un juicio.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "acompanado"
                )
            )
        ),

        "acompanado" to DecisionNode(
            id = "acompanado",
            texto = "¿Estás con alguien de tu confianza ahorita?",
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
            texto = "Vamos a cuidar tu entorno inmediato:",
            mensaje = "Aléjate de lugares donde se vapea. Manda un mensaje a alguien de confianza: traigo antojo de vapear, ayúdame a distraerme. Cámbiate a un espacio sin aerosol.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "sintomas"
                )
            )
        ),

        "sintomas" to DecisionNode(
            id = "sintomas",
            texto = "¿Sientes irritabilidad, tensión, ansiedad o antojo fuerte?",
            mensaje = "Son señales comunes de abstinencia de nicotina y suelen mejorar con el tiempo.",
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
            texto = "El malestar pasa y tus pulmones agradecen cada día sin aerosol.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "alto" to DecisionNode(
            id = "alto",
            texto = "El impulso es fuerte, pero no es permanente.",
            mensaje = "¿Sientes que ya vas a calar el vapeador?",
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
            texto = "Detente un momento. Vamos con pasos concretos:",
            mensaje = "Apaga el vapeador y aléjalo de tu alcance. Toma agua o lávate la cara con agua fría. Sal a un espacio abierto. Llama a alguien de tu confianza. Respira lento 2 minutos.",
            tipo = NodeType.QUESTION,
            opciones = listOf(
                DecisionOption(
                    texto = "Seguir",
                    siguienteNodoId = "sintomas_abstinencia"
                )
            )
        ),

        "sintomas_abstinencia" to DecisionNode(
            id = "sintomas_abstinencia",
            texto = "¿Tienes dolor de cabeza, opresión en el pecho o ansiedad difícil de contener?",
            mensaje = "Si se vuelven intensos o sientes que no puedes solo, pedir ayuda es válido.",
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
            texto = "Acompañamiento profesional puede hacer el proceso más llevadero.",
            mensaje = "Puedes ir con un médico o psicólogo. En México, Línea de la Vida 800 911 2000 (24/7). Si hay riesgo inmediato, 911.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "alto_controlado" to DecisionNode(
            id = "alto_controlado",
            texto = "El pico del deseo suele bajar en minutos. Si te sostienes un poco más, pierde fuerza.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)
