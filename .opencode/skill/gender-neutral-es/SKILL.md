---
description: Lenguaje inclusivo en español para Solvyx. Evitar masculino genérico, formas neutras, inclusividad de género no binario, contexto de jóvenes 15-25.
---

# Skill: Gender-Neutral Spanish

Esta skill te entrega las convenciones para usar lenguaje inclusivo en español dentro de Solvyx. Aplícala cada vez que escribas o modifiques strings visibles para el usuario.

## Principios

1. **Evitar masculino genérico forzado.** "Todos los usuarios" excluye.
2. **Preferir formas neutras** cuando sean naturales.
3. **Evitar binario "solo o sola" / "listo o lista"** cuando exista alternativa neutra.
4. **Respetar pronombres no binarios** cuando el sistema lo permita.
5. **No asumir género del usuario.** Hasta que el usuario lo indique, usar neutro.
6. **Consistencia con la skill `spanish-copy-standards`** del bloque backend.

## Estrategias de inclusión

### 1. Sustantivos neutros colectivos

| En lugar de | Usar |
|---|---|
| Los usuarios | Las personas usuarias / Quienes usan |
| Los jóvenes | La juventud / Las personas jóvenes |
| Los adictos | Las personas que consumen / Quienes consumen |
| Los pacientes | Las personas / Quienes asisten |
| Los profesionales | El equipo profesional |

### 2. Sustantivos epicenos (misma forma, ambos géneros)

| Sustantivo | Uso |
|---|---|
| estudiante | estudiante de cualquier género. |
| adolescente | adolescente. |
| persona | persona. |
| individua | neologismo neutro (no usar). |
| integrante | integrante. |
| participante | participante. |

### 3. Construcción sin género explícito

| En lugar de | Usar |
|---|---|
| "Bienvenido" | "Te damos la bienvenida" |
| "Si estás ansioso" | "Si sientes ansiedad" |
| "Si estás solo" | "Si estás en soledad" |
| "Si eres adicto" | "Si tienes un patrón problemático de consumo" |
| "Cuéntame qué te pasa" | "Cuéntame qué sientes" |

### 4. Construcciones con infinitivo o subjuntivo

| En lugar de | Usar |
|---|---|
| "Cuando estés listo/a" | "Cuando estés listx" (en contextos informales) o "Cuando quieras" |
| "Si eres mayor de edad" | "Si tienes más de 18 años" |
| "Si te sientes ansioso" | "Si sientes ansiedad" o "Si experimentas ansiedad" |

### 5. Dobles formas (con cuidado)

Las dobles formas ("todos y todas", "niños y niñas") son válidas pero pueden sentirse forzadas. Preferir:

1. Sustantivos neutros.
2. Construcciones sin género.
3. Dobles formas (si las anteriores no aplican).

| En lugar de | Usar (opciones) |
|---|---|
| Todos los participantes | Quienes participan / Todas las personas participantes |
| Los niños y las niñas | La infancia / Las niñas y los niños |
| Listo o lista | Listx / Cuando estés preparadx / Cuando quieras |

### 6. Lenguaje de "persona que..."

| En lugar de | Usar |
|---|---|
| Adicto | Persona que consume / Persona con consumo problemático |
| Alcohólico | Persona que consume alcohol de forma problemática |
| Drogadicto | Persona que usa sustancias |
| Consumidor | Persona que consume (mantiene "consumir" pero quita identidad) |
| Usuario (de app) | Persona usuaria / Quien usa Solvyx |

### 7. Segunda persona (tuteo) neutro

El pronombre "tú" en sí mismo es neutro. Pero los adjetivos deben ser neutros:

| Incorrecto | Correcto |
|---|---|
| "Estás ansioso" | "Sientes ansiedad" |
| "Estás listo" | "Estás preparadx" o "Estás listx" |
| "Te ves feliz" | "Tu expresión se ve feliz" |
| "Estás solo" | "Estás en soledad" |

## Casos en Solvyx

### Botones y acciones

```xml
<!-- Mal -->
<string name="btn_registrar">Registrar mi día</string>  <!-- OK, neutro -->

<!-- Mal -->
<string name="btn_listo">Listo</string>  <!-- masculino implícito -->

<!-- Bien -->
<string name="btn_listo">Listo</string>  <!-- OK en este contexto (no adjetivo) -->

<!-- Mal -->
<string name="msg_bienvenida">Bienvenido a Solvyx</string>

<!-- Bien -->
<string name="msg_bienvenida">Te damos la bienvenida a Solvyx</string>
```

### TTS

```xml
<!-- Mal -->
<string name="tts_ejercicio_inhala">Inhala cuando estés listo</string>

<!-- Bien -->
<string name="tts_ejercicio_inhala">Inhala cuando estés preparadx</string>

<!-- O mejor -->
<string name="tts_ejercicio_inhala">Inhala cuando estés listx</string>

<!-- O mejor aún (más fluido) -->
<string name="tts_ejercicio_inhala">Inhala cuando quieras</string>
```

### Mensajes motivacionales

```xml
<!-- Mal -->
<string name="msg_logro">¡Lo lograste! Sigue así.</string>

<!-- Mejor (ya neutro) -->
<string name="msg_logro">Lo lograste. Sigue cuidándote.</string>
```

### Insights de Berto

```xml
<!-- Mal -->
<string name="insight_sueno_bajo">Esta semana dormiste menos que la anterior. ¿Algo cambió?</string>
<!-- "dormiste" es segunda persona, técnicamente neutro -->

<!-- Mejor -->
<string name="insight_sueno_bajo">Esta semana tu sueño fue menor que la anterior. ¿Algo cambió?</string>
```

### ASSIST resultados

```xml
<!-- Mal -->
<string name="assist_resultado_alto">Tienes un patrón de consumo de alto riesgo. Te recomendamos buscar ayuda profesional.</string>

<!-- Mejor -->
<string name="assist_resultado_alto">Tu patrón de consumo sugiere un nivel de riesgo alto. Te recomendamos buscar ayuda profesional.</string>

<!-- Aún mejor -->
<string name="assist_resultado_alto">El patrón que registraste se asocia con un nivel de riesgo alto. La ayuda profesional puede hacer una diferencia grande. Aquí tienes opciones:</string>
```

### Notificaciones

```xml
<!-- Mal -->
<string name="notif_bitacora">¿Qué tal tu día, listo para registrar?</string>

<!-- Mejor -->
<string name="notif_bitacora">¿Qué tal tu día? Aquí seguimos cuando quieras registrar.</string>
```

## Auditar strings actuales

### Cómo encontrar problemas

```bash
# Buscar masculinos genéricos comunes
grep -rn "todos los" app/src/main/res/values/strings.xml
grep -rn "todos las" app/src/main/res/values/strings.xml
grep -rn "los usuarios" app/src/main/res/values/strings.xml
grep -rn "los jóvenes" app/src/main/res/values/strings.xml
grep -rn "Bienvenido" app/src/main/res/values/strings.xml
grep -rn "solo o sola" app/src/main/java/com/solvyx/ui/
grep -rn "listo o lista" app/src/main/java/com/solvyx/ui/
```

### Reportar hallazgo

```markdown
| # | Archivo:línea | Original | Sugerencia |
|---|---------------|----------|------------|
| 1 | strings.xml:42 | "Bienvenido" | "Te damos la bienvenida" |
| 2 | GuiaCravingIntensoScreen.kt:107 | "No consumas solo o sola" | "No consumas en soledad" |
```

## Caso especial: identidad de género del usuario

Si el usuario tiene la opción de indicar su identidad de género en Mi Perfil:

```kotlin
enum class GeneroUsuario(val pronombre: String, val tratamiento: String) {
    HOMBRE("él", "Estimado"),
    MUJER("ella", "Estimada"),
    NO_BINARIO("elle", "Estimade"),
    PREFIERO_NO_DECIR("", "")
}
```

Si no tienen esa opción aún, usa SIEMPRE lenguaje neutro o segunda persona sin adjetivos de género.

## Tono para 15-25 años

En el contexto de Solvyx (jóvenes 15-25):

- **Evitar lenguaje infantil** ("consentidito", "tesoro").
- **Evitar lenguaje académico** ("individuo", "sujeto").
- **Preferir "tú" directo.**
- **Evitar diminutivos.**

Ejemplos:

| Evitar | Preferir |
|---|---|
| "No te preocupes,consentido" | "Aquí estamos contigo" |
| "El sujeto debe..." | "Tú decides..." |
| "Estimado usuario" | "Hola" |

## Anti-patrones prohibidos

1. **"Bienvenido"** sin alternativa. → "Te damos la bienvenida".
2. **"Solo o sola", "listo o lista"** cuando haya alternativa neutra.
3. **"Los usuarios", "los jóvenes", "los profesionales"** sin alternativa neutra.
4. **"Adicto", "alcohólico", "drogadicto"** como sustantivo de identidad.
5. **"Todos"** sin alternativa neutra.
6. **"Estás ansioso/triste/deprimido"** (adjetivo de género) cuando se puede reformular como sustantivo.
7. **Diminutivos no intencionales** ("poquito", "ratito").
8. **Asumir género** del usuario sin pedirlo.
9. **"El hombre", "la mujer"** en lugar de "la persona".
10. **Forzar dobles formas** cuando existe alternativa más fluida.