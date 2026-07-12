---
description: Directorio de líneas de ayuda en México para crisis en Solvyx. Criterios de cuándo sugerir cada línea según el caso (menor de edad, suicidalidad, violencia, urgencia médica).
---

# Skill: Líneas de Ayuda México

Esta skill te entrega el directorio oficial de líneas de ayuda y servicios de salud mental en México, con criterios para sugerir la más adecuada según el caso. Aplícala siempre que el bot de Solvyx recomiende atención profesional, esté en crisis o derive a un usuario.

## Cuándo cargar esta skill

- Cualquier mención a "Línea de la Vida", "911", "ayuda profesional", "psicólogo", "psiquiatra", "clínica", "urgencias".
- Pantallas: SOS, Estoy en Crisis, Directorio Profesional.
- Resultado del ASSIST en nivel ALTO o MODERADO.
- Cualquier texto que recomiende "hablar con alguien" sin especificar a quién.

## Directorio oficial

| Línea / Servicio | Teléfono | Horario | Cobertura | Para qué sirve |
|---|---|---|---|---|
| **Línea de la Vida** | 800 911 2000 | 24/7, 365 días | Nacional | Crisis emocional, ideación suicida, autolesión, consumo problemático, acompañamiento psicosocial gratuito. |
| **911** | 911 | 24/7 | Nacional | Emergencias médicas, seguridad, riesgo vital inminente. |
| **SAPTEL** (Sistema de Apoyo Psicológico por Teléfono) | 55 5259 8121 | 24/7 | Nacional (desde CDMX) | Crisis emocionales, suicidio, contención. Alternativa gratuita. |
| **LOCATEL** (CDMX) | 55 5658 1111 | 24/7 | CDMX | Orientación psicológica, médica, legal, acompañamiento. |
| **CIJ** (Centros de Integración Juvenil) | 800 911 2000 (mismo número) | Horarios variables | Nacional | Prevención y atención de adicciones. Red de centros presenciales. |
| **DIF Nacional** | 800 290 0024 | L-V 8-20h | Nacional | Trabajo social, violencia familiar, menores en riesgo. |
| **UNICEF México** (línea para adolescentes) | 800 422 6000 | L-D 8-22h | Nacional | Orientación para menores de 18 sobre sus derechos, abuso, violencia. |
| **Red FLAG (Federación Latinoamericana de Grupos de Alcohólicos en Rehabilitación)** | 800 422 6000 (orientación) | Horario variable | Nacional | Información sobre grupos de ayuda mutua. |
| **Línea de Ayuda contra la Violencia de Género** | 800 911 2000 (Línea de la Vida) y 911 | 24/7 | Nacional | Violencia familiar, de pareja, sexual. |
| **Hospital General / Psiquiátrico más cercano** | Variable | 24/7 urgencias | Local | Crisis psiquiátrica severa, abstinencia complicada, agitación extrema. |

> **Nota importante:** verifica al momento de usar esta skill que los números y horarios sigan vigentes. Si tienes dudas, indícalo explícitamente en tu reporte.

## Criterios de sugerencia por caso

Usa estos criterios para decidir cuál línea mencionar primero cuando Berto recomiende ayuda.

### Ideación suicida explícita ("me quiero morir", "ya no quiero estar aquí")

1. **Línea de la Vida** 800 911 2000 (primera mención, siempre).
2. Si hay plan concreto o medios disponibles: **911** antes de cualquier otra cosa.
3. Mencionar **SAPTEL** como alternativa.
4. Si es menor de 18: **UNICEF** y **DIF** como recursos complementarios.

### Autolesión activa o reciente

1. Si hay sangrado, heridas que requieren atención: **911** o urgencias hospitalarias.
2. Si la persona está estable pero se autolesiona: **Línea de la Vida** + directorio profesional.
3. Si es menor de 18: agregar **DIF** y servicios locales de protección infantil.

### Pánico agudo sin suicidalidad

1. **Línea de la Vida** solo si la persona lo solicita o si se extiende más de 30 minutos.
2. Berto debe ofrecer primero el ejercicio 5-4-3-2-1 y respiración.
3. Si no mejora: derivar a **Línea de la Vida**.

### Cravings intensos sin crisis

1. **CIJ** o directorio profesional (Centros de Integración Juvenil).
2. **Línea de la Vida** si la persona lo pide.

### Resultado del ASSIST en nivel ALTO

1. **Directorio Profesional** de Solvyx (psicólogos, clínicas, instituciones).
2. **Línea de la Vida** 800 911 2000 como primer contacto gratuito.
3. **CIJ** si hay disposición a centros presenciales.
4. Mencionar **Línea de Ayuda contra la Violencia** si hay contexto de violencia.

### Menores de 18 en cualquier situación de riesgo

1. **Línea de la Vida** 800 911 2000.
2. **UNICEF** 800 422 6000.
3. **DIF** 800 290 0024.
4. **911** si hay riesgo vital.
5. **Recomendar hablar con un adulto de confianza** de su red de apoyo.

### Violencia familiar, de pareja o sexual

1. **911** si hay peligro inmediato.
2. **Línea de la Vida** 800 911 2000.
3. **Línea de Ayuda contra la Violencia de Género**.
4. **DIF** para menores.

## Cómo citar líneas de ayuda en el copy de Solvyx

Reglas para que el bot mencione líneas de ayuda de manera útil y respetuosa:

1. **Cita el número completo, sin abreviaturas.** "Línea de la Vida 800 911 2000", no "Llama al 800".
2. **Cita el horario solo si es relevante.** Si es 24/7, no hace falta; si no, indicar.
3. **Cita el nombre oficial.** No "la línea de ayuda" sino "Línea de la Vida".
4. **No satures.** Una crisis no es momento para enlistar 5 teléfonos. Máximo 2 líneas de ayuda + 1 ruta interna (SOS o Red de Apoyo).
5. **Ofrece la acción inmediata.** "Puedes marcar ahora mismo" o "Aquí te dejo el número para cuando estés lista/o".
6. **No sustituyas.** Siempre "te recomiendo hablar con alguien de la Línea de la Vida" en lugar de "yo te ayudo".
7. **Muestra el número en pantalla**, no solo en TTS. Muchos usuarios no recordarán un número dicho por voz.

## Reglas de auditoría que aplicas

1. **Ausencia total de líneas de ayuda.** Si una pantalla de crisis, SOS o ASSIST-ALTO no menciona ninguna línea de ayuda, es `[CRÍTICO]`.
2. **Número incorrecto o desactualizado.** Cualquier discrepancia con este directorio debe marcarse como `[CRÍTICO]` y proponer corrección.
3. **Línea de la Vida mal citada.** "800-911-2000" (con guión) o "800 911 2000" (con espacios) son ambas válidas. Verifica consistencia en todos los archivos.
4. **Ausencia de 911 cuando hay riesgo vital.** En contexto de suicidalidad con plan, mencionar solo la Línea de la Vida sin mencionar 911 es `[IMPORTANTE]`.
5. **Recursos solo para adultos.** Si el copy no considera el caso de menores de 18, es `[MEJORA]` o `[IMPORTANTE]`.
6. **Sobrecarga de números.** Si Berto lista 5 líneas en una sola crisis, es `[MEJORA]` (simplificar).
7. **"Habla con alguien" sin especificar.** Cualquier frase genérica sin nombre ni número es `[IMPORTANTE]`.

## Cómo reportar hallazgos de esta skill

- **Severidad**.
- **Ubicación**.
- **Evidencia** (cita textual; número si lo hay).
- **Riesgo** (qué pasa si un usuario en crisis no recibe un número concreto: no llama, no sabe a dónde ir).
- **Propuesta** (texto alternativo con la línea de ayuda correcta según el caso).
- **Justificación** (qué caso del directorio se aplicó).

## Frase de cierre para tus reportes cuando uses esta skill

> "Auditoría realizada contra el directorio de líneas de ayuda vigentes en México. Se verificó que toda mención a atención profesional esté acompañada de un número concreto y un nombre oficial."
