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
        // PREVENCIÓN (Si el usuario dice que NO tiene antojo)
        // =========================
        "prevencion" to DecisionNode(
            id = "prevencion",
            texto = "Excelente. Mantenerte firme y consciente cuida tus pulmones y tu salud.",
            mensaje = "¿Quieres revisar estrategias de prevención para mantenerte sin vapear?",
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
            texto = "Estrategias recomendadas para evitar el vapeo:",
            mensaje =
                """
                • Mantén el vapeador guardado o deséchalo
                • Sustituye el gesto de vapear (masticar chicle, usar un popote)
                • Identifica qué lugares o amigos te dan ganas de vapear
                • Toma agua fría cuando sientas un impulso leve
                """.trimIndent(),
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "fin_prevencion" to DecisionNode(
            id = "fin_prevencion",
            texto = "Continúa reforzando tu autonomía y tus hábitos saludables.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // INTENSIDAD
        // =========================
        "intensidad" to DecisionNode(
            id = "intensidad",
            texto = "¿Qué tan fuerte es el deseo de usar el vapeador en este momento?",
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
            texto = "¿Puedes comprometerte a distraerte e intentar posponer el vapeo durante 15 minutos?",
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
            texto = "Prueba estas estrategias para romper el impulso automático:",
            mensaje =
                """
                • Toma un vaso grande de agua fría para refrescar tu garganta.
                • Busca un chicle o una menta fuerte para mantener tu boca ocupada.
                • Haz un ejercicio de respiración: inhala en 4 tiempos y exhala lento.
                • Cambia de habitación o muévete de lugar justo ahora.
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
            texto = "¡Excelente trabajo! Lograste surfear la ola del craving y mantuviste el control sin vapear.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // MODERADO
        // =========================
        "moderado" to DecisionNode(
            id = "moderado",
            texto = "¿Has usado el vapeador el día de hoy?",
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
            texto = "Dar una sola bocanada (*hit*) reactiva los niveles de nicotina en tu cerebro, haciendo que el deseo de seguir vapeando sea más intenso.",
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
            texto = "Romper el entorno que te incita a vapear es clave en este nivel moderado:",
            mensaje =
                """
                • Si estás en una reunión o con un grupo que está vapeando, muévete de lugar unos minutos.
                • Envía un mensaje de texto a alguien de confianza diciéndole cómo te sientes.
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
            texto = "¿Presentas fuerte dolor de cabeza, irritabilidad extrema, desesperación o una fijación mental muy alta por vapear?",
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
            texto = "Lograste estabilizar un episodio moderado de craving. Mantén tus manos ocupadas y recuerda por qué decidiste dejar el vape.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        // =========================
        // ALTO
        // =========================
        "alto" to DecisionNode(
            id = "alto",
            texto = "Tu nivel de craving por la nicotina es críticamente alto y tu cerebro está bajo una fuerte demanda química.",
            mensaje = "¿Sientes que estás a punto de perder el control y usar el vapeador?",
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
            texto = "¡Detén el impulso justo ahora! Recuerda que Solvyx cuenta con un sistema de Alertas SOS vía SMS.",
            mensaje =
                """
                • Considera presionar el botón de pánico en la pantalla principal para notificar a tu red de apoyo.
                • Deshazte del vapeador inmediatamente (mójalo o dáselo a alguien si es necesario).
                • Aléjate por completo del lugar o situación donde te encuentres.
                • No te quedes a solas con el pensamiento.
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
            texto = "¿Presentas sudoración, temblores leves, palpitaciones o una ansiedad que te cuesta mucho trabajo contener?",
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
            texto = "Estos síntomas físicos intensos son una respuesta clara del síndrome de abstinencia a la nicotina.",
            mensaje = "Te recomendamos utilizar el módulo de Canalización Profesional de Solvyx para contactar de forma anónima con un especialista del CESMAA o un centro de salud en Guerrero.",
            tipo = NodeType.FINAL,
            esFinal = true
        ),

        "alto_controlado" to DecisionNode(
            id = "alto_controlado",
            texto = "El craving severo funciona por picos; si resistes unos minutos más, la intensidad biológica del deseo comenzará a descender. Mantente firme.",
            tipo = NodeType.FINAL,
            esFinal = true
        )
    )
)