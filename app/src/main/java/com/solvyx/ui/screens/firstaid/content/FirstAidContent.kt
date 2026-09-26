package com.solvyx.ui.screens.firstaid.content

import com.solvyx.R
import com.solvyx.ui.screens.firstaid.model.ActionPlan
import com.solvyx.ui.screens.firstaid.model.BertoMood
import com.solvyx.ui.screens.firstaid.model.FirstAidRoute
import com.solvyx.ui.screens.firstaid.model.FirstAidSituation
import com.solvyx.ui.screens.firstaid.model.GuideHero
import com.solvyx.ui.screens.firstaid.model.GuideSubstance
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.screens.firstaid.model.InfoSection

/**
 * Every user-facing text of the first-aid module, kept apart from the UI so screens only lay
 * things out. All content works offline — nothing here comes from the network.
 */
object HubContent {
    val situations = listOf(
        FirstAidSituation(
            route = FirstAidRoute.PANIC,
            icon = R.drawable.ic_brain,
            title = "Ansiedad o pánico",
            subtitle = "Calma tu cuerpo en minutos"
        ),
        FirstAidSituation(
            route = FirstAidRoute.CRAVING,
            icon = R.drawable.ic_flame_2,
            title = "Ganas muy fuertes",
            subtitle = "Aguanta la ola de 15 a 20 minutos"
        ),
        FirstAidSituation(
            route = FirstAidRoute.OVERUSE,
            icon = R.drawable.ic_alert_triangle,
            title = "Consumí de más",
            subtitle = "Señales y cómo cuidarte",
            tone = GuideTone.WARNING
        ),
        FirstAidSituation(
            route = FirstAidRoute.CRISIS_CHECK,
            icon = R.drawable.ic_heart_pulse,
            title = "¿Estoy en crisis?",
            subtitle = "Revisa cómo te sientes"
        )
    )
}

/** 911 red flags shared by the overuse guide and the crisis self-check. */
val EmergencySigns = listOf(
    "Pérdida de consciencia",
    "Convulsiones",
    "Dificultad para respirar",
    "Labios o dedos azulados",
    "Temperatura corporal muy alta",
    "No responde cuando le hablas"
)

object CrisisGuideContent {
    val hero = GuideHero(
        mood = BertoMood.WORRIED,
        message = "Que estés leyendo esto ya es un acto de valentía.",
        supporting = "Respira. Estoy aquí contigo."
    )
    val plan = ActionPlan(
        title = "Haz esto ahora mismo",
        steps = listOf(
            "Pon el teléfono en un lugar seguro y siéntate.",
            "Toma 3 respiraciones lentas. Inhala por la nariz, exhala por la boca.",
            "Dite: \"Este momento va a pasar.\"",
            "Avisa a alguien de tu red de apoyo."
        ),
        completionMessage = "Diste cada paso. No tienes que resolver todo solo o sola."
    )
    val feelings = InfoSection(
        icon = R.drawable.ic_heart,
        title = "Lo que sientes tiene nombre",
        paragraph = "La crisis no dura para siempre, aunque en este momento se sienta así. Lo que " +
            "sientes es real y tiene solución. No tienes que resolverlo solo o sola."
    )
}

object PanicGuideContent {
    val hero = GuideHero(
        mood = BertoMood.CALMING,
        message = "Lo que sientes es real, pero no es permanente.",
        supporting = "El pánico llega a su punto más alto en unos 10 minutos y luego baja."
    )
    val plan = ActionPlan(
        title = "Haz esto ahora",
        steps = listOf(
            "Siéntate o recuéstate en un lugar seguro.",
            "Inhala 4 segundos, exhala 6 segundos. Lento.",
            "Repite en tu mente: \"Esto va a pasar. Estoy a salvo.\"",
            "No luches contra el pánico, déjalo pasar."
        ),
        completionMessage = "Lo estás haciendo muy bien. Tu cuerpo ya está empezando a bajar el ritmo."
    )
    val heartWarning = InfoSection(
        icon = R.drawable.ic_info_circle,
        title = "¿Pánico o emergencia cardíaca?",
        tone = GuideTone.WARNING,
        paragraph = "Un ataque de pánico puede sentirse igual que un infarto, pero el pánico baja " +
            "después de unos 10 minutos. Si el dolor de pecho sigue o se extiende al brazo, llama al 911."
    )
    val professionalHelp = InfoSection(
        icon = R.drawable.ic_user_check,
        title = "Cuándo buscar ayuda profesional",
        paragraph = "Si los ataques son frecuentes o interfieren con tu vida diaria, es momento de " +
            "hablar con un profesional."
    )
    /** 5-4-3-2-1 grounding: how many things to notice per sense. */
    val groundingSenses = listOf(5 to "Ver", 4 to "Tocar", 3 to "Oír", 2 to "Oler", 1 to "Probar")
}

object CravingGuideContent {
    val hero = GuideHero(
        mood = BertoMood.CALMING,
        message = "Las ganas van a pasar.",
        supporting = "Un craving suele durar entre 15 y 20 minutos. Vamos a aguantarlo juntos."
    )
    val plan = ActionPlan(
        title = "Mientras pasa la ola",
        steps = listOf(
            "Ponle nombre: \"Esto que siento es craving.\"",
            "Cambia de ambiente: sal del cuarto, muévete.",
            "Toma agua y come algo si puedes.",
            "Contacta a alguien de tu red de apoyo."
        ),
        completionMessage = "Cada vez que aguantas una ola, la siguiente se vuelve más fácil."
    )
    val whatIsCraving = InfoSection(
        icon = R.drawable.ic_brain,
        title = "¿Qué es el craving?",
        paragraph = "Es un deseo muy intenso y urgente de consumir. Es una respuesta del cerebro que " +
            "suele durar entre 15 y 20 minutos. Pasará, aunque ahora se sienta imposible de aguantar."
    )
    val saferUse = InfoSection(
        icon = R.drawable.ic_shield,
        title = "Si decides consumir",
        paragraph = "Sin juicios. Si vas a consumir, hazlo de la manera más segura posible.",
        bullets = listOf(
            "No consumas solo o sola",
            "No mezcles sustancias",
            "Toma agua y come antes",
            "Busca un lugar seguro donde haya alguien de confianza"
        )
    )
}

object OveruseGuideContent {
    val hero = GuideHero(
        mood = BertoMood.WORRIED,
        message = "Pasó. Lo más importante ahora es que estés bien.",
        supporting = "Aquí no hay juicios, solo lo que necesitas para cuidarte."
    )
    val plan = ActionPlan(
        title = "Cuídate ahora mismo",
        steps = listOf(
            "No estés solo o sola. Avisa a alguien.",
            "Si tienes náuseas, acuéstate de lado.",
            "Toma agua a sorbos pequeños.",
            "No tomes medicamentos sin saber qué consumiste.",
            "No te duermas sin que alguien esté contigo."
        ),
        completionMessage = "Te estás cuidando. Quédate cerca de alguien hasta sentirte mejor."
    )
    val warningSigns: Map<GuideSubstance, List<String>> = mapOf(
        GuideSubstance.ALCOHOL to listOf(
            "Vómito que no para",
            "Confusión o desorientación",
            "Respiración lenta o irregular",
            "Piel fría o pálida",
            "No puedes mantenerte despierto o despierta"
        ),
        GuideSubstance.CRISTAL to listOf(
            "Corazón muy acelerado",
            "Sensación de calor extremo",
            "Paranoia o alucinaciones",
            "Temblores fuertes",
            "No puedes dormir desde hace más de 24 horas"
        ),
        GuideSubstance.VAPE to listOf(
            "Tos persistente",
            "Falta de aire o dolor en el pecho",
            "Mareo o náuseas",
            "Pulso acelerado",
            "Sensación de adormecimiento en boca o garganta"
        ),
        GuideSubstance.CIGARRO to listOf(
            "Náuseas o vómito intenso",
            "Ansiedad o pánico fuerte",
            "Sensación de no ser tú mismo o tú misma",
            "Pulso muy acelerado",
            "No puedes coordinar movimientos"
        )
    )
}
