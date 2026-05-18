package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.model.NodeType

/**
 * Árbol de decisión especializado en ansiedad y craving por alcohol.
 * Basado en técnicas cognitivo-conductuales (TCC), HALT y Urge Surfing.
 * Rediseñado para ofrecer una experiencia humana, profunda y accionable.
 */
val alcoholCravingTree = DecisionTree(
    id = "alcohol_craving",
    nombre = "Manejo de Ansiedad por Alcohol",
    nodoInicialId = "inicio",
    nodos = mapOf(

        // =========================
        // 1. BIENVENIDA Y VALIDACIÓN
        // =========================
        "inicio" to DecisionNode(
            id = "inicio",
            texto = "Hola. Noto que algo te inquieta... ¿Sientes ganas de consumir alcohol en este momento?",
            tipo = NodeType.QUESTION,
            bertoState = "PREOCUPADO",
            delayMs = 1500L,
            opciones = listOf(
                DecisionOption(
                    texto = "Sí, son muy fuertes",
                    siguienteNodoId = "intensidad",
                    reaccion = "Gracias por tu honestidad. Admitirlo es el primer paso para mantener el control."
                ),
                DecisionOption(
                    texto = "No, solo quiero prevenir",
                    siguienteNodoId = "ruta_prevencion_inicio",
                    reaccion = "¡Excelente! Estar un paso adelante es la clave de la sobriedad."
                )
            )
        ),

        // =========================
        // 2. RUTA DE PREVENCIÓN (EXPANDIDA)
        // =========================
        "ruta_prevencion_inicio" to DecisionNode(
            id = "ruta_prevencion_inicio",
            texto = "Me alegra que estés en un buen momento. Mantener la guardia alta es lo que construye el éxito a largo plazo.",
            porQue = "La prevención no es solo 'no beber', es fortalecer tu entorno y tu mente para los momentos difíciles.",
            mensaje = """
                Por qué importa:
                • La prevención no es solo "no beber"; es fortalecer tu entorno y tu mente para los momentos difíciles.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            bertoState = "FELIZ",
            opciones = listOf(
                DecisionOption(
                    texto = "Identificar detonantes hoy",
                    siguienteNodoId = "prevencion_detonantes",
                    reaccion = "Muy sabio. Conocer al enemigo es la mitad de la victoria."
                ),
                DecisionOption(
                    texto = "Reforzar mi motivación",
                    siguienteNodoId = "prevencion_motivacion",
                    reaccion = "Recordar tu 'por qué' es como ponerle gasolina a tu voluntad."
                ),
                DecisionOption(
                    texto = "Solo pasaba a saludar",
                    siguienteNodoId = "fin_positivo",
                    reaccion = "¡Y yo encantado de verte! Sigue así."
                )
            )
        ),

        "prevencion_detonantes" to DecisionNode(
            id = "prevencion_detonantes",
            texto = "Hagamos un repaso rápido de tu día. ¿Hay algo que hoy pueda ponerte en riesgo?",
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            recomendaciones = listOf(
                "Lugares: ¿Pasarás por ese bar o tienda que sueles visitar?",
                "Personas: ¿Verás a alguien que te presione para beber?",
                "Emociones: ¿Te sientes estresado o particularmente solo hoy?"
            ),
            mensaje = """
                Revisa estos posibles detonantes:
                • Lugares: ¿Pasarás por ese bar o tienda que sueles visitar?
                • Personas: ¿Verás a alguien que te presione para beber?
                • Emociones: ¿Te sientes estresado o particularmente solo hoy?
                """.trimIndent(),
            opciones = listOf(
                DecisionOption(
                    texto = "Ya identifiqué mis riesgos",
                    siguienteNodoId = "prevencion_plan_accion",
                    reaccion = "Perfecto. Ahora vamos a neutralizarlos."
                )
            )
        ),

        "prevencion_plan_accion" to DecisionNode(
            id = "prevencion_plan_accion",
            texto = "Tener un plan te quita la ansiedad de improvisar.",
            recomendaciones = listOf(
                "Si pasas por un lugar de riesgo, cambia de ruta aunque tardes más.",
                "Si ves a una persona de riesgo, ten una frase lista: 'Hoy no bebo, gracias'.",
                "Si te sientes solo, escríbeme o llama a alguien de tu red."
            ),
            mensaje = """
                Plan rápido de acción:
                • Si pasas por un lugar de riesgo, cambia de ruta aunque tardes más.
                • Si ves a una persona de riesgo, ten una frase lista: "Hoy no bebo, gracias".
                • Si te sientes solo, escríbeme o llama a alguien de tu red.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            bertoState = "FELIZ",
            esFinal = true
        ),

        "prevencion_motivacion" to DecisionNode(
            id = "prevencion_motivacion",
            texto = "Cierra los ojos un momento y piensa: ¿Qué es lo mejor que te ha pasado desde que decidiste no beber?",
            porQue = "El cerebro olvida rápido el dolor del consumo, pero recordar los beneficios reales mantiene viva la meta.",
            recomendaciones = listOf(
                "Piensa en tu salud, tu dinero ahorrado o la tranquilidad de tu familia.",
                "Visualiza cómo te quieres sentir mañana al despertar: sin cruda y con orgullo.",
                "Escribe esa razón en una nota y tenla a la mano hoy."
            ),
            mensaje = """
                Por qué funciona:
                • El cerebro olvida rápido el dolor del consumo, pero recordar beneficios reales mantiene viva la meta.

                Sugerencias:
                • Piensa en tu salud, tu dinero ahorrado o la tranquilidad de tu familia.
                • Visualiza cómo te quieres sentir mañana al despertar: sin cruda y con orgullo.
                • Escribe esa razón en una nota y tenla a la mano hoy.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            bertoState = "FELIZ",
            esFinal = true
        ),

        // =========================
        // 3. DIAGNÓSTICO DE INTENSIDAD Y NATURALEZA
        // =========================
        "intensidad" to DecisionNode(
            id = "intensidad",
            texto = "¿Cómo se siente esa necesidad en este momento?",
            porQue = "El 'craving' o deseo intenso no es una orden, es solo una señal química que pasará si no la alimentas.",
            mensaje = """
                Nota breve:
                • El craving no es una orden; es una señal que pasa si no la alimentas.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            bertoState = "PREOCUPADO",
            opciones = listOf(
                DecisionOption(
                    texto = "Es algo físico (tensión, nudo)",
                    siguienteNodoId = "diagnostico_halt",
                    reaccion = "Entiendo. A veces el cuerpo nos envía señales confusas."
                ),
                DecisionOption(
                    texto = "Es un pensamiento obsesivo",
                    siguienteNodoId = "explicacion_15_minutos",
                    reaccion = "Los pensamientos son como nubes: pueden ser oscuros, pero siempre se mueven."
                ),
                DecisionOption(
                    texto = "Siento que voy a recaer AHORA",
                    siguienteNodoId = "emergencia_sos",
                    reaccion = "¡Detente! Respira conmigo. Vamos a activar el protocolo de seguridad."
                )
            )
        ),

        // =========================
        // 4. RUTA MENTAL: TÉCNICA 15 MIN (URGE SURFING)
        // =========================
        "explicacion_15_minutos" to DecisionNode(
            id = "explicacion_15_minutos",
            texto = "Lo que sientes se llama 'Ola de Deseo'. Científicamente, dura entre 15 y 20 minutos.",
            porQue = "Si intentas luchar contra la ola, te cansarás y te hundirás. Si la 'surfeas', pasará por debajo de ti sin mojarte.",
            mensaje = """
                Idea clave:
                • La ola suele bajar en 15 a 20 minutos si no la alimentas.
                • En lugar de pelear, obsérvala y déjala pasar.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            opciones = listOf(
                DecisionOption(
                    texto = "Ayúdame a surfearla (Ejercicio)",
                    siguienteNodoId = "ejercicio_anclaje_1",
                    reaccion = "¡Excelente elección! Vamos a ocupar tu cerebro en algo mejor."
                ),
                DecisionOption(
                    texto = "¿Qué otra cosa puedo hacer?",
                    siguienteNodoId = "distraccion_cognitiva",
                    reaccion = "Vamos a darle un reto a tu mente."
                )
            )
        ),

        "ejercicio_anclaje_1" to DecisionNode(
            id = "ejercicio_anclaje_1",
            texto = "Técnica de Anclaje (5-4-3-2-1). Empecemos: Mira a tu alrededor y dime (en voz alta o mentalmente) 5 cosas que puedas VER.",
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            opciones = listOf(
                DecisionOption(
                    texto = "Listo, las veo",
                    siguienteNodoId = "ejercicio_anclaje_2",
                    reaccion = "Bien. Eso te trae de vuelta al presente."
                )
            )
        ),

        "ejercicio_anclaje_2" to DecisionNode(
            id = "ejercicio_anclaje_2",
            texto = "Ahora, identifica 4 cosas que puedas TOCAR en este momento (tu ropa, una mesa, tus manos). Siente su textura.",
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            opciones = listOf(
                DecisionOption(
                    texto = "Listo, las toco",
                    siguienteNodoId = "ejercicio_anclaje_3",
                    reaccion = "Excelente. Siente la realidad bajo tus dedos."
                )
            )
        ),

        "ejercicio_anclaje_3" to DecisionNode(
            id = "ejercicio_anclaje_3",
            texto = "Ahora, presta atención y busca 3 sonidos que puedas ESCUCHAR (lejanos o cercanos).",
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            opciones = listOf(
                DecisionOption(
                    texto = "Los escucho",
                    siguienteNodoId = "ejercicio_anclaje_final",
                    reaccion = "Perfecto. Ya casi lo logramos."
                )
            )
        ),

        "ejercicio_anclaje_final" to DecisionNode(
            id = "ejercicio_anclaje_final",
            texto = "Finalmente, busca 2 cosas que puedas OLER y 1 que puedas SABOREAR (o imagina tu sabor favorito).",
            porQue = "Al forzar a tus 5 sentidos a trabajar, el área del cerebro encargada del deseo se 'apaga' para dar paso al procesamiento sensorial.",
            tipo = NodeType.FINAL,
            bertoState = "FELIZ",
            recomendaciones = listOf(
                "¿Notas cómo la intensidad de la ola bajó?",
                "Si aún queda un poco, repite el ejercicio o busca a alguien.",
                "¡Ganaste este round de 15 minutos!"
            ),
            mensaje = """
                Por qué ayuda:
                • Activar los 5 sentidos desplaza la atención del deseo hacia lo sensorial.

                Recuerda:
                • ¿Notas cómo la intensidad de la ola bajó?
                • Si aún queda un poco, repite el ejercicio o busca a alguien.
                • ¡Ganaste este round de 15 minutos!
                """.trimIndent(),
            esFinal = true
        ),

        "distraccion_cognitiva" to DecisionNode(
            id = "distraccion_cognitiva",
            texto = "Reto Mental: Cuenta hacia atrás desde 100 restando de 7 en 7 (100, 93, 86...).",
            porQue = "Hacer cálculos matemáticos activa la corteza prefrontal, la parte del cerebro que toma decisiones lógicas y controla los impulsos.",
            tipo = NodeType.FINAL,
            bertoState = "TRANQUILO",
            recomendaciones = listOf(
                "Hazlo hasta llegar a cero.",
                "Si te equivocas, vuelve a empezar. El punto es concentrarte.",
                "Al terminar, notarás que el impulso de beber ha perdido mucha fuerza."
            ),
            mensaje = """
                Por qué sirve:
                • Los cálculos activan la parte del cerebro que regula impulsos.

                Sugerencias:
                • Hazlo hasta llegar a cero.
                • Si te equivocas, vuelve a empezar. El punto es concentrarte.
                • Al terminar, el impulso suele bajar.
                """.trimIndent(),
            esFinal = true
        ),

        // =========================
        // 5. RUTA FÍSICA: MÉTODO HALT
        // =========================
        "diagnostico_halt" to DecisionNode(
            id = "diagnostico_halt",
            texto = "Hagamos un escaneo físico: ¿Tienes hambre, sed o estás muy cansado?",
            porQue = "El cerebro es un órgano biológico. Si tiene hambre o falta de sueño, su capacidad de decir 'no' disminuye drásticamente.",
            mensaje = """
                Nota breve:
                • Con hambre o falta de sueño, decir "no" se vuelve más difícil.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            opciones = listOf(
                DecisionOption(
                    texto = "Hambre o sed",
                    siguienteNodoId = "accion_halt_hambre",
                    reaccion = "Tu cerebro te pide combustible, no alcohol. Vamos por ello."
                ),
                DecisionOption(
                    texto = "Mucho cansancio o estrés",
                    siguienteNodoId = "accion_halt_descanso",
                    reaccion = "Estás operando en reserva. Necesitas recargar."
                )
            )
        ),

        "accion_halt_hambre" to DecisionNode(
            id = "accion_halt_hambre",
            texto = "Ve por un vaso grande de agua y algo ligero de comer (una fruta o nueces). Hazlo ahora.",
            porQue = "Un pico de insulina por comer algo sano puede calmar la ansiedad cerebral en minutos.",
            mensaje = """
                Sugerencia:
                • Comer algo ligero y beber agua puede calmar la ansiedad en pocos minutos.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            bertoState = "TRANQUILO",
            opciones = listOf(
                DecisionOption(
                    texto = "Ya lo hice / Lo estoy haciendo",
                    siguienteNodoId = "fin_positivo",
                    reaccion = "¡Buen trabajo! Espera 10 minutos y verás cómo el mundo se ve diferente."
                )
            )
        ),

        "accion_halt_descanso" to DecisionNode(
            id = "accion_halt_descanso",
            texto = "Tu cuerpo necesita una pausa real, no un anestésico.",
            recomendaciones = listOf(
                "Si puedes, toma una siesta de 20 minutos.",
                "Si estás en el trabajo, cierra los ojos y respira profundo por 2 minutos.",
                "Date un baño con agua tibia al llegar a casa.",
                "No tomes decisiones importantes (como beber) mientras estés agotado."
            ),
            mensaje = """
                Opciones de descanso:
                • Si puedes, toma una siesta de 20 minutos.
                • Si estás en el trabajo, cierra los ojos y respira profundo por 2 minutos.
                • Date un baño con agua tibia al llegar a casa.
                • Evita decisiones importantes mientras estés agotado.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            bertoState = "TRANQUILO",
            esFinal = true
        ),

        // =========================
        // 6. RUTA DE CRISIS / SOS
        // =========================
        "emergencia_sos" to DecisionNode(
            id = "emergencia_sos",
            texto = "¡ESTO ES UNA EMERGENCIA! No te rindas ahora, el deseo va a pasar, te lo prometo.",
            porQue = "En momentos de crisis, la visión se vuelve 'túnel'. Solo ves el alcohol. Necesitamos romper ese túnel YA.",
            mensaje = """
                En crisis, la visión se vuelve "túnel". Vamos a romperla YA.
                """.trimIndent(),
            tipo = NodeType.QUESTION,
            bertoState = "PREOCUPADO",
            opciones = listOf(
                DecisionOption(
                    texto = "Activar mi Red de Apoyo",
                    siguienteNodoId = "protocolo_sos",
                    reaccion = "Excelente. No estás solo en esta batalla."
                ),
                DecisionOption(
                    texto = "Dime qué hacer YA",
                    siguienteNodoId = "protocolo_mitigacion",
                    reaccion = "Escúchame con atención. Sigue mis pasos."
                )
            )
        ),

        "protocolo_sos" to DecisionNode(
            id = "protocolo_sos",
            texto = "Usa el botón SOS de Solvyx o llama directamente a tu contacto de confianza.",
            recomendaciones = listOf(
                "Diles: 'Tengo muchas ganas de beber, necesito hablar 5 minutos'.",
                "No cuelgues hasta que la intensidad baje.",
                "Si no contestan, llama a la línea de vida (SAPTEL: 5552598121)."
            ),
            mensaje = """
                Pasos rápidos:
                • Diles: "Tengo muchas ganas de beber, necesito hablar 5 minutos".
                • No cuelgues hasta que la intensidad baje.
                • Si no contestan, usa una línea de apoyo local (ej. SAPTEL: 5552598121, México).
                """.trimIndent(),
            tipo = NodeType.FINAL,
            bertoState = "PREOCUPADO",
            esFinal = true
        ),

        "protocolo_mitigacion" to DecisionNode(
            id = "protocolo_mitigacion",
            texto = "Pasos de emergencia para salvar tu sobriedad:",
            recomendaciones = listOf(
                "SAL de donde estés si hay alcohol cerca. Camina a una plaza o lugar público.",
                "Tira el alcohol si lo tienes en la mano. No lo pienses, solo hazlo.",
                "Mójate la cara con agua muy fría. El choque térmico ayuda a reiniciar el cerebro.",
                "Llama a alguien. La soledad es la mejor amiga de la recaída."
            ),
            mensaje = """
                Haz esto ahora:
                • Sal de donde estés si hay alcohol cerca. Camina a un lugar público.
                • Tira el alcohol si lo tienes en la mano. No lo pienses.
                • Mójate la cara con agua muy fría para cambiar el foco sensorial.
                • Llama a alguien de tu red. No te quedes solo.
                """.trimIndent(),
            tipo = NodeType.FINAL,
            bertoState = "PREOCUPADO",
            esFinal = true
        ),

        // =========================
        // NODOS FINALIZADORES
        // =========================
        "fin_positivo" to DecisionNode(
            id = "fin_positivo",
            texto = "¡Lo lograste! Cada vez que eliges tu salud sobre el alcohol, tu cerebro se vuelve más fuerte.",
            tipo = NodeType.FINAL,
            bertoState = "FELIZ",
            recomendaciones = listOf(
                "Hoy has ganado una batalla importante.",
                "Sigue cuidándote. Estás haciendo un gran trabajo.",
                "Mañana te sentirás increíblemente orgulloso de esta decisión."
            ),
            mensaje = """
                Para cerrar este logro:
                • Hoy has ganado una batalla importante.
                • Sigue cuidándote. Estás haciendo un gran trabajo.
                • Mañana te sentirás orgulloso de esta decisión.
                """.trimIndent(),
            esFinal = true
        ),

        "fin_despedida" to DecisionNode(
            id = "fin_despedida",
            texto = "Aquí estaré siempre que me necesites. ¡Que tengas un excelente día!",
            tipo = NodeType.FINAL,
            bertoState = "FELIZ",
            esFinal = true
        )
    )
)
