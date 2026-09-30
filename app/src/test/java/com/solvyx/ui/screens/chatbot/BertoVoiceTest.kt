package com.solvyx.ui.screens.chatbot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BertoVoiceTest {

    @Test
    fun `each paragraph is its own chunk and blank lines are dropped`() {
        val chunks = speechChunks("Primero esto.\n\n  Luego   aquello.\n", maxLength = 4_000)
        assertEquals(listOf("Primero esto.", "Luego aquello."), chunks)
    }

    @Test
    fun `a long paragraph is cut at sentence ends without losing words`() {
        val text = "Una frase corta. Otra frase corta. ¿Una pregunta? ¡Y un cierre!"
        val chunks = speechChunks(text, maxLength = 36)
        assertTrue(chunks.all { it.length <= 36 })
        assertEquals(text, chunks.joinToString(" "))
        assertEquals("¿Una pregunta? ¡Y un cierre!", chunks.last())
    }

    @Test
    fun `a sentence longer than the limit is hard cut as a last resort`() {
        val chunks = speechChunks("a".repeat(25), maxLength = 10)
        assertEquals(listOf(10, 10, 5), chunks.map { it.length })
    }

    @Test
    fun `nothing to say gives no chunks`() {
        assertTrue(speechChunks(" \n \n", maxLength = 100).isEmpty())
    }
}
