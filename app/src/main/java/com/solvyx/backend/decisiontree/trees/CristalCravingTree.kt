package com.solvyx.backend.decisiontree.trees

import com.solvyx.backend.decisiontree.model.*

val cristalCravingTree = DecisionTree(

    id = "cristal_craving",

    nombre = "Craving Cristal",

    nodoInicialId = "inicio",

    nodos = mapOf(

        "inicio" to DecisionNode(

            id = "inicio",

            texto =
                "¿Tienes ganas intensas de consumir cristal en este momento?",

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
                    siguienteNodoId = "entorno"
                )
            )
        ),

        "entorno" to DecisionNode(

            id = "entorno",

            texto =
                "¿Estas en un lugar o con personas relacionadas al consumo?",

            mensaje =
                """
                El entorno puede disparar el antojo sin que te des cuenta.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "evitar_estimulos"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "habitos_saludables"
                )
            )
        ),

        "evitar_estimulos" to DecisionNode(

            id = "evitar_estimulos",

            texto =
                "Cambiar de entorno puede bajar el craving.",

            mensaje =
                """
                • Sal a un lugar publico o seguro.
                • Busca compania de confianza.
                • Alejate de personas o lugares que te disparen el impulso.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "habitos_saludables" to DecisionNode(

            id = "habitos_saludables",

            texto =
                "Vas bien. Sigue reforzando habitos saludables y espacios seguros.",

            mensaje =
                """
                Comer, dormir y mantener rutinas ayuda a que el cuerpo se regule.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "intensidad" to DecisionNode(

            id = "intensidad",

            texto =
                "¿Que tan fuerte es el deseo ahora mismo?",

            mensaje =
                """
                Los picos de ansias suelen bajar si no los alimentas.
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
                ),

                DecisionOption(
                    texto = "Quiero entender el craving",
                    siguienteNodoId = "psicoeducacion"
                )
            )
        ),

        "psicoeducacion" to DecisionNode(

            id = "psicoeducacion",

            texto =
                "¿Que es el craving y por que sube y baja?",

            mensaje =
                """
                El craving es un impulso intenso que suele subir como ola y luego bajar. No es una orden; es una señal del cerebro que se puede atravesar con tiempo y apoyo.
                Si no lo alimentas (por ejemplo, alejandote del estimulo), se debilita mas rapido.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Volver a evaluar intensidad",
                    siguienteNodoId = "intensidad"
                ),

                DecisionOption(
                    texto = "Quiero una tecnica rapida",
                    siguienteNodoId = "grounding_5_4_3_2_1"
                )
            )
        ),

        "grounding_5_4_3_2_1" to DecisionNode(

            id = "grounding_5_4_3_2_1",

            texto =
                "Tecnica 5-4-3-2-1 para bajar el pico:",

            mensaje =
                """
                • 5 cosas que puedas ver
                • 4 cosas que puedas tocar
                • 3 sonidos que puedas oir
                • 2 cosas que puedas oler
                • 1 sabor (o imagina tu sabor favorito)
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Listo",
                    siguienteNodoId = "deseo_disminuyo"
                )
            )
        ),

        "deseo_disminuyo" to DecisionNode(

            id = "deseo_disminuyo",

            texto =
                "¿Sientes que el deseo disminuyo?",

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

            texto =
                "Bien hecho. Lograste manejar el craving por ahora.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "moderado" to DecisionNode(

            id = "moderado",

            texto =
                "¿Sientes ansiedad, tension o agitacion?",

            mensaje =
                """
                Son sintomas comunes cuando el cuerpo pide la sustancia.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "respiracion"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "solo"
                )
            )
        ),

        "respiracion" to DecisionNode(

            id = "respiracion",

            texto =
                "Vamos a bajar la intensidad un poco:",

            mensaje =
                """
                • Inhala 4 segundos y exhala 6 segundos, repite 8 veces.
                • Busca un lugar tranquilo por un momento.
                • Si puedes, manda un mensaje a alguien de confianza.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "solo"
                )
            )
        ),

        "solo" to DecisionNode(

            id = "solo",

            texto =
                "¿Estas solo ahora mismo?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "riesgo_solo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "control_impulso"
                )
            )
        ),

        "riesgo_solo" to DecisionNode(

            id = "riesgo_solo",

            texto =
                "Cuando estas solo, el riesgo puede subir.",

            mensaje =
                """
                Si puedes, busca compania segura o sal a un lugar publico.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "control_impulso"
                )
            )
        ),

        "control_impulso" to DecisionNode(

            id = "control_impulso",

            texto =
                "¿Sientes que puedes controlar el impulso por ahora?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "moderado_controlado"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "alto"
                )
            )
        ),

        "moderado_controlado" to DecisionNode(

            id = "moderado_controlado",

            texto =
                "Bien. Sigue usando tus estrategias. Esto tambien pasa.",

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "alto" to DecisionNode(

            id = "alto",

            texto =
                "Cuando el craving es muy intenso, es facil perder control.",

            mensaje =
                "¿Estas teniendo sintomas fisicos importantes?",

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "apoyo_inmediato"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "plan_seguridad"
                )
            )
        ),

        "plan_seguridad" to DecisionNode(

            id = "plan_seguridad",

            texto =
                "Hagamos un plan rapido de seguridad:",

            mensaje =
                """
                • Alejate del lugar donde estas si hay riesgo.
                • Elige a alguien de confianza y escribelo ahora.
                • Ten a la vista agua y un lugar tranquilo para sentarte.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Listo, siguiente paso",
                    siguienteNodoId = "contactar_apoyo"
                ),

                DecisionOption(
                    texto = "Necesito mas ayuda",
                    siguienteNodoId = "crisis_pasos"
                )
            )
        ),

        "crisis_pasos" to DecisionNode(

            id = "crisis_pasos",

            texto =
                "Pasos de crisis para aguantar el pico:",

            mensaje =
                """
                • Pon un timer de 10 minutos y repite: "Esto va a bajar".
                • Mueve el cuerpo: camina, estira o lava tu cara con agua fria.
                • Si puedes, cambia de espacio o sal a un lugar publico.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "contactar_apoyo"
                )
            )
        ),

        "estrategias" to DecisionNode(

            id = "estrategias",

            texto =
                "Prueba estas estrategias simples:",

            mensaje =
                """
                • Camina 5 a 10 minutos.
                • Escucha musica que te calme.
                • Haz respiraciones lentas por 2 minutos.
                • Toma agua o algo caliente sin cafeina.
                • Alejate del estimulo si esta cerca.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "deseo_disminuyo"
                ),

                DecisionOption(
                    texto = "Quiero otra tecnica",
                    siguienteNodoId = "respiracion_box"
                )
            )
        ),

        "respiracion_box" to DecisionNode(

            id = "respiracion_box",

            texto =
                "Respiracion en caja (box breathing):",

            mensaje =
                """
                • Inhala 4 segundos
                • Sostén 4 segundos
                • Exhala 4 segundos
                • Sostén 4 segundos
                Repite 4 veces. Ayuda a bajar la activacion del cuerpo.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Continuar",
                    siguienteNodoId = "deseo_disminuyo"
                )
            )
        ),

        "apoyo_inmediato" to DecisionNode(

            id = "apoyo_inmediato",

            texto =
                "Si hay sintomas fuertes, busca ayuda inmediata.",

            mensaje =
                """
                • Taquicardia intensa o dolor en el pecho
                • Agitacion que no baja
                • Confusion o desorientacion
                • Ansiedad extrema

                En Mexico, puedes llamar a la Linea de la Vida 800 911 2000.
                Si hay riesgo inmediato, llama al 911.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "contactar_apoyo" to DecisionNode(

            id = "contactar_apoyo",

            texto =
                "¿Puedes contactar a alguien de confianza ahora mismo?",

            mensaje =
                """
                Decir "necesito apoyo" en voz alta puede bajar la intensidad.
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "llamar_apoyo"
                ),

                DecisionOption(
                    texto = "No",
                    siguienteNodoId = "emergencias"
                )
            )
        ),

        "llamar_apoyo" to DecisionNode(

            id = "llamar_apoyo",

            texto =
                "Contacta a una persona de confianza y quedate en linea unos minutos.",

            mensaje =
                """
                Puedes decir: "Traigo muchas ganas de consumir, ayudame a distraerme".
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "emergencias" to DecisionNode(

            id = "emergencias",

            texto =
                "No tienes que manejar esto solo. Busca apoyo profesional.",

            mensaje =
                """
                En Mexico, la Linea de la Vida atiende 24/7 al 800 911 2000.
                Si hay una urgencia, llama al 911.

                ¿Quieres un mensaje breve de reduccion de riesgos (sin promover consumo)?
                """.trimIndent(),

            tipo = NodeType.QUESTION,

            opciones = listOf(

                DecisionOption(
                    texto = "Si",
                    siguienteNodoId = "reduccion_riesgos"
                ),

                DecisionOption(
                    texto = "No, terminar",
                    siguienteNodoId = "fin"
                )
            )
        ),

        "reduccion_riesgos" to DecisionNode(

            id = "reduccion_riesgos",

            texto =
                "Mensaje breve de reduccion de riesgos:",

            mensaje =
                """
                Lo mas seguro es no consumir. Si aun asi sientes que vas a hacerlo, prioriza tu seguridad: no uses solo, evita mezclar con alcohol u otras sustancias, y busca un lugar seguro con alguien de confianza.
                Si te sientes mal o en riesgo, pide ayuda de inmediato.
                """.trimIndent(),

            tipo = NodeType.FINAL,

            esFinal = true
        ),

        "fin" to DecisionNode(

            id = "fin",

            texto =
                "Aqui estoy para apoyarte. No estas solo.",

            tipo = NodeType.FINAL,

            esFinal = true
        )
    )
)