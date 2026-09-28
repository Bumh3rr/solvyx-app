package com.solvyx.ui.screens.chatbot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatKeywordsTest {

    // ── CrisisDetector ───────────────────────────────────────────────────────

    @Test
    fun `detects crisis phrases written without accents or with different casing`() {
        listOf(
            "ya no puedo mas",
            "NO PUEDO MÁS!!",
            "me quiero matar",
            "quiero suicidarme",
            "pienso en el suicidio",
            "no quiero vivir",
            "quiero desaparecer",
            "tengo ganas de morirme",
            "me quiero hacer daño",
            "voy a cortarme",
            "creo que es una sobredosis",
            "no puedo respirar",
            "quiero acabar con todo"
        ).forEach { assertTrue("\"$it\" should be a crisis", CrisisDetector.isCrisis(it)) }
    }

    @Test
    fun `ordinary messages are not a crisis`() {
        listOf(
            "hola berto",
            "tengo ganas de tomar una cerveza",
            "me siento un poco triste hoy",
            "quiero información del vape",
            "puedo más de lo que creía"
        ).forEach { assertFalse("\"$it\" should not be a crisis", CrisisDetector.isCrisis(it)) }
    }

    @Test
    fun `does not match crisis words inside longer unrelated words`() {
        assertFalse(CrisisDetector.isCrisis("recortarme el pelo"))
    }

    // ── MoodDetector ─────────────────────────────────────────────────────────

    @Test
    fun `distress wins over positive words in the same message`() {
        assertEquals(BertoState.PREOCUPADO, MoodDetector.detect("no estoy bien"))
        assertEquals(BertoState.PREOCUPADO, MoodDetector.detect("gracias, pero sigo con ansiedad"))
    }

    @Test
    fun `positive messages make Berto celebrate`() {
        assertEquals(BertoState.CELEBRANDO, MoodDetector.detect("¡Lo logré! Una semana sin vapear"))
        assertEquals(BertoState.CELEBRANDO, MoodDetector.detect("me siento mejor"))
        assertEquals(BertoState.CELEBRANDO, MoodDetector.detect("gracias"))
    }

    @Test
    fun `neutral messages keep the current state`() {
        assertNull(MoodDetector.detect("qué efectos tiene el cristal"))
    }

    @Test
    fun `negated positives are distress, not celebration`() {
        listOf(
            "no estoy bien",
            "no me siento mejor",
            "no me siento muy bien",
            "sigo igual de mal",
            "ya no estoy bien",
            "estoy peor",
            "me siento muy solo",
            "no me ayudó"
        ).forEach { assertEquals("\"$it\"", BertoState.PREOCUPADO, MoodDetector.detect(it)) }
    }

    @Test
    fun `clear improvements are positive even with a leading no`() {
        listOf(
            "no, ya estoy bien",
            "ya me siento mucho mejor, gracias",
            "me siento un poco mejor",
            "ya estoy más tranquilo",
            "ya se me pasó",
            "ya me calmé",
            "me ayudó mucho respirar",
            "gracias berto, eres genial",
            "hoy estoy contenta"
        ).forEach { assertEquals("\"$it\"", BertoState.CELEBRANDO, MoodDetector.detect(it)) }
    }

    @Test
    fun `solo meaning only is not loneliness`() {
        assertNull(MoodDetector.detect("solo quiero información del alcohol"))
    }

    @Test
    fun `recognizes greetings`() {
        listOf("hola", "Holaaa berto", "buenas tardes", "qué onda", "hey").forEach {
            assertTrue("\"$it\"", GreetingDetector.isGreeting(it))
        }
        assertFalse(GreetingDetector.isGreeting("quiero información del vape"))
    }

    // ── ConversationMood ─────────────────────────────────────────────────────

    /** Runs a whole conversation and returns Berto's state after each message. */
    private fun conversation(vararg texts: String): List<MoodReaction> {
        val mood = ConversationMood()
        var state = BertoState.TRANQUILO
        return texts.map { text -> mood.react(state, text).also { state = it.state } }
    }

    @Test
    fun `a positive message ends the crisis calmly`() {
        val reactions = conversation("me quiero hacer daño", "ya me siento mucho mejor, gracias")

        assertEquals(MoodReaction(BertoState.CRISIS, CrisisShift.DETECTED), reactions[0])
        assertEquals(MoodReaction(BertoState.TRANQUILO, CrisisShift.EASED), reactions[1])
    }

    @Test
    fun `distress keeps the crisis no matter how many messages`() {
        val reactions = conversation("ya no puedo más", "sigo mal", "no estoy bien", "tengo miedo")

        reactions.forEach { assertEquals(BertoState.CRISIS, it.state) }
    }

    @Test
    fun `two neutral messages in a row lower the crisis to worried`() {
        val reactions = conversation("quiero desaparecer", "estoy en mi cuarto", "hoy fui a la escuela")

        assertEquals(BertoState.CRISIS, reactions[1].state)
        assertEquals(MoodReaction(BertoState.PREOCUPADO, CrisisShift.STEPPED_DOWN), reactions[2])
    }

    @Test
    fun `distress in between restarts the neutral count`() {
        val reactions = conversation("no quiero vivir", "estoy en mi cuarto", "me siento mal", "hoy fui a la escuela")

        assertEquals(BertoState.CRISIS, reactions[3].state)
    }

    @Test
    fun `risk words bring the crisis back from any state`() {
        val reactions = conversation("me quiero matar", "ya estoy bien", "¡lo logré!", "en realidad me quiero morir")

        assertEquals(BertoState.TRANQUILO, reactions[1].state)
        assertEquals(BertoState.CELEBRANDO, reactions[2].state)
        assertEquals(MoodReaction(BertoState.CRISIS, CrisisShift.DETECTED), reactions[3])
    }

    @Test
    fun `outside a crisis mood follows the message and neutral text keeps it`() {
        val reactions = conversation("estoy muy ansioso", "¿qué efectos tiene el vape?", "gracias, ya estoy más tranquilo")

        assertEquals(BertoState.PREOCUPADO, reactions[0].state)
        assertEquals(BertoState.PREOCUPADO, reactions[1].state)
        assertEquals(MoodReaction(BertoState.CELEBRANDO), reactions[2])
    }

    @Test
    fun `reset forgets neutral messages counted before a button ended the crisis`() {
        val mood = ConversationMood()
        mood.react(BertoState.CRISIS, "estoy en mi cuarto")
        mood.reset()

        assertEquals(BertoState.CRISIS, mood.react(BertoState.CRISIS, "hoy fui a la escuela").state)
    }

    // ── OfflineTopicMatcher ──────────────────────────────────────────────────

    @Test
    fun `recognizes substance and craving together`() {
        assertEquals(TopicMatch(TopicIntent.CRAVING, "alcohol"), OfflineTopicMatcher.match("tengo muchas ganas de unas chelas"))
        assertEquals(TopicMatch(TopicIntent.CRAVING, "cigarro"), OfflineTopicMatcher.match("se me antoja fumar"))
    }

    @Test
    fun `recognizes an information request about a substance`() {
        assertEquals(TopicMatch(TopicIntent.INFO, "cristal"), OfflineTopicMatcher.match("¿qué efectos tiene el foco?"))
    }

    @Test
    fun `an electronic cigarette is a vape, not tobacco`() {
        assertEquals("vape", OfflineTopicMatcher.match("uso cigarro electrónico").substanceId)
    }

    @Test
    fun `recognizes a substance alone or an intent alone`() {
        assertEquals(TopicMatch(null, "vape"), OfflineTopicMatcher.match("el vape"))
        assertEquals(TopicMatch(TopicIntent.CRAVING, null), OfflineTopicMatcher.match("tengo antojo"))
    }

    @Test
    fun `unrelated text matches nothing`() {
        assertTrue(OfflineTopicMatcher.match("hola, ¿cómo estás?").isEmpty)
    }

    @Test
    fun `words that only contain a substance name do not match it`() {
        assertNull(OfflineTopicMatcher.match("no podría decirte").substanceId)
    }
}
