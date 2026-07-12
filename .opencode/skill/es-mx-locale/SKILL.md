---
description: Configuración de locale español (es-MX) para TTS de Solvyx. Selección de voz, fallback, normalización de números y abreviaturas.
---

# Skill: es-MX Locale

Esta skill te entrega las convenciones para configurar el locale español mexicano (es-MX) en el TTS de Solvyx. Aplícala cada vez que configures voz, idioma o normalices texto para TTS.

## Principios

1. **Locale target:** `es-MX` (español de México). No genérico `es`.
2. **Fallback en cascada:** es-MX → es-ES → es → cualquier-no-inglés → default.
3. **Normalizar números y abreviaturas** antes de pasar al TTS.
4. **Voz femenina por defecto** (consistente con la versión actual de Berto).
5. **Pitch y rate por defecto:** 1.15 y 0.85 respectivamente.

## Locale es-MX

### Selección

```kotlin
val targetLocale = Locale("es", "MX")

val result = tts?.setLanguage(targetLocale)
when (result) {
    TextToSpeech.LANG_MISSING_DATA -> {
        // Datos del idioma no instalados
    }
    TextToSpeech.LANG_NOT_SUPPORTED -> {
        // Idioma no soportado, fallback
    }
    TextToSpeech.LANG_AVAILABLE,
    TextToSpeech.LANG_COUNTRY_AVAILABLE,
    TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
        // OK
    }
}
```

### Verificación

```kotlin
fun isLocaleAvailable(tts: TextToSpeech, locale: Locale): Boolean {
    return tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE
}
```

## Selección de voz en cascada

```kotlin
fun selectVoice(tts: TextToSpeech): Voice? {
    val voices = tts.voices ?: return null
    
    // 1. Voz femenina es-MX
    voices.firstOrNull {
        it.locale.language == "es" &&
        it.locale.country == "MX" &&
        it.name.contains("female", ignoreCase = true)
    }?.let { return it }
    
    // 2. Voz es-MX cualquiera
    voices.firstOrNull {
        it.locale.language == "es" && it.locale.country == "MX"
    }?.let { return it }
    
    // 3. Voz es (España) femenina
    voices.firstOrNull {
        it.locale.language == "es" &&
        it.locale.country == "ES" &&
        it.name.contains("female", ignoreCase = true)
    }?.let { return it }
    
    // 4. Cualquier voz en español
    voices.firstOrNull {
        it.locale.language == "es"
    }?.let { return it }
    
    // 5. Cualquier voz no inglesa (fallback)
    voices.firstOrNull {
        it.locale.language != "en"
    }?.let { return it }
    
    // 6. Default
    return tts.defaultVoice
}
```

## Pitch y rate por defecto para es-MX

```kotlin
tts?.setPitch(1.15f)     // ligeramente más agudo para calidez
tts?.setSpeechRate(0.85f) // un poco más lento para claridad
```

Estos son los valores que usa Berto actualmente. Mantener consistencia.

## Normalización de texto para TTS

### Números

TTS puede leer "15-20" como "quince menos veinte" si no se normaliza. Mejor:

```kotlin
fun normalizeNumbers(text: String): String {
    return text
        // Rangos con guión: "15-20" → "15 a 20"
        .replace(Regex("(\\d+)\\s*-\\s*(\\d+)"), "$1 a $2")
        // Decimales: "4.5" → "4 punto 5"
        .replace(Regex("(\\d+)\\.(\\d+)"), "$1 punto $2")
        // Porcentajes: "50%" → "50 por ciento"
        .replace(Regex("(\\d+)%"), "$1 por ciento")
}
```

### Abreviaturas comunes

```kotlin
fun normalizeAbbreviations(text: String): String {
    return text
        .replace("etc.", "etcétera")
        .replace("Dr.", "Doctor")
        .replace("Dra.", "Doctora")
        .replace("Sr.", "Señor")
        .replace("Sra.", "Señora")
        .replace("Ud.", "Usted")
        .replace("ej.", "ejemplo")
        .replace("aprox.", "aproximadamente")
}
```

### URLs y referencias

```kotlin
fun normalizeUrls(text: String): String {
    return text
        .replace(Regex("https?://\\S+"), "enlace en pantalla")
        .replace("@\\w+", "arroba nombre de usuario")
}
```

### Tiempo y duración

```kotlin
fun normalizeTime(text: String): String {
    return text
        // "5 min" → "5 minutos"
        .replace(Regex("(\\d+)\\s*min\\b", RegexOption.IGNORE_CASE), "$1 minutos")
        // "5 seg" → "5 segundos"
        .replace(Regex("(\\d+)\\s*seg\\b", RegexOption.IGNORE_CASE), "$1 segundos")
        // "5h" → "5 horas"
        .replace(Regex("(\\d+)\\s*h\\b"), "$1 horas")
}
```

### Función completa

```kotlin
object TtsTextNormalizer {
    
    fun normalize(text: String): String {
        return text
            .normalizeNumbers()
            .normalizeAbbreviations()
            .normalizeUrls()
            .normalizeTime()
            // Limpiar espacios múltiples
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    private fun String.normalizeNumbers() = this
        .replace(Regex("(\\d+)\\s*-\\s*(\\d+)"), "$1 a $2")
        .replace(Regex("(\\d+)\\.(\\d+)"), "$1 punto $2")
        .replace(Regex("(\\d+)%"), "$1 por ciento")
    
    private fun String.normalizeAbbreviations() = this
        .replace("etc.", "etcétera")
        .replace("Dr.", "Doctor")
        .replace("Dra.", "Doctora")
        .replace("ej.", "ejemplo")
    
    private fun String.normalizeUrls() = this
        .replace(Regex("https?://\\S+"), "enlace en pantalla")
    
    private fun String.normalizeTime() = this
        .replace(Regex("(\\d+)\\s*min\\b", RegexOption.IGNORE_CASE), "$1 minutos")
        .replace(Regex("(\\d+)\\s*seg\\b", RegexOption.IGNORE_CASE), "$1 segundos")
}
```

Uso:

```kotlin
fun speak(text: String) {
    val normalized = TtsTextNormalizer.normalize(text)
    tts?.speak(normalized, QUEUE_FLUSH, null, UUID.randomUUID().toString())
}
```

## Puntuación para TTS

El TTS hace pausas según la puntuación. Para es-MX:

| Carácter | Pausa |
|---|---|
| `.` | Pausa larga. |
| `,` | Pausa corta. |
| `;` | Pausa media. |
| `:` | Pausa corta. |
| `?` | Entonación ascendente. |
| `!` | Entonación enfática. |
| `...` | Pausa larga reflexiva. |

Recomendaciones:

- **Una oración por bloque hablado** (≤ 200 caracteres).
- **Usar puntos** para separar ideas, no solo comas.
- **Evitar signos múltiples** (`!?!`).
- **Saltos de línea** se convierten en "." (ver `clean` en el helper).

## Ejemplos de normalización

| Original | Normalizado |
|---|---|
| "Respira 4-7-8 por 5 min." | "Respira 4 a 7 a 8 por 5 minutos." |
| "El 50% de los usuarios..." | "El 50 por ciento de los usuarios..." |
| "Visita https://ejemplo.com" | "Visita enlace en pantalla" |
| "Haz 3 repeticiones en 2.5 seg." | "Haz 3 repeticiones en 2 punto 5 segundos." |

## Testing

```kotlin
class TtsTextNormalizerTest {
    
    @Test
    fun `rangos se normalizan con a`() {
        assertEquals("15 a 20", TtsTextNormalizer.normalize("15-20"))
    }
    
    @Test
    fun `decimales se leen como punto`() {
        assertEquals("4 punto 5", TtsTextNormalizer.normalize("4.5"))
    }
    
    @Test
    fun `porcentajes se leen como por ciento`() {
        assertEquals("50 por ciento", TtsTextNormalizer.normalize("50%"))
    }
    
    @Test
    fun `min se expande a minutos`() {
        assertEquals("5 minutos", TtsTextNormalizer.normalize("5 min"))
    }
}
```

## Anti-patrones prohibidos

1. **`Locale("es")` sin país.** Usar `Locale("es", "MX")` o el target específico.
2. **Asumir que es-MX está instalado** sin verificar.
3. **TTS sobre números sin normalizar** ("15-20" se lee mal).
4. **TTS sobre URLs** (las deletrea).
5. **TTS sobre abreviaturas** ("Dr.", "etc." se leen mal).
6. **Strings con caracteres escapados** (`\\n`, `\\\"`).
7. **Saltos de línea sin reemplazar** por ". " (pausa natural).
8. **Bloques de texto > 200 caracteres** sin pausas.