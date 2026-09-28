package com.solvyx.ui.screens.chatbot

import java.util.Calendar

/** Everything Berto says outside the decision trees and the AI, in one place. */
internal object BertoScripts {

    private const val MORNING_START_HOUR = 5
    private const val AFTERNOON_START_HOUR = 12
    private const val NIGHT_START_HOUR = 19

    fun greeting(canChatFreely: Boolean, now: Calendar = Calendar.getInstance()): String {
        val salute = when (now.get(Calendar.HOUR_OF_DAY)) {
            in MORNING_START_HOUR until AFTERNOON_START_HOUR -> "Buenos días"
            in AFTERNOON_START_HOUR until NIGHT_START_HOUR -> "Buenas tardes"
            else -> "Buenas noches"
        }
        return if (canChatFreely) {
            "$salute, soy Berto. Puedes escribirme lo que sientes o elegir por dónde empezar:"
        } else {
            "$salute, soy Berto. ¿Por dónde empezamos?"
        }
    }

    const val TOPICS_AGAIN = "Estos son los temas en los que puedo acompañarte:"
    const val GREETING_REPLY = "¡Hola! Qué gusto leerte. ¿Por dónde empezamos?"
    const val OFFLINE_NOT_UNDERSTOOD = "Sin conexión solo puedo acompañarte con temas guiados. Elige el que más se parezca a lo que sientes:"
    const val IN_TREE_NOT_UNDERSTOOD = "Por ahora sigo con estas opciones. Toca la que más se parezca a lo que sientes:"
    const val TREE_FINISHED = "¿Quieres seguir con otra cosa?"
    const val GOODBYE = "Aquí estaré cuando me necesites. Cuídate mucho."

    fun askSubstance(intent: TopicIntent): String =
        if (intent == TopicIntent.CRAVING) "Gracias por decírmelo. ¿Ganas de qué sustancia?"
        else "Claro. ¿Sobre qué sustancia quieres saber?"

    fun askIntentFor(substanceLabel: String): String = "¿Qué necesitas sobre $substanceLabel?"

    fun startingTopic(substanceLabel: String): String = "Va, hablemos de $substanceLabel."

    // ── Support and crisis ────────────────────────────────────────────────────

    const val CRISIS_OPENING = "Gracias por contármelo. Lo que sientes importa y no tienes que pasar por esto sin compañía. " +
        "Estoy aquí contigo. Elige lo que necesites ahora:"
    const val FEELING_BAD_OPENING = "Siento que estés pasando por esto. Vamos paso a paso. ¿Qué te ayudaría ahora?"
    const val KEEP_TALKING = "Aquí estoy. Cuéntame qué está pasando, te leo con calma."
    const val CRISIS_OFFLINE_REPLY = "Te leo y sigo aquí contigo. Sin internet no puedo responderte con mis " +
        "propias palabras, pero puedes llamar a Línea de la Vida desde la barra de arriba. ¿Respiramos juntos?"
    const val CALL_STARTED = "Aquí te espero. Cuando termines, sigo contigo."
    const val ASK_HOW_YOU_FEEL = "¿Cómo te sientes ahora?"
    const val POSITIVE_REPLY = "¡Qué bueno leer eso! Me alegra mucho. ¿Quieres que sigamos con algún tema?"
    const val FEEL_BETTER_REPLY = "Me alegra mucho. Si vuelve a ponerse difícil, el botón SOS siempre está abajo."
    const val STILL_BAD_REPLY = "Está bien no estar bien. No tienes que resolverlo sin ayuda: hablar con alguien ahora puede ayudarte mucho."

    const val OPTION_FEEL_BETTER = "Un poco mejor"
    const val LEAVE_CRISIS = "Ya estoy mejor"
    const val OPTION_STILL_BAD = "Sigo igual"

    // ── 5-4-3-2-1 grounding ──────────────────────────────────────────────────

    const val GROUNDING_INTRO = "Vamos a anclarte al presente con tus sentidos. Tómate tu tiempo en cada paso."
    const val OPTION_GROUNDING_NEXT = "Listo"
    val groundingSteps = listOf(
        "Mira a tu alrededor y nombra 5 cosas que puedas ver.",
        "Ahora 4 cosas que puedas tocar. Siente su textura, su temperatura.",
        "3 cosas que puedas escuchar, aunque sean suaves.",
        "2 cosas que puedas oler. Si no hueles nada, recuerda un olor que te guste.",
        "1 cosa que puedas saborear, o toma un sorbo de agua despacio."
    )
    const val GROUNDING_DONE = "Lo hiciste muy bien. Volviste al aquí y al ahora."

    // ── Breathing ────────────────────────────────────────────────────────────

    const val BREATHING_DONE = "Bien hecho. Respirar así le dice a tu cuerpo que está a salvo."

    // ── Quick replies of the guided flow ─────────────────────────────────────

    const val OPTION_OTHER_TOPICS = "Ver otros temas"
    const val OPTION_BREATHE = "Respirar conmigo"
    const val OPTION_THATS_ALL = "Eso es todo, gracias"

    // ── AI consent ───────────────────────────────────────────────────────────

    const val AI_CONSENT_ACCEPTED = "Gracias por confiar en mí. Ya puedes escribirme lo que sientes o elegir un tema."
}
