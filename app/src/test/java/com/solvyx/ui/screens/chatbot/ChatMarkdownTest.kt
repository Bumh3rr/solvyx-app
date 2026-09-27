package com.solvyx.ui.screens.chatbot

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMarkdownTest {

    @Test
    fun `bold markers are removed and the text is bold`() {
        val result = formatearMarkdown("Esto es **importante** hoy")

        assertEquals("Esto es importante hoy", result.text)
        val span = result.spanStyles.single()
        assertEquals(FontWeight.Bold, span.item.fontWeight)
        assertEquals("importante", result.text.substring(span.start, span.end))
    }

    @Test
    fun `italic markers are removed and the text is italic`() {
        val result = formatearMarkdown("Un *pequeño* paso")

        assertEquals("Un pequeño paso", result.text)
        assertEquals(FontStyle.Italic, result.spanStyles.single().item.fontStyle)
    }

    @Test
    fun `dash and asterisk bullets become dots`() {
        val result = formatearMarkdown("- Respirar\n* Caminar\n  - Tomar agua")

        assertEquals("• Respirar\n• Caminar\n  • Tomar agua", result.text)
    }

    @Test
    fun `headings lose the hashes and become bold`() {
        val result = formatearMarkdown("### Efectos a corto plazo\nTexto")

        assertEquals("Efectos a corto plazo\nTexto", result.text)
        val span = result.spanStyles.single()
        assertEquals("Efectos a corto plazo", result.text.substring(span.start, span.end))
    }

    @Test
    fun `plain text is left untouched`() {
        val texto = "Hola, soy Berto. ¿En qué te puedo apoyar el día de hoy?"

        val result = formatearMarkdown(texto)

        assertEquals(texto, result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `voice text has no markdown symbols`() {
        val texto = "**Cómo actúa**\n- Aumenta la *energía*\n## Riesgos\n* Ansiedad"

        assertEquals("Cómo actúa\nAumenta la energía\nRiesgos\nAnsiedad", textoParaVoz(texto))
    }
}
