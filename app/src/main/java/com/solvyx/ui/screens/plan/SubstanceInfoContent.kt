package com.solvyx.ui.screens.plan

// "Conoce tu sustancia": content approved on 2026-09-29. Harm reduction, es-MX, "tú",
// gender-neutral, without judgment or absolutes. Changing a text here changes the screen.

/** A risky mix: [with] is the bold lead ("Con alcohol"), [why] the explanation. */
data class RiskyMix(val with: String, val why: String)

/**
 * Everything the screen shows for one substance, in its five sections. [id] matches the profile
 * and the chat trees; [careTitle] names the use ("Si decides beber…"); [emergency] is what should
 * make someone call 911; [askBerto] is the substance with its article, for "Pregúntale a Berto".
 */
data class SubstanceInfo(
    val id: String,
    val label: String,
    val askBerto: String,
    val essentials: String,
    val bodyAndMind: List<String>,
    val careTitle: String,
    val careTips: List<String>,
    val riskyMixes: List<RiskyMix>,
    val emergency: String,
    val helpTips: List<String>
)

const val SubstanceInfoIntro = "Información clara y sin juicios, para que te cuides."
const val SubstanceInfoDisclaimer = "Esta información no sustituye la atención médica."

val SubstanceInfos: List<SubstanceInfo> = listOf(
    SubstanceInfo(
        id = "alcohol",
        label = "Alcohol",
        askBerto = "el alcohol",
        essentials = "El alcohol hace más lento tu sistema nervioso. Al principio puede relajarte y " +
            "desinhibirte, pero entre más bebes, más lentos se vuelven tus reflejos, tu juicio y tu " +
            "respiración. Tu cuerpo procesa más o menos una bebida estándar por hora (una cerveza de " +
            "355 ml, una copa de vino o un caballito); ni el café ni un baño frío lo aceleran.",
        bodyAndMind = listOf(
            "En el momento: menos coordinación, decisiones más arriesgadas, cambios de ánimo, náusea o vómito.",
            "Al día siguiente: cruda (dolor de cabeza, deshidratación), ánimo bajo o ansiedad.",
            "Si se vuelve frecuente: afecta tu hígado, tu estómago, tu sueño y tu memoria, y tu cuerpo " +
                "se acostumbra y necesita más para sentir lo mismo.",
            "Tu cerebro sigue desarrollándose hasta cerca de los 25 años, por eso el alcohol te afecta más."
        ),
        careTitle = "Si decides beber, cuídate así",
        careTips = listOf(
            "Come algo antes y toma agua entre cada bebida.",
            "Si sientes ganas, espera 15 minutos antes de decidir.",
            "Habla con alguien de confianza antes, sobre todo si lo estás dudando.",
            "Ponte un límite antes de empezar y lleva la cuenta de tus bebidas.",
            "No manejes ni te subas con alguien que bebió; pide un taxi o que pasen por ti.",
            "Cuida tu vaso y no aceptes bebidas ya abiertas.",
            "Si bebes mucho y seguido, dejarlo de golpe puede sentirse muy mal físicamente; busca " +
                "apoyo médico para bajarle poco a poco."
        ),
        riskyMixes = listOf(
            RiskyMix(
                "Con medicamentos para dormir, para la ansiedad o para el dolor fuerte",
                "ambos frenan la respiración. Es la mezcla más peligrosa."
            ),
            RiskyMix(
                "Con cristal o bebidas energéticas",
                "no sientes cuánto te está afectando el alcohol y terminas bebiendo más de lo que tu cuerpo aguanta."
            ),
            RiskyMix(
                "Con paracetamol para la cruda después de beber mucho",
                "juntos pueden dañar el hígado."
            )
        ),
        emergency = "Llama al 911 si alguien no despierta o no responde, respira muy lento o con pausas, " +
            "tiene labios o piel azulados, vomita estando inconsciente o convulsiona.",
        helpTips = listOf(
            "Mientras llega la ayuda: acuesta a la persona de lado, no la dejes sin compañía y no le des " +
                "café ni la metas a bañar.",
            "Busca apoyo si necesitas beber para sentirte bien, si no logras parar cuando quieres o si " +
                "al no beber sientes temblores, sudor o mucha ansiedad."
        )
    ),
    SubstanceInfo(
        id = "cristal",
        label = "Cristal",
        askBerto = "el cristal",
        essentials = "El cristal (metanfetamina) es un estimulante muy potente: acelera tu cerebro y tu " +
            "corazón. Da energía y euforia y quita el sueño y el hambre; el efecto dura muchas horas y " +
            "la bajada puede ser muy dura. Su pureza cambia mucho, así que es difícil saber cuánto " +
            "estás consumiendo.",
        bodyAndMind = listOf(
            "En el momento: corazón acelerado, presión y temperatura altas, mandíbula apretada, ansiedad o agitación.",
            "En la bajada: cansancio extremo, tristeza, irritabilidad y muchas ganas de volver a consumir.",
            "Con uso frecuente: problemas para dormir, pérdida de peso, daño en dientes y piel, y " +
                "paranoia o ver y escuchar cosas que no están.",
            "Por un tiempo puede costarte más disfrutar otras cosas; con apoyo, tu cerebro se recupera."
        ),
        careTitle = "Si decides consumir, cuídate así",
        careTips = listOf(
            "Come algo antes y toma agua a sorbos, sin exagerar.",
            "Si sientes ganas, espera 15 minutos antes de decidir, y no repitas pronto: el efecto dura mucho.",
            "Habla con alguien de confianza antes y procura no consumir sin compañía.",
            "Empieza con poco, porque la pureza cambia de una vez a otra.",
            "Después, descansa y come aunque no tengas ganas; si bailas o hace calor, toma pausas en un lugar fresco.",
            "No compartas pipas ni otro material: pueden pasar infecciones como la hepatitis C. Si es " +
                "inyectado, usa material nuevo cada vez."
        ),
        riskyMixes = listOf(
            RiskyMix(
                "Con alcohol",
                "el cristal oculta cuánto te está afectando el alcohol y el corazón trabaja de más."
            ),
            RiskyMix(
                "Con otros estimulantes (cocaína, bebidas energéticas, pastillas para bajar de peso)",
                "mucho riesgo para el corazón y de calor excesivo."
            ),
            RiskyMix(
                "Con antidepresivos o medicamentos para la atención (TDAH)",
                "pueden subir mucho la temperatura y la presión. Coméntalo con tu médico."
            )
        ),
        emergency = "Llama al 911 si hay dolor en el pecho, dificultad para respirar, temperatura muy alta " +
            "con confusión, convulsiones, desmayo, o mucha agitación sintiéndose en peligro.",
        helpTips = listOf(
            "Mientras llega la ayuda: lleva a la persona a un lugar fresco y tranquilo, háblale con calma " +
                "y no la dejes sin compañía.",
            "Busca apoyo si consumes varios días seguidos, si te cuesta dormir o comer, si sientes que te " +
                "persiguen, o si la bajada te deja con pensamientos de hacerte daño."
        )
    ),
    SubstanceInfo(
        id = "vape",
        label = "Vape",
        askBerto = "el vape",
        essentials = "La mayoría de los vapes tienen nicotina, a veces más que un cigarro, y a tu edad el " +
            "cerebro se engancha más rápido. No es solo vapor de agua: el aerosol lleva químicos y " +
            "partículas finas, y muchos vapes no dicen con exactitud qué contienen.",
        bodyAndMind = listOf(
            "En el momento: el corazón late más rápido, sientes una calma breve y, con mucha nicotina, mareo o náusea.",
            "Entre caladas: al bajar la nicotina aparecen ansiedad e irritabilidad. Por eso parece que el " +
                "vape te calma, aunque es la propia nicotina la que causa esa ansiedad.",
            "Con uso frecuente: tos, garganta irritada, daño en pulmones y encías, peor sueño y menos concentración."
        ),
        careTitle = "Si decides vapear, cuídate así",
        careTips = listOf(
            "Si sientes ganas, espera 15 minutos: las ganas de nicotina suben y bajan en pocos minutos.",
            "Toma agua y come algo; fíjate en qué momentos vapeas más (aburrimiento, estrés).",
            "Habla con alguien de confianza si quieres bajarle.",
            "Usa líquidos con menos nicotina y date lugares y momentos sin vape, como al despertar o en tu cuarto.",
            "No uses líquidos sin etiqueta ni con THC o aceites: se han relacionado con lesiones graves en los pulmones.",
            "Carga el vape con su cargador y no lo dejes cargando sin vigilancia, porque las baterías pueden explotar.",
            "Guarda los líquidos lejos de niñas, niños y mascotas: tragarlos es tóxico."
        ),
        riskyMixes = listOf(
            RiskyMix("Líquidos con THC o de origen desconocido", "el mayor riesgo de daño pulmonar."),
            RiskyMix("Con cigarro al mismo tiempo", "sumas la nicotina de los dos."),
            RiskyMix("Con alcohol", "terminas vapeando y bebiendo más de lo que planeabas."),
            RiskyMix("Con cristal u otros estimulantes", "más carga para el corazón.")
        ),
        emergency = "Llama al 911 si hay dificultad para respirar o dolor en el pecho que no se quita, tos " +
            "con sangre, o si alguien se tragó líquido de vape, sobre todo una niña o un niño.",
        helpTips = listOf(
            "Si sientes náusea, mareo fuerte o sudor frío por exceso de nicotina: deja de vapear y sal a " +
                "tomar aire. Si no mejora, pide ayuda.",
            "Busca apoyo si vapeas en cuanto despiertas, si te despiertas para vapear o si no aguantas " +
                "unas horas sin él."
        )
    ),
    SubstanceInfo(
        id = "cigarro",
        label = "Tabaco",
        askBerto = "el tabaco",
        essentials = "El cigarro tiene nicotina, que es muy adictiva, y al quemarse produce miles de " +
            "sustancias; muchas dañan los pulmones y el corazón y causan cáncer. No hay una cantidad " +
            "sin riesgo, pero cada cigarro menos sí cuenta.",
        bodyAndMind = listOf(
            "En el momento: sube tu ritmo cardiaco y tu presión; la calma que sientes es la nicotina " +
                "calmando sus propias ganas.",
            "Con uso frecuente: tos, te falta el aire al hacer ejercicio, se te manchan los dientes, " +
                "pierdes olfato y gusto, y te enfermas más de gripa.",
            "A largo plazo: cáncer y enfermedades del corazón y los pulmones.",
            "Lo bueno: a las pocas horas sin fumar baja el monóxido de carbono en tu sangre, y en unas " +
                "semanas mejoran tu respiración y tu circulación."
        ),
        careTitle = "Si decides fumar, cuídate así",
        careTips = listOf(
            "Si sientes ganas, espera 15 minutos; toma un vaso de agua o come algo mientras pasan.",
            "Habla con alguien de confianza si quieres bajarle.",
            "Fuma menos cigarros y date espacios sin fumar, como tu casa o el carro.",
            "No fumes en lugares cerrados ni cerca de niñas, niños o personas embarazadas: el humo también les daña.",
            "No fumes hasta la colilla: la última parte concentra más sustancias tóxicas."
        ),
        riskyMixes = listOf(
            RiskyMix("Con alcohol", "juntos multiplican el riesgo de cáncer de boca y garganta, y bebiendo se fuma más."),
            RiskyMix("Con anticonceptivos hormonales", "sube el riesgo de coágulos. Coméntalo con tu médico."),
            RiskyMix("Con vape al mismo tiempo", "sumas la nicotina de los dos."),
            RiskyMix("Con cristal", "más carga para el corazón.")
        ),
        emergency = "Llama al 911 si hay dolor u opresión en el pecho, falta de aire intensa, o debilidad " +
            "o adormecimiento de un lado del cuerpo.",
        helpTips = listOf(
            "Busca apoyo si fumas en cuanto despiertas, si fumas aunque te sientas mal o si ya intentaste " +
                "dejarlo y no has podido. Existen tratamientos (parches, chicles) y acompañamiento gratuito."
        )
    )
)

/** The substances in the profile first (in their catalog order), then the rest. */
fun orderedSubstanceInfos(profileSubstances: List<String>): List<SubstanceInfo> =
    SubstanceInfos.sortedBy { if (it.id in profileSubstances) 0 else 1 }
