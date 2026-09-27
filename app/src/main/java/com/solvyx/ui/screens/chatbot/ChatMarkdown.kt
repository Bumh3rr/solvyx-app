package com.solvyx.ui.screens.chatbot

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

// Markdown básico que devuelve la IA (DeepSeek): **negritas**, *cursivas*, viñetas y encabezados.
// No es un parser completo; cubre lo que aparece en respuestas de chat.

private val ENCABEZADO = Regex("""^\s{0,3}#{1,6}\s+""")
private val VINETA = Regex("""^(\s*)[-*•]\s+""")
// Grupo 1: **negrita**. Grupo 2: *cursiva* (sin espacios pegados a los asteriscos).
private val ENFASIS = Regex("""\*\*(.+?)\*\*|\*(?![\s*])(.+?)(?<![\s*])\*""")

/** Texto de la burbuja: aplica negritas/cursivas y convierte viñetas y encabezados. */
fun formatearMarkdown(texto: String): AnnotatedString = buildAnnotatedString {
    texto.lines().forEachIndexed { i, original ->
        if (i > 0) append('\n')
        val esEncabezado = ENCABEZADO.containsMatchIn(original)
        val linea = original
            .replace(ENCABEZADO, "")
            .replace(VINETA) { "${it.groupValues[1]}• " }

        val inicioLinea = length
        var cursor = 0
        ENFASIS.findAll(linea).forEach { match ->
            append(linea.substring(cursor, match.range.first))
            val negrita = match.groups[1]
            if (negrita != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(negrita.value) }
            } else {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[2]) }
            }
            cursor = match.range.last + 1
        }
        append(linea.substring(cursor))

        if (esEncabezado) addStyle(SpanStyle(fontWeight = FontWeight.Bold), inicioLinea, length)
    }
}

/** Texto para la voz: sin marcas de Markdown (si no, el TTS lee "asterisco"). */
fun textoParaVoz(texto: String): String =
    texto.lines()
        .joinToString("\n") { linea ->
            linea.replace(ENCABEZADO, "")
                .replace(VINETA, "$1")
                .replace(ENFASIS) { it.groups[1]?.value ?: it.groupValues[2] }
        }
        .replace("*", "")
